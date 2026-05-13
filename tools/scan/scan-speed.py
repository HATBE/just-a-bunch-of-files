#!/usr/bin/env python3
import argparse
import re
import shutil
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import List, Optional

import piexif


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

VERBOSE = True


def log(msg: str) -> None:
    if VERBOSE:
        now = datetime.now().strftime("%H:%M:%S")
        print(f"[{now}] {msg}", flush=True)


def ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def is_jpg(p: Path) -> bool:
    return p.is_file() and p.suffix.lower() in SUPPORTED_EXTS


def natural_sort_key(s: str):
    return [int(part) if part.isdigit() else part.lower() for part in re.split(r"(\d+)", s)]


@dataclass
class ParsedDate:
    date_raw: str
    dt_for_exif: Optional[datetime]
    album: str
    precision: str  # exact | month | year | unknown


def parse_date_input(s: str) -> ParsedDate:
    """
    Erlaubt:
    - exakt:
        2002-09-17
        17.09.2002
        17/09/2002
    - monat+jahr:
        september 2002
        sept 2002
        2002-09
    - jahr:
        2002
    - unknown / leer / ?
    """
    raw = (s or "").strip()
    if not raw or raw.lower() in {"unknown", "?", "u", "na", "n/a"}:
        return ParsedDate(
            date_raw="unknown",
            dt_for_exif=None,
            album="unbekanntes-datum",
            precision="unknown",
        )

    x = raw.lower().strip()
    x = re.sub(r"\s+", " ", x)

    m = re.fullmatch(r"(\d{4})-(\d{2})-(\d{2})", x)
    if m:
        yyyy, mm, dd = int(m.group(1)), int(m.group(2)), int(m.group(3))
        dt = datetime(yyyy, mm, dd, 12, 0, 0)
        return ParsedDate(raw, dt, f"{yyyy:04d}-{mm:02d}", "exact")

    m = re.fullmatch(r"(\d{1,2})[./](\d{1,2})[./](\d{4})", x)
    if m:
        dd, mm, yyyy = int(m.group(1)), int(m.group(2)), int(m.group(3))
        dt = datetime(yyyy, mm, dd, 12, 0, 0)
        return ParsedDate(raw, dt, f"{yyyy:04d}-{mm:02d}", "exact")

    m = re.fullmatch(r"(\d{4})-(\d{2})", x)
    if m:
        yyyy, mm = int(m.group(1)), int(m.group(2))
        if not (1 <= mm <= 12):
            raise ValueError("Monat muss zwischen 01 und 12 liegen.")
        dt = datetime(yyyy, mm, 1, 12, 0, 0)
        return ParsedDate(raw, dt, f"{yyyy:04d}-{mm:02d}", "month")

    m = re.fullmatch(r"(\d{4})", x)
    if m:
        yyyy = int(m.group(1))
        dt = datetime(yyyy, 1, 1, 12, 0, 0)
        return ParsedDate(raw, dt, f"{yyyy:04d}-00", "year")

    m = re.fullmatch(r"([a-z]+)[ ,]+(\d{4})", x)
    if m:
        mon_s, yyyy_s = m.group(1), m.group(2)
        mon = MONTHS.get(mon_s, MONTHS.get(mon_s[:3], None))
        if mon is None:
            raise ValueError("Monat nicht erkannt.")
        yyyy = int(yyyy_s)
        dt = datetime(yyyy, mon, 1, 12, 0, 0)
        return ParsedDate(raw, dt, f"{yyyy:04d}-{mon:02d}", "month")

    raise ValueError("Ungueltiges Datumsformat.")


def exif_dt_str(dt: datetime) -> bytes:
    return dt.strftime("%Y:%m:%d %H:%M:%S").encode("ascii")


def safe_ascii_bytes(s: str) -> bytes:
    return (s or "").encode("ascii", errors="replace")


def sanitize_serial(s: str) -> str:
    """
    Behaelt nur A-Z 0-9 - _
    """
    x = (s or "").strip().upper()
    x = re.sub(r"\s+", "", x)
    x = re.sub(r"[^A-Z0-9_-]+", "", x)
    return x


def normalize_single_id(raw: str) -> str:
    tok = (raw or "").strip()
    if not tok:
        return "xx"
    if tok in {"-", "xx", "XX", "none", "NONE"}:
        return "xx"
    cleaned = sanitize_serial(tok)
    return cleaned if cleaned else "xx"


def next_run_in_album(album_dir: Path, prefix: str) -> int:
    if not album_dir.exists():
        return 1

    max_n = 0
    pref_re = re.escape(prefix)
    pat = re.compile(rf"^{pref_re}-(\d{{4,}})$")

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


def ensure_unique_target(path: Path) -> Path:
    if not path.exists():
        return path

    stem = path.stem
    suffix = path.suffix
    parent = path.parent

    counter = 1
    while True:
        candidate = parent / f"{stem}_{counter}{suffix}"
        if not candidate.exists():
            return candidate
        counter += 1


def write_exif(
    jpg_path: Path,
    album: str,
    date_raw: str,
    dt: Optional[datetime],
    serial: str,
    precision: str,
) -> None:
    try:
        exif_dict = piexif.load(str(jpg_path))
    except Exception:
        exif_dict = {"0th": {}, "Exif": {}, "GPS": {}, "Interop": {}, "1st": {}, "thumbnail": None}

    desc = f"album={album} | date={date_raw} | serial={serial} | precision={precision}"
    exif_dict["0th"][piexif.ImageIFD.ImageDescription] = safe_ascii_bytes(desc)

    if dt is not None:
        b = exif_dt_str(dt)
        exif_dict["0th"][piexif.ImageIFD.DateTime] = b
        exif_dict["Exif"][piexif.ExifIFD.DateTimeOriginal] = b
        exif_dict["Exif"][piexif.ExifIFD.DateTimeDigitized] = b

    comment = (
        f"scan_batch_frontonly_v3\n"
        f"album={album}\n"
        f"date_input={date_raw}\n"
        f"date_precision={precision}\n"
        f"serial={serial}"
    )
    exif_dict["Exif"][piexif.ExifIFD.UserComment] = b"ASCII\0\0\0" + safe_ascii_bytes(comment)

    keywords = f"{album};{serial};{precision}"
    exif_dict["0th"][piexif.ImageIFD.XPKeywords] = (keywords + "\0").encode("utf-16le")

    piexif.insert(piexif.dump(exif_dict), str(jpg_path))


def collect_images(scan_dir: Path) -> List[Path]:
    files = [p for p in scan_dir.iterdir() if is_jpg(p)]
    files.sort(key=lambda p: natural_sort_key(p.name))
    return files


def prompt_ids_reverse(images: List[Path]) -> List[str]:
    """
    Der User gibt IDs in umgekehrter Scanreihenfolge ein:
    letztes Bild zuerst, erstes Bild zuletzt.
    Eine ID pro Zeile.
    """
    print("\nIDs jetzt in UMGEKEHRTER Scanreihenfolge eingeben.")
    print("Also: letztes gescanntes Bild zuerst.")
    print("Eine ID pro Zeile.")
    print("Fuer 'keine ID' einfach xx, - oder leer eingeben.\n")

    reverse_images = list(reversed(images))
    reverse_ids: List[str] = []

    for idx, img in enumerate(reverse_images, start=1):
        prompt = f"[{idx}/{len(reverse_images)}] ID fuer {img.name}: "
        value = input(prompt)
        reverse_ids.append(normalize_single_id(value))

    # Intern wieder in normale Scanreihenfolge drehen
    return list(reversed(reverse_ids))


def process_batch(scan_dir: Path, dest_dir: Path, move_files: bool = True) -> None:
    if not scan_dir.exists():
        raise FileNotFoundError(f"Scan-Ordner nicht gefunden: {scan_dir}")

    ensure_dir(dest_dir)

    images = collect_images(scan_dir)
    if not images:
        print("Keine JPG-Dateien im Scan-Ordner gefunden.")
        return

    log(f"Scan-Ordner: {scan_dir}")
    log(f"Ziel-Ordner: {dest_dir}")
    log(f"Gefundene Bilder: {len(images)}")

    print("\nNormale Scanreihenfolge:")
    for idx, img in enumerate(images, start=1):
        print(f"  {idx:>3}) {img.name}")

    print("\nUmgekehrte Eingabereihenfolge:")
    for idx, img in enumerate(reversed(images), start=1):
        print(f"  {idx:>3}) {img.name}")

    while True:
        try:
            date_in = input(
                "\nDatum fuer diese Runde "
                "(z.B. 2002-09-17, 17.09.2002, 2002-09, sept 2002, 2002, unknown): "
            ).strip()
            parsed_date = parse_date_input(date_in)
            break
        except Exception as e:
            print(f"Fehler: {e}")

    ids = prompt_ids_reverse(images)

    print("\nZusammenfassung:")
    print(f"  Datum Input:     {parsed_date.date_raw}")
    print(f"  EXIF Datum:      {parsed_date.dt_for_exif.isoformat() if parsed_date.dt_for_exif else 'None'}")
    print(f"  Album/Ordner:    {parsed_date.album}")
    print(f"  Praezision:      {parsed_date.precision}")
    print(f"  Bilder:          {len(images)}")

    print("\nZuordnung in normaler Scanreihenfolge:")
    for idx, (img, serial) in enumerate(zip(images, ids), start=1):
        print(f"  {idx:>3}) {img.name} -> {serial}")

    confirm = input("\nWeiter? [Enter = ja / n = abbrechen]: ").strip().lower()
    if confirm == "n":
        print("Abgebrochen.")
        return

    album_dir = dest_dir / parsed_date.album
    ensure_dir(album_dir)

    processed = 0

    for idx, src in enumerate(images, start=1):
        serial = ids[idx - 1]
        serial = sanitize_serial(serial) or "xx"

        run = next_run_in_album(album_dir, serial)
        target_name = f"{serial}-{run:04d}{src.suffix.lower()}"
        target = ensure_unique_target(album_dir / target_name)

        log("-" * 70)
        log(f"[{idx}/{len(images)}] Verarbeite: {src.name}")
        log(f"ID: {serial}")
        log(f"Zielname: {target.name}")
        log(f"Zielpfad: {target}")

        try:
            if move_files:
                shutil.move(str(src), str(target))
                log("Datei verschoben")
            else:
                shutil.copy2(str(src), str(target))
                log("Datei kopiert")
        except Exception as e:
            print(f"FEHLER beim Verschieben/Kopieren von {src.name}: {e}")
            continue

        try:
            write_exif(
                target,
                album=parsed_date.album,
                date_raw=parsed_date.date_raw,
                dt=parsed_date.dt_for_exif,
                serial=serial,
                precision=parsed_date.precision,
            )
            log("EXIF geschrieben")
        except Exception as e:
            print(f"FEHLER beim EXIF schreiben fuer {target.name}: {e}")
            print("Datei wurde trotzdem verarbeitet.")
            continue

        processed += 1

    print("\nFertig.")
    print(f"Verarbeitet: {processed} von {len(images)}")
    print(f"Zielordner: {album_dir}")


def main():
    ap = argparse.ArgumentParser(
        description="Verarbeitet eine Scan-Runde: ein Datum fuer alle Bilder, IDs in umgekehrter Scanreihenfolge, schreibt EXIF und sortiert in Jahres-/Monatsordner."
    )
    ap.add_argument("--scan", required=True, help="Ordner mit neuen JPG-Scans")
    ap.add_argument("--dest", required=True, help="Ziel-Root-Ordner")
    ap.add_argument("--copy", action="store_true", help="Kopieren statt verschieben")
    args = ap.parse_args()

    process_batch(
        scan_dir=Path(args.scan),
        dest_dir=Path(args.dest),
        move_files=(not args.copy),
    )


if __name__ == "__main__":
    main()