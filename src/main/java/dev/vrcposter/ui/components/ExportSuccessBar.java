package dev.vrcposter.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

public class ExportSuccessBar extends HBox {
    private static final Logger log = LoggerFactory.getLogger(ExportSuccessBar.class);

    private final Label pathLabel = new Label();
    private Path storedPath;
    private Timeline dismissTimer;

    public ExportSuccessBar() {
        super(8);
        var dot = new Label("●");
        dot.getStyleClass().add("success-dot");
        var saved = new Label("Export saved:");
        var openFolderLink = new Hyperlink("Open Folder");
        openFolderLink.setOnAction(e -> openFolder());
        getChildren().addAll(dot, saved, pathLabel, openFolderLink);
        getStyleClass().add("export-success-bar");
        setVisible(false);
        setManaged(false);
        setAccessibleRole(AccessibleRole.PARENT);
    }

    public void show(Path filePath) {
        this.storedPath = filePath;
        pathLabel.setText(filePath.toString());
        setAccessibleText("Export complete: " + filePath.getFileName().toString());
        setVisible(true);
        setManaged(true);
        Platform.runLater(() -> notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
        if (dismissTimer != null) dismissTimer.stop();
        dismissTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> hide()));
        dismissTimer.play();
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        setAccessibleText("");
    }

    private void openFolder() {
        if (storedPath == null) return;
        try {
            Desktop.getDesktop().open(storedPath.getParent().toFile());
        } catch (IOException e) {
            log.warn("Could not open folder: {}", storedPath.getParent(), e);
        }
    }
}
