package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

public final class IncrementMajorOperation implements IVersionOperation {
    @Override public Version apply(Version current) { return current.incrementMajor(); }
}
