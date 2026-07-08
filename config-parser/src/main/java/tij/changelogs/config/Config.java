package tij.changelogs.config;

import tij.changelogs.config.model.VersioningConfig;

import java.util.List;
import java.util.Map;

public record Config(VersioningConfig versioningConfig, Map<String, String> categories, List<String> topics, List<String> breakingLevels) {}
