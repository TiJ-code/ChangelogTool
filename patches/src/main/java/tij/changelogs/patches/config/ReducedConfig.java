package tij.changelogs.patches.config;


import tij.changelogs.config.Config;

import java.util.ArrayList;
import java.util.List;

public record ReducedConfig(List<String> categoryValues, List<String> topicValues, List<String> breakingLevelValues) {
    public static ReducedConfig reduce(Config config) {
        return new ReducedConfig(new ArrayList<>(config.categories().keySet()), config.topics(), config.breakingLevels());
    }
}
