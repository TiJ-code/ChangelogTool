package tij.changelogs.patches;

import tij.changelogs.config.ConfigConstants;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.patches.builder.ChangelogBuilder;
import tij.changelogs.patches.config.ReducedConfig;
import tij.changelogs.patches.parser.PatchParser;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    static void main(String[] args) {
        Path configFilePath = CliParser.parse(args);
        ReducedConfig c = ReducedConfig.reduce(ConfigSystem.load(configFilePath));
        System.out.println(c);

        File dir = ConfigConstants.PATCHES_DIR;

        File[] filesArray = dir.listFiles((_, name) ->
                name.toLowerCase().endsWith(".xml")
        );

        if (filesArray == null) {
            throw new IllegalStateException(
                    "Patch directory does not exist: " + dir.getAbsolutePath()
            );
        }

        List<File> files = Arrays.stream(filesArray)
                .map(File::getAbsoluteFile)
                .toList();

        System.out.println(files);

        var list = PatchParser.parsePatches(files.toArray(new File[0]), c);
        System.out.println(list);

        ChangelogBuilder.build(list);
    }

}
