package tij.changelogs.patches.parser.versions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlEntry;
import tij.changelogs.xmlModel.XmlTopic;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static tij.changelogs.xmlModel.XmlConstants.*;

public class PatchParserV1 {
    public static List<XmlTopic> parse(File patchFiles, ReducedConfig config) {
        try (InputStream is = Files.newInputStream(patchFiles.toPath().toAbsolutePath())) {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            var builder = factory.newDocumentBuilder();

            builder.setEntityResolver(createResolver());

            Document doc = builder.parse(is);

            if (!doc.getDocumentElement().getTagName().equals(TAG_PATCH))
                return List.of();

            List<XmlTopic> topics = new ArrayList<>();

            parseTopics(doc, topics, config);

            return topics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseTopics(Document doc, List<XmlTopic> topics, ReducedConfig config) {
        NodeList topicNodes = doc.getElementsByTagName(TAG_TOPIC);

        if (topicNodes.getLength() > config.topicValues().size()) {
            throw new RuntimeException("Too many topic nodes in patch, max possible: " + config.topicValues().size());
        }

        for (int i = 0; i < topicNodes.getLength(); i++) {
            var node = topicNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var nodeElement = (Element) node;

            if (!nodeElement.hasAttribute(ATTRIBUTE_TOPIC_NAME)) {
                throw new RuntimeException("No attribute \"%s\" specified on <%s>".formatted(ATTRIBUTE_TOPIC_NAME, TAG_TOPIC));
            }

            String attrValue = nodeElement.getAttribute(ATTRIBUTE_TOPIC_NAME);

            if (!config.topicValues().contains(attrValue)) {
                throw new RuntimeException("Invalid topic \"%s\" specified: %s".formatted(ATTRIBUTE_TOPIC_NAME, attrValue));
            }

            topics.add(new XmlTopic(attrValue, parseCategories(nodeElement, config)));
        }
    }

    private static List<XmlCategory> parseCategories(Element topicElement, ReducedConfig config) {
        List<XmlCategory> categories = new ArrayList<>();

        NodeList categoryNodes = topicElement.getElementsByTagName(TAG_CATEGORY);

        if (categoryNodes.getLength() > config.categoryValues().size()) {
            throw new RuntimeException("Too many category nodes in topic \"%s\", max possible %d".formatted(
                    topicElement.getAttribute(ATTRIBUTE_TOPIC_NAME), config.categoryValues().size()
            ));
        }

        for (int i = 0; i < categoryNodes.getLength(); i++) {
            var node = categoryNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var nodeElement = (Element) node;

            if (!nodeElement.hasAttribute(ATTRIBUTE_CATEGORY_NAME)) {
                throw new RuntimeException("No attribute \"%s\" specified on <%s>".formatted(ATTRIBUTE_CATEGORY_NAME, TAG_CATEGORY));
            }

            String attrValue = nodeElement.getAttribute(ATTRIBUTE_CATEGORY_NAME);

            if (!config.categoryValues().contains(attrValue)) {
                throw new RuntimeException("Invalid category \"%s\" specified: %s".formatted(ATTRIBUTE_CATEGORY_NAME, attrValue));
            }

            categories.add(new XmlCategory(attrValue, parseBreakings(nodeElement, config), parseEntries(nodeElement)));
        }

        return categories;
    }

    private static List<XmlBreaking> parseBreakings(Element categoryElement, ReducedConfig config) {
        List<XmlBreaking> breakings = new ArrayList<>();

        NodeList breakingNodes = categoryElement.getElementsByTagName(TAG_BREAKING);
        for (int i = 0; i < breakingNodes.getLength(); i++) {
            var node = breakingNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var nodeElement = (Element) node;

            if (!nodeElement.hasAttribute(ATTRIBUTE_BREAKING_SEVERITY)) {
                throw new RuntimeException("No attribute \"%s\" specified on <%s>".formatted(ATTRIBUTE_BREAKING_SEVERITY, TAG_BREAKING));
            }

            String attrValue = nodeElement.getAttribute(ATTRIBUTE_BREAKING_SEVERITY);

            if (!config.breakingLevelValues().contains(attrValue)) {
                throw new RuntimeException("Invalid category \"%s\" specified: %s".formatted(ATTRIBUTE_CATEGORY_NAME, attrValue));
            }

            breakings.add(new XmlBreaking(attrValue, parseEntries(nodeElement)));
        }

        return breakings;
    }

    private static List<XmlEntry> parseEntries(Element parentElement) {
        List<XmlEntry> entries = new ArrayList<>();

        List<Element> entryElements = new ArrayList<>();

        NodeList childrenOfParent = parentElement.getChildNodes();
        for (int i = 0; i < childrenOfParent.getLength(); i++) {
            var node = childrenOfParent.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            var nodeElement = (Element) node;

            if (nodeElement.getTagName().equals(TAG_ENTRY))
                entryElements.add(nodeElement);
        }

        for (Element entryElement : entryElements) {
            entries.add(new XmlEntry(entryElement.getTextContent()));
        }

        return entries;
    }

    private static EntityResolver createResolver() {
        return (_, systemId) -> {

            if (systemId == null) {
                return null;
            }

            String fileName = systemId;

            if (systemId.contains("/")) {
                fileName = systemId.substring(systemId.lastIndexOf('/') + 1);
            }

            String resourcePath = "/" + fileName;

            InputStream resource =
                    PatchParserV1.class.getResourceAsStream(resourcePath);

            if (resource != null) {
                return new InputSource(resource);
            }

            throw new FileNotFoundException(
                    "DTD not found in resources: " + resourcePath
            );
        };
    }
}
