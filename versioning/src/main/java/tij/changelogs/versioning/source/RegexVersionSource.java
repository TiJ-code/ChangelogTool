package tij.changelogs.versioning.source;

import tij.changelogs.config.model.VersioningRule;
import tij.changelogs.versioning.model.Version;
import tij.changelogs.versioning.format.VersionFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;

public final class RegexVersionSource implements VersionSource {
    private final VersioningRule rule;
    private final VersionFormatter formatter;

    public RegexVersionSource(VersioningRule rule, VersionFormatter formatter) {
        this.rule = rule;
        this.formatter = formatter;
    }

    @Override
    public boolean supports(File file) {
        return rule.filenameRegex().matcher(file.getPath().replace('\\', '/')).matches();
    }

    @Override
    public Version read(File file) {
        String content = readContent(file);
        Matcher matcher = rule.versionRegex().matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("No configured version found in " + file);
        }
        return formatter.parse(matcher.group(1));
    }

    @Override
    public String replace(File file, Version version) {
        String content = readContent(file);
        Matcher matcher = rule.versionRegex().matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("No configured version found in " + file);
        }

        return content.substring(0, matcher.start(1))
                + formatter.format(version)
                + content.substring(matcher.end(1));
    }

    private static String readContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
    }
}
