package tij.changelogs.markdown.builder;

import tij.changelogs.config.Config;
import tij.changelogs.config.model.ComponentConfig;
import tij.changelogs.config.model.TopicConfig;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlEntry;
import tij.changelogs.xmlModel.XmlComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MarkdownBuilder {
    private MarkdownBuilder() {}

    public static String build(List<XmlComponent> xmlComponents, Config config) {
        StringBuilder sb = new StringBuilder();

        Map<String, XmlComponent> xmlMap = new HashMap<>();
        if (xmlComponents != null) {
            for (XmlComponent t : xmlComponents) {
                if (t != null && t.path() != null) {
                    xmlMap.put(t.path(), t);
                }
            }
        }

        if (config.topics() == null) return sb.toString();

        for (TopicConfig topicConfig : config.topics()) {
            boolean topicHasDirectData = xmlMap.containsKey(topicConfig.name());
            boolean topicHasChildData = false;

            if (topicConfig.componentRefs() != null && config.components() != null) {
                for (String compRef : topicConfig.componentRefs()) {
                    ComponentConfig compConfig = config.components().get(compRef);
                    if (compConfig != null && hasAnyData(compConfig, xmlMap)) {
                        topicHasChildData = true;
                        break;
                    }
                }
            }

            if (!topicHasDirectData && !topicHasChildData)
                continue;

            boolean isDefaultTopic = "default".equalsIgnoreCase(topicConfig.name());

            if (!isDefaultTopic) {
                sb.append("## ").append(topicConfig.name().toUpperCase()).append("\n");
            }

            if (topicHasDirectData) {
                appendComponentData(xmlMap.get(topicConfig.name()), config, sb, isDefaultTopic ? 1 : 2);
            }

            if (topicConfig.componentRefs() != null && config.components() != null) {
                for (String compRef : topicConfig.componentRefs()) {
                    ComponentConfig compConfig = config.components().get(compRef);
                    if (compConfig != null) {
                        processComponentTree(compConfig, isDefaultTopic ? 2 : 3, xmlMap, config, sb);
                    }
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private static void processComponentTree(ComponentConfig compConfig, int depth, Map<String, XmlComponent> xmlMap, Config config, StringBuilder sb) {
        if (!hasAnyData(compConfig, xmlMap))
            return;

        String headerPrefix = "#".repeat(Math.min(depth, 6)) + " ";
        sb.append(headerPrefix).append(compConfig.name()).append("\n");

        XmlComponent xmlComp = xmlMap.get(compConfig.id());
        if (xmlComp == null) {
            xmlComp = xmlMap.get(compConfig.name());
        }

        if (xmlComp != null) {
            appendComponentData(xmlComp, config, sb, depth);
        }

        if (compConfig.children() != null) {
            for (ComponentConfig child : compConfig.children()) {
                processComponentTree(child, depth + 1, xmlMap, config, sb);
            }
        }
    }

    private static void appendComponentData(XmlComponent xmlComp, Config config, StringBuilder sb, int depth) {
        Map<String, List<String>> categoryEntries = new LinkedHashMap<>();
        Map<String, Map<String, List<String>>> breakingMap = new LinkedHashMap<>();

        if (xmlComp.categories() != null) {
            for (XmlCategory category : xmlComp.categories()) {
                String mappedLabel = config.categories() != null
                        ? config.categories().getOrDefault(category.name(), category.name())
                        : category.name();

                if (category.topLevelEntries() != null) {
                    categoryEntries
                            .computeIfAbsent(mappedLabel, _ -> new ArrayList<>())
                            .addAll(
                                    category.topLevelEntries().stream()
                                            .map(XmlEntry::value)
                                            .toList()
                            );
                }

                if (category.breakingLevels() != null) {
                    for (XmlBreaking breaking : category.breakingLevels()) {
                        if (breaking.entries() != null) {
                            breakingMap
                                    .computeIfAbsent(breaking.severity(), _ -> new LinkedHashMap<>())
                                    .computeIfAbsent(mappedLabel, _ -> new ArrayList<>())
                                    .addAll(
                                            breaking.entries().stream()
                                                    .map(XmlEntry::value)
                                                    .toList()
                                    );
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : categoryEntries.entrySet()) {
            for (String val : entry.getValue()) {
                sb.append("- ").append(entry.getKey()).append(" ").append(val).append("\n");
            }
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : breakingMap.entrySet()) {
            String breakingHeader = "#".repeat(Math.min(depth + 1, 6)) + " ";

            sb.append(breakingHeader)
                    .append("Breaking Changes: ")
                    .append(toTitle(entry.getKey()))
                    .append("\n");

            Map<String, List<String>> byCategory = entry.getValue();

            for (Map.Entry<String, List<String>> catEntry : byCategory.entrySet()) {
                for (String val : catEntry.getValue()) {
                    sb.append("- ").append(catEntry.getKey()).append(" ").append(val).append("\n");
                }
            }
        }

        if (!categoryEntries.isEmpty() || !breakingMap.isEmpty()) {
            sb.append("\n");
        }
    }

    private static boolean hasAnyData(ComponentConfig comp, Map<String, XmlComponent> xmlMap) {
        if (xmlMap.containsKey(comp.id()) || xmlMap.containsKey(comp.name())) {
            return true;
        }
        if (comp.children() != null) {
            for (ComponentConfig child : comp.children()) {
                if (hasAnyData(child, xmlMap)) return true;
            }
        }
        return false;
    }

    private static String toTitle(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
