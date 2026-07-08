package tij.changelogs.versioning;

import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.config.model.VersioningRule;
import tij.changelogs.versioning.cli.CliArguments;
import tij.changelogs.versioning.cli.CliCommand;
import tij.changelogs.versioning.cli.CliParser;
import tij.changelogs.versioning.provider.IVersionProvider;
import tij.changelogs.versioning.provider.RegexVersionProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static void main(String[] args) {
        CliArguments arguments = CliParser.parse(args);
        if (arguments == null) {
            System.exit(1);
        }

        if (arguments.configFilePath() == null) {
            System.exit(1);
        }

        Config config = ConfigSystem.load(arguments.configFilePath());

        List<IVersionProvider> providers = new ArrayList<>();

        for (VersioningRule rule : config.versioningConfig().versionRules()) {
            providers.add(new RegexVersionProvider(rule));
        }

        List<File> files = FileFinder.findPomFiles();

        VersionManager versionManager = new VersionManager(providers, files);

        Version current = versionManager.readCurrentVersion();

        if (CliCommand.STRING.equals(arguments.cmd())) {
            System.out.println(current.displayString());
            System.exit(0);
        }

        Version nextVer = switch (arguments.cmd()) {
            case RELEASE -> current.release();
            case STAGE -> current.stage(arguments.stage());
            case SUFFIX -> current.snapshot();
            default -> throw new IllegalStateException("Unexpected value: " + arguments.cmd());
        };

        versionManager.writeVersion(nextVer);
    }
}
