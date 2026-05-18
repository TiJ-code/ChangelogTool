package tij.changelogs.markdown.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tij.changelogs.config.Config;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlEntry;
import tij.changelogs.xmlModel.XmlTopic;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static tij.changelogs.xmlModel.XmlConstants.*;

public final class ChangelogParser {
    private ChangelogParser() {}

    public static List<XmlTopic> parse(File changelogFile, Config config) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );

            var builder = factory.newDocumentBuilder();
            Document doc = builder.parse(changelogFile);

            var root = doc.getDocumentElement();

            if (!root.getTagName().equals(TAG_CHANGELOG)) {
                throw new RuntimeException(
                        "Root element must be <%s>".formatted(TAG_CHANGELOG)
                );
            }

            return parseTopics(root, config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }

    private static List<XmlTopic> parseTopics(Element root, Config config) {
        List<XmlTopic> topics = new ArrayList<>();

        NodeList topicNodes = root.getChildNodes();

        Set<String> usedTopicNames = new HashSet<>();

        for (int i = 0; i < topicNodes.getLength(); i++) {
            var node = topicNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var topicElement = (Element) node;

            if (!topicElement.getTagName().equals(TAG_TOPIC))
                continue;

            validateAttributeExists(topicElement, ATTRIBUTE_TOPIC_NAME);

            String topicName = topicElement.getAttribute(ATTRIBUTE_TOPIC_NAME);

            if (!config.topics().contains(topicName)) {
                throw new RuntimeException("Invalid topic: " + topicName);
            }

            if (!usedTopicNames.add(topicName)) {
                throw new RuntimeException("Duplicate topic: " + topicName);
            }

            topics.add(new XmlTopic(topicName, parseCategories(topicElement, config)));
        }

        if (topics.size() > config.topics().size()) {
            throw new RuntimeException("Too many topics");
        }

        return topics;
    }

    private static List<XmlCategory> parseCategories(Element topicElement, Config config) {
        List<XmlCategory> categories = new ArrayList<>();

        Set<String> usedCategoryNames = new HashSet<>();

        NodeList categoryNodes = topicElement.getChildNodes();

        for (int i = 0; i < categoryNodes.getLength(); i++) {
            var node = categoryNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var categoryElement = (Element) node;

            if (!categoryElement.getTagName().equals(TAG_CATEGORY))
                continue;

            validateAttributeExists(
                    categoryElement,
                    ATTRIBUTE_CATEGORY_NAME
            );

            String categoryName =
                    categoryElement.getAttribute(
                            ATTRIBUTE_CATEGORY_NAME
                    );

            if (!config.categories().containsKey(categoryName)) {
                throw new RuntimeException(
                        "Invalid category: " + categoryName
                );
            }

            if (!usedCategoryNames.add(categoryName)) {
                throw new RuntimeException(
                        "Duplicate category: " + categoryName
                );
            }

            categories.add(
                    new XmlCategory(
                            categoryName,
                            parseBreakings(categoryElement, config),
                            parseTopLevelEntries(categoryElement)
                    )
            );
        }

        if (categories.size() > config.categories().size()) {
            throw new RuntimeException(
                    "Too many categories in topic: "
                            + topicElement.getAttribute(
                            ATTRIBUTE_TOPIC_NAME
                    )
            );
        }

        return categories;
    }

    private static List<XmlBreaking> parseBreakings(Element categoryElement, Config config) {
        List<XmlBreaking> breakings = new ArrayList<>();

        Set<String> usedBreakingLevels = new HashSet<>();

        NodeList childNodes = categoryElement.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var breakingElement = (Element) node;

            if (!breakingElement.getTagName().equals(TAG_BREAKING))
                continue;

            validateAttributeExists(
                    breakingElement,
                    ATTRIBUTE_BREAKING_SEVERITY
            );

            String severity =
                    breakingElement.getAttribute(
                            ATTRIBUTE_BREAKING_SEVERITY
                    );

            if (!config.breakingLevels().contains(severity)) {
                throw new RuntimeException(
                        "Invalid breaking severity: "
                                + severity
                );
            }

            if (!usedBreakingLevels.add(severity)) {
                throw new RuntimeException(
                        "Duplicate breaking severity: "
                                + severity
                );
            }

            breakings.add(
                    new XmlBreaking(
                            severity,
                            parseEntries(breakingElement)
                    )
            );
        }

        if (breakings.size() > config.breakingLevels().size()) {
            throw new RuntimeException(
                    "Too many breaking levels in category: "
                            + categoryElement.getAttribute(
                            ATTRIBUTE_CATEGORY_NAME
                    )
            );
        }

        return breakings;
    }

    private static List<XmlEntry> parseTopLevelEntries(Element categoryElement) {
        List<XmlEntry> entries = new ArrayList<>();

        NodeList childNodes = categoryElement.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var element = (Element) node;

            if (!element.getTagName().equals(TAG_ENTRY))
                continue;

            entries.add(
                    new XmlEntry(
                            element.getTextContent().trim()
                    )
            );
        }

        return entries;
    }

    private static List<XmlEntry> parseEntries(Element parent) {
        List<XmlEntry> entries = new ArrayList<>();

        NodeList childNodes = parent.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var element = (Element) node;

            if (!element.getTagName().equals(TAG_ENTRY))
                continue;

            entries.add(
                    new XmlEntry(
                            element.getTextContent().trim()
                    )
            );
        }

        return entries;
    }

    private static void validateAttributeExists(Element element, String attribute) {
        if (!element.hasAttribute(attribute)) {
            throw new RuntimeException(
                    "Missing attribute \"%s\" on <%s>"
                            .formatted(
                                    attribute,
                                    element.getTagName()
                            )
            );
        }
    }
}
