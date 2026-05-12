package dev.vrcposter;

import dev.vrcposter.model.DJRecord;
import dev.vrcposter.model.TimeslotEntry;
import dev.vrcposter.repository.DatabaseManager;
import dev.vrcposter.repository.RepositoryException;
import dev.vrcposter.repository.migration.Migration001_InitialSchema;
import dev.vrcposter.repository.migration.MigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerTimeslotTest {

    @TempDir
    Path tempDir;

    private ApplicationController controller;
    private DatabaseManager db;

    @BeforeEach
    void setUp() throws RepositoryException {
        db = new DatabaseManager(tempDir.resolve("test.db"));
        db.initialize();
        new MigrationRunner(db, List.of(new Migration001_InitialSchema())).run();
        controller = new ApplicationController(db);
    }

    @AfterEach
    void tearDown() throws RepositoryException {
        controller.shutdown();
        db.close();
    }

    @Test
    void initDefaultTimeslots_creates6Entries() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.initDefaultTimeslots();
        assertEquals(6, timeslots.size());
    }

    @Test
    void initDefaultTimeslots_entriesHaveCorrectStartTimes() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.initDefaultTimeslots();
        var times = timeslots.stream().map(TimeslotEntry::getStartTime).toList();
        assertEquals(List.of("9pm", "10pm", "11pm", "12am", "1am", "2am"), times);
    }

    @Test
    void addTimeslot_appendsEntryWithEmptyTime() {
        int before = controller.getPosterModel().getTimeslots().size();
        controller.addTimeslot();
        var timeslots = controller.getPosterModel().getTimeslots();
        assertEquals(before + 1, timeslots.size());
        assertEquals("", timeslots.get(timeslots.size() - 1).getStartTime());
    }

    @Test
    void removeTimeslot_removesCorrectEntry() {
        var timeslots = controller.getPosterModel().getTimeslots();
        int before = timeslots.size();
        TimeslotEntry toRemove = timeslots.get(0);
        controller.removeTimeslot(toRemove);
        assertEquals(before - 1, timeslots.size());
        assertFalse(timeslots.contains(toRemove));
    }

    @Test
    void assignDjToTimeslot_setsIdArtistNameAndNullLogoPath() {
        var entry = controller.getPosterModel().getTimeslots().get(0);
        var dj = new DJRecord("dj-1", "DJ Flux", null, null, null, null, null, null,
            System.currentTimeMillis(), System.currentTimeMillis());
        controller.assignDjToTimeslot(entry, dj);
        assertEquals("dj-1", entry.getAssignedDjId());
        assertEquals("DJ Flux", entry.getAssignedDjArtistName());
        assertNull(entry.getAssignedDjLogoPath());
    }

    @Test
    void assignDjToTimeslot_withLogoFilename_setsNonNullLogoPath() {
        var entry = controller.getPosterModel().getTimeslots().get(0);
        var dj = new DJRecord("dj-2", "DJ Nova", null, null, null, null, null, "nova.png",
            System.currentTimeMillis(), System.currentTimeMillis());
        controller.assignDjToTimeslot(entry, dj);
        assertNotNull(entry.getAssignedDjLogoPath());
        assertTrue(entry.getAssignedDjLogoPath().toString().endsWith("nova.png"));
    }

    @Test
    void clearTimeslotAssignment_clearsAllAssignmentFields() {
        var entry = controller.getPosterModel().getTimeslots().get(0);
        var dj = new DJRecord("dj-3", "DJ Prism", null, null, null, null, null, null,
            System.currentTimeMillis(), System.currentTimeMillis());
        controller.assignDjToTimeslot(entry, dj);
        controller.clearTimeslotAssignment(entry);
        assertNull(entry.getAssignedDjId());
        assertNull(entry.getAssignedDjArtistName());
        assertNull(entry.getAssignedDjLogoPath());
    }

    @Test
    void setTimeslotStartTime_updatesEntryTime() {
        var entry = controller.getPosterModel().getTimeslots().get(0);
        controller.setTimeslotStartTime(entry, "8pm");
        assertEquals("8pm", entry.getStartTime());
    }

    @Test
    void setTimeslotStartTime_doesNotSortList() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.addTimeslot();
        controller.addTimeslot();
        var first  = timeslots.get(0);
        var second = timeslots.get(1);
        controller.setTimeslotStartTime(first,  "11pm");
        controller.setTimeslotStartTime(second, "9pm");
        // list order must be preserved — no auto-sort
        assertEquals("11pm", timeslots.get(0).getStartTime());
        assertEquals("9pm",  timeslots.get(1).getStartTime());
    }

    @Test
    void setTimeslotGenre_setsGenreOnEntry() {
        var entry = controller.getPosterModel().getTimeslots().get(0);
        controller.setTimeslotGenre(entry, "Techno");
        assertEquals("Techno", entry.getGenre());
    }

    @Test
    void setTimeslotGenre_null_setsEmptyString() {
        var entry = controller.getPosterModel().getTimeslots().get(0);
        controller.setTimeslotGenre(entry, null);
        assertEquals("", entry.getGenre());
    }

    @Test
    void swapTimeslotDjContent_swapsDjFieldsAndGenreButNotStartTime() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.addTimeslot();
        controller.addTimeslot();
        var a = timeslots.get(0);
        var b = timeslots.get(1);
        controller.setTimeslotStartTime(a, "9pm");
        controller.setTimeslotStartTime(b, "10pm");
        a.setAssignedDjId("dj-a");
        a.setAssignedDjArtistName("DJ Alpha");
        controller.setTimeslotGenre(a, "Techno");
        b.setAssignedDjId("dj-b");
        b.setAssignedDjArtistName("DJ Beta");
        controller.setTimeslotGenre(b, "House");

        controller.swapTimeslotDjContent(0, 1);

        // start times must be unchanged
        assertEquals("9pm",  a.getStartTime());
        assertEquals("10pm", b.getStartTime());
        // DJ content and genre must be swapped
        assertEquals("dj-b",    a.getAssignedDjId());
        assertEquals("DJ Beta", a.getAssignedDjArtistName());
        assertEquals("House",   a.getGenre());
        assertEquals("dj-a",    b.getAssignedDjId());
        assertEquals("DJ Alpha",b.getAssignedDjArtistName());
        assertEquals("Techno",  b.getGenre());
    }

    @Test
    void swapTimeslotDjContent_adjacentRows_workCorrectly() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.addTimeslot();
        controller.addTimeslot();
        var a = timeslots.get(0);
        var b = timeslots.get(1);
        a.setAssignedDjId("dj-1");
        b.setAssignedDjId("dj-2");
        controller.setTimeslotGenre(a, "DNB");
        controller.setTimeslotGenre(b, "Ambient");

        controller.swapTimeslotDjContent(0, 1);

        assertEquals("dj-2", a.getAssignedDjId());
        assertEquals("Ambient", a.getGenre());
        assertEquals("dj-1", b.getAssignedDjId());
        assertEquals("DNB", b.getGenre());
    }
}
