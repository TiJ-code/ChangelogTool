package tij.changelogs.patches.parser;

import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.patches.parser.versions.PatchParserV1;
import tij.changelogs.xmlModel.XmlComponent;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

public enum PatchFileVersion {
    v1("1", PatchParserV1::parse);

    public final String attributeValue;
    public final BiFunction<File, ReducedConfig, List<XmlComponent>> parseFunction;

    PatchFileVersion(String attributeValue,
                     BiFunction<File, ReducedConfig, List<XmlComponent>> parseFunction) {
        this.attributeValue = attributeValue;
        this.parseFunction = parseFunction;
    }

    public static PatchFileVersion from(String value) {
        for (PatchFileVersion version : values()) {
            if (version.attributeValue.equals(value))
                return version;
        }

        throw new IllegalArgumentException("Unsupported patch file version: " + value);
    }

    public static PatchFileVersion getLatest() {
        return values()[0];
    }
}
