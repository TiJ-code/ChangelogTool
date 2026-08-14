package tij.changelogs.versioning.format;

import tij.changelogs.versioning.model.Version;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/** Formatter for the placeholders documented by the configuration schema. */
public final class TemplateVersionFormatter implements VersionFormatter {
    private static final String NUMERIC = "{#numeric_version}";
    private static final String MAJOR = "{#major}";
    private static final String MINOR = "{#minor}";
    private static final String PATCH = "{#patch}";
    private static final String PHASE = "{#phase}";
    private static final String PREFIX = "{#prefix}";
    private static final String SUFFIX = "{#suffix}";

    private final String template;
    private final String phase;
    private final String prefix;
    private final String suffix;
    private final Pattern parser;

    public TemplateVersionFormatter(String template, String phase) {
        this(template, phase, Optional.empty(), Optional.empty());
    }

    public TemplateVersionFormatter(String template, String phase, Optional<String> prefix, Optional<String> suffix) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("A phase formatter cannot be empty");
        }
        this.template = template;
        this.phase = phase;
        this.prefix = prefix.orElse("");
        this.suffix = suffix.orElse("");
        this.parser = Pattern.compile(toRegex(template));
    }

    @Override
    public String format(Version version) {
        String result = template
                .replace(NUMERIC, version.numericString())
                .replace(MAJOR, Integer.toString(version.major()))
                .replace(MINOR, Integer.toString(version.minor()))
                .replace(PATCH, Integer.toString(version.patch()))
                .replace(PHASE, Objects.toString(version.phase(), ""))
                .replace(PREFIX, prefix)
                .replace(SUFFIX, suffix);
        if (result.contains("{#")) {
            throw new IllegalArgumentException("Unknown version formatter placeholder in '" + template + "'");
        }
        return result;
    }

    @Override
    public Version parse(String value) {
        var matcher = parser.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Version '" + value + "' does not match phase '" + phase + "'");
        }
        return new Version(
                Integer.parseInt(matcher.group("major")),
                Integer.parseInt(matcher.group("minor")),
                Integer.parseInt(matcher.group("patch")),
                phase
        );
    }

    private String toRegex(String value) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < value.length();) {
            String placeholder = placeholderAt(value, i);
            if (placeholder != null) {
                switch (placeholder) {
                    case NUMERIC -> regex.append("(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)");
                    case MAJOR -> regex.append("(?<major>\\d+)");
                    case MINOR -> regex.append("(?<minor>\\d+)");
                    case PATCH -> regex.append("(?<patch>\\d+)");
                    case PHASE -> regex.append(Pattern.quote(valuePhasePlaceholder()));
                    case PREFIX -> regex.append(Pattern.quote(prefix));
                    case SUFFIX -> regex.append(Pattern.quote(suffix));
                    default -> throw new IllegalArgumentException("Unknown version formatter placeholder: " + placeholder);
                }
                i += placeholder.length();
            } else {
                regex.append(Pattern.quote(String.valueOf(value.charAt(i++))));
            }
        }
        return regex.append("$").toString();
    }

    private static String placeholderAt(String value, int offset) {
        for (String candidate : new String[]{NUMERIC, MAJOR, MINOR, PATCH, PHASE, PREFIX, SUFFIX}) {
            if (value.startsWith(candidate, offset)) return candidate;
        }
        return null;
    }

    /* The phase placeholder is resolved by the instance-specific parser below. */
    private String valuePhasePlaceholder() { return phase == null ? "" : phase; }
}
