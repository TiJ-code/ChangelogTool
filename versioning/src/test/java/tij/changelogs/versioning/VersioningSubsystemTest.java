package tij.changelogs.versioning;

import tij.changelogs.versioning.model.Version;

import org.junit.jupiter.api.Test;
import tij.changelogs.config.model.VersioningPhase;
import tij.changelogs.config.model.VersioningRule;
import tij.changelogs.versioning.format.ConfiguredVersionFormatter;
import tij.changelogs.versioning.operation.IncrementMinorOperation;
import tij.changelogs.versioning.resolver.VersionSourceResolver;
import tij.changelogs.versioning.service.VersionManager;
import tij.changelogs.versioning.source.RegexVersionSource;
import tij.changelogs.versioning.source.VersionSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VersioningSubsystemTest {
    @Test
    void formatterRecognizesConfiguredPhaseAndRendersIt() {
        var phases = List.of(
                phase("alpha", "alpha-{#numeric_version}{#snapshot}", "beta"),
                phase("release", "{#numeric_version}{#snapshot}", "alpha")
        );
        var formatter = new ConfiguredVersionFormatter(phases);

        Version parsed = formatter.parse("alpha-1.4.0-SNAPSHOT");

        assertEquals(new Version(1, 4, 0, "alpha", true), parsed);
        assertEquals("1.4.0", formatter.format(new Version(1, 4, 0, "release")));
    }

    @Test
    void regexSourceReplacesOnlyConfiguredCapture() throws Exception {
        Path file = Files.createTempFile("versioning", ".txt");
        Files.writeString(file, "version=1.2.3");
        var rule = new VersioningRule(".*versioning.*\\.txt", "version=([^\\n]+)");
        var source = new RegexVersionSource(rule, new ConfiguredVersionFormatter(List.of()));

        assertEquals(new Version(1, 2, 3, null), source.read(file.toFile()));
        assertEquals("version=2.0.0", source.replace(file.toFile(), new Version(2, 0, 0, null)));
    }

    @Test
    void managerRequiresAllSourcesToAgree() throws Exception {
        Path root = Files.createTempDirectory("versioning");
        Path first = root.resolve("first.txt");
        Path second = root.resolve("second.txt");
        Files.writeString(first, "version=1.2.3");
        Files.writeString(second, "version=1.2.4");
        var formatter = new ConfiguredVersionFormatter(List.of());
        var rule = new VersioningRule(".*\\.txt", "version=([^\\n]+)");
        List<VersionSource> sources = List.of(
                new RegexVersionSource(rule, formatter),
                new RegexVersionSource(rule, formatter)
        );
        var manager = new VersionManager(new VersionSourceResolver(root.toFile(), sources));

        IllegalStateException error = assertThrows(IllegalStateException.class, manager::resolveCurrentVersion);
        assertTrue(error.getMessage().contains("Version mismatch"));
    }

    @Test
    void managerPlansThenAppliesEveryLocation() throws Exception {
        Path root = Files.createTempDirectory("versioning");
        Path first = root.resolve("first.txt");
        Path second = root.resolve("second.txt");
        Files.writeString(first, "version=1.2.3");
        Files.writeString(second, "version=1.2.3");
        var formatter = new ConfiguredVersionFormatter(List.of());
        var rule = new VersioningRule(".*\\.txt", "version=([^\\n]+)");
        var source = new RegexVersionSource(rule, formatter);
        var manager = new VersionManager(new VersionSourceResolver(root.toFile(), List.of(source)));

        var current = manager.resolveCurrentVersion();
        var changes = manager.plan(current, new IncrementMinorOperation());

        assertEquals("version=1.3.0", changes.changes().getFirst().newContent());
        assertEquals("version=1.2.3", Files.readString(first));
        manager.apply(changes);
        assertEquals("version=1.3.0", Files.readString(first));
        assertEquals("version=1.3.0", Files.readString(second));
    }

    private static VersioningPhase phase(String name, String formatter, String next) {
        return new VersioningPhase(name, formatter, Optional.of(next), Optional.empty(), Optional.empty(), List.of());
    }
}
