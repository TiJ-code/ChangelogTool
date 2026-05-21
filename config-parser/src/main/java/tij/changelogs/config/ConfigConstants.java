package tij.changelogs.config;

import java.io.File;

public final class ConfigConstants {
    public static final File CHANGELOGS_DIR = new File("changelogs");
    public static final File PATCHES_DIR    = new File(CHANGELOGS_DIR, "patches");
    public static final File CUMULATED_DIR  = new File(CHANGELOGS_DIR, "cumulated");
    public static final File ARCHIVE_DIR    = new File(CHANGELOGS_DIR, "archive");
}
