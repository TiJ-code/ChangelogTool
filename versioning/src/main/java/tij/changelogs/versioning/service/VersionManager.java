package tij.changelogs.versioning.service;

import tij.changelogs.versioning.model.Version;
import tij.changelogs.versioning.change.VersionChange;
import tij.changelogs.versioning.change.VersionChangeSet;
import tij.changelogs.versioning.operation.VersionOperation;
import tij.changelogs.versioning.resolver.ResolvedVersion;
import tij.changelogs.versioning.resolver.VersionSourceResolver;
import tij.changelogs.versioning.source.VersionLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class VersionManager {
    private final VersionSourceResolver resolver;

    public VersionManager(VersionSourceResolver resolver) { this.resolver = resolver; }

    public ResolvedVersion resolveCurrentVersion() {
        List<VersionLocation> locations = resolver.resolve();
        if (locations.isEmpty()) throw new IllegalStateException("No configured version source found");

        Version expected = null;
        for (VersionLocation location : locations) {
            Version actual = location.source().read(location.file());
            if (expected == null) expected = actual;
            else if (!expected.equals(actual)) {
                throw new IllegalStateException("Version mismatch:\n  " + locations.getFirst().file()
                        + " -> " + expected + "\n  " + location.file() + " -> " + actual);
            }
        }
        return new ResolvedVersion(expected, locations);
    }

    public VersionChangeSet plan(ResolvedVersion current, VersionOperation operation) {
        Version next = operation.apply(current.version());
        List<VersionChange> changes = current.locations().stream().map(location -> {
            try {
                String oldContent = Files.readString(location.file().toPath());
                String newContent = location.source().replace(location.file(), next);
                return oldContent.equals(newContent) ? null : new VersionChange(location.file(), oldContent, newContent);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to prepare change for " + location.file(), e);
            }
        }).filter(java.util.Objects::nonNull).toList();
        return new VersionChangeSet(changes);
    }

    public void apply(VersionChangeSet changeSet) {
        try {
            for (VersionChange change : changeSet.changes()) {
                if (!Files.readString(change.file().toPath()).equals(change.oldContent())) {
                    throw new IllegalStateException("File changed while planning: " + change.file());
                }
            }
            for (VersionChange change : changeSet.changes()) {
                Files.writeString(change.file().toPath(), change.newContent());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to apply version changes", e);
        }
    }
}
