package tij.changelogs.patches.config;

import tij.changelogs.config.Config;

import java.util.ArrayList;
import java.util.List;

public record ReducedConfig(
        List<String> categoryValues,
        List<String> componentValues,
        List<String> breakingLevelValues,
        List<String> topicValues
) {
    public static ReducedConfig reduce(Config config) {
        return new ReducedConfig(
                new ArrayList<>(config.categories().keySet()),
                new ArrayList<>(config.components().keySet()),
                config.breakingLevels(),
                config.topics().stream().map(t -> t.name()).toList()
        );
    }
}
