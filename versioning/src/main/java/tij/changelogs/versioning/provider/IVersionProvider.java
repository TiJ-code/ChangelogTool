package tij.changelogs.versioning.provider;

import tij.changelogs.versioning.Version;

import java.io.File;

public interface IVersionProvider {
    boolean supports(File file);

    Version read(File file);

    void write(File file, Version version);
}
