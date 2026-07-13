package tij.changelogs.config.parser.versions;

import org.w3c.dom.Document;
import tij.changelogs.config.Config;
import tij.changelogs.config.model.VersioningConfig;
import tij.changelogs.config.parser.VersioningParser;

public final class ConfigParserV2 {
    private ConfigParserV2() {}

    public static Config parse(Document doc) {
        Config c = ConfigParserV1.parse(doc);

        VersioningConfig vcfg = VersioningParser.parse(doc.getDocumentElement());

        return new Config(vcfg, c.categories(), c.components(), c.topics(), c.breakingLevels());
    }
}
