package dev.vrcposter.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates focus ring CSS rules and Escape key handlers are present.
 * File-based assertions only — no JavaFX runtime required.
 */
class FocusNavigationTest {

    private static final Path CSS_PATH = Paths.get(
            "src/main/resources/dev/vrcposter/css/app-theme.css");

    private static final Path POSTER_BUILDER_PATH = Paths.get(
            "src/main/java/dev/vrcposter/ui/views/PosterBuilderView.java");

    private static final Path DJ_FORM_DIALOG_PATH = Paths.get(
            "src/main/java/dev/vrcposter/ui/components/DJFormDialog.java");

    @Test
    void appThemeCss_containsAllRequiredFocusRingSelectors() throws IOException {
        String css = Files.readString(CSS_PATH);

        String[] requiredSelectors = {
            ".button:focused",
            ".combo-box:focused",
            ".text-field:focused",
            ".text-area:focused",
            ".list-view:focused",
            ".check-box:focused",
            ".radio-button:focused",
            ".table-view:focused",
            ".hyperlink:focused"
        };

        for (String selector : requiredSelectors) {
            assertTrue(css.contains(selector),
                "app-theme.css missing focus ring selector: " + selector);
        }
    }

    @Test
    void appThemeCss_focusRingUsesAccentColorToken() throws IOException {
        String css = Files.readString(CSS_PATH);
        // Focus ring block must use the token, not a raw hex value
        int focusBlockStart = css.indexOf(".button:focused");
        assertTrue(focusBlockStart >= 0, "Focus ring block not found");
        // Extract block up to closing brace
        int blockEnd = css.indexOf("}", focusBlockStart);
        String focusBlock = css.substring(focusBlockStart, blockEnd + 1);
        assertTrue(focusBlock.contains("color-accent"),
            "Focus ring block must use 'color-accent' token, not raw hex");
        assertTrue(focusBlock.contains("-fx-border-width: 2px"),
            "Focus ring must be 2px wide");
        assertTrue(focusBlock.contains("-fx-border-radius: 3px"),
            "Focus ring must have 3px radius");
    }

    @Test
    void posterBuilderView_djManagerStage_hasEscapeHandler() throws IOException {
        String source = Files.readString(POSTER_BUILDER_PATH);
        assertTrue(source.contains("KeyCode.ESCAPE") && source.contains("djManagerStage.close()"),
            "PosterBuilderView must close djManagerStage on Escape key");
    }

    @Test
    void djFormDialog_hasEscapeHandler() throws IOException {
        String source = Files.readString(DJ_FORM_DIALOG_PATH);
        assertTrue(source.contains("KeyCode.ESCAPE"),
            "DJFormDialog must handle Escape key to close");
    }
}
