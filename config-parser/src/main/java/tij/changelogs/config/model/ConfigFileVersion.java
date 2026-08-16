package tij.changelogs.config.model;

import org.w3c.dom.Document;
import tij.changelogs.config.Config;
import tij.changelogs.config.parser.versions.ConfigParserV1;

import java.util.function.Function;

public enum ConfigFileVersion {
    v1("1", ConfigParserV1::parse);

    public final String attribute;
    public final Function<Document, Config> parseFunction;

    ConfigFileVersion(String attribute, Function<Document, Config> parseFunction) {
        this.attribute = attribute;
        this.parseFunction = parseFunction;
    }

    public static ConfigFileVersion from(String attribute) {
        for (ConfigFileVersion fv : values()) {
            if (fv.attribute.equals(attribute))
                return fv;
        }

        throw new IllegalArgumentException("Unsupported config file version: " + attribute);
    }

    public static ConfigFileVersion getLatest() {
        return values()[0];
    }
}
