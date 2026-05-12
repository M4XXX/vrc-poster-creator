package dev.vrcposter.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusBar extends HBox {
    private static final Logger log = LoggerFactory.getLogger(StatusBar.class);

    private final Label saveStateLabel = new Label("● Saved");
    private final Label slotCountLabel = new Label("6 slots");

    public StatusBar() {
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(saveStateLabel, spacer, slotCountLabel);
        log.debug("StatusBar initialized");
    }

    /** Called by ApplicationController (Story 4.1) to update save indicator. */
    public void updateSaveState(String text) {
        saveStateLabel.setText(text);
    }

    /** Called when timeslot count changes (Story 3.3). */
    public void updateSlotCount(int count) {
        slotCountLabel.setText(count + " slots");
    }
}
