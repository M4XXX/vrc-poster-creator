package dev.vrcposter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class ImageIngester {
    private static final Logger log = LoggerFactory.getLogger(ImageIngester.class);

    private ImageIngester() {}

    /**
     * Loads a JPEG or PNG image from {@code path}, scales it to fit within
     * {@code maxWidth} × {@code maxHeight} while preserving aspect ratio, and
     * returns the result.
     *
     * <p>The original full-resolution image is discarded from heap immediately
     * after scaling — only the down-sampled result is retained (NFR4).
     *
     * <p><strong>Caller contract (AC5, AC6, NFR5, NFR11):</strong> This method
     * performs blocking I/O and MUST be called from a background thread.
     * All callers triggered by a user action MUST wrap this in a
     * {@code javafx.concurrent.Task<Optional<BufferedImage>>} submitted to
     * {@code controller.getExecutor()}. On success, deliver the result to the
     * UI via {@code Platform.runLater()}. If the result is empty, display a
     * user-visible error message.
     *
     * <p>Usage pattern (copy verbatim into each consuming story):
     * <pre>{@code
     * var task = new Task<Optional<BufferedImage>>() {
     *     @Override
     *     protected Optional<BufferedImage> call() {
     *         return ImageIngester.load(selectedPath, maxW, maxH);
     *     }
     * };
     * task.setOnSucceeded(e -> Platform.runLater(() -> {
     *     Optional<BufferedImage> img = task.getValue();
     *     if (img.isEmpty()) {
     *         showErrorLabel("Image could not be loaded. Use JPEG or PNG files only.");
     *     } else {
     *         controller.setImage(img.get());
     *     }
     * }));
     * task.setOnFailed(e -> log.error("Image load task failed", task.getException()));
     * controller.getExecutor().submit(task);
     * }</pre>
     *
     * @param path      path to a JPEG or PNG file; {@code toRealPath()} validates
     *                  existence and accessibility
     * @param maxWidth  maximum width of the returned image in pixels
     * @param maxHeight maximum height of the returned image in pixels
     * @return scaled image, or {@link Optional#empty()} if the file cannot be read
     */
    public static Optional<BufferedImage> load(Path path, int maxWidth, int maxHeight) {
        Path realPath;
        try {
            realPath = path.toRealPath();
        } catch (IOException e) {
            log.warn("Image path not accessible: {}", path, e);
            return Optional.empty();
        }

        BufferedImage original;
        try {
            original = ImageIO.read(realPath.toFile());
        } catch (IOException e) {
            log.warn("Failed to read image: {}", realPath, e);
            return Optional.empty();
        }

        if (original == null) {
            log.warn("Unsupported image format or corrupt file: {}", realPath);
            return Optional.empty();
        }

        BufferedImage result = scale(original, maxWidth, maxHeight);
        log.debug("Loaded image: {}x{} → {}x{}", original.getWidth(), original.getHeight(),
            result.getWidth(), result.getHeight());
        return Optional.of(result);
    }

    private static BufferedImage scale(BufferedImage src, int maxWidth, int maxHeight) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        if (srcW <= maxWidth && srcH <= maxHeight) {
            return src;
        }
        double scale = Math.min((double) maxWidth / srcW, (double) maxHeight / srcH);
        int targetW = (int) Math.round(srcW * scale);
        int targetH = (int) Math.round(srcH * scale);

        int type = src.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage scaled = new BufferedImage(targetW, targetH, type);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, targetW, targetH, null);
        } finally {
            g.dispose();
        }
        return scaled;
    }
}
