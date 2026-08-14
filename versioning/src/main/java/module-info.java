module changelogs.versioning {
    requires changelogs.configParser;
    requires changelogs.versioningModel;

    exports tij.changelogs.versioning;
    exports tij.changelogs.versioning.change;
    exports tij.changelogs.versioning.format;
    exports tij.changelogs.versioning.operation;
    exports tij.changelogs.versioning.resolver;
    exports tij.changelogs.versioning.service;
    exports tij.changelogs.versioning.source;
}
