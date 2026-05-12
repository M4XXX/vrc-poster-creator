package dev.vrcposter.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {

    @TempDir
    Path tempDir;

    private DatabaseManager freshDb() throws RepositoryException {
        DatabaseManager db = new DatabaseManager(tempDir.resolve("test.db"));
        db.initialize();
        return db;
    }

    @Test
    void initializeCreatesDbFile() throws Exception {
        Path dbPath = tempDir.resolve("test.db");
        DatabaseManager db = new DatabaseManager(dbPath);
        db.initialize();
        assertTrue(dbPath.toFile().exists(), "Database file should be created on disk");
        db.close();
    }

    @Test
    void initializeCreatesSchemaVersionTable() throws Exception {
        DatabaseManager db = freshDb();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name FROM sqlite_master WHERE type='table' AND name='schema_version'")) {
            assertTrue(rs.next(), "schema_version table must exist after initialization");
        }
        db.close();
    }

    @Test
    void initialSchemaVersionIsZero() throws Exception {
        DatabaseManager db = freshDb();
        assertEquals(0, db.getSchemaVersion(), "Initial schema version must be 0");
        db.close();
    }

    @Test
    void setAndGetSchemaVersionRoundTrip() throws Exception {
        DatabaseManager db = freshDb();
        db.setSchemaVersion(5);
        assertEquals(5, db.getSchemaVersion(), "Schema version must reflect the value that was set");
        db.close();
    }

    @Test
    void initializeIsIdempotent() throws Exception {
        Path dbPath = tempDir.resolve("idempotent.db");
        DatabaseManager db = new DatabaseManager(dbPath);
        db.initialize();
        db.setSchemaVersion(3);
        db.close();

        // Re-open the same database — version must be preserved, no duplicate rows
        DatabaseManager db2 = new DatabaseManager(dbPath);
        db2.initialize();
        assertEquals(3, db2.getSchemaVersion(),
            "Re-initializing an existing DB must preserve the stored schema version");

        try (Statement stmt = db2.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schema_version")) {
            assertEquals(1, rs.getInt(1),
                "schema_version table must have exactly one row after idempotent init");
        }
        db2.close();
    }

    @Test
    void walJournalModeEnabledAfterInit() throws Exception {
        DatabaseManager db = freshDb();
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA journal_mode")) {
            assertTrue(rs.next());
            assertEquals("wal", rs.getString(1).toLowerCase(),
                "Journal mode must be WAL after initialization");
        }
        db.close();
    }

    @Test
    void getConnectionReturnsNonNullAfterInit() throws Exception {
        DatabaseManager db = freshDb();
        assertNotNull(db.getConnection(), "getConnection() must return non-null after initialize()");
        db.close();
    }
}
