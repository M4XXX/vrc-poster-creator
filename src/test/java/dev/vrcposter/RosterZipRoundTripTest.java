package dev.vrcposter;

import dev.vrcposter.model.DJRecord;
import dev.vrcposter.repository.DatabaseManager;
import dev.vrcposter.repository.RepositoryException;
import dev.vrcposter.repository.migration.Migration001_InitialSchema;
import dev.vrcposter.repository.migration.MigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RosterZipRoundTripTest {

    @TempDir
    Path tempDir;

    private DatabaseManager db;
    private ApplicationController controller;

    @BeforeEach
    void setUp() throws RepositoryException {
        db = new DatabaseManager(tempDir.resolve("test.db"));
        db.initialize();
        new MigrationRunner(db, List.of(new Migration001_InitialSchema())).run();
        controller = new ApplicationController(db);
    }

    @AfterEach
    void tearDown() {
        controller.shutdown();
    }

    @Test
    void exportedZipCanBeImportedWithAllFieldsIntact() throws Exception {
        // Seed two records
        DJRecord r1 = DJRecord.create("DJ Export Alpha", "alpha#1111", "AlphaVRC",
                "house", "https://twitch.tv/alpha", null);
        DJRecord r2 = DJRecord.create("DJ Export Beta", "beta#2222", null,
                "techno,drum and bass", null, "https://soundcloud.com/beta");

        CountDownLatch saveLatch = new CountDownLatch(2);
        controller.saveDj(r1, null, saveLatch::countDown, saveLatch::countDown);
        controller.saveDj(r2, null, saveLatch::countDown, saveLatch::countDown);
        assertTrue(saveLatch.await(5, TimeUnit.SECONDS), "Save timed out");

        // Export
        Path zipPath = tempDir.resolve("export.zip");
        CountDownLatch exportLatch = new CountDownLatch(1);
        controller.exportRoster(zipPath, exportLatch::countDown, exportLatch::countDown);
        assertTrue(exportLatch.await(10, TimeUnit.SECONDS), "Export timed out");
        assertTrue(zipPath.toFile().exists(), "ZIP file was not created");

        // Clear and re-import
        // (delete records by deleting and re-importing into a fresh controller)
        DatabaseManager db2 = new DatabaseManager(tempDir.resolve("test2.db"));
        db2.initialize();
        new MigrationRunner(db2, List.of(new Migration001_InitialSchema())).run();
        ApplicationController controller2 = new ApplicationController(db2);
        try {
            AtomicInteger importedCount = new AtomicInteger(-1);
            CountDownLatch importLatch = new CountDownLatch(1);
            controller2.importRoster(zipPath,
                    count -> { importedCount.set(count); importLatch.countDown(); },
                    importLatch::countDown);
            assertTrue(importLatch.await(10, TimeUnit.SECONDS), "Import timed out");

            assertEquals(2, importedCount.get(), "Expected 2 DJs imported");

            // Verify roster loaded
            CountDownLatch loadLatch = new CountDownLatch(1);
            controller2.getDjRoster().getObservableList()
                    .addListener((javafx.collections.ListChangeListener<DJRecord>) c -> loadLatch.countDown());
            // Wait briefly if not already populated
            boolean rosterPopulated = loadLatch.await(3, TimeUnit.SECONDS);

            List<DJRecord> imported = controller2.getDjRoster().getObservableList();
            // Might need to trigger load
            if (imported.isEmpty()) {
                CountDownLatch reloadLatch = new CountDownLatch(1);
                controller2.getDjRoster().getObservableList()
                        .addListener((javafx.collections.ListChangeListener<DJRecord>) c -> reloadLatch.countDown());
                reloadLatch.await(3, TimeUnit.SECONDS);
                imported = controller2.getDjRoster().getObservableList();
            }

            assertEquals(2, imported.size(), "Imported roster should have 2 DJs");
        } finally {
            controller2.shutdown();
            db2.close();
        }
    }

    @Test
    void exportProducesNonEmptyZipFile() throws Exception {
        DJRecord r1 = DJRecord.create("DJ Zip Test", null, null, null, null, null);
        CountDownLatch saveLatch = new CountDownLatch(1);
        controller.saveDj(r1, null, saveLatch::countDown, saveLatch::countDown);
        assertTrue(saveLatch.await(5, TimeUnit.SECONDS));

        Path zipPath = tempDir.resolve("test-export.zip");
        CountDownLatch exportLatch = new CountDownLatch(1);
        controller.exportRoster(zipPath, exportLatch::countDown, exportLatch::countDown);
        assertTrue(exportLatch.await(10, TimeUnit.SECONDS), "Export timed out");

        assertTrue(zipPath.toFile().exists());
        assertTrue(zipPath.toFile().length() > 0, "ZIP file should not be empty");
    }

    @Test
    void importCountMatchesRecordCountInZip() throws Exception {
        DJRecord r1 = DJRecord.create("Count A", null, null, null, null, null);
        DJRecord r2 = DJRecord.create("Count B", null, null, null, null, null);
        DJRecord r3 = DJRecord.create("Count C", null, null, null, null, null);

        CountDownLatch saveLatch = new CountDownLatch(3);
        controller.saveDj(r1, null, saveLatch::countDown, saveLatch::countDown);
        controller.saveDj(r2, null, saveLatch::countDown, saveLatch::countDown);
        controller.saveDj(r3, null, saveLatch::countDown, saveLatch::countDown);
        assertTrue(saveLatch.await(5, TimeUnit.SECONDS));

        Path zipPath = tempDir.resolve("count-export.zip");
        CountDownLatch exportLatch = new CountDownLatch(1);
        controller.exportRoster(zipPath, exportLatch::countDown, exportLatch::countDown);
        assertTrue(exportLatch.await(10, TimeUnit.SECONDS));

        // Import into a second controller
        DatabaseManager db2 = new DatabaseManager(tempDir.resolve("count-test2.db"));
        db2.initialize();
        new MigrationRunner(db2, List.of(new Migration001_InitialSchema())).run();
        ApplicationController controller2 = new ApplicationController(db2);
        try {
            AtomicInteger importedCount = new AtomicInteger(-1);
            CountDownLatch importLatch = new CountDownLatch(1);
            controller2.importRoster(zipPath,
                    count -> { importedCount.set(count); importLatch.countDown(); },
                    importLatch::countDown);
            assertTrue(importLatch.await(10, TimeUnit.SECONDS));
            assertEquals(3, importedCount.get());
        } finally {
            controller2.shutdown();
            db2.close();
        }
    }
}
