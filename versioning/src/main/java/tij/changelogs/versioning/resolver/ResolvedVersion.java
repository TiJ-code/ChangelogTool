package tij.changelogs.versioning.resolver;

import tij.changelogs.versioning.model.Version;
import tij.changelogs.versioning.source.VersionLocation;

import java.util.List;

public record ResolvedVersion(Version version, List<VersionLocation> locations) {}
