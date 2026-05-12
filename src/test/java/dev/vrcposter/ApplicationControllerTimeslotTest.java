package dev.vrcposter;

import dev.vrcposter.model.DJRecord;
import dev.vrcposter.model.TimeslotEntry;
import dev.vrcposter.repository.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for timeslot management methods added to ApplicationController in Story 3.3.
 * Uses an uninitialized DatabaseManager — safe because no database operations are triggered.
 */
class ApplicationControllerTimeslotTest {

    private ApplicationController controller;

    @BeforeEach
    void setUp() {
        controller = new ApplicationController(new DatabaseManager(Path.of("unused.db")));
    }

    @AfterEach
    void tearDown() {
        controller.shutdown();
    }

    @Test
    void initDefaultTimeslots_creates6Entries() {
        assertEquals(6, controller.getPosterModel().getTimeslots().size());
    }

    @Test
    void initDefaultTimeslots_entriesHaveCorrectStartTimes() {
        var times = controller.getPosterModel().getTimeslots()
            .stream().map(TimeslotEntry::getStartTime).toList();
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
        TimeslotEntry toRemove = timeslots.get(0);
        controller.removeTimeslot(toRemove);
        assertEquals(5, timeslots.size());
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
    void setTimeslotStartTime_sortsListByTimeOrder() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.addTimeslot();
        controller.addTimeslot();
        var first  = timeslots.get(0);
        var second = timeslots.get(1);
        controller.setTimeslotStartTime(first,  "11pm");
        controller.setTimeslotStartTime(second, "9pm");
        // 9pm (order 21) < 11pm (order 23) — 9pm must sort first
        assertEquals("9pm",  timeslots.get(0).getStartTime());
        assertEquals("11pm", timeslots.get(1).getStartTime());
    }

    @Test
    void setTimeslotStartTime_midnight_sortsBeforeAfternoon() {
        var timeslots = controller.getPosterModel().getTimeslots();
        timeslots.clear();
        controller.addTimeslot();
        controller.addTimeslot();
        var first  = timeslots.get(0);
        var second = timeslots.get(1);
        controller.setTimeslotStartTime(first,  "9pm");
        controller.setTimeslotStartTime(second, "12am");
        // 12am (order 0) < 9pm (order 21)
        assertEquals("12am", timeslots.get(0).getStartTime());
        assertEquals("9pm",  timeslots.get(1).getStartTime());
    }
}
