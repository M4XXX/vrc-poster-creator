package dev.vrcposter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vrcposter.util.AppPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class JsonSessionRepository implements SessionRepository {
    private static final Logger log = LoggerFactory.getLogger(JsonSessionRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path sessionPath;

    public JsonSessionRepository() {
        this.sessionPath = AppPaths.sessionJson();
    }

    /** Package-visible constructor for tests — allows injecting an alternate path. */
    JsonSessionRepository(Path sessionPath) {
        this.sessionPath = sessionPath;
    }

    @Override
    public void save(SessionData data) throws RepositoryException {
        Path tmpPath = sessionPath.resolveSibling("session.json.tmp");
        try {
            Files.createDirectories(sessionPath.getParent());
            byte[] bytes = MAPPER.writeValueAsBytes(data);
            Files.write(tmpPath, bytes);
            try {
                Files.move(tmpPath, sessionPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                log.warn("ATOMIC_MOVE not supported — falling back to REPLACE_EXISTING");
                Files.move(tmpPath, sessionPath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.debug("Session saved to {}", sessionPath);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tmpPath);
            } catch (IOException cleanupEx) {
                log.warn("Could not delete tmp file after failed save: {}", tmpPath, cleanupEx);
            }
            throw new RepositoryException("Session write failed", e);
        }
    }

    @Override
    public Optional<SessionData> load() throws RepositoryException {
        if (!Files.exists(sessionPath)) {
            log.debug("No session file found at {} — returning empty", sessionPath);
            return Optional.empty();
        }
        try {
            SessionData data = MAPPER.readValue(sessionPath.toFile(), SessionData.class);
            log.info("Session loaded from {}", sessionPath);
            return Optional.of(data);
        } catch (IOException e) {
            throw new RepositoryException("Session read failed", e);
        }
    }
}
