package tij.changelogs.config.model;

import java.util.OptionalInt;

public record VersioningEvent(VersioningEventType type, OptionalInt major, OptionalInt minor, OptionalInt patch) {
}
