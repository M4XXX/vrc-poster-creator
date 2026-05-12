package dev.vrcposter.ui.views;

import dev.vrcposter.ApplicationController;
import dev.vrcposter.model.DJRecord;
import dev.vrcposter.ui.components.DJFormDialog;
import dev.vrcposter.ui.components.ExportSuccessBar;
import dev.vrcposter.util.AppPaths;
import dev.vrcposter.util.ImageIngester;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class DJManagerView extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(DJManagerView.class);

    private final ApplicationController controller;
    private TableView<DJRecord> table;
    private FilteredList<DJRecord> filteredRoster;
    private TextField searchField;
    private Button importBtn;
    private Button exportBtn;
    private final ExportSuccessBar exportSuccessBar = new ExportSuccessBar();
    private Label importStatusLabel;

    public DJManagerView(ApplicationController controller) {
        this.controller = controller;
        exportSuccessBar.setVisible(false);
        exportSuccessBar.setManaged(false);
        importStatusLabel = new Label();
        importStatusLabel.setVisible(false);
        importStatusLabel.setManaged(false);
        setAccessibleRole(AccessibleRole.PARENT);
        setTop(new VBox(buildToolbar(), exportSuccessBar, importStatusLabel));
        setCenter(buildContent());
    }

    private HBox buildToolbar() {
        searchField = new TextField();
        searchField.setPromptText("Search by Artist Name…");
        searchField.setMaxWidth(200);

        var addDjBtn = new Button("+ Add DJ");
        addDjBtn.getStyleClass().add("primary");
        addDjBtn.setOnAction(e -> openFormDialog(null));

        importBtn = new Button("Import");
        importBtn.getStyleClass().add("secondary");
        importBtn.setOnAction(e -> handleImport());

        exportBtn = new Button("Export");
        exportBtn.getStyleClass().add("secondary");
        exportBtn.setOnAction(e -> handleExport());

        var toolbar = new HBox(searchField, addDjBtn, importBtn, exportBtn);
        toolbar.getStyleClass().add("dj-manager-toolbar");
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        return toolbar;
    }

    private TableView<DJRecord> buildContent() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        filteredRoster = new FilteredList<>(controller.getDjRoster().getObservableList(), p -> true);
        table.setItems(filteredRoster);

        var emptyLabel = new Label("No DJs yet — click Add DJ to get started");
        emptyLabel.getStyleClass().add("dj-manager-empty");
        table.setPlaceholder(emptyLabel);

        TableColumn<DJRecord, String> logoCol = new TableColumn<>("");
        logoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLogoFilename()));
        logoCol.setPrefWidth(40);
        logoCol.setMaxWidth(40);
        logoCol.setResizable(false);
        logoCol.setSortable(false);
        logoCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(28);
                imageView.setFitHeight(28);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String filename, boolean empty) {
                super.updateItem(filename, empty);
                if (empty || filename == null) {
                    setGraphic(null);
                    imageView.setImage(null);
                    return;
                }
                setGraphic(imageView);
                imageView.setImage(null);
                var path = AppPaths.logosDir().resolve(filename);
                var task = new Task<Optional<BufferedImage>>() {
                    @Override protected Optional<BufferedImage> call() {
                        return ImageIngester.load(path, 28, 28);
                    }
                };
                task.setOnSucceeded(ev -> Platform.runLater(() -> {
                    if (filename.equals(getItem())) {
                        task.getValue().ifPresent(img ->
                            imageView.setImage(SwingFXUtils.toFXImage(img, null)));
                    }
                }));
                controller.getExecutor().submit(task);
            }
        });

        TableColumn<DJRecord, String> nameCol = new TableColumn<>("Artist Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getArtistName() != null ? data.getValue().getArtistName() : ""));

        TableColumn<DJRecord, String> discordCol = new TableColumn<>("Discord Name");
        discordCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDiscordName() != null ? data.getValue().getDiscordName() : ""));

        TableColumn<DJRecord, String> genresCol = new TableColumn<>("Genres");
        genresCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getGenres() != null ? data.getValue().getGenres() : ""));

        TableColumn<DJRecord, Void> actionsCol = new TableColumn<>("");
        actionsCol.setPrefWidth(150);
        actionsCol.setMaxWidth(150);
        actionsCol.setResizable(false);
        actionsCol.setSortable(false);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(4, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("secondary");
                deleteBtn.getStyleClass().add("danger");
                box.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> openFormDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().add(logoCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(discordCol);
        table.getColumns().add(genresCol);
        table.getColumns().add(actionsCol);

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openFormDialog(table.getSelectionModel().getSelectedItem());
            }
        });

        // searchField is already assigned by buildToolbar() which runs before buildContent() in constructor
        searchField.textProperty().addListener((obs, old, text) -> {
            boolean blank = text == null || text.isBlank();
            filteredRoster.setPredicate(blank ? p -> true
                    : dj -> dj.getArtistName().toLowerCase().contains(text.toLowerCase()));
            var ph = new Label(blank
                    ? "No DJs yet — click Add DJ to get started"
                    : "No results for \"" + text + "\"");
            ph.getStyleClass().add("dj-manager-empty");
            table.setPlaceholder(ph);
        });

        return table;
    }

    private void handleExport() {
        var chooser = new FileChooser();
        chooser.setTitle("Export DJ Roster");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DJ Roster ZIP", "*.zip"));
        chooser.setInitialFileName("roster-export.zip");
        File selected = chooser.showSaveDialog((Stage) getScene().getWindow());
        if (selected == null) return;
        Path path = selected.toPath();
        disableButtons();
        controller.exportRoster(path,
                () -> Platform.runLater(() -> {
                    enableButtons();
                    exportSuccessBar.show(path);
                }),
                () -> Platform.runLater(() -> {
                    enableButtons();
                    showErrorAlert("Export Failed", "Could not export roster.");
                }));
    }

    private void handleImport() {
        var chooser = new FileChooser();
        chooser.setTitle("Import DJ Roster");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DJ Roster ZIP", "*.zip"));
        File selected = chooser.showOpenDialog((Stage) getScene().getWindow());
        if (selected == null) return;
        Path path = selected.toPath();
        disableButtons();
        controller.importRoster(path,
                count -> Platform.runLater(() -> {
                    enableButtons();
                    showImportStatus(count);
                }),
                () -> Platform.runLater(() -> {
                    enableButtons();
                    showErrorAlert("Import Failed", "Could not import roster — check file format.");
                }));
    }

    private void disableButtons() {
        importBtn.setDisable(true);
        exportBtn.setDisable(true);
    }

    private void enableButtons() {
        importBtn.setDisable(false);
        exportBtn.setDisable(false);
    }

    private void showImportStatus(int count) {
        importStatusLabel.setText(count + " DJ" + (count == 1 ? "" : "s") + " imported");
        importStatusLabel.setVisible(true);
        importStatusLabel.setManaged(true);
        var timer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            importStatusLabel.setVisible(false);
            importStatusLabel.setManaged(false);
        }));
        timer.play();
    }

    private void showErrorAlert(String title, String msg) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner((Stage) getScene().getWindow());
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }

    private void handleDelete(DJRecord record) {
        var deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        var alert = new Alert(Alert.AlertType.CONFIRMATION,
                "This cannot be undone.", deleteBtn, ButtonType.CANCEL);
        alert.initOwner((Stage) getScene().getWindow());
        alert.setTitle("Delete DJ");
        alert.setHeaderText("Delete " + record.getArtistName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == deleteBtn) {
                controller.deleteDj(record.getId(), record.getLogoFilename(), null, null);
            }
        });
    }

    private void openFormDialog(DJRecord record) {
        Stage owner = (Stage) getScene().getWindow();
        new DJFormDialog(owner, controller, record).showAndWait();
    }
}
