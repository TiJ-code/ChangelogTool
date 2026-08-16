package tij.changelogs.versioning.source;

import java.io.File;

public record VersionLocation(IVersionSource source, File file) {}
