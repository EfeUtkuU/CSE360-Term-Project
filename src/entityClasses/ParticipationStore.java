package entityClasses;

import database.Database;
import java.sql.SQLException;

/**
 * <h2>Class: ParticipationStore</h2>
 *
 * <p><strong>Description:</strong><br>
 * Centralized manager responsible for loading, computing, and saving
 * participation statistics for students. This class mirrors the design
 * of {@code PostStore}, {@code ReplyStore}, and {@code ThreadStore} by
 * providing a single shared singleton instance.
 * </p>
 *
 * <p>The {@code ParticipationStore} ensures that:</p>
 * <ul>
 *   <li>MVC controllers never interact with the {@link Database} directly</li>
 *   <li>Participation statistics are automatically generated when missing</li>
 *   <li>All participation-related operations reside in one unified service</li>
 * </ul>
 *
 * <p>
 * This implementation follows <strong>Option A</strong>:  
 * if statistics for a student do not exist, they are automatically computed
 * and stored in the database.
 * </p>
 *
 * @version
 *   1.00 — 2025-11-24 — Initial implementation.
 */
public final class ParticipationStore {

    /** Singleton instance of the store. */
    private static final ParticipationStore instance = new ParticipationStore();

    /** Shared database connection used for all operations. */
    private Database db;

    /**
     * Private constructor.
     * <p>
     * Initializes the shared database reference and ensures that the
     * necessary participation statistics table exists.
     * </p>
     */
    private ParticipationStore() {
        try {
            db = applicationMain.FoundationsMain.database;
            db.createParticipationStatsTable();
        } catch (SQLException e) {
            System.err.println("Error initializing ParticipationStore: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton instance of this store.
     *
     * @return shared instance of {@code ParticipationStore}
     */
    public static ParticipationStore getInstance() {
        return instance;
    }

    // ----------------------------------------------------------------------
    //  PUBLIC API
    // ----------------------------------------------------------------------

    /**
     * Loads participation statistics for a specific student.
     * <p>
     * If no saved statistics are found for the given username, new
     * statistics are automatically computed using
     * {@link ParticipationCalculator#computeAndSave(User, Database)}
     * and then stored in the database.
     * </p>
     *
     * @param username the username of the student
     * @return a {@link ParticipationStatistics} object, or {@code null}
     *         if a database error occurs
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
            System.err.println(
                    "Error loading participation stats for " + username + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Forces a full recompute of participation statistics for a specific student.
     * <p>
     * This method is typically invoked by {@code PostStore} or {@code ReplyStore}
     * when a student's activity has changed (post, reply, edit, or delete).
     * </p>
     *
     * @param user the student whose statistics need recomputation
     * @return newly saved {@link ParticipationStatistics}, or {@code null} on error
     */
    public ParticipationStatistics recomputeFor(User user) {
        try {
            return ParticipationCalculator.computeAndSave(user, db);
        } catch (Exception e) {
            System.err.println(
                    "Failed to update participation stats for " + user.getUserName()
                    + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Public helper invoked by other stores whenever student activity occurs.
     * <p>
     * This triggers a full recomputation of the student's participation statistics.
     * </p>
     *
     * @param user the student whose statistics should be updated
     */
    public void updateAfterActivity(User user) {
        recomputeFor(user);
    }
}
