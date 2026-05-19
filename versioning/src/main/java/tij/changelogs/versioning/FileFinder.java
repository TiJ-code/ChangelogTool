package tij.changelogs.versioning;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public final class FileFinder {
    private FileFinder() {}

    public static List<File> findPomFiles() {
        try {
            List<File> files = new LinkedList<>();
            Path rootDir = new File(".").getCanonicalFile().toPath();

            try (Stream<Path> walker = Files.walk(rootDir)) {
                List<File> collected = walker
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals("pom.xml"))
                        .map(Path::toFile)
                        .sorted(Comparator
                                .comparingInt((File f) -> f.getParentFile().equals(rootDir.toFile()) ? 0 : 1)
                                .thenComparing(File::getAbsolutePath))
                        .toList();
                files.addAll(collected);
            }

            return files;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
