# Changelog Tool

Changelog Tool is a modular Java toolchain for collecting XML changelog patches, building cumulative changelogs, rendering Markdown, versioning a project, and archiving released changelogs.

The project is configuration-driven. Project-specific details such as version files, version formats, phases, components, categories, topics, and breaking-change levels belong in the XML configuration rather than in Java code.

## Versioning policy

The project follows the [Semantic Versioning 2.0.0](https://semver.org/) standard for stable releases, starting with `1.0.0`.

For versions at or above `1.0.0`:

- A major version indicates incompatible or breaking changes.
- A minor version adds backward-compatible functionality.
- A patch version contains backward-compatible fixes.

Before `1.0.0`, the public API and behavior are not considered stable. Breaking changes may occur in minor increments, and compatibility with earlier pre-`1.0.0` versions is not guaranteed. Reaching `1.0.0` establishes the first stable compatibility baseline.

## Requirements

- Java 25
- Maven
- GraalVM for building the native command-line tools

The repository is configured for Java 25 in the root `pom.xml`. The release workflow uses GraalVM Java 25.

## Project structure

| Module | Purpose |
| --- | --- |
| `config-parser` | Loads and validates versioned XML configuration files. |
| `xml-model` | Shared XML changelog model: patches, topics, components, categories, breaking changes, and entries. |
| `versioning-model` | Shared immutable `Version` value used by versioning-aware modules. |
| `patches` | Parses patch files and combines them into `changelogs/cumulated/cumulated.xml`. |
| `markdown` | Converts the cumulative XML changelog into Markdown and publishes `changelogs/CHANGELOG.md`. |
| `versioning` | Resolves configured version sources, applies generic version operations, and writes coordinated changes. |
| `archive` | Moves the generated XML and Markdown changelogs into version- and phase-aware archive directories. |


## Configuration

The tools receive one configuration file path. The repository's main example is:

```text
config-parser/src/main/resources/config_changelog_tool.xml
```

A configuration contains these main sections:

```xml
<config version="4">
    <versioning>...</versioning>
    <categories>...</categories>
    <components>...</components>
    <topics>...</topics>
    <breaking.changes>...</breaking.changes>
</config>
```

### Version sources

Version sources identify files and the text to replace. The rules are generic; there is no Maven-specific Java implementation.

```xml
<versioning>
    <rule name="maven">
        <filename regex=".*/?pom\.xml"/>
        <version regex="&lt;version&gt;([^&lt;]+)&lt;/version&gt;"/>
    </rule>
</versioning>
```

The filename regex decides which files are sources. The version regex must contain one capture group containing the version value. Multiple matching sources must contain the same version.

### Phases and formatters

Phases define how versions are represented and how phase transitions work:

```xml
<phases initial="alpha">
    <phase name="alpha">
        <next>beta</next>
        <formatter>alpha-{#numeric_version}{#snapshot}</formatter>
    </phase>
    <phase name="beta">
        <next>release</next>
        <formatter>beta-{#numeric_version}{#snapshot}</formatter>
    </phase>
    <phase name="release">
        <next>alpha</next>
        <formatter>{#numeric_version}{#snapshot}</formatter>
    </phase>
</phases>
```

The `initial` phase is the development channel entered after a numeric increment. `SNAPSHOT` is separate: it is the unreleased state within that channel, represented by `{#snapshot}`. The repository's own configuration uses only a `release` channel; projects can define additional channels such as `prealpha`, `alpha`, `beta`, and `ea` in their own configuration.

For example, the configuration above produces this lifecycle:

```text
alpha-0.1.0-SNAPSHOT
    --release
alpha-0.1.0
    --increment patch
alpha-0.1.1-SNAPSHOT
    --release
alpha-0.1.1
    --next-phase
beta-1.0.0-SNAPSHOT
```

The numeric operation and snapshot state are separate internally. Numeric increments always create a snapshot, while `--release` clears the snapshot state without changing the numeric version. A phase-entry event can reset the numeric version when promoting from one development channel to another.

The phase definitions are still ordinary configuration:

```xml
<phases initial="alpha">
    <phase name="alpha">
        <next>release</next>
        <formatter>{#numeric_version}-alpha</formatter>
    </phase>
    <phase name="release">
        <next>alpha</next>
        <formatter>{#numeric_version}</formatter>
    </phase>
</phases>
```

Supported formatter variables include:

```text
{#major}
{#minor}
{#patch}
{#numeric_version}
{#phase}
{#prefix}
{#suffix}
{#snapshot}
```

Phase names are configuration data. The Java implementation does not contain special cases for names such as `alpha`, `release`, or `SNAPSHOT`.

#### Development channels and snapshot state

A phase represents a development channel. Snapshot state is separate and means that the current version is still being developed. This allows a project to develop many numeric versions within one channel:

```text
alpha-1.1.0-SNAPSHOT
    --release
alpha-1.1.0
    --increment patch
alpha-1.1.1-SNAPSHOT
    --release
alpha-1.1.1
    --increment minor
alpha-1.2.0-SNAPSHOT
```

The corresponding project configuration can be:

```xml
<phases initial="alpha">
    <phase name="prealpha">
        <next>alpha</next>
        <formatter>prealpha-{#numeric_version}{#snapshot}</formatter>
    </phase>

    <phase name="alpha">
        <next>beta</next>
        <formatter>alpha-{#numeric_version}{#snapshot}</formatter>
    </phase>

    <phase name="beta">
        <next>ea</next>
        <formatter>beta-{#numeric_version}{#snapshot}</formatter>
        <events>
            <event on="phase_entered">
                <major>1</major>
                <minor>0</minor>
                <patch>0</patch>
            </event>
        </events>
    </phase>

    <phase name="ea">
        <next>release</next>
        <formatter>ea-{#numeric_version}{#snapshot}</formatter>
    </phase>

    <phase name="release">
        <next>prealpha</next>
        <formatter>{#numeric_version}{#snapshot}</formatter>
    </phase>
</phases>
```

The promotion workflow is then:

```text
alpha-1.28.2
    --next-phase
beta-1.0.0-SNAPSHOT
    --release
beta-1.0.0
    --increment patch
beta-1.0.1-SNAPSHOT
```

The `<event on="phase_entered">` values reset the numeric version when entering a new channel. Without such an event, the numeric version is preserved during the phase transition.

The repository's own `config_changelog_tool.xml` intentionally uses only a generic `release` phase. Projects using this tool can define their own development channels in their own configuration file.

### Components and topics

Components define the chapters of a changelog:

```xml
<components>
    <component id="dungeon" name="Dungeon"/>
    <component id="player" name="Player"/>
    <component id="enemy" name="Enemy"/>
</components>
```

Topics define which components appear in a generated view:

```xml
<topics>
    <topic name="Gameplay">
        <component-ref ref="dungeon"/>
        <component-ref ref="player"/>
        <component-ref ref="enemy"/>
    </topic>

    <topic name="Technical">
        <component-ref ref="dungeon"/>
        <component-ref ref="player"/>
        <component-ref ref="enemy"/>
    </topic>
</topics>
```

At present, topics are generated as views over the cumulative component data. The current patch format does not yet attach different descriptions for the same component to different topics; topic-specific patch content requires extending the patch schema and XML model with topic blocks.

## Patch format

Create patch files under:

```text
changelogs/patches/
```

The current patch format is version 2:

```xml
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE patch SYSTEM "patch.v2.dtd">
<patch version="2">
    <component ref="dungeon">
        <category name="refactorings">
            <entry>Dungeon generation was redesigned.</entry>
        </category>
    </component>
</patch>
```

Available component references and category names must exist in the configuration. Breaking changes are represented inside a category:

```xml
<breaking severity="major">
    <entry>The public dungeon-generation API changed.</entry>
</breaking>
```

The patch template is available at [patch.template.xml](patches/src/main/resources/patch.template.xml).

## Tools

Maven builds four native tools:

```text
ChangelogPatchParser.tool
ChangelogMarkdownBuilder.tool
ChangelogVersioning.tool
ChangelogArchiver.tool
```

The tools are also available as module applications during development.

### 1. Versioning tool

The versioning tool reads all configured version sources, verifies that they agree, calculates a new immutable version, creates a complete change set, and only then applies the file changes.

Usage:

```bash
./ChangelogVersioning.tool <config> --show
./ChangelogVersioning.tool <config> --release
./ChangelogVersioning.tool <config> --increment major
./ChangelogVersioning.tool <config> --increment minor
./ChangelogVersioning.tool <config> --increment patch
./ChangelogVersioning.tool <config> --phase <phase-name>
./ChangelogVersioning.tool <config> --next-phase
```

Examples:

```bash
CONFIG=config-parser/src/main/resources/config_changelog_tool.xml

./ChangelogVersioning.tool "$CONFIG" --show
./ChangelogVersioning.tool "$CONFIG" --release
./ChangelogVersioning.tool "$CONFIG" --increment patch
./ChangelogVersioning.tool "$CONFIG" --next-phase
./ChangelogVersioning.tool "$CONFIG" --phase alpha
```

`--release` clears the snapshot state without changing the numeric version. `--increment` changes the numeric version, marks it as a snapshot, and enters the configured initial phase. `--phase` sets a configured development channel explicitly. `--next-phase` follows the current channel's configured `<next>` value and applies its phase-entry events.

The tool fails before writing anything if no source is found, a source cannot be parsed, or matching sources contain different versions.

### 2. Patch parser and cumulator

The patch tool reads all XML files in `changelogs/patches`, validates their component/category references, combines their contents, and writes:

```text
changelogs/cumulated/cumulated.xml
```

Usage:

```bash
./ChangelogPatchParser.tool <config>
```

Example:

```bash
./ChangelogPatchParser.tool config_changelog_tool.xml
```

After successful cumulation, the current implementation removes the processed patch files from `changelogs/patches`. Commit or back up patches before running this command if they need to be retained independently.

### 3. Markdown builder

The Markdown tool reads the cumulative XML file, creates a Markdown sibling, and copies it to the repository changelog:

```text
changelogs/cumulated/<version>_cumulated.xml
changelogs/cumulated/<version>_cumulated.md
changelogs/CHANGELOG.md
```

The version is currently read from the root `pom.xml`. Usage:

```bash
./ChangelogMarkdownBuilder.tool <config>
```

Example:

```bash
./ChangelogMarkdownBuilder.tool config-parser/src/main/resources/config_changelog_tool.xml
```

Topics with content are rendered as sections. A topic named `default` is rendered without a topic heading; other topics are rendered as headings.

### 4. Archive tool

The archive tool requires the configuration because it uses the configured formatter to determine the version and phase from the generated filename.

Usage:

```bash
./ChangelogArchiver.tool <config>
```

For one or zero configured phases, files are archived directly by version:

```text
changelogs/archive/
    0.4.0/
        0.4.0_cumulated.xml
        0.4.0_cumulated.md
```

For multiple configured phases, the phase becomes the first directory:

```text
changelogs/archive/
    alpha/
        1.7.0/
            1.7.0-alpha_cumulated.xml
            1.7.0-alpha_cumulated.md
    release/
        1.1.0/
            1.1.0_cumulated.xml
            1.1.0_cumulated.md
```

The archiver requires exactly one XML and one Markdown file in `changelogs/cumulated`. It refuses to archive a multi-phase version if its phase cannot be identified.

## Complete local workflow

From a clean project checkout, the normal workflow is:

```bash
CONFIG=config-parser/src/main/resources/config_changelog_tool.xml

# 1. Add one or more files to changelogs/patches/

# 2. Update version files
./ChangelogVersioning.tool "$CONFIG" --increment patch

# 3. Cumulate and remove processed patch files
./ChangelogPatchParser.tool "$CONFIG"

# 4. Render and publish the Markdown changelog
./ChangelogMarkdownBuilder.tool "$CONFIG"

# 5. Move XML and Markdown into the archive
./ChangelogArchiver.tool "$CONFIG"
```

To release the current snapshot without changing its numeric version:

```bash
./ChangelogVersioning.tool "$CONFIG" --release
```

For a custom phase transition, use:

```bash
./ChangelogVersioning.tool "$CONFIG" --next-phase
```

## Building

Build all modules and native tools with:

```bash
mvn clean package
```

The native executables are produced under the individual module `target` directories. The release workflow copies them to the repository root before publishing.

Run tests with:

```bash
mvn test
```

## Release workflow

The GitHub Actions workflow is [`.github/workflows/release.yml`](.github/workflows/release.yml). It:

1. Checks out `main`.
2. Installs GraalVM Java 25.
3. Builds the JARs and native tools.
4. Copies and makes the tools executable.
5. Updates the project version.
6. Generates and renders the changelog.
7. Archives the generated changelog.
8. Commits the release changes and creates a `releases/<version>` tag.
9. Publishes a GitHub release using `changelogs/CHANGELOG.md` as its body.

The workflow uses the same lifecycle commands documented above. The configuration path must point to the repository configuration file:

```text
config-parser/src/main/resources/config_changelog_tool.xml
```

The current versioning CLI uses `--show`, `--release`, `--increment`, `--phase`, and `--next-phase`. 

The release workflow should invoke each tool with that configuration path.

## Prepare-next-phase workflow

The GitHub Actions workflow [`.github/workflows/commit.yml`](.github/workflows/commit.yml) prepares the next development version manually. It accepts `patch`, `minor`, or `major`, downloads the latest versioning tool, and runs the equivalent of:

```bash
CONFIG=config-parser/src/main/resources/config_changelog_tool.xml
./ChangelogVersioning.tool "$CONFIG" --increment patch
```

The increment operation already marks the new version as a snapshot, so no separate suffix or snapshot command is needed. For the repository's single `release` channel, the workflow produces:

```text
1.0.0
    --increment patch
1.0.1-SNAPSHOT
```

## Development notes

- Keep project-specific version behavior in `config.v4.xsd`-validated configuration.
- Keep shared version values in `versioning-model` rather than coupling other modules to the versioning CLI.
- Add new version source formats through configuration where possible.
- Treat `changelogs/cumulated` as a working directory and `changelogs/archive` as the historical output.
- Patch files are intentionally short-lived in the current cumulation workflow because successful processing deletes them.
