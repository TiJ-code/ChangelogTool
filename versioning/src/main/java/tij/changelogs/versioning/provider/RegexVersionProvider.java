package tij.changelogs.versioning.provider;

import tij.changelogs.config.model.VersioningRule;
import tij.changelogs.versioning.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;

public class RegexVersionProvider implements IVersionProvider {

    private final VersioningRule rule;

    public RegexVersionProvider(VersioningRule rule) {
        this.rule = rule;
    }


    @Override
    public boolean supports(File file) {
        String path = file.getPath().replace("\\", "/");

        return rule.filenameRegex().matcher(path).matches();
    }


    @Override
    public Version read(File file) {
        try {
            String content = Files.readString(file.toPath());

            Matcher matcher = rule.versionRegex().matcher(content);

            if (!matcher.find()) {
                throw new RuntimeException(
                        "No version found in " + file
                );
            }

            return Version.fromString(
                    matcher.group(1)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + file, e);
        }
    }


    @Override
    public void write(File file, Version version) {
        try {
            String content =
                    Files.readString(file.toPath());

            Matcher matcher = rule.versionRegex().matcher(content);

            if (!matcher.find()) {
                throw new RuntimeException("No version found in " + file);
            }

            StringBuilder result = new StringBuilder();

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(
                            matcher.group(0)
                                    .substring(
                                            0,
                                            matcher.start(1) - matcher.start()
                                    )
                                    + version
                                    + matcher.group(0)
                                    .substring(
                                            matcher.end(1) - matcher.start()
                                    )
                    )
            );

            matcher.appendTail(result);

            Files.writeString(file.toPath(), result);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + file, e);
        }
    }
}