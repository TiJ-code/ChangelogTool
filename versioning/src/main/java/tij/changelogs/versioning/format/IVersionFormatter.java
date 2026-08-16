package tij.changelogs.versioning.format;

import tij.changelogs.versioning.model.Version;

public interface IVersionFormatter {
    String format(Version version);
    Version parse(String value);
}
