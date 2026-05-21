package tij.changelogs.config;

import java.util.List;
import java.util.Map;

public record Config(Map<String, String> categories, List<String> topics, List<String> breakingLevels) {}
