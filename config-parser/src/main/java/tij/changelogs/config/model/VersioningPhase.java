package tij.changelogs.config.model;

import java.util.List;
import java.util.Optional;

public record VersioningPhase(String name, String formatString,
                              Optional<String> nextPhaseName,
                              Optional<String> prefix, Optional<String> suffix,
                              List<VersioningEvent> events) {
}
