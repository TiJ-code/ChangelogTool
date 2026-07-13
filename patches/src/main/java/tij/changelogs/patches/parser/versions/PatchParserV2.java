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
import tij.changelogs.xmlModel.XmlComponent;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static tij.changelogs.xmlModel.XmlConstants.*;

public final class PatchParserV2 {

    private PatchParserV2() {}

    public static List<XmlComponent> parse(File patchFile, ReducedConfig config) {
        try (InputStream is = Files.newInputStream(patchFile.toPath().toAbsolutePath())) {

            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            var builder = factory.newDocumentBuilder();

            builder.setEntityResolver(createResolver());

            Document doc = builder.parse(is);

            if (!doc.getDocumentElement().getTagName().equals(TAG_PATCH)) {
                return List.of();
            }

            List<XmlComponent> components = new ArrayList<>();

            parseComponents(doc, components, config);

            return components;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void parseComponents(
            Document doc,
            List<XmlComponent> components,
            ReducedConfig config
    ) {
        NodeList componentNodes = doc.getElementsByTagName(TAG_COMPONENT);

        if (componentNodes.getLength() > config.componentValues().size()) {
            throw new RuntimeException(
                    "Too many component nodes in patch, max possible: "
                            + config.componentValues().size()
            );
        }

        for (int i = 0; i < componentNodes.getLength(); i++) {

            Node node = componentNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;

            if (!element.hasAttribute(ATTRIBUTE_COMPONENT_PATH)) {
                throw new RuntimeException(
                        "No attribute \"%s\" specified on <%s>"
                                .formatted(
                                        ATTRIBUTE_COMPONENT_PATH,
                                        TAG_COMPONENT
                                )
                );
            }

            String path =
                    element.getAttribute(ATTRIBUTE_COMPONENT_PATH);

            if (!config.componentValues().contains(path)) {
                throw new RuntimeException(
                        "Invalid component \"%s\" specified: %s"
                                .formatted(
                                        ATTRIBUTE_COMPONENT_PATH,
                                        path
                                )
                );
            }

            components.add(
                    new XmlComponent(
                            path,
                            parseCategories(element, config)
                    )
            );
        }
    }


    private static List<XmlCategory> parseCategories(
            Element componentElement,
            ReducedConfig config
    ) {
        List<XmlCategory> categories = new ArrayList<>();

        NodeList categoryNodes =
                componentElement.getElementsByTagName(TAG_CATEGORY);

        if (categoryNodes.getLength() > config.categoryValues().size()) {
            throw new RuntimeException(
                    "Too many category nodes in component \"%s\", max possible %d"
                            .formatted(
                                    componentElement.getAttribute(ATTRIBUTE_COMPONENT_PATH),
                                    config.categoryValues().size()
                            )
            );
        }

        for (int i = 0; i < categoryNodes.getLength(); i++) {

            Node node = categoryNodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;

            if (!element.hasAttribute(ATTRIBUTE_CATEGORY_NAME)) {
                throw new RuntimeException(
                        "No attribute \"%s\" specified on <%s>"
                                .formatted(
                                        ATTRIBUTE_CATEGORY_NAME,
                                        TAG_CATEGORY
                                )
                );
            }

            String category =
                    element.getAttribute(ATTRIBUTE_CATEGORY_NAME);

            if (!config.categoryValues().contains(category)) {
                throw new RuntimeException(
                        "Invalid category specified: " + category
                );
            }

            categories.add(
                    new XmlCategory(
                            category,
                            parseBreakings(element, config),
                            parseEntries(element)
                    )
            );
        }

        return categories;
    }


    private static List<XmlBreaking> parseBreakings(
            Element categoryElement,
            ReducedConfig config
    ) {
        List<XmlBreaking> breakings = new ArrayList<>();

        NodeList nodes =
                categoryElement.getElementsByTagName(TAG_BREAKING);

        for (int i = 0; i < nodes.getLength(); i++) {

            Node node = nodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;

            if (!element.hasAttribute(ATTRIBUTE_BREAKING_SEVERITY)) {
                throw new RuntimeException(
                        "No attribute \"%s\" specified on <%s>"
                                .formatted(
                                        ATTRIBUTE_BREAKING_SEVERITY,
                                        TAG_BREAKING
                                )
                );
            }

            String severity =
                    element.getAttribute(ATTRIBUTE_BREAKING_SEVERITY);

            if (!config.breakingLevelValues().contains(severity)) {
                throw new RuntimeException(
                        "Invalid breaking severity: " + severity
                );
            }

            breakings.add(
                    new XmlBreaking(
                            severity,
                            parseEntries(element)
                    )
            );
        }

        return breakings;
    }


    private static List<XmlEntry> parseEntries(Element parentElement) {
        List<XmlEntry> entries = new ArrayList<>();

        NodeList children = parentElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;

            if (element.getTagName().equals(TAG_ENTRY)) {
                entries.add(
                        new XmlEntry(
                                element.getTextContent()
                        )
                );
            }
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
                fileName =
                        systemId.substring(
                                systemId.lastIndexOf('/') + 1
                        );
            }

            String resourcePath = "/" + fileName;

            InputStream resource =
                    PatchParserV2.class.getResourceAsStream(resourcePath);

            if (resource != null) {
                return new InputSource(resource);
            }

            throw new FileNotFoundException(
                    "DTD not found in resources: " + resourcePath
            );
        };
    }
}