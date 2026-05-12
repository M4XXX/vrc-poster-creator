package dev.vrcposter.repository;

import dev.vrcposter.model.DJRecord;
import dev.vrcposter.repository.migration.Migration001_InitialSchema;
import dev.vrcposter.repository.migration.MigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DJRepositoryTest {

    @TempDir
    Path tempDir;

    private DatabaseManager db;
    private DJRepository repo;

    @BeforeEach
    void setUp() throws RepositoryException {
        db = new DatabaseManager(tempDir.resolve("test.db"));
        db.initialize();
        new MigrationRunner(db, List.of(new Migration001_InitialSchema())).run();
        repo = new SQLiteDJRepository(db);
    }

    @AfterEach
    void tearDown() throws RepositoryException {
        db.close();
    }

    @Test
    void saveAndFindByIdReturnsRecord() throws RepositoryException {
        DJRecord record = DJRecord.create("DJ Alpha", "alpha#1234", "AlphaVRC",
                "house,techno", "https://twitch.tv/alpha", null);
        repo.save(record);

        Optional<DJRecord> found = repo.findById(record.getId());
        assertTrue(found.isPresent());
        assertEquals(record.getId(), found.get().getId());
        assertEquals("DJ Alpha", found.get().getArtistName());
        assertEquals("alpha#1234", found.get().getDiscordName());
        assertEquals("AlphaVRC", found.get().getVrcName());
        assertEquals("house,techno", found.get().getGenres());
        assertEquals("https://twitch.tv/alpha", found.get().getStreamLink1());
        assertNull(found.get().getStreamLink2());
        assertNull(found.get().getLogoFilename());
    }

    @Test
    void saveAndFindAllReturnsRecord() throws RepositoryException {
        DJRecord record = DJRecord.create("DJ Beta", null, null, null, null, null);
        repo.save(record);

        List<DJRecord> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("DJ Beta", all.get(0).getArtistName());
    }

    @Test
    void updateChangesFieldsAndUpdatedAt() throws RepositoryException {
        DJRecord original = DJRecord.create("DJ Gamma", null, null, null, null, null);
        repo.save(original);
        long originalUpdatedAt = repo.findById(original.getId()).get().getUpdatedAt();

        DJRecord updated = new DJRecord(original.getId(), "DJ Gamma Updated",
                "gamma#9999", null, null, null, null, null,
                original.getCreatedAt(), original.getUpdatedAt());
        repo.update(updated);

        Optional<DJRecord> found = repo.findById(original.getId());
        assertTrue(found.isPresent());
        assertEquals("DJ Gamma Updated", found.get().getArtistName());
        assertEquals("gamma#9999", found.get().getDiscordName());
        assertTrue(found.get().getUpdatedAt() >= originalUpdatedAt);
    }

    @Test
    void deleteRemovesFromFindAll() throws RepositoryException {
        DJRecord record = DJRecord.create("DJ Delta", null, null, null, null, null);
        repo.save(record);
        repo.delete(record.getId());

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void findByIdAfterDeleteReturnsEmpty() throws RepositoryException {
        DJRecord record = DJRecord.create("DJ Epsilon", null, null, null, null, null);
        repo.save(record);
        repo.delete(record.getId());

        assertFalse(repo.findById(record.getId()).isPresent());
    }

    @Test
    void findByNameLikeMatchesSubstring() throws RepositoryException {
        DJRecord a = DJRecord.create("DJ Finder", null, null, null, null, null);
        DJRecord b = DJRecord.create("Unrelated Artist", null, null, null, null, null);
        repo.save(a);
        repo.save(b);

        List<DJRecord> results = repo.findByNameLike("Finder");
        assertEquals(1, results.size());
        assertEquals("DJ Finder", results.get(0).getArtistName());
    }

    @Test
    void findByNameLikeIsCaseInsensitive() throws RepositoryException {
        DJRecord record = DJRecord.create("DJ Alpha", null, null, null, null, null);
        repo.save(record);

        List<DJRecord> results = repo.findByNameLike("dj");
        assertEquals(1, results.size());
        assertEquals("DJ Alpha", results.get(0).getArtistName());
    }

    @Test
    void persistenceAcrossReopenedDb() throws RepositoryException {
        Path dbFile = tempDir.resolve("test.db");

        DatabaseManager db1 = new DatabaseManager(dbFile);
        db1.initialize();
        new MigrationRunner(db1, List.of(new Migration001_InitialSchema())).run();
        DJRepository repo1 = new SQLiteDJRepository(db1);
        DJRecord record = DJRecord.create("DJ Persist", null, null, null, null, null);
        repo1.save(record);
        db1.close();

        DatabaseManager db2 = new DatabaseManager(dbFile);
        db2.initialize();
        new MigrationRunner(db2, List.of(new Migration001_InitialSchema())).run();
        DJRepository repo2 = new SQLiteDJRepository(db2);
        List<DJRecord> all = repo2.findAll();
        db2.close();

        assertEquals(1, all.size());
        assertEquals("DJ Persist", all.get(0).getArtistName());
        assertEquals(record.getId(), all.get(0).getId());
    }
}
