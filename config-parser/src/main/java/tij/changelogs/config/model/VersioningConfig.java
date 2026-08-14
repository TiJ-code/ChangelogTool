package tij.changelogs.config.model;

import java.util.List;
import java.util.Optional;

public record VersioningConfig(
        List<VersioningRule> versionRules,
        List<VersioningPhase> versionPhases,
        Optional<String> initialPhaseName,
        Optional<String> releasePhaseName
) {
    public VersioningConfig(List<VersioningRule> rules, List<VersioningPhase> phases) {
        this(rules, phases, Optional.empty(), Optional.empty());
    }

    public static final VersioningConfig INVALID = new VersioningConfig(
            List.of(), List.of(), Optional.empty(), Optional.empty());
}
