package dev.vrcposter.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SessionRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonSessionRepository repo() {
        return new JsonSessionRepository(tempDir.resolve("session.json"));
    }

    // ── save ────────────────────────────────────────────────────────────────

    @Test
    void save_writesSessionJsonFile() throws Exception {
        SessionData data = buildFullData();
        repo().save(data);
        assertTrue(Files.exists(tempDir.resolve("session.json")));
    }

    @Test
    void save_noTmpFileLeftBehind() throws Exception {
        repo().save(buildFullData());
        assertFalse(Files.exists(tempDir.resolve("session.json.tmp")),
                "session.json.tmp must be cleaned up after successful save");
    }

    @Test
    void save_allFieldsSerializedCorrectly() throws Exception {
        SessionData data = buildFullData();
        JsonSessionRepository r = repo();
        r.save(data);

        String json = Files.readString(tempDir.resolve("session.json"));
        assertTrue(json.contains("\"title\":\"Test Title\""));
        assertTrue(json.contains("\"subtitle\":\"Test Subtitle\""));
        assertTrue(json.contains("\"date\":\"2026-05-01\""));
        assertTrue(json.contains("\"backgroundPath\":\"/path/to/bg.jpg\""));
        assertTrue(json.contains("\"clubLogoPath\":\"/path/to/club.png\""));
        assertTrue(json.contains("\"startTime\":\"9pm\""));
        assertTrue(json.contains("\"assignedDjId\":\"dj-uuid-1\""));
        assertTrue(json.contains("\"assignedDjArtistName\":\"DJ Max\""));
    }

    @Test
    void save_nullPathsSerializeAsNull() throws Exception {
        SessionData data = new SessionData();
        data.title = "Minimal";
        data.timeslots = List.of();
        repo().save(data);

        String json = Files.readString(tempDir.resolve("session.json"));
        assertTrue(json.contains("\"backgroundPath\":null") || !json.contains("backgroundPath")
                || json.contains("\"backgroundPath\":null"));
    }

    // ── load ────────────────────────────────────────────────────────────────

    @Test
    void load_returnsEmptyWhenFileAbsent() throws Exception {
        Optional<SessionData> result = repo().load();
        assertTrue(result.isEmpty());
    }

    @Test
    void load_roundTripPreservesAllFields() throws Exception {
        SessionData original = buildFullData();
        JsonSessionRepository r = repo();
        r.save(original);

        Optional<SessionData> loaded = r.load();
        assertTrue(loaded.isPresent());
        SessionData d = loaded.get();

        assertEquals("Test Title", d.title);
        assertEquals("Test Subtitle", d.subtitle);
        assertEquals("2026-05-01", d.date);
        assertEquals("/path/to/bg.jpg", d.backgroundPath);
        assertEquals("/path/to/club.png", d.clubLogoPath);
        assertNull(d.qrCodePath);
        assertNotNull(d.timeslots);
        assertEquals(2, d.timeslots.size());

        SessionData.TimeslotData slot0 = d.timeslots.get(0);
        assertEquals("9pm", slot0.startTime);
        assertEquals("dj-uuid-1", slot0.assignedDjId);
        assertEquals("DJ Max", slot0.assignedDjArtistName);

        SessionData.TimeslotData slot1 = d.timeslots.get(1);
        assertEquals("10pm", slot1.startTime);
        assertNull(slot1.assignedDjId);
        assertNull(slot1.assignedDjArtistName);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private SessionData buildFullData() {
        SessionData data = new SessionData();
        data.title = "Test Title";
        data.subtitle = "Test Subtitle";
        data.date = "2026-05-01";
        data.backgroundPath = "/path/to/bg.jpg";
        data.clubLogoPath = "/path/to/club.png";
        data.qrCodePath = null;

        SessionData.TimeslotData slot0 = new SessionData.TimeslotData();
        slot0.startTime = "9pm";
        slot0.assignedDjId = "dj-uuid-1";
        slot0.assignedDjArtistName = "DJ Max";

        SessionData.TimeslotData slot1 = new SessionData.TimeslotData();
        slot1.startTime = "10pm";
        slot1.assignedDjId = null;
        slot1.assignedDjArtistName = null;

        data.timeslots = List.of(slot0, slot1);
        return data;
    }
}
