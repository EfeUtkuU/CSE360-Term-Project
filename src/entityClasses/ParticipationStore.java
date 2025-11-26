package entityClasses;

import database.Database;
import java.sql.SQLException;

/**
 * <p><strong>Class:</strong> ParticipationStore</p>
 *
 * <p><strong>Description:</strong>
 * Centralized manager for loading, computing, and saving participation
 * statistics for students. Mirrors PostStore, ReplyStore, and ThreadStore
 * by providing a single shared instance that wraps all database access.
 *
 * Ensures:
 *  - MVC controllers never touch the Database class directly
 *  - Participation statistics are recomputed automatically when missing
 *  - All participation-related operations live in one consistent place
 *
 * Uses OPTION A: auto-compute and save if stats are missing.
 * </p>
 *
 * @version 1.00 — 2025-11-24 — Initial implementation.
 */
public final class ParticipationStore {

    /** Singleton instance */
    private static final ParticipationStore instance = new ParticipationStore();

    /** Shared database connection */
    private Database db;

    /** Private constructor — initializes DB connection and table. */
    private ParticipationStore() {
        try {
            db = applicationMain.FoundationsMain.database;
            db.createParticipationStatsTable();
        } catch (SQLException e) {
            System.err.println("Error initializing ParticipationStore: " + e.getMessage());
        }
    }

    /** @return shared singleton instance */
    public static ParticipationStore getInstance() {
        return instance;
    }

    // ----------------------------------------------------------------------
    //  PUBLIC API
    // ----------------------------------------------------------------------

    /**
     * Loads participation statistics for a user.
     *
     * If the user has no saved stats:
     *   → automatically computes and saves new stats
     *
     * This ensures the stats screen always has full analytics.
     *
     * @param username student's username
     * @return ParticipationStatistics (never null unless DB error)
     */
    public ParticipationStatistics loadStats(String username) {
        try {
            // 1. Attempt to load from DB
            ParticipationStatistics stats =
                    db.loadParticipationStatsForStudent(username);

            // 2. Auto-compute if missing
            if (stats == null) {
                User u = new User(username);  // minimal constructor
                stats = ParticipationCalculator.computeAndSave(u, db);
            }

            return stats;

        } catch (SQLException e) {
            System.err.println("Error loading participation stats for "
                    + username + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Forces a full recompute for a specific student.
     * Used by PostStore and ReplyStore after any activity change.
     */
    public ParticipationStatistics recomputeFor(User user) {
        try {
            return ParticipationCalculator.computeAndSave(user, db);
        } catch (Exception e) {
            System.err.println("Failed to update participation stats for "
                    + user.getUserName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Called by other stores (PostStore, ReplyStore)
     * whenever the student posts, replies, edits, or deletes.
     */
    public void updateAfterActivity(User user) {
        recomputeFor(user);
    }
}
