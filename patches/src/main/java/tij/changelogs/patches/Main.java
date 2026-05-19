package tij.changelogs.patches;

import tij.changelogs.patches.config.Config;
import tij.changelogs.patches.config.ConfigParser;

import java.nio.file.Path;

public class Main {
    static void main(String[] args) {
        Path configFilePath = CliParser.parse(args);
        Config c = ConfigParser.parse(configFilePath);
        System.out.println(c);
    }

}
