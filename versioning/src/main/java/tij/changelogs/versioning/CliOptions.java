package tij.changelogs.versioning;

public enum CliOptions {
    MAJOR("major"),
    MINOR("minor"),
    PATCH("patch"),
    SUFFIX("do_suffix");

    public final String argument;

    CliOptions(String arg) {
        this.argument = arg;
    }

    public static CliOptions from(String text) {
        for (CliOptions option : values()) {
            if (option.argument.equalsIgnoreCase(text))
                return option;
        }
        return null;
    }
}
