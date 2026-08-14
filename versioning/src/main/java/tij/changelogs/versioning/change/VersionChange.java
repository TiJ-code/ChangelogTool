package tij.changelogs.versioning.change;

import java.io.File;

public record VersionChange(File file, String oldContent, String newContent) {
}
