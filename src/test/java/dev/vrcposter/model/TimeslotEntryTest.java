package dev.vrcposter.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TimeslotEntryTest {

    @Test
    void create_setsStartTimeAndNullAssignments() {
        TimeslotEntry entry = TimeslotEntry.create("9pm");

        assertEquals("9pm", entry.getStartTime());
        assertNull(entry.getAssignedDjId());
        assertNull(entry.getAssignedDjArtistName());
        assertNull(entry.getAssignedDjLogoPath());
    }

    @Test
    void create_returnsNonNull() {
        assertNotNull(TimeslotEntry.create("10pm"));
    }

    @Test
    void create_nullStartTime_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> TimeslotEntry.create(null));
    }

    @Test
    void propertyTriads_getSetProperty_roundTrip() {
        TimeslotEntry entry = TimeslotEntry.create("11pm");

        entry.setStartTime("midnight");
        assertEquals("midnight", entry.getStartTime());
        assertEquals("midnight", entry.startTimeProperty().get());

        entry.setAssignedDjId("dj-123");
        assertEquals("dj-123", entry.getAssignedDjId());
        assertEquals("dj-123", entry.assignedDjIdProperty().get());

        entry.setAssignedDjArtistName("DJ Cool");
        assertEquals("DJ Cool", entry.getAssignedDjArtistName());
        assertEquals("DJ Cool", entry.assignedDjArtistNameProperty().get());

        Path logo = Path.of("/logos/test.png");
        entry.setAssignedDjLogoPath(logo);
        assertEquals(logo, entry.getAssignedDjLogoPath());
        assertEquals(logo, entry.assignedDjLogoPathProperty().get());
    }

    @Test
    void clearAssignment_nullsAllThreeAssignmentFields() {
        TimeslotEntry entry = TimeslotEntry.create("1am");
        entry.setAssignedDjId("dj-abc");
        entry.setAssignedDjArtistName("DJ Test");
        entry.setAssignedDjLogoPath(Path.of("/logos/dj.png"));

        entry.clearAssignment();

        assertNull(entry.getAssignedDjId());
        assertNull(entry.getAssignedDjArtistName());
        assertNull(entry.getAssignedDjLogoPath());
    }

    @Test
    void clearAssignment_doesNotClearStartTime() {
        TimeslotEntry entry = TimeslotEntry.create("2am");
        entry.setAssignedDjId("some-id");
        entry.clearAssignment();

        assertEquals("2am", entry.getStartTime());
    }

    @Test
    void propertyAccessors_returnSamePropertyInstance() {
        TimeslotEntry entry = TimeslotEntry.create("9pm");
        assertSame(entry.startTimeProperty(), entry.startTimeProperty());
        assertSame(entry.assignedDjIdProperty(), entry.assignedDjIdProperty());
        assertSame(entry.assignedDjArtistNameProperty(), entry.assignedDjArtistNameProperty());
        assertSame(entry.assignedDjLogoPathProperty(), entry.assignedDjLogoPathProperty());
    }

    @Test
    void genre_defaultIsEmptyString() {
        TimeslotEntry entry = TimeslotEntry.create("9pm");
        assertEquals("", entry.getGenre());
    }

    @Test
    void genre_setGetRoundTrip() {
        TimeslotEntry entry = TimeslotEntry.create("10pm");
        entry.setGenre("Techno");
        assertEquals("Techno", entry.getGenre());
        assertEquals("Techno", entry.genreProperty().get());
    }

    @Test
    void clearAssignment_doesNotClearGenre() {
        TimeslotEntry entry = TimeslotEntry.create("11pm");
        entry.setAssignedDjId("dj-1");
        entry.setGenre("House");
        entry.clearAssignment();
        assertNull(entry.getAssignedDjId());
        assertEquals("House", entry.getGenre());
    }
}
