package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;

public record SetPhaseOperation(String phase) implements IVersionOperation {
    public SetPhaseOperation {
        if (phase == null || phase.isBlank()) throw new IllegalArgumentException("Phase must not be blank");
    }
    @Override public Version apply(Version current) { return current.withPhase(phase); }
}
