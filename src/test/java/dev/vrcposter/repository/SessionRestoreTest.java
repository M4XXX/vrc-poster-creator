package dev.vrcposter.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SessionRestoreTest {

    @TempDir
    Path tempDir;

    @Test
    void corruptJson_throwsRepositoryException() throws Exception {
        Path sessionPath = tempDir.resolve("session.json");
        Files.writeString(sessionPath, "{invalid json!!");
        var repo = new JsonSessionRepository(sessionPath);
        assertThrows(RepositoryException.class, repo::load);
    }

    @Test
    void roundTrip_withTimeslots_preservesAllFields() throws Exception {
        Path sessionPath = tempDir.resolve("session.json");
        var repo = new JsonSessionRepository(sessionPath);

        SessionData data = new SessionData();
        data.title = "Club Night";
        data.subtitle = "Every Friday";
        data.date = "May 9, 2026";
        data.backgroundPath = "/some/bg.jpg";
        data.clubLogoPath = null;
        data.qrCodePath = "/some/qr.png";

        SessionData.TimeslotData td1 = new SessionData.TimeslotData();
        td1.startTime = "9pm";
        td1.assignedDjId = "abc-123";
        td1.assignedDjArtistName = "DJ Max";

        SessionData.TimeslotData td2 = new SessionData.TimeslotData();
        td2.startTime = "10pm";
        td2.assignedDjId = null;
        td2.assignedDjArtistName = null;

        data.timeslots = List.of(td1, td2);
        repo.save(data);

        Optional<SessionData> loaded = repo.load();
        assertTrue(loaded.isPresent());
        SessionData result = loaded.get();

        assertEquals("Club Night", result.title);
        assertEquals("Every Friday", result.subtitle);
        assertEquals("May 9, 2026", result.date);
        assertEquals("/some/bg.jpg", result.backgroundPath);
        assertNull(result.clubLogoPath);
        assertEquals("/some/qr.png", result.qrCodePath);

        assertNotNull(result.timeslots);
        assertEquals(2, result.timeslots.size());

        assertEquals("9pm", result.timeslots.get(0).startTime);
        assertEquals("abc-123", result.timeslots.get(0).assignedDjId);
        assertEquals("DJ Max", result.timeslots.get(0).assignedDjArtistName);

        assertEquals("10pm", result.timeslots.get(1).startTime);
        assertNull(result.timeslots.get(1).assignedDjId);
        assertNull(result.timeslots.get(1).assignedDjArtistName);
    }
}
