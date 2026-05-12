package dev.vrcposter.repository;

import dev.vrcposter.model.DJRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteDJRepository implements DJRepository {
    private static final Logger log = LoggerFactory.getLogger(SQLiteDJRepository.class);

    private static final String COLS =
            "id, artist_name, discord_name, vrc_name, genres, stream_link1, stream_link2, " +
            "logo_filename, created_at, updated_at";

    private final DatabaseManager db;

    public SQLiteDJRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public void save(DJRecord record) throws RepositoryException {
        String sql = "INSERT INTO dj (" + COLS + ") VALUES (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            long now = System.currentTimeMillis();
            ps.setString(1, record.getId());
            ps.setString(2, record.getArtistName());
            ps.setString(3, record.getDiscordName());
            ps.setString(4, record.getVrcName());
            ps.setString(5, record.getGenres());
            ps.setString(6, record.getStreamLink1());
            ps.setString(7, record.getStreamLink2());
            ps.setString(8, record.getLogoFilename());
            ps.setLong(9, now);
            ps.setLong(10, now);
            ps.executeUpdate();
            log.debug("Saved DJ record id={}", record.getId());
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save DJ record: " + record.getId(), e);
        }
    }

    @Override
    public void update(DJRecord record) throws RepositoryException {
        String sql = "UPDATE dj SET artist_name=?, discord_name=?, vrc_name=?, genres=?, " +
                "stream_link1=?, stream_link2=?, logo_filename=?, updated_at=? WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            long now = System.currentTimeMillis();
            ps.setString(1, record.getArtistName());
            ps.setString(2, record.getDiscordName());
            ps.setString(3, record.getVrcName());
            ps.setString(4, record.getGenres());
            ps.setString(5, record.getStreamLink1());
            ps.setString(6, record.getStreamLink2());
            ps.setString(7, record.getLogoFilename());
            ps.setLong(8, now);
            ps.setString(9, record.getId());
            ps.executeUpdate();
            log.debug("Updated DJ record id={}", record.getId());
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update DJ record: " + record.getId(), e);
        }
    }

    @Override
    public Optional<DJRecord> findById(String id) throws RepositoryException {
        String sql = "SELECT " + COLS + " FROM dj WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find DJ record by id: " + id, e);
        }
    }

    @Override
    public List<DJRecord> findAll() throws RepositoryException {
        String sql = "SELECT " + COLS + " FROM dj ORDER BY LOWER(artist_name)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<DJRecord> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to retrieve all DJ records", e);
        }
    }

    @Override
    public List<DJRecord> findByNameLike(String pattern) throws RepositoryException {
        String sql = "SELECT " + COLS + " FROM dj WHERE LOWER(artist_name) LIKE LOWER(?) " +
                "ORDER BY LOWER(artist_name)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + pattern + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<DJRecord> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to search DJ records by name: " + pattern, e);
        }
    }

    @Override
    public void delete(String id) throws RepositoryException {
        String sql = "DELETE FROM dj WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            log.debug("Deleted DJ record id={}", id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete DJ record: " + id, e);
        }
    }

    private DJRecord mapRow(ResultSet rs) throws SQLException {
        return new DJRecord(
                rs.getString("id"),
                rs.getString("artist_name"),
                rs.getString("discord_name"),
                rs.getString("vrc_name"),
                rs.getString("genres"),
                rs.getString("stream_link1"),
                rs.getString("stream_link2"),
                rs.getString("logo_filename"),
                rs.getLong("created_at"),
                rs.getLong("updated_at")
        );
    }
}
