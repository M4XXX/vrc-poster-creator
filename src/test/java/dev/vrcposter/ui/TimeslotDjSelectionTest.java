package dev.vrcposter.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates TimeslotRow ComboBox reliability and PosterBuilderView timeslot ordering fixes.
 * File-based assertions only — no JavaFX runtime required.
 */
class TimeslotDjSelectionTest {

    private static final Path ROW = Paths.get(
            "src/main/java/dev/vrcposter/ui/components/TimeslotRow.java");

    private static final Path VIEW = Paths.get(
            "src/main/java/dev/vrcposter/ui/views/PosterBuilderView.java");

    @Test
    void comboBox_isNotEditable() throws IOException {
        assertFalse(Files.readString(ROW).contains("setEditable(true)"),
                "ComboBox must not be editable — user cannot type free text");
    }

    @Test
    void comboBox_doesNotUseFilteredList() throws IOException {
        assertFalse(Files.readString(ROW).contains("FilteredList"),
                "FilteredList predicate changes caused IndexOutOfBoundsException during popup close");
    }

    @Test
    void comboBox_doesNotUseEditor() throws IOException {
        assertFalse(Files.readString(ROW).contains("getEditor()"),
                "Non-editable ComboBox has no editor");
    }

    @Test
    void timeslotRow_syncsComboBoxValueFromRoster() throws IOException {
        String src = Files.readString(ROW);
        assertTrue(src.contains("syncComboBoxValue"));
        assertTrue(src.contains("private final ObservableList<DJRecord> roster"));
    }

    @Test
    void rosterListener_resyncsComboBoxValue() throws IOException {
        String src = Files.readString(ROW);
        assertTrue(src.contains("updatePrompt.run(); syncComboBoxValue();"),
                "Roster reload replaces DJRecord references; listener must resync the value");
    }

    @Test
    void programmaticUpdateGuard_protectsSetOnAction() throws IOException {
        String src = Files.readString(ROW);
        assertTrue(src.contains("if (programmaticUpdate) return;"));
    }

    @Test
    void wasPermutated_usesRemoveThenAdd() throws IOException {
        String src = Files.readString(VIEW);
        assertTrue(src.contains("slotsContainer.getChildren().remove(from, to)"));
        assertTrue(src.contains("slotsContainer.getChildren().add(from + i, reordered[i])"));
        assertFalse(src.contains("slotsContainer.getChildren().set("),
                "set(i, node) throws 'Duplicate children added'");
    }

    @Test
    void rightPanel_sectionOrder_eventInfoTimeslotsAssets() throws IOException {
        String src = Files.readString(VIEW);
        int content = src.indexOf("var content = new VBox(8,");
        assertTrue(content >= 0);
        String block = src.substring(content);
        int event = block.indexOf("eventInfoHeader");
        int slots = block.indexOf("timeslotsHeader");
        int assets = block.indexOf("assetsHeader");
        assertTrue(event >= 0 && event < slots, "Event Info must precede Timeslots");
        assertTrue(slots < assets, "Timeslots must precede Assets");
    }
}
