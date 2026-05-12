package dev.vrcposter.ui.components;

import dev.vrcposter.App;
import dev.vrcposter.ApplicationController;
import dev.vrcposter.model.DJRecord;
import dev.vrcposter.util.AppPaths;
import dev.vrcposter.util.ImageIngester;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class DJFormDialog extends Stage {
    private static final Logger log = LoggerFactory.getLogger(DJFormDialog.class);

    private final ApplicationController controller;
    private final DJRecord existingRecord;

    private final TextField artistNameField  = new TextField();
    private final TextField discordNameField = new TextField();
    private final TextField vrcNameField     = new TextField();
    private final TextField genresField      = new TextField();
    private final TextField streamLink1Field = new TextField();
    private final TextField streamLink2Field = new TextField();

    private final ImageView logoImageView        = new ImageView();
    private final Label artistNameErrorLabel     = new Label("Artist Name is required");
    private final Label logoErrorLabel           = new Label("Could not load image — unsupported or missing");

    private Path selectedLogoPath;
    private Button saveBtn;

    public DJFormDialog(Stage owner, ApplicationController controller, DJRecord existingRecord) {
        this.controller     = controller;
        this.existingRecord = existingRecord;
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle(existingRecord == null ? "Add DJ" : "Edit DJ");
        setResizable(false);

        var scene = new Scene(buildContent(), 420, 560);
        scene.getStylesheets().add(
            Objects.requireNonNull(
                App.class.getResource("/dev/vrcposter/css/app-theme.css"),
                "app-theme.css not found on classpath"
            ).toExternalForm()
        );
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) close(); });
        setScene(scene);

        if (existingRecord != null) populateFields();
    }

    private VBox buildContent() {
        var grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        var labelCol = new ColumnConstraints(110);
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        int row = 0;

        row = addRow(grid, row, "Artist Name *", artistNameField);

        artistNameErrorLabel.getStyleClass().add("form-error-label");
        artistNameErrorLabel.setVisible(false);
        artistNameErrorLabel.setManaged(false);
        GridPane.setColumnSpan(artistNameErrorLabel, 2);
        grid.add(artistNameErrorLabel, 0, row++);

        row = addRow(grid, row, "Discord Name",  discordNameField);
        row = addRow(grid, row, "VRC Name",      vrcNameField);
        row = addRow(grid, row, "Genres",        genresField);
        row = addRow(grid, row, "Stream Link 1", streamLink1Field);
        row = addRow(grid, row, "Stream Link 2", streamLink2Field);

        // Logo row
        logoImageView.setFitWidth(64);
        logoImageView.setFitHeight(64);
        logoImageView.setPreserveRatio(true);
        logoImageView.setAccessibleText("No logo selected");
        var logoStack = new StackPane(logoImageView);
        logoStack.getStyleClass().add("logo-preview");
        logoStack.setPrefSize(64, 64);
        logoStack.setMinSize(64, 64);
        logoStack.setMaxSize(64, 64);

        var browseBtn = new Button("Browse…");
        browseBtn.getStyleClass().add("secondary");
        browseBtn.setOnAction(e -> browseLogo());

        logoErrorLabel.getStyleClass().add("form-error-label");
        logoErrorLabel.setVisible(false);
        logoErrorLabel.setManaged(false);

        var logoControls = new VBox(4, logoStack, browseBtn, logoErrorLabel);
        logoControls.setAlignment(Pos.TOP_LEFT);

        var logoLabel = new Label("Logo");
        GridPane.setValignment(logoLabel, javafx.geometry.VPos.TOP);
        grid.add(logoLabel, 0, row);
        grid.add(logoControls, 1, row);

        // Button bar
        var cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("ghost");
        cancelBtn.setOnAction(e -> close());

        saveBtn = new Button("Save DJ");
        saveBtn.getStyleClass().add("primary");
        saveBtn.setOnAction(e -> handleSave());

        var btnBar = new HBox(8, cancelBtn, saveBtn);
        btnBar.setAlignment(Pos.CENTER_RIGHT);

        var root = new VBox(16, grid, btnBar);
        root.setPadding(new Insets(16));
        root.setAccessibleRole(AccessibleRole.DIALOG);
        return root;
    }

    private int addRow(GridPane grid, int row, String labelText, TextField field) {
        var label = new Label(labelText);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setFillWidth(field, true);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }

    private void populateFields() {
        artistNameField.setText(s(existingRecord.getArtistName()));
        discordNameField.setText(s(existingRecord.getDiscordName()));
        vrcNameField.setText(s(existingRecord.getVrcName()));
        genresField.setText(s(existingRecord.getGenres()));
        streamLink1Field.setText(s(existingRecord.getStreamLink1()));
        streamLink2Field.setText(s(existingRecord.getStreamLink2()));

        if (existingRecord.getLogoFilename() != null) {
            Path logoPath = AppPaths.logosDir().resolve(existingRecord.getLogoFilename());
            Task<Optional<BufferedImage>> task = new Task<>() {
                @Override protected Optional<BufferedImage> call() {
                    return ImageIngester.load(logoPath, 200, 200);
                }
            };
            task.setOnSucceeded(e -> javafx.application.Platform.runLater(() ->
                task.getValue().ifPresent(img -> {
                    logoImageView.setImage(SwingFXUtils.toFXImage(img, null));
                    logoImageView.setAccessibleText(existingRecord.getArtistName() + " logo");
                })));
            task.setOnFailed(e -> log.warn("Could not load existing logo for preview", task.getException()));
            controller.getExecutor().submit(task);
        }
    }

    private void browseLogo() {
        var chooser = new FileChooser();
        chooser.setTitle("Select DJ Logo");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selected = chooser.showOpenDialog(this);
        if (selected == null) return;

        logoErrorLabel.setVisible(false);
        logoErrorLabel.setManaged(false);
        Path path = selected.toPath();

        Task<Optional<BufferedImage>> task = new Task<>() {
            @Override protected Optional<BufferedImage> call() {
                return ImageIngester.load(path, 200, 200);
            }
        };
        task.setOnSucceeded(e -> javafx.application.Platform.runLater(() -> {
            Optional<BufferedImage> result = task.getValue();
            if (result.isPresent()) {
                logoImageView.setImage(SwingFXUtils.toFXImage(result.get(), null));
                selectedLogoPath = path;
                logoImageView.setAccessibleText(artistNameField.getText().trim() + " logo");
            } else {
                logoErrorLabel.setVisible(true);
                logoErrorLabel.setManaged(true);
                logoImageView.setAccessibleText("Logo could not be loaded");
            }
        }));
        task.setOnFailed(e -> javafx.application.Platform.runLater(() -> {
            logoErrorLabel.setVisible(true);
            logoErrorLabel.setManaged(true);
            logoImageView.setAccessibleText("Logo could not be loaded");
        }));
        controller.getExecutor().submit(task);
    }

    private void handleSave() {
        artistNameField.getStyleClass().remove("field-error");
        artistNameErrorLabel.setVisible(false);
        artistNameErrorLabel.setManaged(false);

        if (artistNameField.getText().isBlank()) {
            artistNameField.getStyleClass().add("field-error");
            artistNameErrorLabel.setVisible(true);
            artistNameErrorLabel.setManaged(true);
            return;
        }

        saveBtn.setDisable(true);
        if (existingRecord == null) {
            DJRecord record = DJRecord.create(
                artistNameField.getText().trim(),
                nb(discordNameField.getText()),
                nb(vrcNameField.getText()),
                nb(genresField.getText()),
                nb(streamLink1Field.getText()),
                nb(streamLink2Field.getText())
            );
            controller.saveDj(record, selectedLogoPath, this::close, () -> saveBtn.setDisable(false));
        } else {
            DJRecord updated = new DJRecord(
                existingRecord.getId(),
                artistNameField.getText().trim(),
                nb(discordNameField.getText()),
                nb(vrcNameField.getText()),
                nb(genresField.getText()),
                nb(streamLink1Field.getText()),
                nb(streamLink2Field.getText()),
                existingRecord.getLogoFilename(),
                existingRecord.getCreatedAt(),
                System.currentTimeMillis()
            );
            controller.updateDj(updated, selectedLogoPath, existingRecord.getLogoFilename(), this::close, () -> saveBtn.setDisable(false));
        }
    }

    private static String s(String val) { return val == null ? "" : val; }
    private static String nb(String val) { return (val == null || val.isBlank()) ? null : val.trim(); }
}
