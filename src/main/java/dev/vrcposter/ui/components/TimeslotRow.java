package dev.vrcposter.ui.components;

import dev.vrcposter.ApplicationController;
import dev.vrcposter.model.DJRecord;
import dev.vrcposter.model.TimeslotEntry;
import dev.vrcposter.util.ImageIngester;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.AccessibleRole;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TimeslotRow extends HBox {
    private static final Logger log = LoggerFactory.getLogger(TimeslotRow.class);

    private final TimeslotEntry entry;
    private final ApplicationController controller;
    private final ObservableList<DJRecord> roster;

    private final Label dragHandle;
    private final TextField timeField;
    private final ComboBox<DJRecord> djComboBox;
    private final TextField genreField;
    private final ImageView logoView;
    private final Button clearBtn;
    private final Button removeBtn;

    private boolean programmaticUpdate = false;

    public TimeslotRow(TimeslotEntry entry, ObservableList<DJRecord> roster,
                       ApplicationController controller) {
        this.entry = entry;
        this.controller = controller;
        this.roster = roster;

        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 8, 4, 8));

        dragHandle = new Label("⠃");
        dragHandle.setStyle("-fx-cursor: open-hand; -fx-font-size: 14px;");

        timeField = new TextField(entry.getStartTime());
        timeField.setPrefWidth(55);
        timeField.setMinWidth(55);
        timeField.setMaxWidth(55);
        timeField.setOnAction(e -> controller.setTimeslotStartTime(entry, timeField.getText().trim()));
        timeField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                controller.setTimeslotStartTime(entry, timeField.getText().trim());
            }
        });

        djComboBox = new ComboBox<>(roster);
        djComboBox.setPromptText("Assign DJ…");
        Runnable updatePrompt = () -> {
            if (entry.getAssignedDjId() == null || entry.getAssignedDjId().isBlank()) {
                djComboBox.setPromptText(roster.isEmpty()
                    ? "No DJs in roster — add DJs via DJ Manager"
                    : "Assign DJ…");
            }
        };
        updatePrompt.run();
        roster.addListener((javafx.collections.ListChangeListener<DJRecord>) c ->
            Platform.runLater(() -> { updatePrompt.run(); syncComboBoxValue(); }));
        HBox.setHgrow(djComboBox, Priority.ALWAYS);
        djComboBox.setMaxWidth(Double.MAX_VALUE);
        djComboBox.setButtonCell(new DJCell());
        djComboBox.setCellFactory(lv -> new DJCell());

        djComboBox.setOnAction(e -> {
            if (programmaticUpdate) return;
            DJRecord selected = djComboBox.getValue();
            if (selected != null) {
                controller.assignDjToTimeslot(entry, selected);
            }
        });

        logoView = new ImageView();
        logoView.setFitWidth(18);
        logoView.setFitHeight(18);
        logoView.setPreserveRatio(true);

        clearBtn = new Button("×");
        clearBtn.getStyleClass().add("timeslot-clear-btn");
        clearBtn.getStyleClass().add("ghost");
        clearBtn.setOnAction(e -> controller.clearTimeslotAssignment(entry));

        removeBtn = new Button("−");
        removeBtn.getStyleClass().add("timeslot-remove-btn");
        removeBtn.getStyleClass().add("ghost");
        removeBtn.setOnAction(e -> controller.removeTimeslot(entry));

        genreField = new TextField(entry.getGenre());
        genreField.setPromptText("Genre…");
        genreField.setPrefWidth(80);
        genreField.setOnAction(e -> controller.setTimeslotGenre(entry, genreField.getText()));
        genreField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) controller.setTimeslotGenre(entry, genreField.getText());
        });
        entry.genreProperty().addListener((obs, old, newVal) -> {
            if (!genreField.getText().equals(newVal)) genreField.setText(newVal);
        });

        // DnD: drag initiated from the handle
        dragHandle.setOnDragDetected(e -> {
            int fromIndex = ((VBox) getParent()).getChildren().indexOf(this);
            var db = dragHandle.startDragAndDrop(TransferMode.MOVE);
            var cc = new ClipboardContent();
            cc.put(DataFormat.PLAIN_TEXT, String.valueOf(fromIndex));
            db.setContent(cc);
            e.consume();
        });

        // DnD: this row accepts drops from other rows
        setOnDragOver(e -> {
            if (e.getGestureSource() != this && e.getDragboard().hasContent(DataFormat.PLAIN_TEXT)) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        setOnDragDropped(e -> {
            var db = e.getDragboard();
            if (db.hasContent(DataFormat.PLAIN_TEXT)) {
                int fromIndex = Integer.parseInt((String) db.getContent(DataFormat.PLAIN_TEXT));
                int toIndex = ((VBox) getParent()).getChildren().indexOf(this);
                if (fromIndex != toIndex) controller.swapTimeslotDjContent(fromIndex, toIndex);
                e.setDropCompleted(true);
            }
            e.consume();
        });

        getChildren().addAll(dragHandle, timeField, djComboBox, genreField, logoView, clearBtn, removeBtn);
        setAccessibleRole(AccessibleRole.LIST_ITEM);

        entry.assignedDjIdProperty().addListener((obs, old, newId) -> {
            updateFillState(newId);
            syncComboBoxValue();
        });

        entry.assignedDjLogoPathProperty().addListener((obs, old, newPath) -> {
            if (newPath == null) {
                logoView.setImage(null);
                return;
            }
            if (getScene() == null) return;
            var task = new Task<Optional<java.awt.image.BufferedImage>>() {
                @Override
                protected Optional<java.awt.image.BufferedImage> call() {
                    return ImageIngester.load(newPath, 18, 18);
                }
            };
            task.setOnSucceeded(ev -> Platform.runLater(() ->
                task.getValue().ifPresent(img -> logoView.setImage(SwingFXUtils.toFXImage(img, null)))));
            task.setOnFailed(ev -> log.warn("Logo load failed for timeslot row", task.getException()));
            controller.getExecutor().submit(task);
        });

        updateFillState(entry.getAssignedDjId());

        // Load logo for entries restored from session — the property listener won't fire for initial values
        var initialLogo = entry.getAssignedDjLogoPath();
        if (initialLogo != null) {
            var logoTask = new Task<Optional<java.awt.image.BufferedImage>>() {
                @Override
                protected Optional<java.awt.image.BufferedImage> call() {
                    return ImageIngester.load(initialLogo, 18, 18);
                }
            };
            logoTask.setOnSucceeded(ev -> Platform.runLater(() ->
                logoTask.getValue().ifPresent(img -> logoView.setImage(SwingFXUtils.toFXImage(img, null)))));
            logoTask.setOnFailed(ev -> log.warn("Initial logo load failed for timeslot row", logoTask.getException()));
            controller.getExecutor().submit(logoTask);
        }
    }

    private void syncComboBoxValue() {
        String assignedId = entry.getAssignedDjId();
        if (assignedId == null || assignedId.isBlank()) return;
        roster.stream()
            .filter(dj -> assignedId.equals(dj.getId()))
            .findFirst()
            .ifPresent(dj -> {
                if (djComboBox.getValue() != dj) {
                    programmaticUpdate = true;
                    try { djComboBox.setValue(dj); } finally { programmaticUpdate = false; }
                }
            });
    }

    private void updateFillState(String assignedDjId) {
        boolean filled = assignedDjId != null && !assignedDjId.isBlank();
        if (filled) {
            getStyleClass().remove("timeslot-row-empty");
            if (!getStyleClass().contains("timeslot-row-filled")) {
                getStyleClass().add("timeslot-row-filled");
            }
            clearBtn.setVisible(true);
            clearBtn.setManaged(true);
            logoView.setVisible(true);
            logoView.setManaged(true);
            syncComboBoxValue();
        } else {
            getStyleClass().remove("timeslot-row-filled");
            if (!getStyleClass().contains("timeslot-row-empty")) {
                getStyleClass().add("timeslot-row-empty");
            }
            clearBtn.setVisible(false);
            clearBtn.setManaged(false);
            logoView.setVisible(false);
            logoView.setManaged(false);
            programmaticUpdate = true;
            try {
                djComboBox.setValue(null);
            } finally {
                programmaticUpdate = false;
            }
            logoView.setImage(null);
        }
        String djName = filled ? resolveAccessibleDjName(assignedDjId) : "";
        setAccessibleText(entry.getStartTime() + (filled ? " — " + djName : " — empty"));
        logoView.setAccessibleText(filled ? djName + " logo" : "");
    }

    private String resolveAccessibleDjName(String djId) {
        if (djId == null || djId.isBlank()) return "";
        DJRecord selected = djComboBox.getValue();
        if (selected != null && djId.equals(selected.getId())) {
            return selected.getArtistName();
        }
        return djComboBox.getItems().stream()
            .filter(dj -> djId.equals(dj.getId()))
            .map(DJRecord::getArtistName)
            .findFirst()
            .orElse("assigned DJ");
    }

    private static class DJCell extends ListCell<DJRecord> {
        @Override
        protected void updateItem(DJRecord item, boolean empty) {
            super.updateItem(item, empty);
            setText((item == null || empty) ? null : item.getArtistName());
        }
    }
}
