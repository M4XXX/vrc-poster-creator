package dev.vrcposter.repository;

import dev.vrcposter.util.AppPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final Path dbPath;
    private Connection connection;

    public DatabaseManager() {
        this(AppPaths.rosterDb());
    }

    public DatabaseManager(Path dbPath) {
        this.dbPath = dbPath;
    }

    public void initialize() throws RepositoryException {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
            connection = null;
        }
        try {
            Files.createDirectories(dbPath.getParent());
        } catch (IOException e) {
            throw new RepositoryException("Failed to create database directory: " + dbPath.getParent(), e);
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schema_version")) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        stmt.execute("INSERT INTO schema_version (version) VALUES (0)");
                    }
                }
            }
            log.info("Database initialized at {}", dbPath);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to initialize database at " + dbPath, e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public int getSchemaVersion() throws RepositoryException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM schema_version LIMIT 1")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to read schema version", e);
        }
    }

    public void setSchemaVersion(int version) throws RepositoryException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE schema_version SET version = ?")) {
            ps.setInt(1, version);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update schema version to " + version, e);
        }
    }

    public void close() throws RepositoryException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("Database connection closed");
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to close database connection", e);
        }
    }
}
