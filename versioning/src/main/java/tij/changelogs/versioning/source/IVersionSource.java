package tij.changelogs.versioning.source;

import tij.changelogs.versioning.model.Version;

import java.io.File;

public interface IVersionSource {
    boolean supports(File file);
    Version read(File file);
    String replace(File file, Version version);
}
