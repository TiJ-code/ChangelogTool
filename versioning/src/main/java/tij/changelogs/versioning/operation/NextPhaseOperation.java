package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

import java.util.Map;

public final class NextPhaseOperation implements VersionOperation {
    private final Map<String, String> nextPhases;

    public NextPhaseOperation(Map<String, String> nextPhases) {
        this.nextPhases = Map.copyOf(nextPhases);
    }

    @Override
    public Version apply(Version current) {
        if (current.phase() == null) throw new IllegalStateException("Cannot advance a version without a phase");
        String next = nextPhases.get(current.phase());
        if (next == null || next.isBlank()) throw new IllegalStateException("Phase has no configured next phase: " + current.phase());
        return current.withPhase(next);
    }
}
