package tij.changelogs.versioning.cli;

public enum CliCommand {
    SHOW("--show", 0),
    RELEASE("--release", 0),
    INCREMENT("--increment", 1),
    NEXT_PHASE("--next-phase", 0),
    PHASE("--phase", 1);

    private final String argument;
    private final int followingArguments;

    CliCommand(String argument, int followingArguments) {
        this.argument = argument;
        this.followingArguments = followingArguments;
    }

    public String getArgument() { return argument; }
    public int getFollowingArguments() { return followingArguments; }
}
