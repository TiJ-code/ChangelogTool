package tij.changelogs.markdown.builder;

import tij.changelogs.config.Config;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlEntry;
import tij.changelogs.xmlModel.XmlTopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MarkdownBuilder {
    private MarkdownBuilder() {}

    private static final String MD_HEADER_2 = "## ";
    private static final String MD_HEADER_3 = "### ";

    public static String build(List<XmlTopic> topics, Config config) {
        StringBuilder sb = new StringBuilder();

        Map<String, XmlTopic> topicMap = new HashMap<>();
        for (XmlTopic t : topics) {
            topicMap.put(t.name(), t);
        }

        for (String topicName : config.topics()) {
            XmlTopic topic = topicMap.get(topicName);
            if (topic == null)
                continue;

            sb.append(MD_HEADER_2).append(topicName.toUpperCase()).append("\n");

            Map<String, List<String>> categoryEntries = new LinkedHashMap<>();

            Map<String, Map<String, List<String>>> breakingMap = new LinkedHashMap<>();

            for (XmlCategory category : topic.categories()) {
                String mappedLabel = config.categories().getOrDefault(category.name(), category.name());

                categoryEntries
                        .computeIfAbsent(mappedLabel, _ -> new ArrayList<>())
                        .addAll(
                                category.topLevelEntries()
                                        .stream()
                                        .map(XmlEntry::value)
                                        .toList()
                        );

                for (XmlBreaking breaking : category.breakingLevels()) {
                    breakingMap
                            .computeIfAbsent(breaking.severity(), _ -> new LinkedHashMap<>())
                            .computeIfAbsent(mappedLabel, _ -> new ArrayList<>())
                            .addAll(
                                    breaking.entries()
                                            .stream()
                                            .map(XmlEntry::value)
                                            .toList()
                            );
                }
            }

            for (String cat : categoryEntries.keySet()) {
                for (String entry : categoryEntries.get(cat)) {
                    sb.append("- ")
                            .append(cat)
                            .append(" ")
                            .append(entry)
                            .append("\n");
                }
            }

            for (String severity : breakingMap.keySet()) {
                sb.append(MD_HEADER_3)
                        .append("Breaking Changes: ")
                        .append(toTitle(severity))
                        .append("\n");

                Map<String, List<String>> byCategory = breakingMap.get(severity);

                for (String cat : byCategory.keySet()) {
                    for (String entry : byCategory.get(cat)) {
                        sb.append("- ")
                                .append(cat)
                                .append(" ")
                                .append(entry)
                                .append("\n");
                    }
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private static String toTitle(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
