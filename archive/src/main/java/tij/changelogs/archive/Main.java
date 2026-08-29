package tij.changelogs.archive;

import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigConstants;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.config.model.ArchiveConfig;
import tij.changelogs.config.model.VersioningPhase;
import tij.changelogs.versioning.format.ConfiguredVersionFormatter;
import tij.changelogs.versioning.model.Version;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class Main {
    private Main() {}

    static void main(String[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Usage: archiver <configuration-file>");
        }

        try {
            Config config = ConfigSystem.load(Path.of(args[0]));
            List<VersioningPhase> phases = config.versioningConfig().versionPhases();
            ConfiguredVersionFormatter formatter = new ConfiguredVersionFormatter(phases);

            File[] files = findCumulatedFiles();
            File xml = findSingle(files, ".xml");
            File md = findSingle(files, ".md");

            Version version = formatter.parse(versionPart(xml.getName()));
            Path outputDirectory = archiveDirectory(version, phases, config.archiveConfig());
            Files.createDirectories(outputDirectory);

            move(xml, outputDirectory);
            move(md, outputDirectory);
        } catch (Exception e) {
            throw new RuntimeException("Could not archive cumulated changelog", e);
        }
    }

    static Path archiveDirectory(Version version, List<VersioningPhase> phases) {
        return archiveDirectory(version, phases, ArchiveConfig.DEFAULT);
    }

    static Path archiveDirectory(
            Version version,
            List<VersioningPhase> phases,
            ArchiveConfig archiveConfig
    ) {
        Path directory = ConfigConstants.ARCHIVE_DIR.toPath();

        if (phases.size() > 1) {
            if (version.phase() == null) {
                throw new IllegalStateException("Cannot archive a version without a phase when multiple phases are configured");
            }
            directory = directory.resolve(safePathPart(version.phase()));
        }

        return switch (archiveConfig.hierarchy()) {
            case NONE -> directory.resolve(safePathPart(version.numericString()));
            case MAJOR -> directory
                    .resolve(safePathPart(Integer.toString(version.major())))
                    .resolve(safePathPart("%d.%d".formatted(version.minor(), version.patch())));
            case MAJOR_MINOR -> directory
                    .resolve(safePathPart(Integer.toString(version.major())))
                    .resolve(safePathPart(Integer.toString(version.minor())))
                    .resolve(safePathPart(Integer.toString(version.patch())));
        };
    }

    private static File[] findCumulatedFiles() {
        File[] files = ConfigConstants.CUMULATED_DIR.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            throw new IllegalStateException("No cumulated files in " + ConfigConstants.CUMULATED_DIR.getAbsolutePath());
        }
        return files;
    }

    private static File findSingle(File[] files, String extension) {
        List<File> matches = java.util.Arrays.stream(files)
                .filter(file -> file.getName().toLowerCase().endsWith(extension))
                .toList();
        if (matches.size() != 1) {
            throw new IllegalStateException("Expected exactly one " + extension + " cumulated file, found " + matches.size());
        }
        return matches.getFirst();
    }

    static String versionPart(String filename) {
        String base = strip(filename);
        return base.endsWith("_cumulated")
                ? base.substring(0, base.length() - "_cumulated".length())
                : base;
    }

    static String strip(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? filename : filename.substring(0, index);
    }

    private static String safePathPart(String value) {
        if (value == null || value.isBlank() || value.equals(".") || value.equals("..")
                || value.contains("/") || value.contains("\\")) {
            throw new IllegalArgumentException("Invalid archive path part: " + value);
        }
        return value;
    }

    private static void move(File source, Path targetDirectory) throws Exception {
        Files.move(source.toPath(), targetDirectory.resolve(source.getName()), StandardCopyOption.ATOMIC_MOVE);
    }
}
