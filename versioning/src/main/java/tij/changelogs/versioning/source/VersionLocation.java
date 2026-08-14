package tij.changelogs.versioning.source;

import java.io.File;

public record VersionLocation(VersionSource source, File file) {}
