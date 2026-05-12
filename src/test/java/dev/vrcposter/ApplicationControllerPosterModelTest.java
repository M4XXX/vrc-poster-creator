package dev.vrcposter;

import dev.vrcposter.model.PosterModel;
import dev.vrcposter.model.TimeslotEntry;
import dev.vrcposter.repository.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerPosterModelTest {

    private ApplicationController controller;

    @BeforeEach
    void setUp() {
        // DatabaseManager with no initialize() call: connection stays null.
        // close() is a no-op when connection == null, so shutdown() is safe.
        DatabaseManager dbManager = new DatabaseManager(Path.of("unused-for-test.db"));
        controller = new ApplicationController(dbManager);
    }

    @AfterEach
    void tearDown() {
        controller.shutdown();
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
