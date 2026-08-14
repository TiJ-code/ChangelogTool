package tij.changelogs.versioning.cli;

import java.nio.file.Path;
import java.util.Arrays;

public final class CliParser {
    private CliParser() {}

    public static CliArguments parse(String[] args) {
        if (args.length < 2) return usage("Usage: versioning <config> <command> [value]");
        Path config = Path.of(args[0]);
        CliCommand command = Arrays.stream(CliCommand.values())
                .filter(candidate -> candidate.getArgument().equals(args[1]))
                .findFirst().orElse(null);
        if (command == null) return usage("Unknown command: " + args[1]);

        int expected = 2 + command.getFollowingArguments();
        if (args.length != expected) return usage(command.getArgument() + " expects " + command.getFollowingArguments() + " parameter(s)");
        return new CliArguments(config, command, command.getFollowingArguments() == 1 ? args[2] : null);
    }

    private static CliArguments usage(String message) {
        System.err.println(message);
        System.err.println("Commands: --show, --increment <major|minor|patch>, --next-phase, --phase <name>");
        return null;
    }
}
