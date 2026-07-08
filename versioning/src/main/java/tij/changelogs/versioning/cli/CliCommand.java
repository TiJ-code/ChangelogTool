package tij.changelogs.versioning.cli;

import java.util.StringJoiner;

public enum CliCommand {
    RELEASE("--release", 0),
    STAGE("--stage", 1),
    SUFFIX("--suffix", 0),
    STRING("--string", 0);

    private final String argument;
    private final int followingArguments;

    CliCommand(String argument, int followingArguments) {
        this.argument = argument;
        this.followingArguments = followingArguments;
    }

    public String getArgument() {
        return argument;
    }

    public int getFollowingArguments() {
        return followingArguments;
    }

    @Override
    public String toString() {
        if (followingArguments == 0)
            return argument;

        StringJoiner s = new StringJoiner(" ");
        s.add(argument);
        for (int i = 0; i < followingArguments; i++)
            s.add("{%d}".formatted(i));
        return s.toString();
    }
}
