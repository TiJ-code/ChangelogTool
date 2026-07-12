package tij.changelogs.versioning;

import tij.changelogs.versioning.cli.StageType;

import java.util.Optional;

public record Version(int major, int minor, int patch, Optional<String> optionalSuffix) {
    public static Version fromString(String verTxt) {
        String[] sufSplit = verTxt.split("-");
        if (sufSplit.length > 2) {
            throw new RuntimeException("Only one suffix is allowed!");
        }

        Optional<String> suffix;
        if (sufSplit.length == 2)
            suffix = Optional.of(sufSplit[1]);
        else
            suffix = Optional.empty();


        String[] verSplit = sufSplit[0].split("\\.");
        if (verSplit.length != 3) {
            throw new RuntimeException("Invalid version format. Requires major.minor.patch?");
        }

        int major = Integer.parseInt(verSplit[0]);
        int minor = Integer.parseInt(verSplit[1]);
        int patch = Integer.parseInt(verSplit[2]);

        return new Version(major, minor, patch, suffix);
    }

    public Version release() {
        if (optionalSuffix.isEmpty()) {
            throw new IllegalStateException("Cannot release a version without suffix.");
        }

        return new Version(major, minor, patch, Optional.empty());
    }

    public Version snapshot() {
        if (optionalSuffix.isPresent()) {
            throw new IllegalStateException("Already a snapshot.");
        }

        return new Version(major, minor, patch, Optional.of("SNAPSHOT"));
    }

    public Version stage(StageType type) {
        if(optionalSuffix.isPresent()) {
            throw new IllegalStateException("Can only do staging for versions without suffix.");
        }

        return switch (type) {
            case PATCH -> new Version(major, minor, patch + 1, Optional.empty());
            case MINOR -> new Version(major, minor + 1, 0, Optional.empty());
            case MAJOR -> new Version(major + 1, 0, 0, Optional.empty());
        };
    }

    public String displayString() {
        if(optionalSuffix.isPresent()) {
            throw new IllegalStateException("Version string only available for released versions.");
        }

        return "v%d.%d.%d".formatted(major, minor, patch);
    }

    @Override
    public String toString() {
        if (optionalSuffix().isPresent())
            return "%d.%d.%d-%s".formatted(major, minor, patch, optionalSuffix.get());
        return "%d.%d.%d".formatted(major, minor, patch);
    }
}
