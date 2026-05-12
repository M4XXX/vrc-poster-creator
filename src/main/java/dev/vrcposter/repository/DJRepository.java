package dev.vrcposter.repository;

import dev.vrcposter.model.DJRecord;

import java.util.List;
import java.util.Optional;

public interface DJRepository {

    void save(DJRecord record) throws RepositoryException;

    void update(DJRecord record) throws RepositoryException;

    Optional<DJRecord> findById(String id) throws RepositoryException;

    List<DJRecord> findAll() throws RepositoryException;

    /** Pattern is the bare search term; implementation wraps it in {@code %pattern%}. */
    List<DJRecord> findByNameLike(String pattern) throws RepositoryException;

    void delete(String id) throws RepositoryException;
}
