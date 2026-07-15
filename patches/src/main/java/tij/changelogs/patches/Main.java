package tij.changelogs.patches;

import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigConstants;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.patches.builder.ChangelogBuilder;
import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.patches.parser.PatchParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    static void main(String[] args) {
        Path configFilePath = CliParser.parse(args);

        Config config = ConfigSystem.load(configFilePath);

        ReducedConfig reducedConfig = ReducedConfig.reduce(config);

        File dir = ConfigConstants.PATCHES_DIR;

        File[] filesArray = dir.listFiles((_, name) ->
                name.toLowerCase().endsWith(".xml")
        );


        if (filesArray == null) {
            throw new IllegalStateException(
                    "Patch directory does not exist: "
                            + dir.getAbsolutePath()
            );
        }

        List<File> files = Arrays.stream(filesArray)
                .map(File::getAbsoluteFile)
                .toList();

        var components = PatchParser.parsePatches(files.toArray(new File[0]), reducedConfig);

        ChangelogBuilder.build(config, components);

        files.forEach(f -> {
            try {
                Files.deleteIfExists(f.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}