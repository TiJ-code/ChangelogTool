package tij.changelogs.versioning.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class CliParser {

    private CliParser() {}


    public static CliArguments parse(String[] args) {
        if (args.length < 2) {
            printUsage();
            return null;
        }

        Path configFilePath = Path.of(args[0]);

        CliCommand command = Arrays.stream(CliCommand.values())
                .filter(c -> c.getArgument().equals(args[1]))
                .findFirst()
                .orElse(null);

        if (command == null) {
            System.err.println("Unknown command: " + args[1]);
            printUsage();
            return null;
        }

        int expectedArgs = 2 + command.getFollowingArguments();

        if (args.length != expectedArgs) {
            System.err.printf("Command '%s' expects %d parameter(s).%n",
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
                            configFilePath,
                            command,
                            StageType.valueOf(
                                    args[2].toUpperCase()
                            )
                    );
                } catch (IllegalArgumentException ex) {
                    System.err.println("Invalid stage type. Expected: " + Arrays.toString(StageType.values()));
                    yield null;
                }
            }

            default -> new CliArguments(configFilePath, command, null);
        };
    }


    private static void printUsage() {
        System.err.println("Usage: versioning <config> <command>");

        Arrays.stream(CliCommand.values())
                .forEach(cmd -> System.err.println("  " + cmd));
    }
}