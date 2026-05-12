package dev.vrcposter;

import dev.vrcposter.repository.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PosterModel text-setter methods added to ApplicationController in Story 3.2.
 * Uses an uninitialized DatabaseManager (no actual DB connection) — the null connection
 * is safe because these tests never trigger any database operations.
 */
class ApplicationControllerPosterTextTest {

    private ApplicationController controller;

    @BeforeEach
    void setUp() {
        controller = new ApplicationController(new DatabaseManager(Path.of("unused.db")));
    }

    @AfterEach
    void tearDown() {
        controller.shutdown();
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
