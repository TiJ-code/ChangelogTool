package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tij.changelogs.config.Config;
import tij.changelogs.config.model.ComponentConfig;
import tij.changelogs.config.model.TopicConfig;

import java.util.*;

public final class ConfigParserV1 {
    private ConfigParserV1() {}

    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_TOPICS = "topics";
    private static final String TAG_BREAKING_CHANGES = "breaking.changes";
    private static final String TAG_ENTRY = "entry";

    private static final String ATTRIBUTE_CATEGORY_NAME = "name";

    public static Config parse(Document doc) {

        Element root = doc.getDocumentElement();

        Map<String, String> categories =
                parseCategories(root);

        List<String> oldTopics =
                parseEntries(root, TAG_TOPICS);

        List<String> breaking =
                parseEntries(root, TAG_BREAKING_CHANGES);


        Map<String, ComponentConfig> components = new HashMap<>();

        List<String> refs = new ArrayList<>();

        for (String topic : oldTopics) {
            ComponentConfig component =
                    new ComponentConfig(
                            topic,
                            topic,
                            List.of()
                    );

            components.put(topic, component);
            refs.add(topic);
        }


        List<TopicConfig> topics = List.of(
                new TopicConfig(
                        "default",
                        refs
                )
        );


        return new Config(
                null,
                categories,
                components,
                topics,
                breaking
        );
    }


    private static Map<String, String> parseCategories(Element root) {
        NodeList categoriesList =
                root.getElementsByTagName(TAG_CATEGORIES);

        if (categoriesList.getLength() != 1) {
            throw new RuntimeException(
                    "There must be exactly one <" + TAG_CATEGORIES + ">"
            );
        }

        Element parent =
                (Element) categoriesList.item(0);

        NodeList categories =
                parent.getElementsByTagName(TAG_CATEGORY);

        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < categories.getLength(); i++) {

            Node node = categories.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;

            result.put(
                    element.getAttribute(ATTRIBUTE_CATEGORY_NAME),
                    element.getTextContent().trim()
            );
        }

        return result;
    }


    private static List<String> parseEntries(
            Element root,
            String parentTag
    ) {

        NodeList parents =
                root.getElementsByTagName(parentTag);

        if (parents.getLength() != 1) {
            throw new RuntimeException(
                    "There must be exactly one <" + parentTag + ">"
            );
        }

        Element parent =
                (Element) parents.item(0);

        NodeList entries =
                parent.getElementsByTagName(TAG_ENTRY);

        Set<String> result = new HashSet<>();

        for (int i = 0; i < entries.getLength(); i++) {
            result.add(
                    entries.item(i)
                            .getTextContent()
                            .trim()
            );
        }

        return new ArrayList<>(result);
    }
}