package dev.vrcposter;

import atlantafx.base.theme.PrimerDark;
import dev.vrcposter.repository.DatabaseManager;
import dev.vrcposter.repository.migration.Migration001_InitialSchema;
import dev.vrcposter.repository.migration.MigrationRunner;
import dev.vrcposter.ui.views.PosterBuilderView;
import dev.vrcposter.util.AppPaths;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private ApplicationController applicationController;

    @Override
    public void start(Stage stage) throws Exception {
        initAppDirectories();
        loadEthnocentricFont();

        var databaseManager = new DatabaseManager(AppPaths.rosterDb());
        databaseManager.initialize();
        new MigrationRunner(databaseManager, List.of(new Migration001_InitialSchema())).run();
        applicationController = new ApplicationController(databaseManager);
        applicationController.loadRoster();

        // Apply AtlantaFX Primer Dark theme globally BEFORE creating the Scene
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        var root = new PosterBuilderView(stage, applicationController);
        var scene = new Scene(root, 1400, 780);

        // Load supplemental app-theme.css (custom tokens + overrides)
        scene.getStylesheets().add(
            Objects.requireNonNull(
                App.class.getResource("/dev/vrcposter/css/app-theme.css"),
                "app-theme.css not found on classpath"
            ).toExternalForm()
        );

        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setTitle("VRChat DJ Event Poster Creator");
        stage.setScene(scene);
        stage.show();

        log.info("Application started");
    }

    @Override
    public void stop() {
        if (applicationController != null) {
            applicationController.shutdown();
        }
    }

    private void loadEthnocentricFont() {
        try (var stream = App.class.getResourceAsStream("/dev/vrcposter/fonts/Ethnocentric.ttf")) {
            if (stream == null) {
                log.warn("Ethnocentric.ttf not found on classpath — fallback font will be used");
                return;
            }
            java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, stream);
            java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            log.info("Ethnocentric font registered: {}", font.getName());
        } catch (Exception e) {
            log.warn("Could not load Ethnocentric font — fallback font will be used", e);
        }
    }

    private void initAppDirectories() throws IOException {
        Files.createDirectories(AppPaths.logosDir());
        Files.createDirectories(AppPaths.assetsDir());
        Files.createDirectories(AppPaths.logsDir());
    }

    public static void main(String[] args) {
        launch();
    }
}
