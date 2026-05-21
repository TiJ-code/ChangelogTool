package tij.changelogs.patches.parser;

import org.w3c.dom.Document;
import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.xmlModel.XmlBreaking;
import tij.changelogs.xmlModel.XmlCategory;
import tij.changelogs.xmlModel.XmlEntry;
import tij.changelogs.xmlModel.XmlTopic;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class PatchParser {
    private PatchParser() {}

    public static List<XmlTopic> parsePatches(File[] patchFile, ReducedConfig config) {
        List<XmlTopic> all = new ArrayList<>();

        for (File f : patchFile) {
            PatchFileVersion patchFileVersion = parseVersionWithoutDtd(f);
            List<XmlTopic> topics = patchFileVersion.parseFunction.apply(f, config);
            mergeTopics(all, topics, config);
        }

        return all;
    }

    private static void mergeTopics(List<XmlTopic> result, List<XmlTopic> toMerge, ReducedConfig config) {
        for (XmlTopic incomingTopic : toMerge) {
            XmlTopic existingTopic = findMatchingTopic(incomingTopic, result);

            if (existingTopic == null) {
                result.add(incomingTopic);
                continue;
            }

            List<XmlCategory> mergedCategories = new ArrayList<>(existingTopic.categories());

            for (XmlCategory incomingCategory : incomingTopic.categories()) {
                XmlCategory existingCategory = findMatchingCategory(incomingCategory, mergedCategories);

                if (existingCategory == null) {
                    mergedCategories.add(incomingCategory);
                    continue;
                }

                mergedCategories.remove(existingCategory);
                mergedCategories.add(mergeCategory(existingCategory, incomingCategory));
            }

            result.remove(existingTopic);
            result.add(new XmlTopic(existingTopic.name(), mergedCategories));
        }


    }

    private static XmlCategory mergeCategory(XmlCategory base, XmlCategory incoming) {
        List<XmlEntry> mergedTopLevelEntries = new ArrayList<>(base.topLevelEntries());
        addMissingEntries(mergedTopLevelEntries, incoming.topLevelEntries());

        List<XmlBreaking> mergedBreakings = new ArrayList<>(base.breakingLevels());
        for (XmlBreaking incomingBreaking : incoming.breakingLevels()) {
            XmlBreaking existingBreaking = findMatchingBreaking(incomingBreaking, mergedBreakings);

            if (existingBreaking == null) {
                mergedBreakings.add(incomingBreaking);
                continue;
            }

            List<XmlEntry> mergedEntries = new ArrayList<>(existingBreaking.entries());

            addMissingEntries(mergedEntries, incomingBreaking.entries());

            mergedBreakings.remove(existingBreaking);
            mergedBreakings.add(new XmlBreaking(existingBreaking.severity(), mergedEntries));
        }

        return new XmlCategory(
                base.name(),
                mergedBreakings,
                mergedTopLevelEntries
        );
    }

    private static void addMissingEntries(List<XmlEntry> target, List<XmlEntry> source) {
        for (XmlEntry sourceEntry : source) {
            if (target.contains(sourceEntry))
                continue;
            target.add(sourceEntry);
        }
    }

    private static XmlTopic findMatchingTopic(XmlTopic target, List<XmlTopic> topics) {
        for (XmlTopic xmlTopic : topics) {
            if (xmlTopic.name().equals(target.name()))
                return xmlTopic;
        }
        return null;
    }

    private static XmlCategory findMatchingCategory(XmlCategory target, List<XmlCategory> categories) {
        for (XmlCategory xmlCategory : categories) {
            if (xmlCategory.name().equals(target.name()))
                return xmlCategory;
        }
        return null;
    }

    private static XmlBreaking findMatchingBreaking(XmlBreaking target, List<XmlBreaking> breaking) {
        for (XmlBreaking xmlBreak : breaking) {
            if (xmlBreak.severity().equals(target.severity()))
                return xmlBreak;
        }
        return null;
    }

    private static PatchFileVersion parseVersionWithoutDtd(
            File file
    ) {

        try {
            var factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/validation/dynamic", false);

            var builder = factory.newDocumentBuilder();

            Document doc = builder.parse(file);

            var el = doc.getDocumentElement();

            return PatchFileVersion.from(
                    el.getAttribute("version")
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not parse patch version",
                    e
            );
        }
    }
}