package entityClasses;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import database.Database;

/**
 * <p><strong>Class:</strong> ReplyStore</p>
 *
 * <p><strong>Description:</strong>
 * The {@code ReplyStore} class manages all {@link Reply} objects in the system.
 * It follows the Singleton design pattern, ensuring a single shared instance.
 * It provides CRUD operations (create, read, update, delete) and filtering
 * for replies associated with specific posts.</p>
 *
 * <p>This class synchronizes replies between an in-memory list
 * and the persistent H2 database managed by the {@link Database} class.</p>
 *
 * @version 2.30 — 2025-10-27 — Finalized for DB-managed post/reply IDs and full CRUD integrity
 */
public class ReplyStore {

    // ---------------------- Fields ----------------------

    private static final ReplyStore instance = new ReplyStore();
    private final List<Reply> allReplies = new ArrayList<>();
    private List<Reply> subsetReplies = new ArrayList<>();
    private Database db;

    // ---------------------- Constructor ----------------------

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

    public static ReplyStore getInstance() {
        return instance;
    }

    // ---------------------- PARTICIPATION UPDATE HOOK ----------------------

    /**
     * Recomputes participation statistics after any reply action.
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

    /** Reload all replies from the database. */
    public void reloadAllReplies() {
        try {
            allReplies.clear();
            // Optional preload in future
        } catch (Exception e) {
            System.err.println("Error reloading replies: " + e.getMessage());
        }
    }

    // ---------------------- CREATE ----------------------

    public void addReply(Reply reply) {
        if (reply.getBody().isBlank()) {
            System.out.println("Cannot create reply: body is empty.");
            return;
        }

        try {
            db.insertReply(reply);
            allReplies.add(reply);
            System.out.println("Reply added by " + reply.getAuthor()
                    + " (Reply DB ID: " + reply.getDatabaseId()
                    + " → Post DB ID: " + reply.getPostDatabaseId() + ")");

            // ★ Participation hook
            updateParticipationFor(new User(reply.getAuthor()));

        } catch (SQLException e) {
            System.err.println("Database error while adding reply: " + e.getMessage());
        }
    }

    // ---------------------- READ ----------------------

    public List<Reply> loadRepliesForPost(int postDatabaseId) {
        subsetReplies.clear();
        try {
            List<Reply> loaded = db.getRepliesForPost(postDatabaseId);
            subsetReplies.addAll(loaded);

            // Merge into master reply list
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

            System.out.println("Loaded " + subsetReplies.size()
                    + " replies for Post (DB ID: " + postDatabaseId + ")");
        } catch (SQLException e) {
            System.err.println("Database error while loading replies: " + e.getMessage());
        }
        return subsetReplies;
    }

    // ---------------------- UPDATE ----------------------

    public boolean updateReply(int localId, User user, String newBody) {
        System.out.println("updateReply called with localId=" + localId +
                ", user=" + user.getUserName());

        Reply target = null;

        for (Reply r : subsetReplies) {
            if (r.getId() == localId) {
                target = r;
                break;
            }
        }

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

        if (!(target.getAuthor().equals(user.getUserName()) || user.getNewStaff())) {
            System.out.println("Permission denied.");
            return false;
        }

        target.setBody(newBody);
        try {
            db.updateReply(target.getDatabaseId(), newBody);

            // ★ Participation hook
            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while updating reply: " + e.getMessage());
        }
        return true;
    }

    // ---------------------- DELETE ----------------------

    public boolean deleteReply(int localId, User user) {
        System.out.println("deleteReply called with localId=" + localId +
                ", user=" + user.getUserName());

        Reply target = null;

        for (Reply r : subsetReplies) {
            if (r.getId() == localId) {
                target = r;
                break;
            }
        }

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

            // ★ Participation hook
            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while deleting reply: " + e.getMessage());
            return false;
        }
        return true;
    }

    // ---------------------- Utility ----------------------

    public List<Reply> getSubsetReplies() { return subsetReplies; }

    public void clearSubset() { subsetReplies.clear(); }

    public void removeReply(Reply reply) {
        allReplies.remove(reply);
        try {
            db.deleteReply(reply.getDatabaseId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateReplyContent(Reply reply, String newBody) {
        reply.setBody(newBody);
        try {
            db.updateReply(reply.getDatabaseId(), newBody);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearAllReplies() { allReplies.clear(); }

    public List<Reply> getRepliesForPost(int postDatabaseId) {
        try {
            Database db = new Database();
            db.connectToDatabase();
            List<Reply> list = db.getRepliesForPost(postDatabaseId);
            db.closeConnection();
            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // =====================================================================
    //  PARTICIPATION STATISTICS HELPERS (NEW)
    // =====================================================================

    /** Counts how many replies a user authored. */
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

    /** Computes the average length of replies written by the user. */
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

    /** Counts distinct days on which the user wrote replies. */
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
