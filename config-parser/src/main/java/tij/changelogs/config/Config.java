package tij.changelogs.config;

import tij.changelogs.config.model.ComponentConfig;
import tij.changelogs.config.model.TopicConfig;
import tij.changelogs.config.model.VersioningConfig;

import java.util.List;
import java.util.Map;

public record Config(
        VersioningConfig versioningConfig,
        Map<String, String> categories,
        Map<String, ComponentConfig> components,
        List<TopicConfig> topics,
        List<String> breakingLevels
) {}
