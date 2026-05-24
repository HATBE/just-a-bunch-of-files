#!/usr/bin/env python3
import re
import time
import shutil
import signal
from pathlib import Path
from typing import Tuple, List, Optional
from datetime import datetime

import piexif
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

SUPPORTED_EXTS = {".jpg", ".jpeg"}

MONTHS = {
    "jan": 1, "january": 1,
    "feb": 2, "february": 2,
    "mar": 3, "march": 3,
    "apr": 4, "april": 4,
    "may": 5,
    "jun": 6, "june": 6,
    "jul": 7, "july": 7,
    "aug": 8, "august": 8,
    "sep": 9, "sept": 9, "september": 9,
    "oct": 10, "october": 10,
    "nov": 11, "november": 11,
    "dec": 12, "december": 12,
}


def ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def is_jpg(p: Path) -> bool:
    return p.is_file() and p.suffix.lower() in SUPPORTED_EXTS


def slugify(s: str, max_len: int = 80) -> str:
    s = (s or "").strip().lower()
    s = s.replace("&", " and ")
    s = re.sub(r"[^\w\s-]", "", s, flags=re.UNICODE)
    s = re.sub(r"[\s_-]+", "-", s).strip("-")
    return s[:max_len] if len(s) > max_len else s


def wait_until_file_stable(path: Path, timeout_s: int = 45, poll_s: float = 0.5) -> bool:
    """Wait until file size stops changing (scanner writing slowly)."""
    start = time.time()
    last_size = -1
    while time.time() - start < timeout_s:
        try:
            size = path.stat().st_size
        except FileNotFoundError:
            time.sleep(poll_s)
            continue
        if size == last_size and size > 0:
            return True
        last_size = size
        time.sleep(poll_s)
    return False


def load_album_cache(cache_path: Path) -> List[str]:
    if not cache_path.exists():
        return []
    lines = [ln.strip() for ln in cache_path.read_text(encoding="utf-8").splitlines()]
    albums = [ln for ln in lines if ln]
    seen = set()
    out = []
    for a in albums:
        k = a.lower()
        if k not in seen:
            out.append(a)
            seen.add(k)
    return out


def save_album_cache(cache_path: Path, albums: List[str]) -> None:
    ensure_dir(cache_path.parent)
    cache_path.write_text("\n".join(albums) + ("\n" if albums else ""), encoding="utf-8")


def parse_date_input(s: str) -> Tuple[str, Optional[datetime], Optional[str]]:
    """
    Returns (date_raw, exif_dt_or_None, album_default_or_None)

    Accepted:
    - exact date:
        * "2002-09-17"
        * "17.09.2002"
        * "17/09/2002"
    - month+year:
        * "sept 2002" / "september 2002"
        * "2002-09"
    - year:
        * "2002"
    - "unknown" / blank / "?"

    album_default:
    - exact date -> "YYYY-MM"
    - month/year -> "YYYY-MM"
    - year -> "YYYY-00"
    - unknown -> None
    """
    raw = (s or "").strip()
    if not raw or raw.lower() in {"unknown", "?", "u", "na", "n/a"}:
        return ("unknown", None, None)

    x = raw.lower().strip()
    x = re.sub(r"\s+", " ", x)

    # exact: YYYY-MM-DD
    m = re.fullmatch(r"(\d{4})-(\d{2})-(\d{2})", x)
    if m:
        yyyy, mm, dd = int(m.group(1)), int(m.group(2)), int(m.group(3))
        try:
            dt = datetime(yyyy, mm, dd, 12, 0, 0)
        except ValueError:
            return ("invalid", None, None)
        return (raw, dt, f"{yyyy:04d}-{mm:02d}")

    # exact: DD.MM.YYYY or DD/MM/YYYY
    m = re.fullmatch(r"(\d{1,2})[./](\d{1,2})[./](\d{4})", x)
    if m:
        dd, mm, yyyy = int(m.group(1)), int(m.group(2)), int(m.group(3))
        try:
            dt = datetime(yyyy, mm, dd, 12, 0, 0)
        except ValueError:
            return ("invalid", None, None)
        return (raw, dt, f"{yyyy:04d}-{mm:02d}")

    # YYYY-MM (month+year)
    m = re.fullmatch(r"(\d{4})-(\d{2})", x)
    if m:
        yyyy, mm = int(m.group(1)), int(m.group(2))
        if 1 <= mm <= 12:
            return (raw, datetime(yyyy, mm, 1, 12, 0, 0), f"{yyyy:04d}-{mm:02d}")
        return ("invalid", None, None)

    # YYYY (year only)
    m = re.fullmatch(r"(\d{4})", x)
    if m:
        yyyy = int(m.group(1))
        return (raw, datetime(yyyy, 1, 1, 12, 0, 0), f"{yyyy:04d}-00")

    # monthname YYYY (month+year)
    m = re.fullmatch(r"([a-z]+)[ ,]+(\d{4})", x)
    if m:
        mon_s, yyyy_s = m.group(1), m.group(2)
        mon = MONTHS.get(mon_s, MONTHS.get(mon_s[:3], None))
        if mon is None:
            return ("invalid", None, None)
        yyyy = int(yyyy_s)
        return (raw, datetime(yyyy, mon, 1, 12, 0, 0), f"{yyyy:04d}-{mon:02d}")

    return ("invalid", None, None)

def exif_dt_str(dt: datetime) -> bytes:
    return dt.strftime("%Y:%m:%d %H:%M:%S").encode("ascii")


def safe_ascii_bytes(s: str) -> bytes:
    return (s or "").encode("ascii", errors="replace")


def write_exif(jpg_path: Path, album: str, date_raw: str, dt: Optional[datetime]) -> None:
    exif_dict = piexif.load(str(jpg_path))
    desc = f"album={album} | date={date_raw}"
    exif_dict["0th"][piexif.ImageIFD.ImageDescription] = safe_ascii_bytes(desc)

    if dt is not None:
        b = exif_dt_str(dt)
        exif_dict["0th"][piexif.ImageIFD.DateTime] = b
        exif_dict["Exif"][piexif.ExifIFD.DateTimeOriginal] = b
        exif_dict["Exif"][piexif.ExifIFD.DateTimeDigitized] = b

    comment = f"scan_sorter_v2\nalbum={album}\ndate_input={date_raw}"
    exif_dict["Exif"][piexif.ExifIFD.UserComment] = b"ASCII\0\0\0" + safe_ascii_bytes(comment)

    if album:
        exif_dict["0th"][piexif.ImageIFD.XPKeywords] = (album + "\0").encode("utf-16le")

    piexif.insert(piexif.dump(exif_dict), str(jpg_path))


def sanitize_serial(s: str) -> str:
    """
    Keep only A-Z 0-9 - _ (uppercase). Empty -> "".
    """
    x = (s or "").strip().upper()
    x = re.sub(r"\s+", "", x)
    x = re.sub(r"[^A-Z0-9_-]+", "", x)
    return x


def next_run_in_album(album_dir: Path, prefix: str) -> int:
    """
    Find next run number based on existing files like:
      PREFIX-0001.jpg, PREFIX-0002.jpg, ...
    """
    if not album_dir.exists():
        return 1

    max_n = 0
    # PREFIX can contain - _ so escape for regex
    pref_re = re.escape(prefix)
    pat = re.compile(rf"^{pref_re}-(\d{{4,}})$")  # allow 0001, 00001 etc.

    for p in album_dir.iterdir():
        if not p.is_file():
            continue
        if p.suffix.lower() not in SUPPORTED_EXTS:
            continue
        m = pat.fullmatch(p.stem)
        if not m:
            continue
        try:
            n = int(m.group(1))
        except ValueError:
            continue
        if n > max_n:
            max_n = n

    return max_n + 1


def choose_album_with_default(cache_path: Path, default_album: Optional[str]) -> str:
    """Shows cached albums. User can:
    - press Enter -> use default_album (YYYY-MM / YYYY-00)
    - type number -> pick cached album
    - type text -> new album
    """
    albums = load_album_cache(cache_path)
    print("\nAlbums (Cache):")
    if albums:
        for i, a in enumerate(albums, start=1):
            print(f" {i:>2}) {a}")
    else:
        print(" (leer)")

    if default_album:
        prompt = f"Album [Enter = {default_album}]: "
    else:
        prompt = "Album: "

    choice = input(prompt).strip()
    if choice == "" and default_album:
        album = default_album
    elif choice.isdigit():
        idx = int(choice)
        if 1 <= idx <= len(albums):
            album = albums[idx - 1]
        else:
            album = default_album or "unsorted"
    else:
        album = choice.strip() or (default_album or "unsorted")

    # Update cache (MRU)
    albums = [album] + [a for a in albums if a.lower() != album.lower()]
    save_album_cache(cache_path, albums)
    return album


class Sorter:
    def __init__(self, out_dir: Path, dest_dir: Path, cache_path: Path, move_files: bool = True):
        self.out_dir = out_dir
        self.dest_dir = dest_dir
        self.cache_path = cache_path
        self.move_files = move_files
        ensure_dir(self.out_dir)
        ensure_dir(self.dest_dir)

    def process(self, src: Path) -> None:
        if not is_jpg(src):
            return

        print("\n" + "=" * 70)
        print(f"Neuer Scan: {src.name}")
        print("=" * 70)

        if not wait_until_file_stable(src):
            print("Datei wurde nicht stabil (Scanner schreibt evtl. noch). Ueberspringe vorerst.")
            return

        # Date prompt
        while True:
            date_in = input("Datum (z.B. 'sept 2002', '2002-08', '2002', 'unknown'): ").strip()
            date_raw, dt, album_default = parse_date_input(date_in)
            if date_raw == "invalid":
                print(" Ungueltiges Datumsformat. Nochmal.")
                continue
            break

        # Album prompt with default = YYYY-MM (or YYYY-00) when Enter
        album = choose_album_with_default(self.cache_path, album_default)
        album_slug = slugify(album) or "unsorted"
        album_dir = self.dest_dir / album_slug
        ensure_dir(album_dir)

        # Serial prompt (like date: just ask, Enter = none)
        serial_in = input("Seriennummer (Enter = keine): ").strip()
        serial = sanitize_serial(serial_in) or "xx"

        # Filename: serial-run
        run = next_run_in_album(album_dir, serial)
        filename = f"{serial}-{run:04d}{src.suffix.lower()}"
        target = album_dir / filename

        # Move/copy
        try:
            if self.move_files:
                shutil.move(str(src), str(target))
            else:
                shutil.copy2(str(src), str(target))
        except Exception as e:
            print(f"ERROR beim Verschieben/Kopieren: {e}")
            return

        # Write EXIF
        try:
            write_exif(target, album=album, date_raw=date_raw, dt=dt)
        except Exception as e:
            print(f"ERROR beim EXIF schreiben: {e}")
            print("Datei wurde trotzdem verschoben. (Du kannst sie manuell nachbearbeiten.)")
            return

        print(f"OK: {target}")
        if dt is None:
            print("Hinweis: Datum war 'unknown' -> DateTimeOriginal wurde nicht gesetzt.")
        else:
            print("EXIF: DateTimeOriginal gesetzt (best-effort), originaler Input steht auch in UserComment.")


class Handler(FileSystemEventHandler):
    def __init__(self, sorter: Sorter):
        self.sorter = sorter

    def on_created(self, event):
        if event.is_directory:
            return
        p = Path(event.src_path)
        time.sleep(0.2)
        self.sorter.process(p)

    def on_moved(self, event):
        if event.is_directory:
            return
        p = Path(event.dest_path)
        time.sleep(0.2)
        self.sorter.process(p)


def main():
    import argparse

    ap = argparse.ArgumentParser(
        description="Watch OUT for new JPG scans, ask date+album+serial, write EXIF, move into DEST/<album>/ with serial-run filenames."
    )
    ap.add_argument("--out", required=True, help="Ordner wo der Scanner JPGs ablegt")
    ap.add_argument("--dest", required=True, help="Ziel-Root Ordner")
    ap.add_argument("--cache", default=str(Path.home() / ".scan_sorter" / "albums.txt"), help="Pfad zur Album-Cache Datei")
    ap.add_argument("--copy", action="store_true", help="Kopieren statt verschieben")

    args = ap.parse_args()

    sorter = Sorter(
        out_dir=Path(args.out),
        dest_dir=Path(args.dest),
        cache_path=Path(args.cache),
        move_files=(not args.copy),
    )

    observer = Observer()
    observer.schedule(Handler(sorter), str(sorter.out_dir), recursive=False)

    def stop(*_):
        observer.stop()

    signal.signal(signal.SIGINT, stop)
    signal.signal(signal.SIGTERM, stop)

    print("Watching OUT: ", sorter.out_dir)
    print("Destination: ", sorter.dest_dir)
    print("Album cache: ", sorter.cache_path)
    print("\nScan JPGs nach OUT werfen. Ctrl+C beendet.\n")

    observer.start()
    try:
        while observer.is_alive():
            time.sleep(0.5)
    finally:
        observer.stop()
        observer.join()


if __name__ == "__main__":
    main()