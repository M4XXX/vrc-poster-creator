package dev.vrcposter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DJRecordTest {

    private DJRecord base() {
        return DJRecord.create("DJ Alpha", "alpha#1234", "Alpha_VRC", "House", "https://twitch.tv/alpha", null);
    }

    @Test
    void withLogoFilenameReturnsNewInstance() {
        DJRecord original = base();
        DJRecord updated = original.withLogoFilename("logo.png");
        assertNotSame(original, updated);
    }

    @Test
    void withLogoFilenameUpdatesLogoFilename() {
        DJRecord updated = base().withLogoFilename("logo.png");
        assertEquals("logo.png", updated.getLogoFilename());
    }

    @Test
    void withLogoFilenamePreservesId() {
        DJRecord original = base();
        DJRecord updated = original.withLogoFilename("logo.png");
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void withLogoFilenamePreservesCreatedAt() {
        DJRecord original = base();
        DJRecord updated = original.withLogoFilename("logo.png");
        assertEquals(original.getCreatedAt(), updated.getCreatedAt());
    }

    @Test
    void withLogoFilenamePreservesAllTextFields() {
        DJRecord original = base();
        DJRecord updated = original.withLogoFilename("logo.png");
        assertEquals(original.getArtistName(),  updated.getArtistName());
        assertEquals(original.getDiscordName(), updated.getDiscordName());
        assertEquals(original.getVrcName(),     updated.getVrcName());
        assertEquals(original.getGenres(),      updated.getGenres());
        assertEquals(original.getStreamLink1(), updated.getStreamLink1());
        assertEquals(original.getStreamLink2(), updated.getStreamLink2());
    }

    @Test
    void withLogoFilenameOriginalLogoRemainsNull() {
        DJRecord original = base();
        assertNull(original.getLogoFilename());
        original.withLogoFilename("logo.png"); // must not mutate original
        assertNull(original.getLogoFilename());
    }

    @Test
    void withLogoFilenameAcceptsNull() {
        DJRecord withLogo = base().withLogoFilename("logo.png");
        DJRecord cleared = withLogo.withLogoFilename(null);
        assertNull(cleared.getLogoFilename());
    }
}
