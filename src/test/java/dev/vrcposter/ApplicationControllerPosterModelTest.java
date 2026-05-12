package dev.vrcposter;

import dev.vrcposter.model.PosterModel;
import dev.vrcposter.model.TimeslotEntry;
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

class ApplicationControllerPosterModelTest {

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
    void getPosterModel_returnsNonNull() {
        assertNotNull(controller.getPosterModel());
    }

    @Test
    void getPosterModel_returnsSameInstance() {
        assertSame(controller.getPosterModel(), controller.getPosterModel());
    }

    @Test
    void getPosterModel_isInstanceOfPosterModel() {
        assertInstanceOf(PosterModel.class, controller.getPosterModel());
    }

    @Test
    void titlePropertyChange_doesNotThrow() {
        PosterModel model = controller.getPosterModel();
        assertDoesNotThrow(() -> model.setTitle("Test Event"));
        assertEquals("Test Event", model.getTitle());
    }

    @Test
    void timeslotListChange_doesNotThrow() {
        PosterModel model = controller.getPosterModel();
        assertDoesNotThrow(() -> {
            TimeslotEntry entry = TimeslotEntry.create("9pm");
            model.getTimeslots().add(entry);
            entry.setAssignedDjId("dj-test");
            model.getTimeslots().remove(entry);
        });
    }

    @Test
    void rapidPropertyChanges_collapseWithoutException() throws InterruptedException {
        PosterModel model = controller.getPosterModel();
        for (int i = 0; i < 10; i++) {
            model.setTitle("Title " + i);
        }
        assertEquals("Title 9", model.getTitle());
        // Allow debounce window to drain so background threads don't outlive the test
        Thread.sleep(400);
    }
}
