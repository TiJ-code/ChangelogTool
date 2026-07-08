package tij.changelogs.config.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tij.changelogs.config.model.VersioningConfig;
import tij.changelogs.config.model.VersioningRule;

import java.util.ArrayList;
import java.util.List;

public final class VersioningParser {
    private static final String TAG_VERSIONING = "versioning";
    private static final String TAG_RULE = "rule";
    private static final String TAG_FILENAME = "filename";
    private static final String TAG_VERSION = "version";
    private static final String ATTR_REGEX = "regex";

    private VersioningParser() {}

    public static VersioningConfig parse(Element root) {
        NodeList versioning = root.getElementsByTagName(TAG_VERSIONING);

        if (versioning.getLength() == 0)
            return new VersioningConfig(List.of());

        Element versioningElement = (Element) versioning.item(0);

        NodeList ruleList = versioningElement.getElementsByTagName(TAG_RULE);

        List<VersioningRule> result = new ArrayList<>();

        for (int i = 0; i < ruleList.getLength(); i++) {
            Node node = ruleList.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element ruleElement = (Element) node;

            Element filename = (Element) ruleElement.getElementsByTagName(TAG_FILENAME).item(0);

            Element version = (Element) ruleElement.getElementsByTagName(TAG_VERSION).item(0);

            result.add(new VersioningRule(filename.getAttribute(ATTR_REGEX), version.getAttribute(ATTR_REGEX)));
        }

        return new VersioningConfig(result);
    }
}
