package tij.changelogs.versioning.operation;

import tij.changelogs.versioning.model.Version;
import tij.changelogs.config.model.VersioningEvent;
import tij.changelogs.config.model.VersioningEventType;
import tij.changelogs.config.model.VersioningPhase;

import java.util.Map;

public final class NextPhaseOperation implements VersionOperation {
    private final Map<String, VersioningPhase> phases;

    public NextPhaseOperation(Map<String, VersioningPhase> phases) {
        this.phases = Map.copyOf(phases);
    }

    @Override
    public Version apply(Version current) {
        if (current.phase() == null) throw new IllegalStateException("Cannot advance a version without a phase");
        VersioningPhase currentPhase = phases.get(current.phase());
        if (currentPhase == null || currentPhase.nextPhaseName().isEmpty()) {
            throw new IllegalStateException("Phase has no configured next phase: " + current.phase());
        }
        String next = currentPhase.nextPhaseName().get();
        VersioningPhase target = phases.get(next);
        if (target == null) throw new IllegalStateException("Unknown configured next phase: " + next);

        Version nextVersion = current.withPhase(next).withSnapshot(true);
        for (VersioningEvent event : target.events()) {
            if (event.type() != VersioningEventType.PHASE_ENTERED) continue;
            nextVersion = new Version(
                    event.major().orElse(nextVersion.major()),
                    event.minor().orElse(nextVersion.minor()),
                    event.patch().orElse(nextVersion.patch()),
                    next,
                    true
            );
        }
        return nextVersion;
    }
}
