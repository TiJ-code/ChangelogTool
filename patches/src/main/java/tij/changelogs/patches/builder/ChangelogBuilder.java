package tij.changelogs.patches.builder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tij.changelogs.config.ConfigConstants;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlEntry;
import tij.changelogs.xmlModel.XmlTopic;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static tij.changelogs.xmlModel.XmlConstants.*;

public final class ChangelogBuilder {
    private ChangelogBuilder() {}

    public static void build(List<XmlTopic> cumulatedTopics) {
        try {
            Document doc = buildDocument(cumulatedTopics);

            var transformerFactory = TransformerFactory.newInstance();
            var transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "4"
            );


            final File cumulatedFile = new File(ConfigConstants.CUMULATED_DIR, "cumulated.xml");

            try (FileOutputStream out = new FileOutputStream(cumulatedFile)) {
                transformer.transform(new DOMSource(doc), new StreamResult(out));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Document buildDocument(List<XmlTopic> topics) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            var builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();

            var rootElement = doc.createElement(TAG_CHANGELOG);

            for (XmlTopic topic : topics) {
                var topicElement = createTopic(doc, topic);
                rootElement.appendChild(topicElement);
            }

            doc.appendChild(rootElement);

            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element createTopic(Document doc, XmlTopic topic) {
        var topicElement = doc.createElement(TAG_TOPIC);

        topicElement.setAttribute(ATTRIBUTE_TOPIC_NAME, topic.name());

        for (XmlCategory category : topic.categories()) {
            var categoryElement = createCategory(doc, category);
            topicElement.appendChild(categoryElement);
        }

        return topicElement;
    }

    private static Element createCategory(Document doc, XmlCategory category) {
        var categoryElement = doc.createElement(TAG_CATEGORY);

        categoryElement.setAttribute(ATTRIBUTE_CATEGORY_NAME, category.name());

        for (XmlBreaking breaking : category.breakingLevels()) {
            var breakingElement = createBreaking(doc, breaking);
            categoryElement.appendChild(breakingElement);
        }

        appendEntries(doc, categoryElement, category.topLevelEntries());

        return categoryElement;
    }

    private static Element createBreaking(Document doc, XmlBreaking breaking) {
        var breakingElement = doc.createElement(TAG_BREAKING);

        breakingElement.setAttribute(ATTRIBUTE_BREAKING_SEVERITY, breaking.severity());

        appendEntries(doc, breakingElement, breaking.entries());

        return breakingElement;
    }

    private static void appendEntries(Document doc, Element parent, List<XmlEntry> entriesToAdd) {
        for (XmlEntry entry : entriesToAdd) {
            var entryElement = createEntry(doc, entry);
            parent.appendChild(entryElement);
        }
    }

    private static Element createEntry(Document doc, XmlEntry entry) {
        var entryElement = doc.createElement(TAG_ENTRY);

        entryElement.setTextContent(entry.value());

        return entryElement;
    }
}
