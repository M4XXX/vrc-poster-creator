package dev.vrcposter.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.junit.jupiter.api.Assertions.*;

class ImageIngesterTest {

    @TempDir
    Path tempDir;

    private Path createTestImage(String filename, int w, int h, String format) throws IOException {
        Path file = tempDir.resolve(filename);
        BufferedImage img = new BufferedImage(w, h, TYPE_INT_ARGB);
        ImageIO.write(img, format, file.toFile());
        return file;
    }

    @Test
    void imageExactlyAtBoundsIsReturnedUnchanged() throws IOException {
        Path img = createTestImage("exact.png", 100, 200, "png");
        Optional<BufferedImage> result = ImageIngester.load(img, 100, 200);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getWidth());
        assertEquals(200, result.get().getHeight());
    }

    @Test
    void imageLargerThanBoundsIsScaledDown() throws IOException {
        Path img = createTestImage("large.png", 3840, 2160, "png");
        Optional<BufferedImage> result = ImageIngester.load(img, 1080, 1920);
        assertTrue(result.isPresent());
        assertTrue(result.get().getWidth() <= 1080,
            "Width should be <= maxWidth after scaling");
        assertTrue(result.get().getHeight() <= 1920,
            "Height should be <= maxHeight after scaling");
    }

    @Test
    void imageSmallerThanBoundsIsNotUpscaled() throws IOException {
        Path img = createTestImage("small.png", 50, 80, "png");
        Optional<BufferedImage> result = ImageIngester.load(img, 1080, 1920);
        assertTrue(result.isPresent());
        assertEquals(50, result.get().getWidth(), "Should not upscale width");
        assertEquals(80, result.get().getHeight(), "Should not upscale height");
    }

    @Test
    void aspectRatioIsPreservedWithinRoundingTolerance() throws IOException {
        // 3840×2160 is 16:9 landscape; maxW=1080, maxH=1920 → width-limited
        Path img = createTestImage("wide.png", 3840, 2160, "png");
        Optional<BufferedImage> result = ImageIngester.load(img, 1080, 1920);
        assertTrue(result.isPresent());
        BufferedImage out = result.get();
        double expectedRatio = 3840.0 / 2160.0;
        double actualRatio = (double) out.getWidth() / out.getHeight();
        assertEquals(expectedRatio, actualRatio, 0.02,
            "Aspect ratio must be preserved within rounding tolerance");
    }

    @Test
    void nonExistentFileReturnsEmpty() {
        Path missing = tempDir.resolve("does-not-exist.png");
        Optional<BufferedImage> result = ImageIngester.load(missing, 1080, 1920);
        assertFalse(result.isPresent(),
            "Non-existent file should return Optional.empty()");
    }

    @Test
    void unsupportedFormatFileReturnsEmpty() throws IOException {
        Path textFile = tempDir.resolve("notanimage.txt");
        Files.writeString(textFile, "this is not an image");
        Optional<BufferedImage> result = ImageIngester.load(textFile, 1080, 1920);
        assertFalse(result.isPresent(),
            "Unsupported/corrupt file should return Optional.empty()");
    }

    @Test
    void resultIsNeverNull() throws IOException {
        Path img = createTestImage("valid.png", 200, 200, "png");
        Optional<BufferedImage> result = ImageIngester.load(img, 1080, 1920);
        assertNotNull(result, "load() must never return null — always Optional");
    }

    @Test
    void jpegImageLoadsSuccessfully() throws IOException {
        // JPEG does not support alpha; must use TYPE_INT_RGB or ImageIO.write() returns false
        Path file = tempDir.resolve("test.jpg");
        BufferedImage rgb = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(rgb, "jpg", file.toFile());
        Optional<BufferedImage> result = ImageIngester.load(file, 1080, 1920);
        assertTrue(result.isPresent(), "JPEG image should load successfully");
        assertEquals(640, result.get().getWidth(), "JPEG width should be unchanged (within bounds)");
        assertEquals(480, result.get().getHeight(), "JPEG height should be unchanged (within bounds)");
    }
}
