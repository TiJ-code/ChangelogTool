package tij.changelogs.config.model;

import java.util.List;

public record ComponentConfig(
        String id,
        String name,
        List<ComponentConfig> children
) {}
