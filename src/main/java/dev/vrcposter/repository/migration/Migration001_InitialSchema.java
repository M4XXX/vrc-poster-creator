package dev.vrcposter.repository.migration;

import dev.vrcposter.repository.RepositoryException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Migration001_InitialSchema implements Migration {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Create dj table";
    }

    @Override
    public void execute(Connection connection) throws RepositoryException {
        String sql = "CREATE TABLE IF NOT EXISTS dj (" +
                "id TEXT PRIMARY KEY, " +
                "artist_name TEXT NOT NULL, " +
                "discord_name TEXT, " +
                "vrc_name TEXT, " +
                "genres TEXT, " +
                "stream_link1 TEXT, " +
                "stream_link2 TEXT, " +
                "logo_filename TEXT, " +
                "created_at INTEGER, " +
                "updated_at INTEGER" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to create dj table", e);
        }
    }
}
