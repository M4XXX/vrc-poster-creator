package dev.vrcposter.model;

import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PosterModelTest {

    @Test
    void defaultValues_stringsAreEmptyAndPathsAreNull() {
        PosterModel model = new PosterModel();

        assertEquals("", model.getTitle());
        assertEquals("", model.getSubtitle());
        assertEquals("", model.getDate());
        assertNull(model.getBackgroundPath());
        assertNull(model.getClubLogoPath());
        assertNull(model.getQrCodePath());
        assertTrue(model.getTimeslots().isEmpty());
    }

    @Test
    void propertyTriads_getSetProperty_roundTrip() {
        PosterModel model = new PosterModel();

        model.setTitle("VRC Night");
        assertEquals("VRC Night", model.getTitle());
        assertEquals("VRC Night", model.titleProperty().get());

        model.setSubtitle("Ft. DJ Cool");
        assertEquals("Ft. DJ Cool", model.getSubtitle());
        assertEquals("Ft. DJ Cool", model.subtitleProperty().get());

        model.setDate("2026-05-01");
        assertEquals("2026-05-01", model.getDate());
        assertEquals("2026-05-01", model.dateProperty().get());

        Path bg = Path.of("/images/bg.png");
        model.setBackgroundPath(bg);
        assertEquals(bg, model.getBackgroundPath());
        assertEquals(bg, model.backgroundPathProperty().get());

        Path logo = Path.of("/images/logo.png");
        model.setClubLogoPath(logo);
        assertEquals(logo, model.getClubLogoPath());
        assertEquals(logo, model.clubLogoPathProperty().get());

        Path qr = Path.of("/images/qr.png");
        model.setQrCodePath(qr);
        assertEquals(qr, model.getQrCodePath());
        assertEquals(qr, model.qrCodePathProperty().get());
    }

    @Test
    void getTimeslots_returnsSameListInstance() {
        PosterModel model = new PosterModel();
        assertSame(model.getTimeslots(), model.getTimeslots());
    }

    @Test
    void timeslotsObservableList_firesOnAdd() {
        PosterModel model = new PosterModel();
        AtomicInteger changeCount = new AtomicInteger(0);
        model.getTimeslots().addListener((ListChangeListener<TimeslotEntry>) c -> changeCount.incrementAndGet());

        model.getTimeslots().add(TimeslotEntry.create("9pm"));

        assertEquals(1, changeCount.get());
    }

    @Test
    void timeslotsObservableList_firesOnElementPropertyChange_extractor() {
        // Without extractor, this would NOT fire a list change event.
        // The extractor makes the list track element property changes.
        PosterModel model = new PosterModel();
        TimeslotEntry entry = TimeslotEntry.create("10pm");
        model.getTimeslots().add(entry);

        AtomicInteger changeCount = new AtomicInteger(0);
        model.getTimeslots().addListener((ListChangeListener<TimeslotEntry>) c -> changeCount.incrementAndGet());

        entry.setAssignedDjId("dj-xyz");

        assertEquals(1, changeCount.get());
    }

    @Test
    void timeslotsObservableList_firesOnRemove() {
        PosterModel model = new PosterModel();
        TimeslotEntry entry = TimeslotEntry.create("11pm");
        model.getTimeslots().add(entry);

        AtomicInteger changeCount = new AtomicInteger(0);
        model.getTimeslots().addListener((ListChangeListener<TimeslotEntry>) c -> changeCount.incrementAndGet());

        model.getTimeslots().remove(entry);

        assertEquals(1, changeCount.get());
    }
}
