package dev.vrcposter.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that all custom JavaFX components declare the required
 * AccessibleRole and setAccessibleText annotations.
 * File-based assertions only — no JavaFX runtime required.
 */
class ScreenReaderAnnotationTest {

    private static final String COMPONENTS = "src/main/java/dev/vrcposter/ui/components/";
    private static final String VIEWS       = "src/main/java/dev/vrcposter/ui/views/";

    private static Path component(String name) { return Paths.get(COMPONENTS + name); }
    private static Path view(String name)      { return Paths.get(VIEWS      + name); }

    // ── AssetThumbnail ──────────────────────────────────────────────────────

    @Test
    void assetThumbnail_importsAccessibleRole() throws IOException {
        String src = Files.readString(component("AssetThumbnail.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "AssetThumbnail must import javafx.scene.AccessibleRole");
    }

    @Test
    void assetThumbnail_setsAccessibleRole() throws IOException {
        String src = Files.readString(component("AssetThumbnail.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.PARENT)"),
            "AssetThumbnail must call setAccessibleRole(AccessibleRole.PARENT)");
    }

    @Test
    void assetThumbnail_setsAccessibleTextOnThumbnailBox() throws IOException {
        String src = Files.readString(component("AssetThumbnail.java"));
        assertTrue(src.contains("thumbnailBox.setAccessibleText("),
            "AssetThumbnail must set accessible text on thumbnailBox");
    }

    @Test
    void assetThumbnail_setsAccessibleTextOnActionButton() throws IOException {
        String src = Files.readString(component("AssetThumbnail.java"));
        assertTrue(src.contains("actionButton.setAccessibleText("),
            "AssetThumbnail must set accessible text on actionButton");
    }

    // ── TimeslotRow ─────────────────────────────────────────────────────────

    @Test
    void timeslotRow_importsAccessibleRole() throws IOException {
        String src = Files.readString(component("TimeslotRow.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "TimeslotRow must import javafx.scene.AccessibleRole");
    }

    @Test
    void timeslotRow_setsAccessibleRoleListItem() throws IOException {
        String src = Files.readString(component("TimeslotRow.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.LIST_ITEM)"),
            "TimeslotRow must call setAccessibleRole(AccessibleRole.LIST_ITEM)");
    }

    @Test
    void timeslotRow_setsAccessibleTextInUpdateFillState() throws IOException {
        String src = Files.readString(component("TimeslotRow.java"));
        assertTrue(src.contains("setAccessibleText(entry.getStartTime()"),
            "TimeslotRow must set accessible text based on start time in updateFillState");
    }

    @Test
    void timeslotRow_setsLogoViewAccessibleText() throws IOException {
        String src = Files.readString(component("TimeslotRow.java"));
        assertTrue(src.contains("logoView.setAccessibleText("),
            "TimeslotRow must set accessible text on logoView");
    }

    // ── PosterPreviewPanel ──────────────────────────────────────────────────

    @Test
    void posterPreviewPanel_importsAccessibleRole() throws IOException {
        String src = Files.readString(component("PosterPreviewPanel.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "PosterPreviewPanel must import javafx.scene.AccessibleRole");
    }

    @Test
    void posterPreviewPanel_setsAccessibleRoleImageView() throws IOException {
        String src = Files.readString(component("PosterPreviewPanel.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.IMAGE_VIEW)"),
            "PosterPreviewPanel must call setAccessibleRole(AccessibleRole.IMAGE_VIEW)");
    }

    @Test
    void posterPreviewPanel_setsAccessibleText() throws IOException {
        String src = Files.readString(component("PosterPreviewPanel.java"));
        assertTrue(src.contains("setAccessibleText("),
            "PosterPreviewPanel must call setAccessibleText(...)");
    }

    // ── ExportSuccessBar ────────────────────────────────────────────────────

    @Test
    void exportSuccessBar_importsAccessibleRole() throws IOException {
        String src = Files.readString(component("ExportSuccessBar.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "ExportSuccessBar must import javafx.scene.AccessibleRole");
    }

    @Test
    void exportSuccessBar_importsAccessibleAttribute() throws IOException {
        String src = Files.readString(component("ExportSuccessBar.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleAttribute;"),
            "ExportSuccessBar must import javafx.scene.AccessibleAttribute");
    }

    @Test
    void exportSuccessBar_setsAccessibleRoleParent() throws IOException {
        String src = Files.readString(component("ExportSuccessBar.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.PARENT)"),
            "ExportSuccessBar must call setAccessibleRole(AccessibleRole.PARENT)");
    }

    @Test
    void exportSuccessBar_notifiesAccessibleAttributeOnShow() throws IOException {
        String src = Files.readString(component("ExportSuccessBar.java"));
        assertTrue(src.contains("notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT)"),
            "ExportSuccessBar.show() must call notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT)");
    }

    @Test
    void exportSuccessBar_clearsAccessibleTextOnHide() throws IOException {
        String src = Files.readString(component("ExportSuccessBar.java"));
        assertTrue(src.contains("setAccessibleText(\"\")"),
            "ExportSuccessBar.hide() must clear accessible text");
    }

    // ── RosterListCell ──────────────────────────────────────────────────────

    @Test
    void rosterListCell_importsAccessibleRole() throws IOException {
        String src = Files.readString(component("RosterListCell.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "RosterListCell must import javafx.scene.AccessibleRole");
    }

    @Test
    void rosterListCell_setsAccessibleRoleListItem() throws IOException {
        String src = Files.readString(component("RosterListCell.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.LIST_ITEM)"),
            "RosterListCell must call setAccessibleRole(AccessibleRole.LIST_ITEM)");
    }

    @Test
    void rosterListCell_setsAccessibleTextWithGenres() throws IOException {
        String src = Files.readString(component("RosterListCell.java"));
        assertTrue(src.contains("setAccessibleText(item.getArtistName()"),
            "RosterListCell must set accessible text including artist name");
    }

    @Test
    void rosterListCell_clearsAccessibleTextWhenEmpty() throws IOException {
        String src = Files.readString(component("RosterListCell.java"));
        assertTrue(src.contains("setAccessibleText(\"\")"),
            "RosterListCell must clear accessible text for empty cells");
    }

    // ── DJFormDialog ────────────────────────────────────────────────────────

    @Test
    void djFormDialog_importsAccessibleRole() throws IOException {
        String src = Files.readString(component("DJFormDialog.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "DJFormDialog must import javafx.scene.AccessibleRole");
    }

    @Test
    void djFormDialog_setsAccessibleRoleDialog() throws IOException {
        String src = Files.readString(component("DJFormDialog.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.DIALOG)"),
            "DJFormDialog must call setAccessibleRole(AccessibleRole.DIALOG)");
    }

    @Test
    void djFormDialog_setsLogoImageViewAccessibleText() throws IOException {
        String src = Files.readString(component("DJFormDialog.java"));
        assertTrue(src.contains("logoImageView.setAccessibleText("),
            "DJFormDialog must set accessible text on logoImageView");
    }

    // ── DJManagerView ───────────────────────────────────────────────────────

    @Test
    void djManagerView_importsAccessibleRole() throws IOException {
        String src = Files.readString(view("DJManagerView.java"));
        assertTrue(src.contains("import javafx.scene.AccessibleRole;"),
            "DJManagerView must import javafx.scene.AccessibleRole");
    }

    @Test
    void djManagerView_setsAccessibleRoleParent() throws IOException {
        String src = Files.readString(view("DJManagerView.java"));
        assertTrue(src.contains("setAccessibleRole(AccessibleRole.PARENT)"),
            "DJManagerView must call setAccessibleRole(AccessibleRole.PARENT)");
    }

    // ── PosterBuilderView ───────────────────────────────────────────────────

    @Test
    void posterBuilderView_passesBackgroundImageLabel() throws IOException {
        String src = Files.readString(view("PosterBuilderView.java"));
        assertTrue(src.contains("\"Background image\""),
            "PosterBuilderView must pass \"Background image\" as assetLabel to AssetThumbnail");
    }

    @Test
    void posterBuilderView_passesClubLogoLabel() throws IOException {
        String src = Files.readString(view("PosterBuilderView.java"));
        assertTrue(src.contains("\"Club logo\""),
            "PosterBuilderView must pass \"Club logo\" as assetLabel to AssetThumbnail");
    }

    @Test
    void posterBuilderView_passesQrCodeLabel() throws IOException {
        String src = Files.readString(view("PosterBuilderView.java"));
        assertTrue(src.contains("\"QR code\""),
            "PosterBuilderView must pass \"QR code\" as assetLabel to AssetThumbnail");
    }
}
