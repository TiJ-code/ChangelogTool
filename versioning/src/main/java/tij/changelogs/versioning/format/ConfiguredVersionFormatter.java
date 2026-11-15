package tij.changelogs.versioning.format;

import tij.changelogs.config.model.VersioningPhase;
import tij.changelogs.versioning.model.Version;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ConfiguredVersionFormatter implements IVersionFormatter {
    private final Map<String, IVersionFormatter> formatters;

    public ConfiguredVersionFormatter(List<VersioningPhase> phases) {
        Map<String, IVersionFormatter> result = new LinkedHashMap<>();
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
        if (formatters.isEmpty()) return formatGeneric(version);
        if (version.phase() == null) {
            if (formatters.size() != 1) throw new IllegalArgumentException("A phase is required when multiple phases are configured");
            return formatters.values().iterator().next().format(version);
        }
        IVersionFormatter formatter = formatters.get(version.phase());
        if (formatter == null) throw new IllegalArgumentException("Unknown configured phase: " + version.phase());
        return formatter.format(version);
    }

    @Override
    public Version parse(String value) {
        if (formatters.isEmpty()) return parseGeneric(value);
        IllegalArgumentException last = null;
        for (Map.Entry<String, IVersionFormatter> entry : formatters.entrySet()) {
            try { return entry.getValue().parse(value); }
            catch (IllegalArgumentException e) { last = e; }
        }
        throw new IllegalArgumentException("Version does not match any configured phase: " + value, last);
    }

    private static Version parseGeneric(String value) {
        var matcher = Pattern.compile(
                "^(?:([A-Za-z0-9][A-Za-z0-9.-]*)-)?(\\d+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z0-9][A-Za-z0-9.-]*))?$"
        ).matcher(value);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid numeric version: " + value);
        String prefix = matcher.group(1);
        String suffix = matcher.group(5);
        return new Version(
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                prefix == null ? ("SNAPSHOT".equals(suffix) ? null : suffix) : prefix,
                "SNAPSHOT".equals(suffix));
    }

    private static String formatGeneric(Version version) {
        String value = version.numericString();
        if (version.phase() != null) value = version.phase() + "-" + value;
        return version.snapshot() ? value + "-SNAPSHOT" : value;
    }
}
