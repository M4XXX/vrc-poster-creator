package dev.vrcposter.ui.views;

import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Structural tests for PosterBuilderView.
 *
 * TestFX tests that construct JavaFX nodes require a running JavaFX platform.
 * Until CI is configured with a headless JavaFX runtime (e.g., Monocle), these
 * tests are disabled. Run them manually on a developer machine with Java 21.
 *
 * To enable: remove @Disabled and ensure the JVM is started with:
 *   -Dtestfx.robot=glass -Dglass.platform=Monocle -Dmonocle.platform=Headless
 */
@Disabled("Requires JavaFX runtime — enable on machines with Java 21 + headless Monocle")
class PosterBuilderViewTest {

    @Test
    void posterBuilderViewExtendsPane() {
        // PosterBuilderView must extend a Pane subtype for use as Scene root
        assertTrue(BorderPane.class.isAssignableFrom(PosterBuilderView.class));
    }

    @Test
    void threeZoneLayoutHasCorrectPanelWidths() {
        // Construct PosterBuilderView with a real Stage using TestFX ApplicationTest.
        // Verify: left panel prefWidth == 220, right panel prefWidth == 280,
        // center panel HGrow == Priority.ALWAYS.
        fail("Implement with TestFX ApplicationTest once headless Monocle is available");
    }
}
