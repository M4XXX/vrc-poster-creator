package dev.vrcposter.ui.views;

import dev.vrcposter.App;
import dev.vrcposter.ApplicationController;
import dev.vrcposter.model.TimeslotEntry;
import dev.vrcposter.ui.components.AssetThumbnail;
import dev.vrcposter.ui.components.DJFormDialog;
import dev.vrcposter.ui.components.ExportSuccessBar;
import dev.vrcposter.ui.components.PosterPreviewPanel;
import dev.vrcposter.ui.components.StatusBar;
import dev.vrcposter.ui.components.TimeslotRow;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class PosterBuilderView extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(PosterBuilderView.class);

    // Stored for FileChooser dialogs in Story 2.5+
    private final Stage stage;
    // Stored for routing all model mutations in Story 2.2+
    private final ApplicationController controller;

    // Reused across repeated menu clicks — prevents duplicate windows
    private Stage djManagerStage;

    private final StatusBar statusBar = new StatusBar();
    private final PosterPreviewPanel posterPreviewPanel = new PosterPreviewPanel();
    private final ExportSuccessBar exportSuccessBar = new ExportSuccessBar();
    private final Label exportErrorLabel = new Label();

    public PosterBuilderView(Stage stage, ApplicationController controller) {
        this.stage = stage;
        this.controller = controller;
        exportSuccessBar.setVisible(false);
        exportSuccessBar.setManaged(false);
        exportErrorLabel.setWrapText(true);
        exportErrorLabel.getStyleClass().add("error-label");
        exportErrorLabel.setVisible(false);
        exportErrorLabel.setManaged(false);
        setTop(new VBox(buildMenuBar(), exportSuccessBar, exportErrorLabel));
        setCenter(buildThreeZoneLayout());
        setBottom(statusBar);
        // Wire preview callback after layout is built (panel is now attached to scene graph)
        controller.getPreviewManager().setPreviewCallback(posterPreviewPanel::setImage);
        // Wire save-state callback — controller calls this on the JAT, so no extra marshaling needed
        controller.setSaveStateCallback(statusBar::updateSaveState);
        log.debug("PosterBuilderView initialized");
    }

    private MenuBar buildMenuBar() {
        // ── File ──────────────────────────────────────────────────────────
        var exportPosterJpeg = new MenuItem("Export Poster as JPEG…");
        exportPosterJpeg.setOnAction(e -> triggerExport("jpeg"));

        var exportPosterPng = new MenuItem("Export Poster as PNG…");
        exportPosterPng.setOnAction(e -> triggerExport("png"));

        var exportRoster = new MenuItem("Export DJ Roster…");
        exportRoster.setOnAction(e -> {
            var chooser = new FileChooser();
            chooser.setTitle("Export DJ Roster");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DJ Roster ZIP", "*.zip"));
            chooser.setInitialFileName("roster-export.zip");
            File selected = chooser.showSaveDialog(stage);
            if (selected == null) return;
            controller.exportRoster(selected.toPath(), null, () -> Platform.runLater(() -> {
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Export Failed");
                alert.setContentText("Could not export roster.");
                alert.show();
            }));
        });

        var importRoster = new MenuItem("Import DJ Roster…");
        importRoster.setOnAction(e -> {
            var chooser = new FileChooser();
            chooser.setTitle("Import DJ Roster");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DJ Roster ZIP", "*.zip"));
            File selected = chooser.showOpenDialog(stage);
            if (selected == null) return;
            importRoster.setDisable(true);
            controller.importRoster(selected.toPath(),
                count -> Platform.runLater(() -> importRoster.setDisable(false)),
                () -> Platform.runLater(() -> {
                    importRoster.setDisable(false);
                    var alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(stage);
                    alert.setTitle("Import Failed");
                    alert.setContentText("Could not import roster — check file format.");
                    alert.show();
                }));
        });

        var quit = new MenuItem("Quit");
        quit.setOnAction(e -> Platform.exit());

        var fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
            exportPosterJpeg, exportPosterPng, new SeparatorMenuItem(),
            exportRoster, importRoster, new SeparatorMenuItem(), quit);

        // ── DJ Manager — acts as a direct-action button in the menu bar ──
        // A Menu with no items never fires onShowing; the dummy item ensures
        // JavaFX treats it as a real menu and triggers the event on click.
        var djManagerMenu = new Menu("DJ Manager");
        djManagerMenu.getItems().add(new MenuItem());
        djManagerMenu.setOnShowing(e -> Platform.runLater(() -> {
            djManagerMenu.hide();
            openDJManager();
        }));

        return new MenuBar(fileMenu, djManagerMenu);
    }

    private void openDJManager() {
        if (djManagerStage == null || !djManagerStage.isShowing()) {
            djManagerStage = new Stage();
            djManagerStage.initOwner(stage);
            djManagerStage.initModality(Modality.WINDOW_MODAL);
            djManagerStage.setTitle("DJ Manager");
            var scene = new Scene(new DJManagerView(controller), 760, 540);
            scene.getStylesheets().add(
                Objects.requireNonNull(
                    App.class.getResource("/dev/vrcposter/css/app-theme.css"),
                    "app-theme.css not found on classpath"
                ).toExternalForm()
            );
            scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) djManagerStage.close(); });
            djManagerStage.setScene(scene);
        }
        controller.loadRoster();
        djManagerStage.show();
        djManagerStage.requestFocus();
    }

    private HBox buildThreeZoneLayout() {
        var centerPanel = buildCenterPanel();
        var rightPanel  = buildRightPanel();

        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        var layout = new HBox(centerPanel, rightPanel);
        layout.setMinHeight(0);
        // Bind each panel to exactly 50% of the HBox width so preview and
        // the event/assets/timeslots panel share space equally.
        centerPanel.prefWidthProperty().bind(layout.widthProperty().multiply(0.5));
        rightPanel.prefWidthProperty().bind(layout.widthProperty().multiply(0.5));
        return layout;
    }

    private void triggerExport(String format) {
        var chooser = new FileChooser();
        String title = controller.getPosterModel().getTitle();
        String baseName = (title != null && !title.isBlank())
                ? title.trim().replaceAll("[^a-zA-Z0-9_\\-]", "-").toLowerCase()
                : "poster";
        if ("jpeg".equals(format)) {
            chooser.setTitle("Export as JPEG");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"));
            chooser.setInitialFileName(baseName + ".jpg");
        } else {
            chooser.setTitle("Export as PNG");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            chooser.setInitialFileName(baseName + ".png");
        }
        File selected = chooser.showSaveDialog(stage);
        if (selected == null) return;

        Path destPath = selected.toPath();
        exportErrorLabel.setVisible(false);
        exportErrorLabel.setManaged(false);
        exportSuccessBar.hide();

        controller.exportPoster(format, destPath,
            () -> exportSuccessBar.show(destPath),
            msg -> {
                exportErrorLabel.setText(msg);
                exportErrorLabel.setVisible(true);
                exportErrorLabel.setManaged(true);
            }
        );
    }

    private StackPane buildCenterPanel() {
        var panel = new StackPane();
        panel.getStyleClass().add("panel-preview");
        panel.setMinHeight(0);
        panel.setMinWidth(0);
        // StackPane fills its child automatically — no pref binding needed,
        // and binding prefHeight to height creates a circular dependency that
        // prevents the panel from shrinking when the window gets smaller.
        panel.getChildren().add(posterPreviewPanel);
        return panel;
    }

    private VBox buildRightPanel() {
        // ── Event Info section ─────────────────────────────────────────
        var eventInfoHeader = new Label("EVENT INFO");
        eventInfoHeader.getStyleClass().add("section-header");

        var titleField = new TextField();
        titleField.setPromptText("Event Title");
        titleField.setMaxWidth(Double.MAX_VALUE);
        titleField.setText(controller.getPosterModel().getTitle());

        var subtitleField = new TextField();
        subtitleField.setPromptText("Subtitle");
        subtitleField.setMaxWidth(Double.MAX_VALUE);
        subtitleField.setText(controller.getPosterModel().getSubtitle());

        var dateField = new TextField();
        dateField.setPromptText("Date");
        dateField.setMaxWidth(Double.MAX_VALUE);
        dateField.setText(controller.getPosterModel().getDate());

        titleField.textProperty().addListener((obs, o, n) -> controller.setPosterTitle(n));
        subtitleField.textProperty().addListener((obs, o, n) -> controller.setPosterSubtitle(n));
        dateField.textProperty().addListener((obs, o, n) -> controller.setPosterDate(n));

        var fontColorPicker = new ColorPicker(controller.getPosterModel().getFontColor());
        fontColorPicker.setMaxWidth(Double.MAX_VALUE);
        fontColorPicker.setOnAction(e -> controller.getPosterModel().setFontColor(fontColorPicker.getValue()));

        var fontColorRow = new HBox(8, new Label("Font color"), fontColorPicker);
        fontColorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ── Assets section ─────────────────────────────────────────────
        var assetsHeader = new Label("ASSETS");
        assetsHeader.getStyleClass().add("section-header");

        // Array holders allow the lambda to capture the component reference
        // even though the component hasn't been assigned to a final local yet.
        // The lambda is not invoked during construction — only on user click.
        AssetThumbnail[] bgRef   = new AssetThumbnail[1];
        AssetThumbnail[] logoRef = new AssetThumbnail[1];
        AssetThumbnail[] qrRef   = new AssetThumbnail[1];

        bgRef[0] = new AssetThumbnail(stage, "Background image", p -> {
            bgRef[0].clearError();
            controller.ingestBackgroundImage(p,
                img -> bgRef[0].showImage(img, p.getFileName().toString()),
                () -> bgRef[0].showError("Image could not be loaded. Use JPEG or PNG files only."));
        });
        logoRef[0] = new AssetThumbnail(stage, "Club logo", p -> {
            logoRef[0].clearError();
            controller.ingestClubLogoImage(p,
                img -> logoRef[0].showImage(img, p.getFileName().toString()),
                () -> logoRef[0].showError("Image could not be loaded. Use JPEG or PNG files only."));
        });
        qrRef[0] = new AssetThumbnail(stage, "QR code", p -> {
            qrRef[0].clearError();
            controller.ingestQrCodeImage(p,
                img -> qrRef[0].showImage(img, p.getFileName().toString()),
                () -> qrRef[0].showError("Image could not be loaded. Use JPEG or PNG files only."));
        });

        var bgThumb   = bgRef[0];
        var logoThumb = logoRef[0];
        var qrThumb   = qrRef[0];

        // Load thumbnails for any session-restored assets (path was verified to exist during restore)
        var model = controller.getPosterModel();
        var restoredBg   = model.getBackgroundPath();
        var restoredLogo = model.getClubLogoPath();
        var restoredQr   = model.getQrCodePath();
        if (restoredBg != null)
            controller.loadImageForThumbnail(restoredBg,
                img -> bgThumb.showImage(img, restoredBg.getFileName().toString()), () -> {});
        if (restoredLogo != null)
            controller.loadImageForThumbnail(restoredLogo,
                img -> logoThumb.showImage(img, restoredLogo.getFileName().toString()), () -> {});
        if (restoredQr != null)
            controller.loadImageForThumbnail(restoredQr,
                img -> qrThumb.showImage(img, restoredQr.getFileName().toString()), () -> {});

        // ── Timeslots section ──────────────────────────────────────────
        var timeslotsHeader = new Label("TIMESLOTS");
        timeslotsHeader.getStyleClass().add("section-header");

        var slotsContainer = new VBox(2);

        var addSlotBtn = new Button("+ Add Slot");
        addSlotBtn.getStyleClass().addAll("button", "secondary");
        addSlotBtn.setMaxWidth(Double.MAX_VALUE);
        addSlotBtn.setOnAction(e -> controller.addTimeslot());

        var addDjBtn = new Button("+ Add new DJ");
        addDjBtn.getStyleClass().addAll("button", "secondary");
        addDjBtn.setOnAction(e -> new DJFormDialog(stage, controller, null).showAndWait());

        var addBtnRow = new HBox(6, addSlotBtn, addDjBtn);
        addDjBtn.prefWidthProperty().bind(addBtnRow.widthProperty().subtract(6).divide(3));
        HBox.setHgrow(addSlotBtn, Priority.ALWAYS);

        var timeslots = controller.getPosterModel().getTimeslots();
        var roster = controller.getDjRoster().getObservableList();

        var noRosterLink = new javafx.scene.control.Hyperlink("Open DJ Manager");
        noRosterLink.setOnAction(e -> openDJManager());
        var noRosterBanner = new HBox(4, new Label("No DJs yet — "), noRosterLink);
        noRosterBanner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        noRosterBanner.getStyleClass().add("no-roster-banner");
        Runnable updateBanner = () -> {
            boolean empty = roster.isEmpty();
            noRosterBanner.setVisible(empty);
            noRosterBanner.setManaged(empty);
        };
        updateBanner.run();
        roster.addListener((ListChangeListener<dev.vrcposter.model.DJRecord>) c -> Platform.runLater(updateBanner));

        for (TimeslotEntry entry : timeslots) {
            slotsContainer.getChildren().add(new TimeslotRow(entry, roster, controller));
        }

        timeslots.addListener((ListChangeListener<TimeslotEntry>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    int insertAt = c.getFrom();
                    for (TimeslotEntry entry : c.getAddedSubList()) {
                        slotsContainer.getChildren().add(insertAt++,
                            new TimeslotRow(entry, roster, controller));
                    }
                } else if (c.wasRemoved()) {
                    slotsContainer.getChildren().remove(c.getFrom(),
                        c.getFrom() + c.getRemovedSize());
                } else if (c.wasPermutated()) {
                    int from = c.getFrom();
                    int to   = c.getTo();
                    int n    = to - from;
                    // Snapshot the nodes in their current (pre-permutation) order.
                    javafx.scene.Node[] snapshot = slotsContainer.getChildren()
                        .subList(from, to).toArray(new javafx.scene.Node[0]);
                    // Build target order: reordered[newPos - from] = nodeAtOldPos.
                    javafx.scene.Node[] reordered = new javafx.scene.Node[n];
                    for (int i = 0; i < n; i++) {
                        reordered[c.getPermutation(from + i) - from] = snapshot[i];
                    }
                    // Remove the range first, then re-insert in new order.
                    // set(i, node) would throw "Duplicate children added" because
                    // each node is still in the list at its old index when set()
                    // tries to adopt it at the new index.
                    slotsContainer.getChildren().remove(from, to);
                    for (int i = 0; i < n; i++) {
                        slotsContainer.getChildren().add(from + i, reordered[i]);
                    }
                }
                // wasUpdated() ignored — extractor fires on property changes;
                // rebuilding rows would destroy focus and filter state.
            }
            statusBar.updateSlotCount(timeslots.size());
        });

        statusBar.updateSlotCount(timeslots.size());

        // ── Assemble content ───────────────────────────────────────────
        var avatarHintCheck = new CheckBox("Show Avatar optimization hint");
        avatarHintCheck.setSelected(controller.getPosterModel().isShowAvatarHint());
        avatarHintCheck.selectedProperty().addListener((obs, o, n) ->
            controller.getPosterModel().setShowAvatarHint(n));

        var bgCol   = new VBox(4, new Label("Background"), bgThumb);
        var logoCol = new VBox(4, new Label("Club Logo"),  logoThumb);
        var qrCol   = new VBox(4, new Label("QR Code"),   qrThumb);
        for (var col : new VBox[]{bgCol, logoCol, qrCol}) {
            HBox.setHgrow(col, Priority.ALWAYS);
            col.setMaxWidth(Double.MAX_VALUE);
        }
        var assetsRow = new HBox(8, bgCol, logoCol, qrCol);

        var content = new VBox(8,
            eventInfoHeader, titleField, dateField, subtitleField, fontColorRow,
            new Separator(),
            timeslotsHeader,
            noRosterBanner,
            slotsContainer,
            addBtnRow,
            new Separator(),
            assetsHeader,
            avatarHintCheck,
            assetsRow
        );
        content.setPadding(new Insets(12));

        var scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(0);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        var panel = new VBox(scroll);
        panel.getStyleClass().add("panel-side");
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setMinHeight(0);
        return panel;
    }
}
