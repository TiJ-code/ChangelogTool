package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

@FunctionalInterface
public interface IVersionOperation {
    Version apply(Version current);
}
