package dev.vrcposter.service;

import dev.vrcposter.renderer.PosterRenderer;
import dev.vrcposter.renderer.PosterSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PosterExportService {

    private static final Logger log = LoggerFactory.getLogger(PosterExportService.class);

    public void exportJpeg(PosterSnapshot snapshot, Path destPath) throws ServiceException {
        export(snapshot, destPath, "jpg");
    }

    public void exportPng(PosterSnapshot snapshot, Path destPath) throws ServiceException {
        export(snapshot, destPath, "png");
    }

    private void export(PosterSnapshot snapshot, Path destPath, String format) throws ServiceException {
        String fmtLabel = "jpg".equals(format) ? "JPEG" : "PNG";
        Path temp = null;
        try {
            Path parent = destPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tempParent = (parent != null) ? parent : Path.of(".");
            temp = Files.createTempFile(tempParent, ".export-", ".tmp");
            BufferedImage image = PosterRenderer.render(snapshot, 1080, 1920);
            // JPEG does not support alpha — convert ARGB canvas to RGB before writing.
            if ("jpg".equals(format)) {
                BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                try { g.drawImage(image, 0, 0, null); } finally { g.dispose(); }
                image = rgb;
            }
            boolean wrote = ImageIO.write(image, format, temp.toFile());
            if (!wrote) {
                throw new IOException("ImageIO found no writer for format: " + format);
            }
            Files.move(temp, destPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Poster exported as {} to {}", fmtLabel, destPath);
        } catch (Exception e) {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException cleanupEx) {
                    log.warn("Could not delete temp export file: {}", temp, cleanupEx);
                }
            }
            throw new ServiceException(
                    "Could not save poster as " + fmtLabel + " — check disk space and write permissions.", e);
        }
    }
}
