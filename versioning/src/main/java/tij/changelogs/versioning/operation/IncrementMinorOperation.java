package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

public final class IncrementMinorOperation implements IVersionOperation {
    @Override public Version apply(Version current) { return current.incrementMinor(); }
}
