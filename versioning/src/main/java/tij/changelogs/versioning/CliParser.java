package tij.changelogs.versioning;

import java.util.Arrays;

public final class CliParser {
    private CliParser() {}

    public static CliOptions parse(String[] args) {
        if (args.length < 1) {
            System.err.println("Version bump type has to be supplied!");
            System.err.printf("Possibilities are: %s%n", Arrays.stream(CliOptions.values()).map(op -> op.argument).toList());
            return null;
        }

        return CliOptions.from(args[0]);
    }
}
