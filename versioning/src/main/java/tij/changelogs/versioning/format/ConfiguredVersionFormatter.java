package tij.changelogs.versioning.format;

import tij.changelogs.config.model.VersioningPhase;
import tij.changelogs.versioning.model.Version;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfiguredVersionFormatter implements VersionFormatter {
    private final Map<String, VersionFormatter> formatters;

    public ConfiguredVersionFormatter(List<VersioningPhase> phases) {
        Map<String, VersionFormatter> result = new LinkedHashMap<>();
        for (VersioningPhase phase : phases) {
            if (phase.name() == null || phase.name().isBlank()) throw new IllegalArgumentException("Phase name must not be blank");
            if (result.containsKey(phase.name())) throw new IllegalArgumentException("Duplicate phase: " + phase.name());
            result.put(phase.name(), new TemplateVersionFormatter(
                    phase.formatString(), phase.name(), phase.prefix(), phase.suffix()));
        }
        this.formatters = Map.copyOf(result);
    }

    @Override
    public String format(Version version) {
        if (formatters.isEmpty()) return version.numericString();
        if (version.phase() == null) {
            if (formatters.size() != 1) throw new IllegalArgumentException("A phase is required when multiple phases are configured");
            return formatters.values().iterator().next().format(version);
        }
        VersionFormatter formatter = formatters.get(version.phase());
        if (formatter == null) throw new IllegalArgumentException("Unknown configured phase: " + version.phase());
        return formatter.format(version);
    }

    @Override
    public Version parse(String value) {
        if (formatters.isEmpty()) return parseGeneric(value);
        IllegalArgumentException last = null;
        for (Map.Entry<String, VersionFormatter> entry : formatters.entrySet()) {
            try { return entry.getValue().parse(value); }
            catch (IllegalArgumentException e) { last = e; }
        }
        throw new IllegalArgumentException("Version does not match any configured phase: " + value, last);
    }

    private static Version parseGeneric(String value) {
        var matcher = java.util.regex.Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z0-9][A-Za-z0-9.-]*))?$").matcher(value);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid numeric version: " + value);
        String suffix = matcher.group(4);
        return new Version(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                "SNAPSHOT".equals(suffix) ? null : suffix,
                "SNAPSHOT".equals(suffix));
    }
}
