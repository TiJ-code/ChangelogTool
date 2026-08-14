package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

public final class IncrementMajorOperation implements VersionOperation {
    @Override public Version apply(Version current) { return current.incrementMajor(); }
}
