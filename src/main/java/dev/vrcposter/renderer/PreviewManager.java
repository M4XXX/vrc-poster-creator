package dev.vrcposter.renderer;

import dev.vrcposter.model.PosterModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * Coordinates debounced background preview rendering.
 * requestRender() MUST be called on the JavaFX Application Thread —
 * it snapshots the model there, then submits the render to the executor.
 */
public class PreviewManager {
    private static final Logger log = LoggerFactory.getLogger(PreviewManager.class);

    private static final int PREVIEW_WIDTH  = 540;
    private static final int PREVIEW_HEIGHT = 960;

    private final PosterModel posterModel;
    private final ScheduledExecutorService executor;
    private volatile Consumer<WritableImage> previewCallback;

    public PreviewManager(PosterModel posterModel, ScheduledExecutorService executor) {
        this.posterModel = posterModel;
        this.executor    = executor;
    }

    /** Called from PosterBuilderView after PosterPreviewPanel is created. Thread-safe (volatile write). */
    public void setPreviewCallback(Consumer<WritableImage> callback) {
        this.previewCallback = callback;
    }

    /**
     * Snapshots the PosterModel on the JAT, then renders at 540×960 on the executor thread
     * and delivers the result back to the JAT via Platform.runLater().
     * Must be called on the JavaFX Application Thread.
     */
    public void requestRender() {
        PosterSnapshot snapshot = PosterSnapshot.of(posterModel); // JAT — reads all properties safely
        executor.submit(() -> {
            try {
                BufferedImage bi = PosterRenderer.render(snapshot, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                WritableImage wi = SwingFXUtils.toFXImage(bi, null);
                Consumer<WritableImage> cb = previewCallback;
                if (cb != null) {
                    Platform.runLater(() -> cb.accept(wi));
                }
            } catch (Exception e) {
                log.warn("Preview render failed", e);
            }
        });
    }
}
