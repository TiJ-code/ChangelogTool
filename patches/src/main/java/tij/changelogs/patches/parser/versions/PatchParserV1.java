package tij.changelogs.patches.parser.versions;

import org.w3c.dom.*;
import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.xmlModel.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import static tij.changelogs.xmlModel.XmlConstants.*;

public final class PatchParserV1 {
    private PatchParserV1() {}
    public static List<XmlComponent> parse(File file, ReducedConfig config) {
        try (var in = Files.newInputStream(file.toPath())) {
            var f = DocumentBuilderFactory.newInstance(); f.setNamespaceAware(false); f.setIgnoringComments(true);
            Element root = f.newDocumentBuilder().parse(in).getDocumentElement();
            if (!TAG_PATCH.equals(root.getTagName())) throw new IllegalArgumentException("Root element must be <patch>");
            List<XmlComponent> out = new ArrayList<>();
            for (Element topic : children(root, TAG_TOPIC)) {
                String topicName = required(topic, ATTRIBUTE_TOPIC_NAME);
                if (!config.topicValues().contains(topicName)) throw new IllegalArgumentException("Invalid topic: " + topicName);
                for (Element component : children(topic, TAG_COMPONENT)) {
                    String ref = required(component, ATTRIBUTE_COMPONENT_REF);
                    if (!config.componentValues().contains(ref)) throw new IllegalArgumentException("Invalid component reference: " + ref);
                    out.add(new XmlComponent(topicName, ref, categories(component, config)));
                }
            }
            return out;
        } catch (Exception e) { throw new RuntimeException("Failed to parse patch " + file, e); }
    }
    private static List<XmlCategory> categories(Element component, ReducedConfig config) {
        List<XmlCategory> out = new ArrayList<>();
        for (Element category : children(component, TAG_CATEGORY)) {
            String name = required(category, ATTRIBUTE_CATEGORY_NAME);
            if (!config.categoryValues().contains(name)) throw new IllegalArgumentException("Invalid category: " + name);
            List<XmlBreaking> breaking = new ArrayList<>();
            for (Element b : children(category, TAG_BREAKING)) {
                String severity = required(b, ATTRIBUTE_BREAKING_SEVERITY);
                if (!config.breakingLevelValues().contains(severity)) throw new IllegalArgumentException("Invalid breaking severity: " + severity);
                breaking.add(new XmlBreaking(severity, entries(b)));
            }
            out.add(new XmlCategory(name, breaking, entries(category)));
        }
        return out;
    }
    private static List<XmlEntry> entries(Element parent) {
        List<XmlEntry> out = new ArrayList<>();
        for (Element e : children(parent, TAG_ENTRY)) out.add(new XmlEntry(e.getTextContent().trim()));
        return out;
    }
    private static List<Element> children(Element parent, String name) {
        List<Element> out = new ArrayList<>();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node n = parent.getChildNodes().item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) out.add((Element)n);
        }
        return out;
    }
    private static String required(Element e, String name) {
        if (!e.hasAttribute(name)) throw new IllegalArgumentException("Missing attribute '" + name + "' on <" + e.getTagName() + ">");
        return e.getAttribute(name);
    }
}
