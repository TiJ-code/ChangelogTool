package tij.changelogs.versioning.model;

public record Version(int major, int minor, int patch, String phase, boolean snapshot) {
    public Version(int major, int minor, int patch, String phase) {
        this(major, minor, patch, phase, false);
    }
    public Version {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version numbers must not be negative");
        }
        phase = phase == null || phase.isBlank() ? null : phase;
    }

    public String numericString() {
        return "%d.%d.%d".formatted(major, minor, patch);
    }

    public Version incrementMajor() { return new Version(major + 1, 0, 0, phase, true); }
    public Version incrementMinor() { return new Version(major, minor + 1, 0, phase, true); }
    public Version incrementPatch() { return new Version(major, minor, patch + 1, phase, true); }
    public Version withPhase(String newPhase) { return new Version(major, minor, patch, newPhase, snapshot); }
    public Version withSnapshot(boolean value) { return new Version(major, minor, patch, phase, value); }

    @Override
    public String toString() {
        String result = phase == null ? numericString() : phase + "-" + numericString();
        return snapshot ? result + "-SNAPSHOT" : result;
    }
}
