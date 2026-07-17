package tij.changelogs.config.model;

import java.util.List;

public record VersioningConfig(List<VersioningRule> versionRules, List<VersioningPhase> versionPhases) {
    public static final VersioningConfig INVALID = new VersioningConfig(List.of(), List.of());
}
