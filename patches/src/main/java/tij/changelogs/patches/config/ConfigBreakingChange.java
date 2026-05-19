package tij.changelogs.patches.config;

import java.util.List;

public record ConfigBreakingChange(List<ConfigEntry> entries) {}
