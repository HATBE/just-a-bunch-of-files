package ch.hatbe;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageFinder {
    private static final Set<String> KNOWN_MEDIA_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tif", "tiff", "heic", "heif",
            "mp4", "mov", "avi", "mkv", "webm", "mpg", "mpeg", "m4v", "wmv", "flv", "3gp"
    );

    private static final Set<String> DELETE_FILE_EXTENSION = Set.of(
            "db", "ini", "thm"
    );

    private final Path rootPath;

    public ImageFinder(String rootPath) {
        this.rootPath = Paths.get(rootPath);

        if (!Files.exists(this.rootPath)) {
            throw new RuntimeException("Path does not exist!");
        }
    }

    public List<File> find() throws IOException {
        List<Path> paths;

        try (Stream<Path> stream = Files.walk(this.rootPath)) {
            paths = stream
                    .filter(Files::isRegularFile)
                    .toList();
        }

        int total = paths.size();
        AtomicInteger counter = new AtomicInteger();

        return paths.parallelStream()
                .map(path -> {
                    int current = counter.incrementAndGet();

                    if (current % 100 == 0 || current == total) {
                        System.out.println(current + "/" + total + " files processed");
                    }

                    return toFile(path);
                })
                .toList();
    }

    public void writeReport(Path outputFile) throws IOException {
        List<File> files = find();

        Map<String, List<File>> duplicates = files.stream()
                .collect(Collectors.groupingBy(File::hash))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        List<File> unknownFormats = files.stream()
                .filter(file -> !isDeleteMediaExtension(file.type()))
                .filter(file -> !isKnownMediaExtension(file.type()))
                .toList();

        List<String> lines = new ArrayList<>();

        lines.add("DUPLICATES");
        lines.add("==========");
        lines.add("");

        if (duplicates.isEmpty()) {
            lines.add("No duplicate files found.");
            lines.add("");
        } else {
            for (Map.Entry<String, List<File>> entry : duplicates.entrySet()) {
                lines.add("HASH: " + entry.getKey());

                for (File file : entry.getValue()) {
                    lines.add("  " + file.path());
                }

                lines.add("");
            }
        }

        lines.add("");
        lines.add("UNKNOWN / NON-STANDARD FORMATS");
        lines.add("==============================");
        lines.add("");

        if (unknownFormats.isEmpty()) {
            lines.add("No unknown formats found.");
        } else {
            for (File file : unknownFormats) {
                lines.add(file.path() + "    [." + file.type() + "]");
            }
        }

        Files.write(outputFile, lines);

        System.out.println("Report written to:");
        System.out.println(outputFile.toAbsolutePath());

        this.deleteTrashFiles(files);
    }

    public void deleteTrashFiles(List<File> files) {
        List<File> deleteCandidates = files.stream()
                .filter(file -> isDeleteMediaExtension(file.type()))
                .toList();

        int total = deleteCandidates.size();
        AtomicInteger counter = new AtomicInteger();

        deleteCandidates.parallelStream().forEach(file -> {

            int current = counter.incrementAndGet();

            try {
                Files.delete(Path.of(file.path()));
                System.out.println(current + "/" + total + " deleted: " + file.path());
            } catch (IOException e) {
                System.out.println("Could not delete: " + file.path());
                System.out.println(e.getMessage());
            }
        });
    }

    private File toFile(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            OffsetDateTime createdAt = OffsetDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());

            String name = path.getFileName().toString();
            String extension = FilenameUtils.getExtension(name).toLowerCase();

            return new File(
                    path.toAbsolutePath().toString(),
                    name,
                    extension,
                    HashUtil.sha256(path),
                    createdAt
            );

        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + path, e);
        }
    }

    private boolean isKnownMediaExtension(String extension) {
        return KNOWN_MEDIA_EXTENSIONS.contains(extension.toLowerCase());
    }

    private boolean isDeleteMediaExtension(String extension) {
        return DELETE_FILE_EXTENSION.contains(extension.toLowerCase());
    }
}