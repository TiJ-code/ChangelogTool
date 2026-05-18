package tij.changelogs.markdown;

import java.nio.file.Path;

public final class CliParser {
    private CliParser() {}

    public static Path parse(String[] args) {
        if (args == null || args.length != 1) {
            throw new RuntimeException("You have to configure a configuration file path");
        }
        return Path.of(args[0]);
    }
}
