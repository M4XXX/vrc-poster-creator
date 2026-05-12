package dev.vrcposter.repository;

import java.util.Optional;

public interface SessionRepository {

    void save(SessionData data) throws RepositoryException;

    Optional<SessionData> load() throws RepositoryException;
}

