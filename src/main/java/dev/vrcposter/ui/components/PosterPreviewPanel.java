package dev.vrcposter.ui.components;

import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

/**
 * Displays the live poster preview inside the center zone.
 * Background is #000000; the ImageView letterboxes automatically because
 * setPreserveRatio(true) is combined with both fitWidth and fitHeight bindings —
 * whichever axis is the limiting constraint gets filled, the other shows black padding.
 * Must only be mutated from the JavaFX Application Thread (PreviewManager ensures this).
 */
public class PosterPreviewPanel extends StackPane {

    private final ImageView imageView = new ImageView();
    private final Label emptyLabel = new Label(
            "Add a background and assign DJs to see your poster preview");

    public PosterPreviewPanel() {
        setStyle("-fx-background-color: #000000;");
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());
        getChildren().add(imageView);
        emptyLabel.getStyleClass().add("preview-empty-label");
        emptyLabel.setWrapText(true);
        emptyLabel.setMaxWidth(260);
        emptyLabel.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(emptyLabel);
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        setAccessibleText("Poster preview — updates automatically as changes are made");
    }

    /** Replaces the displayed preview image. Must be called on the JavaFX Application Thread. */
    public void setImage(WritableImage image) {
        imageView.setImage(image);
        emptyLabel.setVisible(image == null);
        emptyLabel.setManaged(image == null);
    }
}
