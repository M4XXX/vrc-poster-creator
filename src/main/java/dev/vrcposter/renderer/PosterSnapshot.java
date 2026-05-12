package dev.vrcposter.renderer;

import dev.vrcposter.model.PosterModel;

import java.nio.file.Path;
import java.util.List;

/**
 * Immutable snapshot of PosterModel state for thread-safe handoff to the renderer.
 * Must be created on the JavaFX Application Thread via {@link #of(PosterModel)}.
 * All fields are public final — safe to read from any thread without synchronization.
 */
public class PosterSnapshot {

    public record TimeslotSnapshot(
        String startTime,
        String assignedDjId,
        String assignedDjArtistName,
        Path assignedDjLogoPath,
        String genre
    ) {}

    public final String title;
    public final String subtitle;
    public final String date;
    public final Path backgroundPath;
    public final Path clubLogoPath;
    public final Path qrCodePath;
    public final List<TimeslotSnapshot> timeslots;

    private PosterSnapshot(
        String title,
        String subtitle,
        String date,
        Path backgroundPath,
        Path clubLogoPath,
        Path qrCodePath,
        List<TimeslotSnapshot> timeslots
    ) {
        this.title = title;
        this.subtitle = subtitle;
        this.date = date;
        this.backgroundPath = backgroundPath;
        this.clubLogoPath = clubLogoPath;
        this.qrCodePath = qrCodePath;
        this.timeslots = timeslots;
    }

    /** Must be called on the JavaFX Application Thread. Captures all model state at this instant. */
    public static PosterSnapshot of(PosterModel model) {
        List<TimeslotSnapshot> slots = model.getTimeslots().stream()
            .map(te -> new TimeslotSnapshot(
                te.getStartTime(),
                te.getAssignedDjId(),
                te.getAssignedDjArtistName(),
                te.getAssignedDjLogoPath(),
                te.getGenre()))
            .toList();
        return new PosterSnapshot(
            model.getTitle(),
            model.getSubtitle(),
            model.getDate(),
            model.getBackgroundPath(),
            model.getClubLogoPath(),
            model.getQrCodePath(),
            slots
        );
    }
}
