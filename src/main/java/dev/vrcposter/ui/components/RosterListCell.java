package dev.vrcposter.ui.components;

import dev.vrcposter.model.DJRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class RosterListCell extends ListCell<DJRecord> {

    private final HBox layout;
    private final StackPane logoPlaceholder;
    private final Label nameLabel;

    public RosterListCell() {
        getStyleClass().add("roster-cell");

        var placeholderText = new Label("DJ");
        placeholderText.getStyleClass().add("roster-placeholder-text");

        logoPlaceholder = new StackPane(placeholderText);
        logoPlaceholder.getStyleClass().add("roster-placeholder");
        logoPlaceholder.setPrefSize(28, 28);
        logoPlaceholder.setMinSize(28, 28);
        logoPlaceholder.setMaxSize(28, 28);

        nameLabel = new Label();
        nameLabel.getStyleClass().add("roster-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        layout = new HBox(8, logoPlaceholder, nameLabel);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(4, 8, 4, 8));
    }

    @Override
    protected void updateItem(DJRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setAccessibleText("");
            logoPlaceholder.setAccessibleText("");
            return;
        }
        nameLabel.setText(item.getArtistName());
        setAccessibleRole(AccessibleRole.LIST_ITEM);
        setAccessibleText(item.getArtistName()
            + (item.getGenres() != null && !item.getGenres().isBlank() ? ", " + item.getGenres() : ""));
        logoPlaceholder.setAccessibleText(item.getArtistName() + " logo");
        // Story 2.3 integration point: if item.getLogoFilename() != null,
        // load logo via ImageIngester.load() wrapped in Task<T> and set on an ImageView.
        // For now: placeholder is always shown (all logoFilename values are null until Story 2.3).
        setGraphic(layout);
        setText(null);
    }
}
