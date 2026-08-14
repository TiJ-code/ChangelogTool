package tij.changelogs.versioning.change;

import java.util.List;

public record VersionChangeSet(List<VersionChange> changes) {
    public VersionChangeSet { changes = List.copyOf(changes); }
}
