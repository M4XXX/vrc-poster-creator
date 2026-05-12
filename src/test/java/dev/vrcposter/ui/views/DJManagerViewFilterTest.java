package dev.vrcposter.ui.views;

import dev.vrcposter.model.DJRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DJManagerViewFilterTest {

    private static DJRecord record(String name) {
        return DJRecord.create(name, null, null, null, null, null);
    }

    @Test
    void defaultPredicateShowsAllRecords() {
        ObservableList<DJRecord> source = FXCollections.observableArrayList(
                record("DJ Alpha"), record("DJ Beta"));
        var filtered = new FilteredList<>(source, p -> true);
        assertEquals(2, filtered.size());
    }

    @Test
    void searchPredicateFiltersByArtistNameCaseInsensitive() {
        ObservableList<DJRecord> source = FXCollections.observableArrayList(
                record("DJ Alpha"), record("DJ Beta"), record("MC Gamma"));
        var filtered = new FilteredList<>(source, p -> true);

        String text = "dj";
        filtered.setPredicate(dj -> dj.getArtistName().toLowerCase().contains(text.toLowerCase()));

        assertEquals(2, filtered.size());
    }

    @Test
    void clearingSearchRestoresFullList() {
        ObservableList<DJRecord> source = FXCollections.observableArrayList(
                record("DJ Alpha"), record("DJ Beta"));
        var filtered = new FilteredList<>(source, p -> true);

        filtered.setPredicate(dj -> dj.getArtistName().toLowerCase().contains("alpha"));
        assertEquals(1, filtered.size());

        filtered.setPredicate(p -> true);
        assertEquals(2, filtered.size());
    }

    @Test
    void noMatchSearchReturnsEmptyList() {
        ObservableList<DJRecord> source = FXCollections.observableArrayList(
                record("DJ Alpha"), record("DJ Beta"));
        var filtered = new FilteredList<>(source, p -> true);

        String text = "zzznomatch";
        filtered.setPredicate(dj -> dj.getArtistName().toLowerCase().contains(text.toLowerCase()));

        assertTrue(filtered.isEmpty());
    }

    @Test
    void partialMatchReturnsOnlyMatchingRecords() {
        ObservableList<DJRecord> source = FXCollections.observableArrayList(
                record("DJ Alpha"), record("DJ Beta"), record("MC Gamma"));
        var filtered = new FilteredList<>(source, p -> true);

        String text = "Beta";
        filtered.setPredicate(dj -> dj.getArtistName().toLowerCase().contains(text.toLowerCase()));

        assertEquals(1, filtered.size());
        assertEquals("DJ Beta", filtered.get(0).getArtistName());
    }
}
