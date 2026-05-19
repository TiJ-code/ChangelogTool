package tij.changelogs.patches.config;

import org.w3c.dom.Document;
import tij.changelogs.patches.config.versions.ConfigParserV1;

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

        return getLatest();
    }

    public static ConfigFileVersion getLatest() {
        return values()[0];
    }
}
