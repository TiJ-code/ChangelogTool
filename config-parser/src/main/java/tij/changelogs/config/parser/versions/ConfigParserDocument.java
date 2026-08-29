package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tij.changelogs.config.Config;
import tij.changelogs.config.model.ArchiveConfig;
import tij.changelogs.config.model.ArchiveHierarchy;
import tij.changelogs.config.model.ComponentConfig;
import tij.changelogs.config.model.TopicConfig;
import tij.changelogs.config.model.VersioningConfig;
import tij.changelogs.config.parser.VersioningParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigParserDocument {
    private ConfigParserDocument() {}

    public static Config parse(Document doc) {
        Element root = doc.getDocumentElement();

        VersioningConfig versioning = VersioningParser.parse(root);

        Map<String, String> categories = parseCategories(root);

        Map<String, ComponentConfig> components = new HashMap<>();
        parseComponents(root, components);

        List<TopicConfig> topics = parseTopics(root);

        List<String> breakingLevels = parseBreakingLevels(root);
        ArchiveConfig archive = parseArchive(root);

        return new Config(
                versioning,
                categories,
                components,
                topics,
                breakingLevels,
                archive
        );
    }

    private static ArchiveConfig parseArchive(Element root) {
        Element archive = getFirst(root, "archive");
        if (archive == null) {
            archive = getFirst(root, "archiving");
        }
        if (archive == null) {
            return ArchiveConfig.DEFAULT;
        }

        String hierarchy = archive.getAttribute("hierarchy").trim();
        if (hierarchy.isEmpty()) {
            Element hierarchyElement = getFirst(archive, "hierarchy");
            if (hierarchyElement != null) {
                hierarchy = hierarchyElement.getTextContent().trim();
            }
        }
        if (hierarchy.isEmpty()) {
            NodeList rules = archive.getElementsByTagName("rule");
            for (int i = 0; i < rules.getLength(); i++) {
                Element rule = (Element) rules.item(i);
                if ("hierarchy".equals(rule.getAttribute("name"))) {
                    hierarchy = rule.getTextContent().trim();
                    break;
                }
            }
        }
        if (hierarchy.isEmpty()) {
            return ArchiveConfig.DEFAULT;
        }

        try {
            ArchiveHierarchy parsed = ArchiveHierarchy.valueOf(hierarchy.toUpperCase().replace('-', '_'));
            if (parsed == ArchiveHierarchy.NONE) {
                throw new IllegalArgumentException();
            }
            return new ArchiveConfig(parsed);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported archive hierarchy: " + hierarchy
                            + ". Expected major or major-minor", e);
        }
    }

    private static Map<String, String> parseCategories(Element root) {
        Map<String, String> categories = new HashMap<>();

        Element element = getFirst(root, "categories");

        if (element == null) {
            return categories;
        }

        NodeList nodes = element.getElementsByTagName("category");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element category = (Element) nodes.item(i);

            categories.put(
                    category.getAttribute("name"),
                    category.getTextContent().trim()
            );
        }

        return categories;
    }


    private static void parseComponents(
            Element root,
            Map<String, ComponentConfig> registry
    ) {
        Element components = getFirst(root, "components");

        if (components == null) {
            return;
        }

        NodeList nodes = components.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element element)) {
                continue;
            }

            if (element.getTagName().equals("component")) {
                parseComponent(element, "", registry);
            }
        }
    }


    private static ComponentConfig parseComponent(
            Element element,
            String parentPath,
            Map<String, ComponentConfig> registry
    ) {
        String id = element.getAttribute("id");

        String path = parentPath.isEmpty()
                ? id
                : parentPath + "/" + id;


        List<ComponentConfig> children = new ArrayList<>();

        NodeList nodes = element.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element child)) {
                continue;
            }

            if (child.getTagName().equals("component")) {
                children.add(
                        parseComponent(child, path, registry)
                );
            }
        }

        ComponentConfig config = new ComponentConfig(
                id,
                element.getAttribute("name"),
                children
        );

        registry.put(path, config);

        return config;
    }


    private static List<TopicConfig> parseTopics(Element root) {
        List<TopicConfig> topics = new ArrayList<>();

        Element topicsElement = getFirst(root, "topics");

        if (topicsElement == null) {
            return topics;
        }

        NodeList nodes = topicsElement.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element element)) {
                continue;
            }

            if (element.getTagName().equals("topic")) {
                topics.add(parseTopic(element));
            }
        }

        return topics;
    }


    private static TopicConfig parseTopic(Element element) {
        List<String> refs = new ArrayList<>();

        NodeList nodes = element.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element child)) {
                continue;
            }

            if (child.getTagName().equals("component-ref")) {
                refs.add(child.getAttribute("ref"));
            }
        }

        return new TopicConfig(
                element.getAttribute("name"),
                refs
        );
    }


    private static List<String> parseBreakingLevels(Element root) {
        List<String> levels = new ArrayList<>();

        Element element = getFirst(root, "breaking.changes");

        if (element == null) {
            return levels;
        }

        NodeList nodes = element.getElementsByTagName("entry");

        for (int i = 0; i < nodes.getLength(); i++) {
            levels.add(nodes.item(i).getTextContent().trim());
        }

        return levels;
    }


    private static Element getFirst(Element root, String tag) {
        NodeList list = root.getElementsByTagName(tag);

        if (list.getLength() == 0) {
            return null;
        }

        return (Element) list.item(0);
    }
}
