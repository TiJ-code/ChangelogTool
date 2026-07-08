package tij.changelogs.config.model;

import java.util.List;

public record VersioningConfig(List<VersioningRule> versionRules) {
}
