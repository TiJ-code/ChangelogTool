package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigValidator;
import tij.changelogs.config.model.ConfigFileVersion;

public final class ConfigParserV4 {
    private ConfigParserV4() {}

    public static Config parse(Document doc) {
        try {
            ConfigValidator.validate(doc, "/config.v4.xsd");
        } catch (Exception e) {
            throw new RuntimeException("Invalid configuration file: " + e.getMessage(), e);
        }

        return ConfigParserV3.parseDocument(doc, ConfigFileVersion.v4);
    }
}
