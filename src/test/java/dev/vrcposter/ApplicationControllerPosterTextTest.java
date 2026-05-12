package dev.vrcposter;

import dev.vrcposter.repository.DatabaseManager;
import dev.vrcposter.repository.RepositoryException;
import dev.vrcposter.repository.migration.Migration001_InitialSchema;
import dev.vrcposter.repository.migration.MigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerPosterTextTest {

    @TempDir
    Path tempDir;

    private ApplicationController controller;
    private DatabaseManager db;

    @BeforeEach
    void setUp() throws RepositoryException {
        db = new DatabaseManager(tempDir.resolve("test.db"));
        db.initialize();
        new MigrationRunner(db, List.of(new Migration001_InitialSchema())).run();
        controller = new ApplicationController(db);
    }

    @AfterEach
    void tearDown() throws RepositoryException {
        controller.shutdown();
        db.close();
    }

    @Test
    void setPosterTitle_updatesModelProperty() {
        controller.setPosterTitle("My Event");
        assertEquals("My Event", controller.getPosterModel().getTitle());
    }

    @Test
    void setPosterSubtitle_updatesModelProperty() {
        controller.setPosterSubtitle("A DJ Night");
        assertEquals("A DJ Night", controller.getPosterModel().getSubtitle());
    }

    @Test
    void setPosterDate_updatesModelProperty() {
        controller.setPosterDate("2026-05-10");
        assertEquals("2026-05-10", controller.getPosterModel().getDate());
    }

    @Test
    void setPosterTitle_emptyString_doesNotThrow() {
        assertDoesNotThrow(() -> controller.setPosterTitle(""));
        assertEquals("", controller.getPosterModel().getTitle());
    }

    @Test
    void setPosterSubtitle_emptyString_doesNotThrow() {
        assertDoesNotThrow(() -> controller.setPosterSubtitle(""));
        assertEquals("", controller.getPosterModel().getSubtitle());
    }

    @Test
    void setPosterDate_emptyString_doesNotThrow() {
        assertDoesNotThrow(() -> controller.setPosterDate(""));
        assertEquals("", controller.getPosterModel().getDate());
    }

    @Test
    void setPosterTitle_null_setsNullOnModel() {
        // SimpleStringProperty.set(null) is valid — clears to null
        controller.setPosterTitle(null);
        assertNull(controller.getPosterModel().getTitle());
    }

    @Test
    void setPosterTitle_overwritesPreviousValue() {
        controller.setPosterTitle("First");
        controller.setPosterTitle("Second");
        assertEquals("Second", controller.getPosterModel().getTitle());
    }

    @Test
    void setters_operateOnSamePosterModelInstance() {
        controller.setPosterTitle("T");
        controller.setPosterSubtitle("S");
        controller.setPosterDate("D");
        var model = controller.getPosterModel();
        assertEquals("T", model.getTitle());
        assertEquals("S", model.getSubtitle());
        assertEquals("D", model.getDate());
    }
}
