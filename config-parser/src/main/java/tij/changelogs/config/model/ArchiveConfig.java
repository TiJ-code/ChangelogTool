package tij.changelogs.config.model;

public record ArchiveConfig(ArchiveHierarchy hierarchy) {
    public static final ArchiveConfig DEFAULT = new ArchiveConfig(ArchiveHierarchy.NONE);

    public ArchiveConfig {
        if (hierarchy == null) {
            throw new IllegalArgumentException("Archive hierarchy must not be null");
        }
    }
}
