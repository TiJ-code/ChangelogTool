package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

public final class IncrementPatchOperation implements IVersionOperation {
    @Override public Version apply(Version current) { return current.incrementPatch(); }
}
