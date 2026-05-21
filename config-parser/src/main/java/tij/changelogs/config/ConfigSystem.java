package tij.changelogs.config;

import tij.changelogs.config.parser.ConfigParser;

import java.nio.file.Path;

public final class ConfigSystem {
    private ConfigSystem() {}

    public static Config load(Path configPath) {
        return ConfigParser.parse(configPath);
    }
}
