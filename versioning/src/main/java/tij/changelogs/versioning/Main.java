package tij.changelogs.versioning;

import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.config.model.VersioningPhase;
import tij.changelogs.config.model.VersioningConfig;
import tij.changelogs.config.model.VersioningRule;
import tij.changelogs.versioning.cli.CliArguments;
import tij.changelogs.versioning.cli.CliCommand;
import tij.changelogs.versioning.cli.CliParser;
import tij.changelogs.versioning.change.VersionChangeSet;
import tij.changelogs.versioning.format.ConfiguredVersionFormatter;
import tij.changelogs.versioning.operation.IncrementMajorOperation;
import tij.changelogs.versioning.operation.IncrementMinorOperation;
import tij.changelogs.versioning.operation.IncrementPatchOperation;
import tij.changelogs.versioning.operation.NextPhaseOperation;
import tij.changelogs.versioning.operation.SetPhaseOperation;
import tij.changelogs.versioning.operation.VersionOperation;
import tij.changelogs.versioning.resolver.VersionSourceResolver;
import tij.changelogs.versioning.service.VersionManager;
import tij.changelogs.versioning.source.RegexVersionSource;
import tij.changelogs.versioning.source.VersionSource;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class Main {
    private Main() {}

    static void main(String[] args) {
        CliArguments arguments = CliParser.parse(args);
        if (arguments == null) { System.exit(1); return; }

        Config config = ConfigSystem.load(arguments.configFilePath());
        List<VersioningPhase> phases = config.versioningConfig().versionPhases();
        var formatter = new ConfiguredVersionFormatter(phases);
        List<VersionSource> sources = config.versioningConfig().versionRules().stream()
                .map(rule -> new RegexVersionSource(rule, formatter))
                .map(source -> (VersionSource) source)
                .toList();

        VersionManager manager = new VersionManager(new VersionSourceResolver(new File("."), sources));
        var current = manager.resolveCurrentVersion();

        if (arguments.command() == CliCommand.SHOW) {
            System.out.println(formatter.format(current.version()));
            return;
        }

        VersionOperation operation = operation(arguments, config.versioningConfig());
        VersionChangeSet changes = manager.plan(current, operation);
        manager.apply(changes);
    }

    private static VersionOperation operation(CliArguments arguments, VersioningConfig versioning) {
        return switch (arguments.command()) {
            case RELEASE -> current -> current.withSnapshot(false);
            case INCREMENT -> {
                VersionOperation increment = switch (arguments.value().toLowerCase(Locale.ROOT)) {
                    case "major" -> new IncrementMajorOperation();
                    case "minor" -> new IncrementMinorOperation();
                    case "patch" -> new IncrementPatchOperation();
                    default -> throw new IllegalArgumentException("Unknown increment: " + arguments.value());
                };
                yield current -> increment.apply(current).withPhase(configuredInitialPhase(versioning, current.phase()));
            }
            case NEXT_PHASE -> {
                HashMap<String, VersioningPhase> phases = new HashMap<>();
                for (VersioningPhase phase : versioning.versionPhases()) phases.put(phase.name(), phase);
                yield new NextPhaseOperation(phases);
            }
            case PHASE -> {
                if (versioning.versionPhases().stream().noneMatch(phase -> phase.name().equals(arguments.value()))) {
                    throw new IllegalArgumentException("Unknown configured phase: " + arguments.value());
                }
                yield new SetPhaseOperation(arguments.value());
            }
            case SHOW -> throw new IllegalStateException("SHOW does not have an operation");
        };
    }

    private static String configuredInitialPhase(VersioningConfig versioning, String currentPhase) {
        return versioning.initialPhaseName().orElse(currentPhase);
    }
}
