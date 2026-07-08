package tij.changelogs.config.model;

import java.util.regex.Pattern;

public record VersioningRule(Pattern filenameRegex, Pattern versionRegex) {
    public VersioningRule(String filenameRegex, String versionRegex) {
        this(
                Pattern.compile(filenameRegex),
                Pattern.compile(versionRegex)
        );

        if (this.versionRegex.matcher("").groupCount() != 1) {
            throw new IllegalArgumentException(
                    "Version regex must contain exactly one capture group"
            );
        }
    }
}
