package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tij.changelogs.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

        List<String> topics =
                parseEntries(root, TAG_TOPICS);

        List<String> breaking =
                parseEntries(root, TAG_BREAKING_CHANGES);

        return new Config(null, categories, topics, breaking);
    }

    private static Map<String, String> parseCategories(Element root) {
        NodeList categoriesList = root.getElementsByTagName(TAG_CATEGORIES);

        if (categoriesList.getLength() != 1) {
            throw new RuntimeException("There must be exactly one <" + TAG_CATEGORIES + ">");
        }

        Element parent = (Element) categoriesList.item(0);

        NodeList categories = parent.getElementsByTagName(TAG_CATEGORY);

        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < categories.getLength(); i++) {
            Node node = categories.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element nodeEl = (Element) node;

            if (!TAG_CATEGORY.equals(nodeEl.getTagName()))
                continue;

            String attributeXmlWrapper = nodeEl.getAttribute(ATTRIBUTE_CATEGORY_NAME);
            String textContent = nodeEl.getTextContent().trim();

            result.put(attributeXmlWrapper, textContent);
        }

        return result;
    }

    private static List<String> parseEntries(Element root, String parentTag) {

        NodeList parents = root.getElementsByTagName(parentTag);

        if (parents.getLength() != 1) {
            throw new RuntimeException("There must be exactly one <" + parentTag + ">");
        }

        Element parent = (Element) parents.item(0);

        NodeList entries = parent.getElementsByTagName(TAG_ENTRY);

        List<String> result = new ArrayList<>();

        for (int i = 0; i < entries.getLength(); i++) {
            Element e = (Element) entries.item(i);
            result.add(e.getTextContent().trim());
        }

        return makeUnique(result);
    }

    private static List<String> makeUnique(List<String> in) {
        return new ArrayList<>(new HashSet<>(in));
    }
}
