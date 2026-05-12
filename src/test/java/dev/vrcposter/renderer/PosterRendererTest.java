package dev.vrcposter.renderer;

import dev.vrcposter.model.PosterModel;
import dev.vrcposter.model.TimeslotEntry;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class PosterRendererTest {

    /** Creates a snapshot from an empty model — all paths null, no timeslots, blank text. */
    private static PosterSnapshot emptySnapshot() {
        // PosterModel uses SimpleStringProperty / SimpleObjectProperty — no JAT required for creation or reads
        return PosterSnapshot.of(new PosterModel());
    }

    @Test
    void render_fullResolution_returns1080x1920() {
        BufferedImage result = PosterRenderer.render(emptySnapshot());
        assertEquals(1080, result.getWidth());
        assertEquals(1920, result.getHeight());
    }

    @Test
    void render_halfResolution_returns540x960() {
        BufferedImage result = PosterRenderer.render(emptySnapshot(), 540, 960);
        assertEquals(540, result.getWidth());
        assertEquals(960, result.getHeight());
    }

    @Test
    void render_emptySnapshot_noException() {
        assertDoesNotThrow(() -> PosterRenderer.render(emptySnapshot()));
    }

    @Test
    void render_pureFunction_sameSnapshotSameDimensions() {
        PosterSnapshot snapshot = emptySnapshot();
        BufferedImage r1 = PosterRenderer.render(snapshot, 540, 960);
        BufferedImage r2 = PosterRenderer.render(snapshot, 540, 960);
        assertEquals(r1.getWidth(), r2.getWidth());
        assertEquals(r1.getHeight(), r2.getHeight());
    }

    @Test
    void render_returnsTypeIntArgb() {
        BufferedImage result = PosterRenderer.render(emptySnapshot());
        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType());
    }

    @Test
    void render_timeslots_preservesInsertionOrder_noException() {
        PosterModel model = new PosterModel();
        model.getTimeslots().add(TimeslotEntry.create("2am"));
        model.getTimeslots().add(TimeslotEntry.create("12am"));
        model.getTimeslots().add(TimeslotEntry.create("10pm"));
        model.getTimeslots().add(TimeslotEntry.create("9pm"));
        PosterSnapshot snapshot = PosterSnapshot.of(model);
        assertDoesNotThrow(() -> PosterRenderer.render(snapshot, 540, 960));
    }

    @Test
    void render_snapshotWithGenre_noException() {
        PosterModel model = new PosterModel();
        TimeslotEntry entry = TimeslotEntry.create("9pm");
        entry.setAssignedDjArtistName("DJ Test");
        entry.setGenre("Techno");
        model.getTimeslots().add(entry);
        PosterSnapshot snapshot = PosterSnapshot.of(model);
        assertNotNull(PosterRenderer.render(snapshot, 540, 960));
    }
}
