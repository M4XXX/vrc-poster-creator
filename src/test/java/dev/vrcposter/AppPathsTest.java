package dev.vrcposter;

import dev.vrcposter.util.AppPaths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppPathsTest {

    @Test
    void appDataEndsWithVRCPosterCreator() {
        assertTrue(AppPaths.appData().endsWith("VRCPosterCreator"));
    }

    @Test
    void rosterDbResolvedUnderAppData() {
        assertEquals(AppPaths.appData().resolve("roster.db"), AppPaths.rosterDb());
    }

    @Test
    void sessionJsonResolvedUnderAppData() {
        assertEquals(AppPaths.appData().resolve("session.json"), AppPaths.sessionJson());
    }

    @Test
    void logosDirResolvedUnderAppData() {
        assertEquals(AppPaths.appData().resolve("logos"), AppPaths.logosDir());
    }

    @Test
    void assetsDirResolvedUnderAppData() {
        assertEquals(AppPaths.appData().resolve("assets"), AppPaths.assetsDir());
    }

    @Test
    void logsDirResolvedUnderAppData() {
        assertEquals(AppPaths.appData().resolve("logs"), AppPaths.logsDir());
    }

    @Test
    void logoFileBuildsCorrectPath() {
        var expected = AppPaths.logosDir().resolve("dj-alpha.png");
        assertEquals(expected, AppPaths.logoFile("dj-alpha", "png"));
    }
}
