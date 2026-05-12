package dev.vrcposter.renderer;

import dev.vrcposter.model.PosterModel;
import dev.vrcposter.model.TimeslotEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PosterSnapshotTest {

    @Test
    void of_capturesAllScalarFieldsFromModel() {
        PosterModel model = new PosterModel();
        model.setTitle("VRC Night");
        model.setSubtitle("Ft. DJ Cool");
        model.setDate("2026-05-01");
        model.setBackgroundPath(Path.of("/bg.png"));
        model.setClubLogoPath(Path.of("/logo.png"));
        model.setQrCodePath(Path.of("/qr.png"));

        PosterSnapshot snap = PosterSnapshot.of(model);

        assertEquals("VRC Night", snap.title);
        assertEquals("Ft. DJ Cool", snap.subtitle);
        assertEquals("2026-05-01", snap.date);
        assertEquals(Path.of("/bg.png"), snap.backgroundPath);
        assertEquals(Path.of("/logo.png"), snap.clubLogoPath);
        assertEquals(Path.of("/qr.png"), snap.qrCodePath);
    }

    @Test
    void of_capturesTimeslotsAsImmutableList() {
        PosterModel model = new PosterModel();
        TimeslotEntry entry = TimeslotEntry.create("9pm");
        entry.setAssignedDjId("dj-1");
        entry.setAssignedDjArtistName("DJ One");
        entry.setAssignedDjLogoPath(Path.of("/logos/dj1.png"));
        model.getTimeslots().add(entry);

        PosterSnapshot snap = PosterSnapshot.of(model);

        assertEquals(1, snap.timeslots.size());
        PosterSnapshot.TimeslotSnapshot ts = snap.timeslots.get(0);
        assertEquals("9pm", ts.startTime());
        assertEquals("dj-1", ts.assignedDjId());
        assertEquals("DJ One", ts.assignedDjArtistName());
        assertEquals(Path.of("/logos/dj1.png"), ts.assignedDjLogoPath());
    }

    @Test
    void of_subsequentModelMutationDoesNotChangeSnapshot() {
        PosterModel model = new PosterModel();
        model.setTitle("Original Title");
        PosterSnapshot snap = PosterSnapshot.of(model);

        model.setTitle("Changed Title");

        assertEquals("Original Title", snap.title);
    }

    @Test
    void of_timeslotsListIsUnmodifiable() {
        PosterModel model = new PosterModel();
        model.getTimeslots().add(TimeslotEntry.create("9pm"));
        PosterSnapshot snap = PosterSnapshot.of(model);

        assertThrows(UnsupportedOperationException.class,
            () -> snap.timeslots.add(new PosterSnapshot.TimeslotSnapshot("10pm", null, null, null, null)));
    }

    @Test
    void of_emptyModelProducesDefaultValues() {
        PosterModel model = new PosterModel();
        PosterSnapshot snap = PosterSnapshot.of(model);

        assertEquals("", snap.title);
        assertEquals("", snap.subtitle);
        assertEquals("", snap.date);
        assertNull(snap.backgroundPath);
        assertNull(snap.clubLogoPath);
        assertNull(snap.qrCodePath);
        assertTrue(snap.timeslots.isEmpty());
    }

    @Test
    void of_timeslotWithNullAssignmentsPreservesNulls() {
        PosterModel model = new PosterModel();
        model.getTimeslots().add(TimeslotEntry.create("2am"));
        PosterSnapshot snap = PosterSnapshot.of(model);

        PosterSnapshot.TimeslotSnapshot ts = snap.timeslots.get(0);
        assertNull(ts.assignedDjId());
        assertNull(ts.assignedDjArtistName());
        assertNull(ts.assignedDjLogoPath());
    }

    @Test
    void of_multipleTimeslotsSnapshotted() {
        PosterModel model = new PosterModel();
        model.getTimeslots().add(TimeslotEntry.create("9pm"));
        model.getTimeslots().add(TimeslotEntry.create("10pm"));
        model.getTimeslots().add(TimeslotEntry.create("11pm"));

        PosterSnapshot snap = PosterSnapshot.of(model);

        assertEquals(3, snap.timeslots.size());
        assertEquals("9pm", snap.timeslots.get(0).startTime());
        assertEquals("10pm", snap.timeslots.get(1).startTime());
        assertEquals("11pm", snap.timeslots.get(2).startTime());
    }
}
