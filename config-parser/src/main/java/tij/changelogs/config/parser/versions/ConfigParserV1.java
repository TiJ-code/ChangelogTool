package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigValidator;

/** Parser for the stable configuration format version 1. */
public final class ConfigParserV1 {
    private ConfigParserV1() {}

    public static Config parse(Document doc) {
        try {
            ConfigValidator.validate(doc, "/config.v1.xsd");
        } catch (Exception e) {
            throw new RuntimeException("Invalid configuration file: " + e.getMessage(), e);
        }
        return ConfigParserDocument.parse(doc);
    }
}
