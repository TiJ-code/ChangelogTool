package tij.changelogs.versioning;

import tij.changelogs.versioning.provider.IVersionProvider;

import java.io.File;
import java.util.List;

public record VersionManager(List<IVersionProvider> providers, List<File> files) {
    public Version readCurrentVersion() {
        for (IVersionProvider provider : providers) {
            for (File file : files) {
                if (provider.supports(file)) {
                    return provider.read(file);
                }
            }
        }

        throw new RuntimeException("Could not find any version source.");
    }

    public void writeVersion(Version version) {
        for (IVersionProvider provider : providers) {
            for (File file : files) {
                if (provider.supports(file)) {
                    provider.write(file, version);
                }
            }
        }
    }
}
