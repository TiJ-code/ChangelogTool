package tij.changelogs.patches.builder;

import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigConstants;
import tij.changelogs.config.model.TopicConfig;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlComponent;
import tij.changelogs.xmlModel.XmlEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static tij.changelogs.xmlModel.XmlConstants.*;

public final class ChangelogBuilder {
    private ChangelogBuilder() {}

    public static void build(Config config, List<XmlComponent> components) {
        try {
            Document doc = buildDocument(config, components);

            var transformerFactory = TransformerFactory.newInstance();

            var transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(
                    OutputKeys.METHOD,
                    "xml"
            );

            transformer.setOutputProperty(
                    OutputKeys.ENCODING,
                    "utf-8"
            );

            transformer.setOutputProperty(
                    OutputKeys.INDENT,
                    "yes"
            );

            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "4"
            );

            File cumulatedFile = new File(
                            ConfigConstants.CUMULATED_DIR,
                            "cumulated.xml"
            );

            try (FileOutputStream out = new FileOutputStream(cumulatedFile)) {
                transformer.transform(
                        new DOMSource(doc),
                        new StreamResult(out)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Document buildDocument(Config config, List<XmlComponent> components) {
        try {
            var factory = DocumentBuilderFactory.newInstance();

            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
            );

            var builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            Element root = doc.createElement(TAG_CHANGELOG);

            Map<String, XmlComponent> componentMap = components.stream()
                            .collect(Collectors.toMap(
                                    XmlComponent::path,
                                    Function.identity()
                            ));

            for (TopicConfig topic : config.topics()) {
                root.appendChild(
                        createTopic(
                                doc,
                                topic,
                                componentMap
                        )
                );
            }

            doc.appendChild(root);

            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element createTopic(Document doc, TopicConfig topic, Map<String, XmlComponent> components) {
        Element topicElement = doc.createElement(TAG_TOPIC);

        topicElement.setAttribute(ATTRIBUTE_TOPIC_NAME, topic.name());

        for (String ref : topic.componentRefs()) {
            XmlComponent component = components.get(ref);

            if (component == null)
                continue;

            topicElement.appendChild(
                    createComponent(
                            doc,
                            component
                    )
            );
        }

        return topicElement;
    }

    private static Element createComponent(Document doc, XmlComponent component) {
        Element componentElement = doc.createElement(TAG_COMPONENT);

        componentElement.setAttribute(ATTRIBUTE_COMPONENT_REF, component.path());

        for (XmlCategory category : component.categories()) {
            componentElement.appendChild(
                    createCategory(
                            doc,
                            category
                    )
            );
        }

        return componentElement;
    }

    private static Element createCategory(Document doc, XmlCategory category) {
        Element categoryElement = doc.createElement(TAG_CATEGORY);

        categoryElement.setAttribute(ATTRIBUTE_CATEGORY_NAME, category.name());

        for (XmlBreaking breaking : category.breakingLevels()) {
            categoryElement.appendChild(
                    createBreaking(
                            doc,
                            breaking
                    )
            );
        }

        appendEntries(doc, categoryElement, category.topLevelEntries());

        return categoryElement;
    }

    private static Element createBreaking(Document doc, XmlBreaking breaking) {
        Element breakingElement = doc.createElement(TAG_BREAKING);

        breakingElement.setAttribute(ATTRIBUTE_BREAKING_SEVERITY, breaking.severity());

        appendEntries(doc, breakingElement, breaking.entries());

        return breakingElement;
    }

    private static void appendEntries(Document doc, Element parent, List<XmlEntry> entries) {
        for (XmlEntry entry : entries) {
            parent.appendChild(createEntry(doc, entry));
        }
    }


    private static Element createEntry(Document doc, XmlEntry entry) {
        Element entryElement = doc.createElement(TAG_ENTRY);

        entryElement.setTextContent(entry.value());

        return entryElement;
    }
}