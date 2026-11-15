package tij.changelogs.markdown;

import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigConstants;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.markdown.builder.MarkdownBuilder;
import tij.changelogs.markdown.parser.ChangelogParser;
import tij.changelogs.versioning.format.ConfiguredVersionFormatter;
import tij.changelogs.versioning.resolver.VersionSourceResolver;
import tij.changelogs.versioning.service.VersionManager;
import tij.changelogs.versioning.source.IVersionSource;
import tij.changelogs.versioning.source.RegexVersionSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    static void main(String[] args) {
        Path configFilePath = CliParser.parse(args);
        Config config = ConfigSystem.load(configFilePath);

        File dir = ConfigConstants.CUMULATED_DIR;

        File[] filesArray = dir.listFiles((_, name) ->
                name.toLowerCase().endsWith(".xml")
        );

        if (filesArray == null) {
            throw new IllegalStateException(
                    "Path direcotry does not exist: " + dir.getAbsolutePath()
            );
        }

        List<File> files = Arrays.stream(filesArray)
                .map(File::getAbsoluteFile)
                .toList();

        if (files.isEmpty()) {
            throw new IllegalStateException(
                    "No cumulated changelog xml exists"
            );
        }

        File cumulatedFile = files.getFirst();

        String version = resolveVersion(config, new File("."));

        cumulatedFile = renameFileToReleaseVersion(cumulatedFile, version);

        var list = ChangelogParser.parse(cumulatedFile, config);
        System.out.println(list);

        File markdownFile = ensureMarkdownSibling(cumulatedFile);

        try {
            Files.writeString(markdownFile.getAbsoluteFile().toPath(), MarkdownBuilder.build(list, config));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        publishChangelog(markdownFile);
    }

    private static void publishChangelog(File markdownFile) {
        try {
            Path source = markdownFile.toPath();

            Path parentDir = source.getParent();
            if (parentDir == null) {
                throw new IllegalStateException("Markdown file has no parent directory");
            }

            Path target = parentDir
                    .getParent()
                    .resolve("CHANGELOG.md");

            Files.copy(
                    source,
                    target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not publish CHANGELOG.md",
                    e
            );
        }
    }

    private static File ensureMarkdownSibling(File xmlFile) {
        String name = xmlFile.getName();

        if (!name.endsWith(".xml")) {
            throw new IllegalArgumentException("Not an xml file: " + name);
        }

        String base = name.substring(0, name.length() - 4);
        File mdFile = new File(xmlFile.getParentFile(), base + ".md");

        try {
            if (!mdFile.exists()) {
                Files.createFile(mdFile.toPath());
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not create markdown file: " + mdFile.getAbsolutePath(),
                    e
            );
        }

        return mdFile;
    }

    private static File renameFileToReleaseVersion(File file, String version) {
        if (file.getName().contains(version))
            return file;

        Path original = file.toPath();
        Path sibling = original.resolveSibling(version + "_" + file.getName());
        file.renameTo(sibling.toAbsolutePath().toFile());
        return sibling.toAbsolutePath().toFile();
    }

    static String resolveVersion(Config config, File root) {
        var formatter = new ConfiguredVersionFormatter(
                config.versioningConfig().versionPhases()
        );
        List<IVersionSource> sources = config.versioningConfig().versionRules().stream()
                .map(rule -> new RegexVersionSource(rule, formatter))
                .map(source -> (IVersionSource) source)
                .toList();
        var manager = new VersionManager(
                new VersionSourceResolver(root, sources)
        );
        return formatter.format(manager.resolveCurrentVersion().version());
    }
}
