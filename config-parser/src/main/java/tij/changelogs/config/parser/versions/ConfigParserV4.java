package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigValidator;
import tij.changelogs.config.model.ComponentConfig;
import tij.changelogs.config.model.TopicConfig;
import tij.changelogs.config.model.VersioningConfig;
import tij.changelogs.config.parser.VersioningParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigParserV4 {
    private ConfigParserV4() {}

    public static Config parse(Document doc) {
        try {
            ConfigValidator.validate(doc, "/config.v4.xsd");
        } catch (Exception e) {
            throw new RuntimeException("Invalid configuration file: " + e.getMessage(), e);
        }

        return ConfigParserV3.parseDocument(doc);
    }
}