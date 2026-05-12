package dev.vrcposter.repository.migration;

import dev.vrcposter.repository.DatabaseManager;
import dev.vrcposter.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

public class MigrationRunner {
    private static final Logger log = LoggerFactory.getLogger(MigrationRunner.class);

    static final int CURRENT_SCHEMA_VERSION = 1;

    private final DatabaseManager db;
    private final List<Migration> migrations;

    public MigrationRunner(DatabaseManager db, List<Migration> migrations) {
        this.db = db;
        this.migrations = migrations;
    }

    public void run() throws RepositoryException {
        int currentVersion = db.getSchemaVersion();
        log.info("Schema version: {}, migrations available: {}", currentVersion, migrations.size());
        List<Migration> sorted = migrations.stream()
            .sorted(Comparator.comparingInt(Migration::getVersion))
            .toList();
        for (Migration migration : sorted) {
            if (migration.getVersion() > currentVersion) {
                log.info("Running migration v{}: {}",
                    migration.getVersion(), migration.getDescription());
                migration.execute(db.getConnection());
                db.setSchemaVersion(migration.getVersion());
                currentVersion = migration.getVersion();
            }
        }
        log.debug("Migration run complete. Final schema version: {}", currentVersion);
    }
}
