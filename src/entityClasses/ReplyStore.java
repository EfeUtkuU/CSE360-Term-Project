package entityClasses;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
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
            db = new Database();
            db.connectToDatabase();
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


    // ---------------------- Sync Helpers ----------------------

    /** Reload all replies from the database. */
    public void reloadAllReplies() {
        try {
            allReplies.clear();
            // optional: preload all replies for all posts
            // (you can also call loadRepliesForPost(postId) lazily)
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
        } catch (SQLException e) {
            System.err.println("Database error while adding reply: " + e.getMessage());
        }
    }

    // ---------------------- READ ----------------------

    /** Loads all replies associated with a specific post (by DB ID). */
    public List<Reply> loadRepliesForPost(int postDatabaseId) {
        subsetReplies.clear();
        try {
            subsetReplies.addAll(db.getRepliesForPost(postDatabaseId));
            System.out.println("Loaded " + subsetReplies.size()
                    + " replies for Post (DB ID: " + postDatabaseId + ")");
        } catch (SQLException e) {
            System.err.println("Database error while loading replies for post " + postDatabaseId + ": " + e.getMessage());
        }
        return subsetReplies;
    }

    public List<Reply> getAllReplies() {
        return new ArrayList<>(allReplies);
    }

    // ---------------------- UPDATE ----------------------

    public boolean updateReply(int localId, User user, String newBody) {
        for (Reply r : allReplies) {
            if (r.getId() == localId) {
                if (r.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    r.setBody(newBody);
                    try {
                        db.updateReply(r.getDatabaseId(), newBody);
                        System.out.println("Reply updated by " + user.getUserName()
                                + " (DB ID: " + r.getDatabaseId() + ")");
                    } catch (SQLException e) {
                        System.err.println("Database error while updating reply: " + e.getMessage());
                    }
                    return true;
                } else {
                    System.out.println("Permission denied: only staff or the author can edit this reply.");
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------- DELETE ----------------------

    public boolean deleteReply(int localId, User user) {
        for (Reply r : new ArrayList<>(allReplies)) {
            if (r.getId() == localId) {
                if (r.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    try {
                        db.deleteReply(r.getDatabaseId());
                        allReplies.remove(r);
                        System.out.println("Reply deleted by " + user.getUserName()
                                + " (DB ID: " + r.getDatabaseId() + ")");
                        return true;
                    } catch (SQLException e) {
                        System.err.println("Database error while deleting reply: " + e.getMessage());
                        return false;
                    }
                } else {
                    System.out.println("Permission denied: only staff or the author can delete this reply.");
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------- UTILITY ----------------------

    public List<Reply> getSubsetReplies() {
        return subsetReplies;
    }

    public void clearSubset() {
        subsetReplies.clear();
    }

    public void removeReply(Reply reply) {
        allReplies.remove(reply);
        try {
            db.deleteReply(reply.getDatabaseId()); 
            System.out.println("Reply removed (DB ID: " + reply.getDatabaseId() + ")");
        } catch (SQLException e) {
            System.err.println("Database error while removing reply: " + e.getMessage());
        }
    }

    public void updateReplyContent(Reply reply, String newBody) {
        reply.setBody(newBody);
        try {
            db.updateReply(reply.getDatabaseId(), newBody); 
            System.out.println("Reply content updated (DB ID: " + reply.getDatabaseId() + ")");
        } catch (SQLException e) {
            System.err.println("Database error while updating reply content: " + e.getMessage());
        }
    }
    
    public void clearAllReplies() {
        allReplies.clear();        // or DELETE FROM replies
    }
}
