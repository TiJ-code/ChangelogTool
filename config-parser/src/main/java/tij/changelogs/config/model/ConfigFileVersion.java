package tij.changelogs.config.model;

import org.w3c.dom.Document;
import tij.changelogs.config.Config;
import tij.changelogs.config.parser.versions.ConfigParserV1;
import tij.changelogs.config.parser.versions.ConfigParserV2;
import tij.changelogs.config.parser.versions.ConfigParserV3;
import tij.changelogs.config.parser.versions.ConfigParserV4;

import java.util.function.Function;

public enum ConfigFileVersion {
    v4("4", ConfigParserV4::parse),
    v3("3", ConfigParserV3::parse),
    v2("2", ConfigParserV2::parse),
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

        return getLatest();
    }

    public static ConfigFileVersion getLatest() {
        return values()[0];
    }
}
