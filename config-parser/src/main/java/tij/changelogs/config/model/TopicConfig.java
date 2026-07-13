package tij.changelogs.config.model;

import java.util.List;

public record TopicConfig(
        String name,
        List<String> componentRefs
) {}
