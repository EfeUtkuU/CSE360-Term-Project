package entityClasses;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import database.Database;

/**
 * <h2>Class: ReplyStore</h2>
 *
 * <p><strong>Description:</strong><br>
 * The {@code ReplyStore} class manages all {@link Reply} objects in the
 * discussion system. It is responsible for loading, caching, updating,
 * and deleting replies, while maintaining synchronization with the
 * underlying H2 database.
 * </p>
 *
 * <p>This class:</p>
 * <ul>
 *   <li>Follows the Singleton design pattern</li>
 *   <li>Maintains both a full in-memory list and an active subset view</li>
 *   <li>Provides full CRUD support</li>
 *   <li>Automatically updates participation statistics when replies are added, edited, or deleted</li>
 * </ul>
 *
 * @version
 *   2.30 — 2025-10-27 — Finalized for DB-managed post/reply IDs and full CRUD integrity
 */
public class ReplyStore {

    // ---------------------- Fields ----------------------

    /** Singleton instance of the store. */
    private static final ReplyStore instance = new ReplyStore();

    /** All replies known to the application (across all posts). */
    private final List<Reply> allReplies = new ArrayList<>();

    /** Replies shown in the current UI view (e.g., replies for a selected post). */
    private List<Reply> subsetReplies = new ArrayList<>();

    /** Shared database connection. */
    private Database db;

    // ---------------------- Constructor ----------------------

    /**
     * Private constructor.
     * <p>
     * Initializes the database connection, ensures required tables
     * exist, and loads all replies into memory.
     * </p>
     */
    private ReplyStore() {
        try {
            db = applicationMain.FoundationsMain.database;
            db.createPostAndReplyTables();
            reloadAllReplies();
            System.out.println("ReplyStore initialized and connected to database.");
        } catch (SQLException e) {
            System.err.println("Error initializing ReplyStore: " + e.getMessage());
        }
    }

    // ---------------------- Singleton Accessor ----------------------

    /**
     * @return the shared singleton instance of {@code ReplyStore}
     */
    public static ReplyStore getInstance() {
        return instance;
    }

    // ---------------------- PARTICIPATION UPDATE HOOK ----------------------

    /**
     * Triggers a full recomputation of participation statistics for a user
     * after any reply-related action.
     *
     * @param user the user whose participation data should be refreshed
     */
    private void updateParticipationFor(User user) {
        try {
            ParticipationCalculator.computeAndSave(user, db);
        } catch (Exception e) {
            System.err.println("Failed to update participation stats for user "
                    + user.getUserName() + ": " + e.getMessage());
        }
    }

    // ---------------------- Sync Helpers ----------------------

    /**
     * Reloads all replies from the database into memory.
     * <p>Currently used as a placeholder for future full-loading behavior.</p>
     */
    public void reloadAllReplies() {
        try {
            allReplies.clear();
            // Potential preload of all replies could go here
        } catch (Exception e) {
            System.err.println("Error reloading replies: " + e.getMessage());
        }
    }

    // ---------------------- CREATE ----------------------

    /**
     * Creates a new reply and persists it to the database.
     *
     * @param reply the reply to insert
     */
    public void addReply(Reply reply) {
        if (reply.getBody().isBlank()) {
            System.out.println("Cannot create reply: body is empty.");
            return;
        }

        try {
            db.insertReply(reply);
            allReplies.add(reply);

            System.out.println("Reply added by " + reply.getAuthor() +
                    " (Reply DB ID: " + reply.getDatabaseId() +
                    " → Post DB ID: " + reply.getPostDatabaseId() + ")");

            updateParticipationFor(new User(reply.getAuthor()));

        } catch (SQLException e) {
            System.err.println("Database error while adding reply: " + e.getMessage());
        }
    }

    // ---------------------- READ ----------------------

    /**
     * Loads all replies associated with a specific post.
     *
     * @param postDatabaseId the database ID of the post
     * @return a list of replies for the post
     */
    public List<Reply> loadRepliesForPost(int postDatabaseId) {
        subsetReplies.clear();
        try {
            List<Reply> loaded = db.getRepliesForPost(postDatabaseId);
            subsetReplies.addAll(loaded);

            // Merge into master list without duplicating
            for (Reply r : loaded) {
                boolean exists = false;
                for (Reply existing : allReplies) {
                    if (existing.getDatabaseId() == r.getDatabaseId()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allReplies.add(r);
                }
            }

            System.out.println("Loaded " + subsetReplies.size() +
                    " replies for Post (DB ID: " + postDatabaseId + ")");

        } catch (SQLException e) {
            System.err.println("Database error while loading replies: " + e.getMessage());
        }
        return subsetReplies;
    }

    // ---------------------- UPDATE ----------------------

    /**
     * Updates the body of a reply.
     *
     * @param localId the in-memory reply identifier
     * @param user    the user performing the update
     * @param newBody new text for the reply
     * @return {@code true} if the update succeeded, {@code false} otherwise
     */
    public boolean updateReply(int localId, User user, String newBody) {
        System.out.println("updateReply called with localId=" + localId +
                ", user=" + user.getUserName());

        Reply target = null;

        // Check subset first
        for (Reply r : subsetReplies) {
            if (r.getId() == localId) {
                target = r;
                break;
            }
        }

        // Fallback: search master list
        if (target == null) {
            for (Reply r : allReplies) {
                if (r.getId() == localId) {
                    target = r;
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("updateReply: no reply found with id=" + localId);
            return false;
        }

        // Permission check
        if (!(target.getAuthor().equals(user.getUserName()) || user.getNewStaff())) {
            System.out.println("Permission denied.");
            return false;
        }

        target.setBody(newBody);

        try {
            db.updateReply(target.getDatabaseId(), newBody);
            updateParticipationFor(user);
        } catch (SQLException e) {
            System.err.println("Database error while updating reply: " + e.getMessage());
        }

        return true;
    }

    // ---------------------- DELETE ----------------------

    /**
     * Deletes a reply using its local ID.
     *
     * @param localId the in-memory ID of the reply
     * @param user    the user performing the deletion
     * @return {@code true} if deletion succeeded, {@code false} otherwise
     */
    public boolean deleteReply(int localId, User user) {
        System.out.println("deleteReply called with localId=" + localId +
                ", user=" + user.getUserName());

        Reply target = null;

        // Check subset first
        for (Reply r : subsetReplies) {
            if (r.getId() == localId) {
                target = r;
                break;
            }
        }

        // Fallback: master list lookup
        if (target == null) {
            for (Reply r : allReplies) {
                if (r.getId() == localId) {
                    target = r;
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("deleteReply: no reply found");
            return false;
        }

        if (!(target.getAuthor().equals(user.getUserName()) || user.getNewStaff())) {
            System.out.println("Permission denied.");
            return false;
        }

        try {
            db.deleteReply(target.getDatabaseId());
            allReplies.remove(target);
            subsetReplies.remove(target);
            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while deleting reply: " + e.getMessage());
            return false;
        }

        return true;
    }

    // ---------------------- Utility ----------------------

    /**
     * @return the currently active subset of replies used for UI rendering
     */
    public List<Reply> getSubsetReplies() {
        return subsetReplies;
    }

    /** Clears the active subset view. */
    public void clearSubset() {
        subsetReplies.clear();
    }

    /**
     * Removes a reply object from memory and deletes it from the database.
     *
     * @param reply the reply to remove
     */
    public void removeReply(Reply reply) {
        allReplies.remove(reply);
        try {
            db.deleteReply(reply.getDatabaseId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates only the body of a reply.
     *
     * @param reply    reply to modify
     * @param newBody  new body text
     */
    public void updateReplyContent(Reply reply, String newBody) {
        reply.setBody(newBody);
        try {
            db.updateReply(reply.getDatabaseId(), newBody);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Clears all replies from memory only (does not alter the database). */
    public void clearAllReplies() {
        allReplies.clear();
    }

    /**
     * Loads replies for a specific post using a fresh database connection.
     * <p>
     * This method is mainly used for ad-hoc retrieval and does not update
     * the internal store.
     * </p>
     *
     * @param postDatabaseId ID of the post
     * @return list of replies for the post
     */
    public List<Reply> getRepliesForPost(int postDatabaseId) {
        try {
            Database temp = new Database();
            temp.connectToDatabase();
            List<Reply> list = temp.getRepliesForPost(postDatabaseId);
            temp.closeConnection();
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // =====================================================================
    //  PARTICIPATION STATISTICS HELPERS
    // =====================================================================

    /**
     * Counts how many replies a user has authored.
     *
     * @param username target username
     * @return number of replies written
     */
    public int countRepliesByAuthor(String username) {
        int count = 0;
        for (Reply r : allReplies) {
            if (r.getAuthor() != null &&
                    r.getAuthor().equalsIgnoreCase(username)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the average reply length for a user.
     *
     * @param username target username
     * @return average reply length (characters), or 0 if none exist
     */
    public int computeAverageReplyLength(String username) {
        int totalLength = 0;
        int replyCount = 0;

        for (Reply r : allReplies) {
            if (r.getAuthor() != null &&
                    r.getAuthor().equalsIgnoreCase(username)) {

                if (r.getBody() != null) {
                    totalLength += r.getBody().length();
                    replyCount++;
                }
            }
        }

        if (replyCount == 0) return 0;
        return totalLength / replyCount;
    }

    /**
     * Counts the number of unique days on which the user wrote replies.
     *
     * @param username the user to evaluate
     * @return number of active reply days
     */
    public int countReplyActiveDays(String username) {
        Set<LocalDate> days = new HashSet<>();

        for (Reply r : allReplies) {
            if (r.getAuthor() != null &&
                    r.getAuthor().equalsIgnoreCase(username) &&
                    r.getCreatedAt() != null) {

                days.add(r.getCreatedAt().toLocalDate());
            }
        }
        return days.size();
    }
}
