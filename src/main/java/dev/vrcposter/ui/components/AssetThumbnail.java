package dev.vrcposter.ui.components;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Composite component for a single asset upload slot.
 * Extends VBox so the error label can live below the thumbnail row.
 * Stateless with respect to model data — the caller owns path and model updates.
 */
public class AssetThumbnail extends VBox {

    private final String assetLabel;
    private final ImageView thumbnailView;
    private final VBox placeholderBox;
    private final Label filenameLabel;
    private final Button actionButton;
    private final StackPane thumbnailBox;
    private final Label errorLabel;

    public AssetThumbnail(Stage ownerStage, String assetLabel, Consumer<Path> onFileSelected) {
        this.assetLabel = assetLabel;
        // ── Thumbnail box (36×36) ────────────────────────────────────────
        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(36);
        thumbnailView.setFitHeight(36);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setVisible(false);

        var uploadIcon = new Label("⬆");
        uploadIcon.setStyle("-fx-text-fill: color-text-secondary; -fx-font-size: 16px;");
        var uploadText = new Label("Click to upload");
        uploadText.setStyle("-fx-text-fill: color-text-secondary; -fx-font-size: 9px;");
        placeholderBox = new VBox(2, uploadIcon, uploadText);
        placeholderBox.setAlignment(Pos.CENTER);

        thumbnailBox = new StackPane(thumbnailView, placeholderBox);
        thumbnailBox.setMinSize(36, 36);
        thumbnailBox.setMaxSize(36, 36);
        thumbnailBox.setPrefSize(36, 36);
        thumbnailBox.setStyle("-fx-border-color: color-border; -fx-border-width: 1px;");

        thumbnailBox.setAccessibleText(assetLabel + ": not set — click to upload");

        // Hover + click: thumbnail acts as a second browse affordance (UX spec C3)
        thumbnailBox.setCursor(Cursor.HAND);
        thumbnailBox.setOnMouseEntered(e -> thumbnailBox.setOpacity(0.7));
        thumbnailBox.setOnMouseExited(e -> thumbnailBox.setOpacity(1.0));

        // ── Right side: filename + button ────────────────────────────────
        filenameLabel = new Label("—");
        filenameLabel.setMaxWidth(180);
        filenameLabel.setStyle("-fx-text-fill: color-text-secondary; -fx-font-size: 11px;");

        actionButton = new Button("Upload");
        actionButton.getStyleClass().add("secondary");
        actionButton.setAccessibleText("Upload " + assetLabel.toLowerCase());
        // Delegate thumbnail click to the action button (moved after actionButton init to satisfy definite assignment)
        thumbnailBox.setOnMouseClicked(e -> actionButton.fire());
        actionButton.setOnAction(e -> {
            var chooser = new FileChooser();
            chooser.setTitle("Select Image");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
            );
            File selected = chooser.showOpenDialog(ownerStage);
            if (selected != null) {
                onFileSelected.accept(selected.toPath());
            }
        });

        var rightBox = new VBox(4, filenameLabel, actionButton);
        rightBox.setAlignment(Pos.CENTER_LEFT);

        // ── Error label (hidden by default) ─────────────────────────────
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: color-destructive; -fx-font-size: 11px;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // ── Assemble ────────────────────────────────────────────────────
        var row = new HBox(8, thumbnailBox, rightBox);
        row.setAlignment(Pos.CENTER_LEFT);

        setSpacing(2);
        getChildren().addAll(row, errorLabel);
        setAccessibleRole(AccessibleRole.PARENT);
    }

    /** Switches component to the loaded state and displays the given image and filename. Must be called from JAT. */
    public void showImage(BufferedImage img, String filename) {
        thumbnailView.setImage(SwingFXUtils.toFXImage(img, null));
        thumbnailView.setVisible(true);
        placeholderBox.setVisible(false);
        placeholderBox.setManaged(false);
        filenameLabel.setText(filename);
        actionButton.setText("Replace…");
        thumbnailBox.setAccessibleText(assetLabel + ": " + filename);
        actionButton.setAccessibleText("Replace " + assetLabel.toLowerCase());
    }

    /** Displays an error message below the thumbnail row. */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /** Hides the error label. */
    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
