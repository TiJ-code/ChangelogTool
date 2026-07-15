package tij.changelogs.patches.parser;

import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.patches.parser.versions.PatchParserV1;
import tij.changelogs.patches.parser.versions.PatchParserV2;
import tij.changelogs.xmlModel.XmlComponent;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

public enum PatchFileVersion {
    v2("1", "patch.v2.dtd", PatchParserV2::parse),
    v1("1", "patch.v1.dtd", PatchParserV1::parse);

    public final String attributeValue;
    public final String dtdFile;
    public final BiFunction<File, ReducedConfig, List<XmlComponent>> parseFunction;

    PatchFileVersion(String attributeValue, String dtdFile,
                     BiFunction<File, ReducedConfig, List<XmlComponent>> parseFunction) {
        this.attributeValue = attributeValue;
        this.dtdFile = dtdFile;
        this.parseFunction = parseFunction;
    }

    public static PatchFileVersion from(String value) {
        for (PatchFileVersion version : values()) {
            if (version.attributeValue.equals(value))
                return version;
        }

        return getLatest();
    }

    public static PatchFileVersion getLatest() {
        return values()[0];
    }
}
