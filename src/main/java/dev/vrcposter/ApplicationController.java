package dev.vrcposter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dev.vrcposter.model.DJRecord;
import dev.vrcposter.model.DJRoster;
import dev.vrcposter.model.PosterModel;
import dev.vrcposter.model.TimeslotEntry;
import dev.vrcposter.renderer.PreviewManager;
import dev.vrcposter.renderer.PosterSnapshot;
import dev.vrcposter.repository.DatabaseManager;
import dev.vrcposter.repository.JsonSessionRepository;
import dev.vrcposter.repository.SessionData;
import dev.vrcposter.repository.SessionRepository;
import dev.vrcposter.service.PosterExportService;
import dev.vrcposter.service.ServiceException;
import dev.vrcposter.repository.DJRepository;
import dev.vrcposter.repository.RepositoryException;
import dev.vrcposter.repository.SQLiteDJRepository;
import dev.vrcposter.util.AppPaths;
import dev.vrcposter.util.DebounceUtil;
import dev.vrcposter.util.ImageIngester;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Single entry point for all model mutations. Owns the shared executor and debouncer.
 * Views must never write to model objects directly — all mutations route through here.
 */
public class ApplicationController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    private final DatabaseManager databaseManager;
    private final ScheduledExecutorService executor;
    private final DebounceUtil debouncer;
    private final DJRepository djRepository;
    private final DJRoster djRoster;
    private final PosterModel posterModel = new PosterModel();
    private final PreviewManager previewManager;
    private final PosterExportService posterExportService;
    private final SessionRepository sessionRepository;
    private Consumer<String> saveStateCallback;

    public ApplicationController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.executor = Executors.newScheduledThreadPool(2);
        this.debouncer = new DebounceUtil(executor, 300);
        this.djRepository = new SQLiteDJRepository(databaseManager);
        this.djRoster = new DJRoster();
        this.previewManager = new PreviewManager(posterModel, executor);
        this.posterExportService = new PosterExportService();
        this.sessionRepository = new JsonSessionRepository();
        wirePosterModelListeners();
        restoreOrInitSession();
        log.info("ApplicationController initialized");
    }

    /** Shared executor for all background I/O and rendering tasks. Never create a new one in components. */
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /** Shared debouncer (300ms default). Use for preview re-render and auto-save triggers. */
    public DebounceUtil getDebouncer() {
        return debouncer;
    }

    /** Provides access to the database for repository construction in feature stories. */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /** Observable DJ roster — bind ListView items to this. Never mutate directly from views. */
    public DJRoster getDjRoster() {
        return djRoster;
    }

    /** Observable poster model — bind controls to its properties. Never mutate directly from views. */
    public PosterModel getPosterModel() {
        return posterModel;
    }

    /** Preview coordinator — PosterBuilderView registers its callback here after creating the panel. */
    public PreviewManager getPreviewManager() {
        return previewManager;
    }

    // ── PosterModel text mutations (ARCH-8) ──────────────────────────────

    public void setPosterTitle(String value) { posterModel.setTitle(value); }

    public void setPosterSubtitle(String value) { posterModel.setSubtitle(value); }

    public void setPosterDate(String value) { posterModel.setDate(value); }

    // ── PosterModel asset ingest methods (ARCH-7, ARCH-8, ARCH-9) ────────

    public void ingestBackgroundImage(Path path, Consumer<BufferedImage> onSuccess, Runnable onError) {
        var task = new Task<Optional<BufferedImage>>() {
            @Override
            protected Optional<BufferedImage> call() {
                return ImageIngester.load(path, 72, 72);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Optional<BufferedImage> result = task.getValue();
            if (result.isEmpty()) {
                if (onError != null) onError.run();
            } else {
                posterModel.setBackgroundPath(path);
                if (onSuccess != null) onSuccess.accept(result.get());
            }
        }));
        task.setOnFailed(e -> {
            log.error("Background image ingest failed", task.getException());
            Platform.runLater(() -> { if (onError != null) onError.run(); });
        });
        executor.submit(task);
    }

    public void ingestClubLogoImage(Path path, Consumer<BufferedImage> onSuccess, Runnable onError) {
        var task = new Task<Optional<BufferedImage>>() {
            @Override
            protected Optional<BufferedImage> call() {
                return ImageIngester.load(path, 72, 72);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Optional<BufferedImage> result = task.getValue();
            if (result.isEmpty()) {
                if (onError != null) onError.run();
            } else {
                posterModel.setClubLogoPath(path);
                if (onSuccess != null) onSuccess.accept(result.get());
            }
        }));
        task.setOnFailed(e -> {
            log.error("Club logo image ingest failed", task.getException());
            Platform.runLater(() -> { if (onError != null) onError.run(); });
        });
        executor.submit(task);
    }

    public void ingestQrCodeImage(Path path, Consumer<BufferedImage> onSuccess, Runnable onError) {
        var task = new Task<Optional<BufferedImage>>() {
            @Override
            protected Optional<BufferedImage> call() {
                return ImageIngester.load(path, 72, 72);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Optional<BufferedImage> result = task.getValue();
            if (result.isEmpty()) {
                if (onError != null) onError.run();
            } else {
                posterModel.setQrCodePath(path);
                if (onSuccess != null) onSuccess.accept(result.get());
            }
        }));
        task.setOnFailed(e -> {
            log.error("QR code image ingest failed", task.getException());
            Platform.runLater(() -> { if (onError != null) onError.run(); });
        });
        executor.submit(task);
    }

    /** Loads an image for thumbnail display without setting any model property. */
    public void loadImageForThumbnail(Path path, Consumer<BufferedImage> onSuccess, Runnable onError) {
        Task<Optional<BufferedImage>> task = new Task<>() {
            @Override
            protected Optional<BufferedImage> call() {
                return ImageIngester.load(path, 72, 72);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Optional<BufferedImage> result = task.getValue();
            if (result.isPresent()) {
                if (onSuccess != null) onSuccess.accept(result.get());
            } else {
                if (onError != null) onError.run();
            }
        }));
        task.setOnFailed(e -> {
            log.error("Thumbnail load failed for {}", path, task.getException());
            Platform.runLater(() -> { if (onError != null) onError.run(); });
        });
        executor.submit(task);
    }

    /** Registers the callback that updates the status bar save indicator. Called by PosterBuilderView. */
    public void setSaveStateCallback(Consumer<String> callback) {
        this.saveStateCallback = callback;
    }

    private void wirePosterModelListeners() {
        Runnable trigger = () -> {
            debouncer.schedule("preview", this::onPreviewNeeded);
            debouncer.schedule("autosave", this::onAutosaveNeeded);
        };
        posterModel.titleProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.subtitleProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.dateProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.backgroundPathProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.clubLogoPathProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.qrCodePathProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.showAvatarHintProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.fontColorProperty().addListener((obs, o, n) -> trigger.run());
        posterModel.getTimeslots().addListener(
            (ListChangeListener<TimeslotEntry>) c -> trigger.run());
    }

    private void onPreviewNeeded() {
        // DebounceUtil fires on the executor thread; PosterSnapshot.of() requires the JAT, so marshal over.
        Platform.runLater(previewManager::requestRender);
    }

    private void onAutosaveNeeded() {
        // DebounceUtil fires on the executor thread; PosterModel Properties must be read on the JAT.
        Platform.runLater(this::persistSession);
    }

    private void persistSession() {
        // Must run on the JAT — reads JavaFX Property values before handing off to background task.
        notifySaveState("Saving…");
        String title = posterModel.getTitle();
        String subtitle = posterModel.getSubtitle();
        String date = posterModel.getDate();
        String bgPath = posterModel.getBackgroundPath() != null
                ? posterModel.getBackgroundPath().toAbsolutePath().toString() : null;
        String clubPath = posterModel.getClubLogoPath() != null
                ? posterModel.getClubLogoPath().toAbsolutePath().toString() : null;
        String qrPath = posterModel.getQrCodePath() != null
                ? posterModel.getQrCodePath().toAbsolutePath().toString() : null;

        List<SessionData.TimeslotData> slots = new ArrayList<>();
        for (TimeslotEntry ts : posterModel.getTimeslots()) {
            SessionData.TimeslotData td = new SessionData.TimeslotData();
            td.startTime = ts.getStartTime();
            td.assignedDjId = ts.getAssignedDjId();
            td.assignedDjArtistName = ts.getAssignedDjArtistName();
            td.genre = ts.getGenre();
            slots.add(td);
        }

        Color fc = posterModel.getFontColor();
        String fontColorHex = String.format("#%02X%02X%02X",
            (int)(fc.getRed() * 255), (int)(fc.getGreen() * 255), (int)(fc.getBlue() * 255));

        SessionData data = new SessionData();
        data.title = title;
        data.subtitle = subtitle;
        data.date = date;
        data.backgroundPath = bgPath;
        data.clubLogoPath = clubPath;
        data.qrCodePath = qrPath;
        data.fontColor = fontColorHex;
        data.timeslots = slots;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                sessionRepository.save(data);
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> notifySaveState("● Saved")));
        task.setOnFailed(e -> log.error("Auto-save failed — session may not persist", task.getException()));
        executor.submit(task);
    }

    private void notifySaveState(String text) {
        if (saveStateCallback != null) saveStateCallback.accept(text);
    }

    // ── Session restore (ARCH-9 startup exception) ───────────────────────

    private void restoreOrInitSession() {
        try {
            Optional<SessionData> loaded = sessionRepository.load();
            if (loaded.isEmpty()) {
                initDefaultTimeslots();
                return;
            }
            applySessionData(loaded.get());
            log.info("Session restored from {}", AppPaths.sessionJson());
        } catch (RepositoryException e) {
            log.warn("Session file corrupt or unreadable — starting with clean defaults", e);
            tryRenameCorruptSession();
            initDefaultTimeslots();
        }
    }

    private void applySessionData(SessionData data) {
        posterModel.setTitle(data.title != null ? data.title : "");
        posterModel.setSubtitle(data.subtitle != null ? data.subtitle : "");
        posterModel.setDate(data.date != null ? data.date : "");
        if (data.fontColor != null) {
            try { posterModel.setFontColor(Color.web(data.fontColor)); }
            catch (IllegalArgumentException ignored) { /* corrupt value — keep default white */ }
        }

        if (data.backgroundPath != null) {
            Path p = Path.of(data.backgroundPath);
            if (Files.exists(p)) posterModel.setBackgroundPath(p);
            else log.debug("Session restore: background not found at {}, skipping", p);
        }
        if (data.clubLogoPath != null) {
            Path p = Path.of(data.clubLogoPath);
            if (Files.exists(p)) posterModel.setClubLogoPath(p);
            else log.debug("Session restore: club logo not found at {}, skipping", p);
        }
        if (data.qrCodePath != null) {
            Path p = Path.of(data.qrCodePath);
            if (Files.exists(p)) posterModel.setQrCodePath(p);
            else log.debug("Session restore: QR code not found at {}, skipping", p);
        }

        List<SessionData.TimeslotData> slots = data.timeslots;
        if (slots == null || slots.isEmpty()) {
            initDefaultTimeslots();
            return;
        }
        for (SessionData.TimeslotData td : slots) {
            String time = td.startTime != null ? td.startTime : "";
            TimeslotEntry entry = TimeslotEntry.create(time);
            entry.setGenre(td.genre != null ? td.genre : "");
            if (td.assignedDjId != null) {
                entry.setAssignedDjId(td.assignedDjId);
                entry.setAssignedDjArtistName(td.assignedDjArtistName);
                try {
                    Optional<DJRecord> dj = djRepository.findById(td.assignedDjId);
                    if (dj.isPresent() && dj.get().getLogoFilename() != null) {
                        Path logoPath = AppPaths.logosDir().resolve(dj.get().getLogoFilename());
                        if (Files.exists(logoPath)) entry.setAssignedDjLogoPath(logoPath);
                    }
                } catch (RepositoryException ex) {
                    log.warn("Could not look up DJ {} during session restore — assignment kept without logo",
                            td.assignedDjId, ex);
                }
            }
            posterModel.getTimeslots().add(entry);
        }
    }

    private void tryRenameCorruptSession() {
        Path sessionPath = AppPaths.sessionJson();
        if (!Files.exists(sessionPath)) return;
        Path bakPath = sessionPath.resolveSibling("session.json.bak");
        try {
            Files.move(sessionPath, bakPath, StandardCopyOption.REPLACE_EXISTING);
            log.warn("Renamed corrupt session.json to session.json.bak");
        } catch (IOException e) {
            log.warn("Could not rename corrupt session.json to session.json.bak", e);
        }
    }

    // ── Timeslot management (ARCH-8) ─────────────────────────────────────

    public void initDefaultTimeslots() {
        List.of("9pm", "10pm", "11pm", "12am", "1am", "2am")
            .forEach(t -> posterModel.getTimeslots().add(TimeslotEntry.create(t)));
    }

    public void addTimeslot() {
        posterModel.getTimeslots().add(TimeslotEntry.create(""));
    }

    public void removeTimeslot(TimeslotEntry entry) {
        posterModel.getTimeslots().remove(entry);
    }

    public void assignDjToTimeslot(TimeslotEntry entry, DJRecord dj) {
        entry.setAssignedDjId(dj.getId());
        entry.setAssignedDjArtistName(dj.getArtistName());
        Path logoPath = dj.getLogoFilename() != null
            ? AppPaths.logosDir().resolve(dj.getLogoFilename()) : null;
        entry.setAssignedDjLogoPath(logoPath);
    }

    public void clearTimeslotAssignment(TimeslotEntry entry) {
        entry.clearAssignment();
    }

    public void setTimeslotStartTime(TimeslotEntry entry, String time) {
        entry.setStartTime(time);
    }

    public void setTimeslotGenre(TimeslotEntry entry, String genre) {
        entry.setGenre(genre != null ? genre : "");
    }

    public void swapTimeslotDjContent(int fromIndex, int toIndex) {
        var slots = posterModel.getTimeslots();
        TimeslotEntry a = slots.get(fromIndex);
        TimeslotEntry b = slots.get(toIndex);
        String tmpId = a.getAssignedDjId();
        String tmpName = a.getAssignedDjArtistName();
        Path tmpLogo = a.getAssignedDjLogoPath();
        String tmpGenre = a.getGenre();
        a.setAssignedDjId(b.getAssignedDjId());
        a.setAssignedDjArtistName(b.getAssignedDjArtistName());
        a.setAssignedDjLogoPath(b.getAssignedDjLogoPath());
        a.setGenre(b.getGenre());
        b.setAssignedDjId(tmpId);
        b.setAssignedDjArtistName(tmpName);
        b.setAssignedDjLogoPath(tmpLogo);
        b.setGenre(tmpGenre);
    }

    // ── Poster export (ARCH-9, FR31–FR33) ───────────────────────────────

    /**
     * Exports the current poster as JPEG or PNG on a background Task.
     * Must be called on the JAT — PosterSnapshot.of() reads JavaFX Properties here,
     * before the task is submitted to the executor.
     *
     * @param format    "jpeg" or "png"
     * @param destPath  destination file path chosen by the user via FileChooser
     */
    public void exportPoster(String format, Path destPath, Runnable onSuccess, Consumer<String> onFailure) {
        PosterSnapshot snapshot = PosterSnapshot.of(posterModel);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if ("jpeg".equalsIgnoreCase(format)) {
                    posterExportService.exportJpeg(snapshot, destPath);
                } else {
                    posterExportService.exportPng(snapshot, destPath);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            if (onSuccess != null) onSuccess.run();
        }));
        task.setOnFailed(e -> {
            log.error("Poster export failed", task.getException());
            Platform.runLater(() -> {
                if (onFailure != null) {
                    Throwable cause = task.getException();
                    String msg = (cause instanceof ServiceException)
                            ? cause.getMessage()
                            : "Unexpected error during export";
                    onFailure.accept(msg);
                }
            });
        });
        executor.submit(task);
    }

    /** Loads all DJ records from the database on a background thread and populates the DJRoster. */
    public void loadRoster() {
        Task<List<DJRecord>> task = new Task<>() {
            @Override
            protected List<DJRecord> call() throws Exception {
                return djRepository.findAll();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            djRoster.setAll(task.getValue());
            syncTimeslotsWithRoster();
        }));
        task.setOnFailed(e -> log.error("Failed to load DJ roster", task.getException()));
        executor.submit(task);
    }

    /**
     * Reconciles every assigned timeslot entry against the current roster.
     * Must be called on the JAT after the roster list has been refreshed.
     * - DJ updated: re-applies current name and logo path to the entry.
     * - DJ deleted: clears the assignment so the preview shows no stale data.
     */
    private void syncTimeslotsWithRoster() {
        var roster = djRoster.getObservableList();
        for (TimeslotEntry entry : posterModel.getTimeslots()) {
            String djId = entry.getAssignedDjId();
            if (djId == null || djId.isBlank()) continue;
            roster.stream()
                .filter(dj -> djId.equals(dj.getId()))
                .findFirst()
                .ifPresentOrElse(
                    dj -> assignDjToTimeslot(entry, dj),
                    () -> entry.clearAssignment()
                );
        }
    }

    public void saveDj(DJRecord record, Path logoSourcePath, Runnable onSuccess, Runnable onFailure) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DJRecord toSave = logoSourcePath != null ? writeLogo(record, logoSourcePath) : record;
                djRepository.save(toSave);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            Platform.runLater(ApplicationController.this::loadRoster);
            if (onSuccess != null) onSuccess.run();
        });
        task.setOnFailed(e -> {
            log.error("Failed to save DJ record", task.getException());
            if (onFailure != null) onFailure.run();
        });
        executor.submit(task);
    }

    public void updateDj(DJRecord record, Path logoSourcePath, String oldLogoFilename, Runnable onSuccess, Runnable onFailure) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DJRecord toUpdate = record;
                if (logoSourcePath != null) {
                    deleteOldLogo(oldLogoFilename);
                    toUpdate = writeLogo(record, logoSourcePath);
                }
                djRepository.update(toUpdate);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            Platform.runLater(ApplicationController.this::loadRoster);
            if (onSuccess != null) onSuccess.run();
        });
        task.setOnFailed(e -> {
            log.error("Failed to update DJ record", task.getException());
            if (onFailure != null) onFailure.run();
        });
        executor.submit(task);
    }

    public void deleteDj(String djId, String logoFilename, Runnable onSuccess, Runnable onFailure) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                djRepository.delete(djId);
                deleteOldLogo(logoFilename);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            Platform.runLater(ApplicationController.this::loadRoster);
            if (onSuccess != null) onSuccess.run();
        });
        task.setOnFailed(e -> {
            log.error("Failed to delete DJ record", task.getException());
            if (onFailure != null) onFailure.run();
        });
        executor.submit(task);
    }

    public void exportRoster(Path destPath, Runnable onSuccess, Runnable onFailure) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<DJRecord> records = djRepository.findAll();
                ObjectMapper mapper = new ObjectMapper();
                try (var zipOut = new ZipOutputStream(Files.newOutputStream(destPath))) {
                    zipOut.putNextEntry(new ZipEntry("roster.json"));
                    zipOut.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(records));
                    zipOut.closeEntry();
                    for (DJRecord r : records) {
                        if (r.getLogoFilename() == null) continue;
                        Path logoPath = AppPaths.logosDir().resolve(r.getLogoFilename());
                        if (!Files.exists(logoPath)) continue;
                        zipOut.putNextEntry(new ZipEntry("logos/" + r.getLogoFilename()));
                        Files.copy(logoPath, zipOut);
                        zipOut.closeEntry();
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            if (onSuccess != null) onSuccess.run();
        });
        task.setOnFailed(e -> {
            log.error("Failed to export roster", task.getException());
            if (onFailure != null) onFailure.run();
        });
        executor.submit(task);
    }

    public void importRoster(Path sourcePath, Consumer<Integer> onSuccess, Runnable onFailure) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                byte[] rosterJson = null;
                Path tempImportDir = Files.createTempDirectory("vrc-import-");
                try {
                    try (var zipIn = new ZipInputStream(Files.newInputStream(sourcePath))) {
                        ZipEntry entry;
                        while ((entry = zipIn.getNextEntry()) != null) {
                            if ("roster.json".equals(entry.getName())) {
                                rosterJson = zipIn.readAllBytes();
                            } else if (entry.getName().startsWith("logos/") && !entry.isDirectory()) {
                                String rawName = entry.getName().substring("logos/".length());
                                String filename = Path.of(rawName).getFileName().toString();
                                Path tmp = tempImportDir.resolve(filename);
                                Files.copy(zipIn, tmp, StandardCopyOption.REPLACE_EXISTING);
                            }
                            zipIn.closeEntry();
                        }
                    }
                    if (rosterJson == null) {
                        throw new RepositoryException("ZIP missing roster.json", null);
                    }

                    List<DJRecord> parsedRecords = new ArrayList<>();
                    JsonNode rootNode = mapper.readTree(rosterJson);
                    if (!(rootNode instanceof ArrayNode)) {
                        throw new RepositoryException("roster.json must be a JSON array", null);
                    }
                    ArrayNode array = (ArrayNode) rootNode;
                    for (JsonNode node : array) {
                        String id         = node.path("id").asText(null);
                        String artistName = node.path("artistName").asText(null);
                        if (id == null || id.isBlank() || artistName == null || artistName.isBlank()) {
                            throw new RepositoryException(
                                    "Invalid record in roster.json: missing id or artistName", null);
                        }
                        String discordName  = node.path("discordName").asText(null);
                        String vrcName      = node.path("vrcName").asText(null);
                        String genres       = node.path("genres").asText(null);
                        String streamLink1  = node.path("streamLink1").asText(null);
                        String streamLink2  = node.path("streamLink2").asText(null);
                        String rawLogoFilename = node.path("logoFilename").asText(null);
                    String logoFilename = (rawLogoFilename != null && !rawLogoFilename.isBlank())
                            ? Path.of(rawLogoFilename).getFileName().toString()
                            : null;
                        long createdAt      = node.path("createdAt").asLong(System.currentTimeMillis());
                        long updatedAt      = System.currentTimeMillis();
                        parsedRecords.add(new DJRecord(id, artistName, discordName, vrcName,
                                genres, streamLink1, streamLink2, logoFilename, createdAt, updatedAt));
                    }

                    // Copy logos from temp dir to permanent logosDir BEFORE DB transaction
                    Files.createDirectories(AppPaths.logosDir());
                    for (DJRecord r : parsedRecords) {
                        if (r.getLogoFilename() == null) continue;
                        Path tmpLogo = tempImportDir.resolve(r.getLogoFilename());
                        if (Files.exists(tmpLogo)) {
                            Files.copy(tmpLogo, AppPaths.logosDir().resolve(r.getLogoFilename()),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    // Atomic upsert via SQL transaction
                    Connection conn = databaseManager.getConnection();
                    conn.setAutoCommit(false);
                    try {
                        for (DJRecord r : parsedRecords) {
                            Optional<DJRecord> existing = djRepository.findById(r.getId());
                            if (existing.isPresent()) {
                                djRepository.update(r);
                            } else {
                                djRepository.save(r);
                            }
                        }
                        conn.commit();
                    } catch (Exception e) {
                        try { conn.rollback(); } catch (SQLException rollbackEx) {
                            log.warn("Rollback failed during import error handling", rollbackEx);
                        }
                        throw e;
                    } finally {
                        try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                    }

                    return parsedRecords.size();
                } finally {
                    try (var walk = Files.walk(tempImportDir)) {
                        walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                    } catch (IOException cleanupEx) {
                        log.warn("Could not clean up temp import directory", cleanupEx);
                    }
                }
            }
        };
        task.setOnSucceeded(e -> {
            Platform.runLater(ApplicationController.this::loadRoster);
            if (onSuccess != null) onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            log.error("Failed to import roster", task.getException());
            if (onFailure != null) onFailure.run();
        });
        executor.submit(task);
    }

    private DJRecord writeLogo(DJRecord record, Path logoSourcePath) throws Exception {
        String name = logoSourcePath.getFileName().toString().toLowerCase();
        String ext = (name.endsWith(".jpg") || name.endsWith(".jpeg")) ? "jpg" : "png";
        String ioFormat = ext.equals("jpg") ? "jpeg" : "png"; // ImageIO requires "jpeg" not "jpg"
        Path dest = AppPaths.logoFile(record.getId(), ext);
        Files.createDirectories(dest.getParent());
        Optional<BufferedImage> img = ImageIngester.load(logoSourcePath, 200, 200);
        if (img.isEmpty()) {
            throw new RepositoryException("Cannot ingest logo: " + logoSourcePath, null);
        }
        ImageIO.write(img.get(), ioFormat, dest.toFile());
        return record.withLogoFilename(dest.getFileName().toString());
    }

    private void deleteOldLogo(String oldLogoFilename) {
        if (oldLogoFilename == null) return;
        try {
            Files.deleteIfExists(AppPaths.logosDir().resolve(oldLogoFilename));
        } catch (IOException e) {
            log.warn("Could not delete old logo: {}", oldLogoFilename, e);
        }
    }

    /** Must be called on application exit (wired to App.stop()). */
    public void shutdown() {
        log.info("ApplicationController shutdown");
        debouncer.cancelAll();
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Shutdown interrupted while waiting for executor to terminate");
        }
        try {
            databaseManager.close();
        } catch (Exception e) {
            log.error("Error closing database during shutdown", e);
        }
    }
}
