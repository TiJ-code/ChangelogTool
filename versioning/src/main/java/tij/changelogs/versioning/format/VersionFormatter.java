package tij.changelogs.versioning.format;

import tij.changelogs.versioning.model.Version;

public interface VersionFormatter {
    String format(Version version);
    Version parse(String value);
}
