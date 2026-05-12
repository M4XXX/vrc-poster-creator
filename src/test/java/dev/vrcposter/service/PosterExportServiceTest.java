package dev.vrcposter.service;

import dev.vrcposter.model.PosterModel;
import dev.vrcposter.renderer.PosterSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PosterExportServiceTest {

    private final PosterExportService service = new PosterExportService();

    private PosterSnapshot blankSnapshot() {
        return PosterSnapshot.of(new PosterModel());
    }

    @Test
    void exportJpeg_writesFileAtCorrectDimensions(@TempDir Path tempDir) throws Exception {
        Path dest = tempDir.resolve("poster.jpg");

        service.exportJpeg(blankSnapshot(), dest);

        assertTrue(Files.exists(dest), "JPEG file must exist after export");
        BufferedImage img = ImageIO.read(dest.toFile());
        assertNotNull(img, "JPEG must be readable by ImageIO");
        assertEquals(1080, img.getWidth(), "JPEG width must be exactly 1080");
        assertEquals(1920, img.getHeight(), "JPEG height must be exactly 1920");
    }

    @Test
    void exportPng_writesFileAtCorrectDimensions(@TempDir Path tempDir) throws Exception {
        Path dest = tempDir.resolve("poster.png");

        service.exportPng(blankSnapshot(), dest);

        assertTrue(Files.exists(dest), "PNG file must exist after export");
        BufferedImage img = ImageIO.read(dest.toFile());
        assertNotNull(img, "PNG must be readable by ImageIO");
        assertEquals(1080, img.getWidth(), "PNG width must be exactly 1080");
        assertEquals(1920, img.getHeight(), "PNG height must be exactly 1920");
    }

    @Test
    void export_failure_throwsServiceExceptionAndLeavesNoPartialFile(@TempDir Path tempDir) throws IOException {
        // Make the intended parent a regular file so Files.createDirectories() fails,
        // causing a ServiceException with no temp file created or left behind.
        Path fakeParent = tempDir.resolve("notadir");
        Files.createFile(fakeParent); // regular file, not a directory
        Path dest = fakeParent.resolve("poster.jpg");

        long filesBefore = Files.list(tempDir).count();

        assertThrows(ServiceException.class, () -> service.exportJpeg(blankSnapshot(), dest));

        long filesAfter = Files.list(tempDir).count();
        assertEquals(filesBefore, filesAfter, "No extra files should remain after a failed export");
    }
}
