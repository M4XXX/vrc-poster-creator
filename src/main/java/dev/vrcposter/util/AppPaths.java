package dev.vrcposter.util;

import java.nio.file.Path;

public final class AppPaths {
    private AppPaths() {}

    public static Path appData() {
        return Path.of(System.getenv("APPDATA"), "VRCPosterCreator");
    }

    public static Path rosterDb() {
        return appData().resolve("roster.db");
    }

    public static Path sessionJson() {
        return appData().resolve("session.json");
    }

    public static Path logosDir() {
        return appData().resolve("logos");
    }

    public static Path assetsDir() {
        return appData().resolve("assets");
    }

    public static Path logsDir() {
        return appData().resolve("logs");
    }

    public static Path logoFile(String djId, String ext) {
        return logosDir().resolve(djId + "." + ext);
    }
}
