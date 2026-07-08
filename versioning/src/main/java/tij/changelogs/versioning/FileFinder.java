package tij.changelogs.versioning;

import tij.changelogs.versioning.provider.IVersionProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public final class FileFinder {
    private FileFinder() {}

    public static List<File> findFiles(
            List<IVersionProvider> providers
    ) {
        try {
            Path root = new File(".")
                            .getCanonicalFile()
                            .toPath();

            try (Stream<Path> walker = Files.walk(root)) {
                return walker
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .filter(file ->
                                providers.stream()
                                        .anyMatch(provider -> provider.supports(file))
                        )
                        .toList();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find version files", e);
        }
    }
}
