package tij.changelogs.patches.config.versions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tij.changelogs.patches.config.Config;
import tij.changelogs.patches.config.ConfigBreakingChange;
import tij.changelogs.patches.config.ConfigCategory;
import tij.changelogs.patches.config.ConfigEntry;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ConfigParserV1 {
    private ConfigParserV1() {}

    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_BREAKING_CHANGES = "breaking.changes";
    private static final String TAG_ENTRY = "entry";

    public static Config parse(Document doc) {

        Element root = doc.getDocumentElement();

        List<ConfigEntry> categories =
                parseEntries(root, TAG_CATEGORIES);

        List<ConfigEntry> breaking =
                parseEntries(root, TAG_BREAKING_CHANGES);

        return new Config(
                new ConfigCategory(categories),
                new ConfigBreakingChange(breaking)
        );
    }

    private static List<ConfigEntry> parseEntries(Element root, String parentTag) {

        NodeList parents = root.getElementsByTagName(parentTag);

        if (parents.getLength() != 1) {
            throw new RuntimeException("There must be exactly one <" + parentTag + ">");
        }

        Element parent = (Element) parents.item(0);

        NodeList entries = parent.getElementsByTagName(TAG_ENTRY);

        List<ConfigEntry> result = new ArrayList<>();

        for (int i = 0; i < entries.getLength(); i++) {
            Element e = (Element) entries.item(i);
            result.add(new ConfigEntry(e.getTextContent().trim()));
        }

        return result;
    }
}
