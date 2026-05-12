package dev.vrcposter.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class EmptyStateButtonAuditTest {

    private static final String COMP = "src/main/java/dev/vrcposter/ui/components/";
    private static final String VIEW = "src/main/java/dev/vrcposter/ui/views/";
    private static final String CSS  = "src/main/resources/dev/vrcposter/css/app-theme.css";

    // AC1 — roster empty banner in timeslots section
    @Test
    void posterBuilderView_rosterEmptyBanner() throws IOException {
        String src = Files.readString(Paths.get(VIEW + "PosterBuilderView.java"));
        assertTrue(src.contains("No DJs yet — "),
            "PosterBuilderView must contain 'No DJs yet — ' banner label");
        assertTrue(src.contains("Open DJ Manager"),
            "PosterBuilderView must contain 'Open DJ Manager' hyperlink");
    }

    // AC2 — ComboBox empty-roster prompt text
    @Test
    void timeslotRow_emptyRosterPromptText() throws IOException {
        String src = Files.readString(Paths.get(COMP + "TimeslotRow.java"));
        assertTrue(src.contains("No DJs in roster — add DJs via DJ Manager"),
            "TimeslotRow must contain empty-roster ComboBox prompt text");
    }

    // AC3 — preview empty overlay label
    @Test
    void posterPreviewPanel_emptyStateLabel() throws IOException {
        String src = Files.readString(Paths.get(COMP + "PosterPreviewPanel.java"));
        assertTrue(src.contains("Add a background and assign DJs to see your poster preview"),
            "PosterPreviewPanel must contain empty-state label text");
    }

    // AC4 — asset thumbnail "Click to upload" label
    @Test
    void assetThumbnail_clickToUploadLabel() throws IOException {
        String src = Files.readString(Paths.get(COMP + "AssetThumbnail.java"));
        assertTrue(src.contains("Click to upload"),
            "AssetThumbnail must contain 'Click to upload' label");
    }

    // AC5 — DJ Manager empty state (pre-existing)
    @Test
    void djManagerView_emptyStateMessage() throws IOException {
        String src = Files.readString(Paths.get(VIEW + "DJManagerView.java"));
        assertTrue(src.contains("No DJs yet — click Add DJ to get started"),
            "DJManagerView must contain empty-state placeholder text");
    }

    // AC6 — button hierarchy
    @Test
    void djFormDialog_saveBtnIsPrimary() throws IOException {
        String src = Files.readString(Paths.get(COMP + "DJFormDialog.java"));
        assertTrue(src.contains("saveBtn.getStyleClass().add(\"primary\")"),
            "DJFormDialog saveBtn must have 'primary' style class");
    }

    @Test
    void djFormDialog_cancelBtnIsGhost() throws IOException {
        String src = Files.readString(Paths.get(COMP + "DJFormDialog.java"));
        assertTrue(src.contains("cancelBtn.getStyleClass().add(\"ghost\")"),
            "DJFormDialog cancelBtn must have 'ghost' style class");
    }

    @Test
    void djManagerView_addDjBtnIsPrimary() throws IOException {
        String src = Files.readString(Paths.get(VIEW + "DJManagerView.java"));
        assertTrue(src.contains("addDjBtn.getStyleClass().add(\"primary\")"),
            "DJManagerView addDjBtn must have 'primary' style class");
    }

    @Test
    void timeslotRow_clearBtnHasGhost() throws IOException {
        String src = Files.readString(Paths.get(COMP + "TimeslotRow.java"));
        assertTrue(src.contains("clearBtn.getStyleClass().add(\"ghost\")"),
            "TimeslotRow clearBtn must have 'ghost' style class");
    }

    @Test
    void timeslotRow_removeBtnHasGhost() throws IOException {
        String src = Files.readString(Paths.get(COMP + "TimeslotRow.java"));
        assertTrue(src.contains("removeBtn.getStyleClass().add(\"ghost\")"),
            "TimeslotRow removeBtn must have 'ghost' style class");
    }

    @Test
    void appThemeCss_hasGhostButtonRule() throws IOException {
        String css = Files.readString(Paths.get(CSS));
        assertTrue(css.contains(".button.ghost"),
            "app-theme.css must define .button.ghost rule");
    }

    @Test
    void appThemeCss_hasPreviewEmptyLabelRule() throws IOException {
        String css = Files.readString(Paths.get(CSS));
        assertTrue(css.contains(".preview-empty-label"),
            "app-theme.css must define .preview-empty-label rule");
    }

    @Test
    void appThemeCss_hasNoRosterBannerRule() throws IOException {
        String css = Files.readString(Paths.get(CSS));
        assertTrue(css.contains(".no-roster-banner"),
            "app-theme.css must define .no-roster-banner rule");
    }
}
