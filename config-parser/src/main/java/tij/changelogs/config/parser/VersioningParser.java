package tij.changelogs.config.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tij.changelogs.config.model.ConfigFileVersion;
import tij.changelogs.config.model.VersioningConfig;
import tij.changelogs.config.model.VersioningEvent;
import tij.changelogs.config.model.VersioningEventType;
import tij.changelogs.config.model.VersioningPhase;
import tij.changelogs.config.model.VersioningRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class VersioningParser {
    private static final String TAG_VERSIONING = "versioning";
    private static final String TAG_RULE = "rule";
    private static final String TAG_FILENAME = "filename";
    private static final String TAG_VERSION = "version";
    private static final String TAG_PHASES = "phases";
    private static final String TAG_PHASE = "phase";
    private static final String TAG_NEXT = "next";
    private static final String TAG_FORMATTER = "formatter";
    private static final String TAG_EVENTS = "events";
    private static final String TAG_EVENT = "event";
    private static final String TAG_MAJOR = "major";
    private static final String TAG_MINOR = "minor";
    private static final String TAG_PATCH = "patch";

    private static final String ATTR__REGEX = "regex";
    private static final String ATTR__PHASE__NAME = "name";
    private static final String ATTR__EVENT__ON = "on";

    public static final String TEMPLATE__NUMERIC_VERSION = "{#numeric_version}";
    public static final String TEMPLATE__PREFIX = "{#prefix}";
    public static final String TEMPLATE__SUFFIX = "{#suffix}";

    private VersioningParser() {}

    public static VersioningConfig parse(Element root) {
        return VersioningParserV1.parse(root);
    }

    public static VersioningConfig parse(Element root, ConfigFileVersion configFileVersion) {
        return switch (configFileVersion) {
            case ConfigFileVersion.v3 -> VersioningParserV1.parse(root);
            case ConfigFileVersion.v4 -> VersioningParserV2.parse(root);
            default -> VersioningConfig.INVALID;
        };
    }

    private static class VersioningParserV1 {
        public static VersioningConfig parse(Element root) {
            NodeList versioning = root.getElementsByTagName(TAG_VERSIONING);

            if (versioning.getLength() == 0)
                return VersioningConfig.INVALID;

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

                result.add(new VersioningRule(filename.getAttribute(ATTR__REGEX), version.getAttribute(ATTR__REGEX)));
            }

            return new VersioningConfig(result, List.of());
        }
    }

    private static class VersioningParserV2 {
        public static VersioningConfig parse(Element root) {
            NodeList versioningNodes = root.getElementsByTagName(TAG_VERSIONING);

            if (versioningNodes.getLength() == 0)
                return VersioningConfig.INVALID;

            Element versioningElement = (Element) versioningNodes.item(0);

            List<VersioningRule> rules = parseRules(versioningElement);
            List<VersioningPhase> phases = parsePhases(versioningElement);

            return new VersioningConfig(rules, phases);
        }

        private static List<VersioningRule> parseRules(Element versioningElement) {
            List<VersioningRule> rules = new ArrayList<>();

            NodeList ruleList = versioningElement.getElementsByTagName(TAG_RULE);

            for (int i = 0; i < ruleList.getLength(); i++) {
                Node node = ruleList.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element ruleElement = (Element) node;

                Element filename =
                        (Element) ruleElement.getElementsByTagName(TAG_FILENAME).item(0);

                Element version =
                        (Element) ruleElement.getElementsByTagName(TAG_VERSION).item(0);

                if (filename == null || version == null)
                    continue;

                rules.add(new VersioningRule(
                        filename.getAttribute(ATTR__REGEX),
                        version.getAttribute(ATTR__REGEX)
                ));
            }

            return rules;
        }

        private static List<VersioningPhase> parsePhases(Element versioningElement) {
            List<VersioningPhase> phases = new ArrayList<>();

            NodeList phasesNodes = versioningElement.getElementsByTagName(TAG_PHASES);

            if (phasesNodes.getLength() == 0)
                return phases;

            Element phasesElement = (Element) phasesNodes.item(0);

            NodeList phaseNodes = phasesElement.getElementsByTagName(TAG_PHASE);

            for (int i = 0; i < phaseNodes.getLength(); i++) {
                Node node = phaseNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element phaseElement = (Element) node;

                String name = phaseElement.getAttribute(ATTR__PHASE__NAME);

                String next = getTextContent(
                        phaseElement,
                        TAG_NEXT
                );

                String formatter = getTextContent(
                        phaseElement,
                        TAG_FORMATTER
                );

                String prefix = getOptionalTextContent(
                        phaseElement,
                        "prefix"
                );

                String suffix = getOptionalTextContent(
                        phaseElement,
                        "suffix"
                );

                List<VersioningEvent> events = parseEvents(phaseElement);

                phases.add(new VersioningPhase(
                        name,
                        formatter,
                        Optional.ofNullable(next),
                        Optional.ofNullable(prefix),
                        Optional.ofNullable(suffix),
                        events
                ));
            }

            return phases;
        }

        private static List<VersioningEvent> parseEvents(Element phaseElement) {
            List<VersioningEvent> events = new ArrayList<>();

            NodeList eventsNodes = phaseElement.getElementsByTagName(TAG_EVENTS);

            if (eventsNodes.getLength() == 0)
                return events;

            Element eventsElement = (Element) eventsNodes.item(0);

            NodeList eventNodes = eventsElement.getElementsByTagName(TAG_EVENT);

            for (int i = 0; i < eventNodes.getLength(); i++) {
                Node node = eventNodes.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element eventElement = (Element) node;

                String trigger = eventElement.getAttribute(ATTR__EVENT__ON);
                VersioningEventType vet = VersioningEventType.valueOf(trigger.toUpperCase());

                events.add(new VersioningEvent(
                        vet,
                        getOptionalInt(eventElement, TAG_MAJOR),
                        getOptionalInt(eventElement, TAG_MINOR),
                        getOptionalInt(eventElement, TAG_PATCH)
                ));
            }

            return events;
        }

        private static String getTextContent(Element parent, String tag) {
            Element element = (Element) parent.getElementsByTagName(tag).item(0);

            if (element == null)
                return null;

            String value = element.getTextContent().trim();
            return value.isEmpty() ? null : value;
        }

        private static String getOptionalTextContent(Element parent, String tag) {
            Element element = (Element) parent.getElementsByTagName(tag).item(0);

            if (element == null)
                return null;

            return element.getTextContent().trim();
        }

        private static OptionalInt getOptionalInt(Element parent, String tag) {
            String value = getOptionalTextContent(parent, tag);
            return value == null || value.isBlank()
                    ? OptionalInt.empty()
                    : OptionalInt.of(Integer.parseInt(value));
        }
    }
}
