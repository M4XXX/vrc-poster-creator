package dev.vrcposter.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DJRosterTest {

    private DJRecord record(String name) {
        return DJRecord.create(name, null, null, null, null, null);
    }

    @Test
    void initialListIsEmpty() {
        var roster = new DJRoster();
        assertTrue(roster.getObservableList().isEmpty());
    }

    @Test
    void getObservableListIsNeverNull() {
        var roster = new DJRoster();
        assertNotNull(roster.getObservableList());
    }

    @Test
    void setAllPopulatesObservableList() {
        var roster = new DJRoster();
        roster.setAll(List.of(record("DJ Alpha"), record("DJ Beta")));
        assertEquals(2, roster.getObservableList().size());
    }

    @Test
    void setAllReplacesPreviousContent() {
        var roster = new DJRoster();
        roster.setAll(List.of(record("DJ Alpha"), record("DJ Beta")));
        roster.setAll(List.of(record("DJ Gamma")));
        assertEquals(1, roster.getObservableList().size());
        assertEquals("DJ Gamma", roster.getObservableList().get(0).getArtistName());
    }

    @Test
    void setAllWithEmptyListClearsList() {
        var roster = new DJRoster();
        roster.setAll(List.of(record("DJ Alpha")));
        roster.setAll(List.of());
        assertTrue(roster.getObservableList().isEmpty());
    }
}
