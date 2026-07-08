package tij.changelogs.versioning.cli;

import java.util.Arrays;

public final class CliParser {
    private CliParser() {}

    public static CliArguments parse(String[] args) {
        if (args.length == 0) {
            printUsage();
            return null;
        }

        CliCommand command = Arrays.stream(CliCommand.values())
                .filter(c -> c.getArgument().equals(args[0]))
                .findFirst()
                .orElse(null);

        if (command == null) {
            System.err.println("Unknown command: " + args[0]);
            printUsage();
            return null;
        }

        int expectedArgs = 1 + command.getFollowingArguments();

        if (args.length != expectedArgs) {
            System.err.printf(
                    "Command '%s' expects %d parameter(s).%n",
                    command.getArgument(),
                    command.getFollowingArguments()
            );
            System.err.println("Usage: " + command);
            return null;
        }

        return switch (command) {
            case STAGE -> {
                try {
                    yield new CliArguments(
                            command,
                            StageType.valueOf(args[1].toUpperCase())
                    );
                } catch (IllegalArgumentException ex) {
                    System.err.println(
                            "Invalid stage type. Expected one of: " +
                                    Arrays.toString(StageType.values())
                    );
                    yield null;
                }
            }

            default -> new CliArguments(command, null);
        };
    }

    private static void printUsage() {
        System.err.println("Usage:");
        Arrays.stream(CliCommand.values())
                .forEach(cmd -> System.err.println("  " + cmd));
    }
}