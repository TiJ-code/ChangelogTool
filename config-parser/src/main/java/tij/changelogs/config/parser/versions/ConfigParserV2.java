package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigValidator;

/** Parser for the reduced versioning-only configuration format version 2. */
public final class ConfigParserV2 {
    private ConfigParserV2() {}

    public static Config parse(Document doc) {
        try {
            ConfigValidator.validate(doc, "/config.v2.xsd");
        } catch (Exception e) {
            throw new RuntimeException("Invalid configuration file: " + e.getMessage(), e);
        }
        return ConfigParserDocument.parse(doc);
    }
}
