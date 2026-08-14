package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

@FunctionalInterface
public interface VersionOperation {
    Version apply(Version current);
}
