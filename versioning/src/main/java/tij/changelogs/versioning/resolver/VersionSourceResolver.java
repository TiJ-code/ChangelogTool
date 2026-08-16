package tij.changelogs.versioning.resolver;

import tij.changelogs.versioning.source.VersionLocation;
import tij.changelogs.versioning.source.IVersionSource;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

public final class VersionSourceResolver {
    private final File root;
    private final List<IVersionSource> sources;

    public VersionSourceResolver(File root, List<IVersionSource> sources) {
        this.root = root;
        this.sources = List.copyOf(sources);
    }

    public List<VersionLocation> resolve() {
        try (Stream<java.nio.file.Path> paths = Files.walk(root.toPath())) {
            return paths.filter(Files::isRegularFile)
                    .map(java.nio.file.Path::toFile)
                    .flatMap(file -> sources.stream()
                            .filter(source -> source.supports(file))
                            .map(source -> new VersionLocation(source, file)))
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve version sources below " + root, e);
        }
    }
}
