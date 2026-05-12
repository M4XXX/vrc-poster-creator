package dev.vrcposter.repository.migration;

import dev.vrcposter.repository.RepositoryException;

import java.sql.Connection;

public interface Migration {

    int getVersion();

    String getDescription();

    void execute(Connection connection) throws RepositoryException;
}
