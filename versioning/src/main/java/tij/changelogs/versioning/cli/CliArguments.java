package tij.changelogs.versioning.cli;

import java.nio.file.Path;

public record CliArguments(Path configFilePath, CliCommand cmd, StageType stage) {}
