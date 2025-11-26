package entityClasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.SQLException;
import java.time.LocalDate;
import database.Database;

/**
 * <p><strong>Class:</strong> PostStore</p>
 *
 * <p><strong>Description:</strong>
 * Central manager for all {@link Post} objects.
 * Keeps a master in-memory list (allPosts) plus a "current view"
 * list (subsetPosts) similar to {@link ReplyStore}.
 *
 * Edit/delete operations are keyed by the <strong>local in-memory ID</strong>
 * (Post.getId()), which matches how your GUI has always called this class.
 * The database ID is still stored inside each Post for DB operations, but
 * GUI code never has to worry about it.
 * </p>
 *
 * @version 2.60 — 2025-11-22 — Restored legacy signatures and behavior
 *                               so GUI code (searchPosts, getAllPosts, etc.)
 *                               compiles and post editing/deleting works.
 */
public class PostStore {

    // ---------------------- Fields ----------------------

    private static final PostStore instance = new PostStore();

    /** All posts known to the application (across all threads). */
    private final List<Post> allPosts = new ArrayList<>();

    /** Posts currently visible / in the active view (e.g., selected thread). */
    private List<Post> subsetPosts = new ArrayList<>();

    private Database db;

    // ---------------------- Constructor ----------------------

    private PostStore() {
        try {
            db = applicationMain.FoundationsMain.database;
            db.createPostAndReplyTables();

            reloadFromDatabase();   // always load from DB on startup
            System.out.println("Loaded " + allPosts.size() + " posts from database.");
        } catch (SQLException e) {
            System.err.println("Error initializing PostStore: " + e.getMessage());
        }
    }

    // ---------------------- Singleton Accessor ----------------------

    public static PostStore getInstance() {
        return instance;
    }

    // ---------------------- PARTICIPATION UPDATE HOOK ----------------------

    /**
     * Recomputes participation statistics for a user after any post event.
     */
    private void updateParticipationFor(User user) {
        try {
            ParticipationCalculator.computeAndSave(user, db);
        } catch (Exception e) {
            System.err.println("Failed to update participation stats for user "
                    + user.getUserName() + ": " + e.getMessage());
        }
    }

    // ---------------------- DB Sync Helpers ----------------------

    /** Reload all posts from the database, replacing the in-memory list. */
    public void reloadFromDatabase() {
        try {
            allPosts.clear();
            allPosts.addAll(db.getAllPosts());
            System.out.println("Loaded " + allPosts.size() + " posts from database.");
        } catch (SQLException e) {
            System.err.println("Error reloading posts: " + e.getMessage());
        }
    }

    // ---------------------- CREATE ----------------------

    public void addPost(Post post) {
        if (post.getTitle().isBlank() || post.getBody().isBlank()) {
            System.out.println("Cannot create post: title or body is empty.");
            return;
        }
        try {
            db.insertPost(post);  // auto-creates a thread for this post
            allPosts.add(post);
            System.out.println("Post added by " + post.getAuthor() +
                               " (DB ID: " + post.getDatabaseId() + ")");

            // ★ Participation hook
            updateParticipationFor(new User(post.getAuthor()));

        } catch (SQLException e) {
            System.err.println("Database error while adding post: " + e.getMessage());
        }
    }

    public void addPostToThread(int threadId, User user, String title, String content) {
        if (content == null || content.isBlank()) {
            System.out.println("Cannot create post: body is empty.");
            return;
        }

        String safeTitle = (title == null || title.isBlank())
                ? "Post"
                : title.trim();

        Post post = new Post(user, safeTitle, content.trim(), "", false, false);

        try {
            db.insertPost(post, threadId);
            allPosts.add(post);
            System.out.println("Post added to thread " + threadId + " by " + post.getAuthor() +
                               " (DB ID: " + post.getDatabaseId() + ")");

            // ★ Participation hook
            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while adding post to thread: " + e.getMessage());
        }
    }

    // ---------------------- READ ----------------------

    public List<Post> getAllPosts() {
        return new ArrayList<>(allPosts);
    }

    public List<Post> getPostsForThread(int threadId) {
        subsetPosts = new ArrayList<>();
        try {
            List<Post> loaded = db.getPostsForThread(threadId);
            subsetPosts.addAll(loaded);

            // Merge into master list (ensure uniqueness by databaseId)
            for (Post p : loaded) {
                boolean exists = false;
                for (Post existing : allPosts) {
                    if (existing.getDatabaseId() == p.getDatabaseId()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allPosts.add(p);
                }
            }

            System.out.println("Loaded " + subsetPosts.size() +
                               " posts for thread " + threadId);
        } catch (SQLException e) {
            System.err.println("Error loading posts for thread " + threadId + ": " + e.getMessage());
            return List.of();
        }
        return subsetPosts;
    }

    // ---------------------- UPDATE ----------------------

    public boolean updatePost(int localId, User user, String newTitle, String newBody) {
        System.out.println("updatePost called with localId=" + localId +
                           ", user=" + user.getUserName());

        Post target = null;

        for (Post p : subsetPosts) {
            if (p.getId() == localId) {
                target = p;
                break;
            }
        }

        if (target == null) {
            for (Post p : allPosts) {
                if (p.getId() == localId) {
                    target = p;
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("updatePost: no post found with localId=" + localId);
            return false;
        }

        if (!(target.getAuthor().equals(user.getUserName()) || user.getNewStaff())) {
            System.out.println("Permission denied: Only staff or the author can edit this post.");
            return false;
        }

        String t = (newTitle == null || newTitle.isBlank()) ? target.getTitle() : newTitle.trim();
        String b = (newBody == null || newBody.isBlank()) ? target.getBody() : newBody.trim();

        target.setTitle(t);
        target.setBody(b);

        try {
            db.updatePost(target.getDatabaseId(), t, b);
            System.out.println("Post updated by " + user.getUserName() +
                               " (DB ID: " + target.getDatabaseId() + ")");

            // ★ Participation hook
            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while updating post: " + e.getMessage());
        }
        return true;
    }

    // ---------------------- DELETE ----------------------

    public boolean deletePost(int localId, User user) {
        System.out.println("deletePost called with localId=" + localId +
                           ", user=" + user.getUserName());

        Post target = null;

        for (Post p : subsetPosts) {
            if (p.getId() == localId) {
                target = p;
                break;
            }
        }

        if (target == null) {
            for (Post p : allPosts) {
                if (p.getId() == localId) {
                    target = p;
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("deletePost: no post found with localId=" + localId);
            return false;
        }

        if (!(target.getAuthor().equals(user.getUserName()) || user.getNewStaff())) {
            System.out.println("Permission denied: Only staff or the author can delete this post.");
            return false;
        }

        try {
            db.deletePost(target.getDatabaseId());
            allPosts.remove(target);
            subsetPosts.remove(target);
            System.out.println("Post deleted by " + user.getUserName() +
                               " (DB ID: " + target.getDatabaseId() + ")");

            // ★ Participation hook
            updateParticipationFor(user);

            return true;

        } catch (SQLException e) {
            System.err.println("Database error while deleting post: " + e.getMessage());
            return false;
        }
    }

    // ---------------------- Search ----------------------

    public List<Post> searchPosts(String keyword) {
        subsetPosts = new ArrayList<>();
        for (Post p : allPosts) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains(keyword.toLowerCase())
                    || p.getBody() != null && p.getBody().toLowerCase().contains(keyword.toLowerCase())) {
                subsetPosts.add(p);
            }
        }
        return subsetPosts;
    }

    public List<Post> getSubsetPosts() {
        return subsetPosts;
    }

    public void clearSubset() {
        subsetPosts.clear();
    }

    // ---------------------- Utility ----------------------

    public void removePost(Post post) {
        allPosts.remove(post);
        try {
            db.deletePost(post.getDatabaseId());
            System.out.println("Post removed (DB ID: " + post.getDatabaseId() + ")");
        } catch (SQLException e) {
            System.err.println("Database error while removing post: " + e.getMessage());
        }
    }

    public void updatePostContent(Post post, String newContent) {
        post.setBody(newContent);
        try {
            db.updatePost(post.getDatabaseId(), post.getTitle(), newContent);
            System.out.println("Post content updated for " + post.getAuthor());
        } catch (SQLException e) {
            System.err.println("Database error while updating post content: " + e.getMessage());
        }
    }

    public void clearAllPosts() {
        allPosts.clear();
    }

    // =====================================================================
    //  PARTICIPATION STATISTICS HELPERS (NEW)
    // =====================================================================

    /** Counts how many posts were authored by the given username. */
    public int countPostsByAuthor(String username) {
        int count = 0;
        for (Post p : allPosts) {
            if (p.getAuthor() != null &&
                p.getAuthor().equalsIgnoreCase(username)) {
                count++;
            }
        }
        return count;
    }

    /** Counts number of unique threads a user has posted in. */
    public int countThreadsParticipated(String username) {
        Set<Integer> threadIds = new HashSet<>();

        for (Post p : allPosts) {
            if (p.getAuthor() != null &&
                p.getAuthor().equalsIgnoreCase(username)) {

                try {
                    int dbId = p.getDatabaseId();
                    var conn = db.getConnection();

                    var ps = conn.prepareStatement(
                        "SELECT threadId FROM posts WHERE id = ?"
                    );
                    ps.setInt(1, dbId);
                    var rs = ps.executeQuery();

                    if (rs.next()) {
                        threadIds.add(rs.getInt("threadId"));
                    }

                    rs.close();
                    ps.close();

                } catch (SQLException e) {
                    System.err.println("Error fetching threadId for post " +
                                       p.getDatabaseId() + ": " + e.getMessage());
                }
            }
        }

        return threadIds.size();
    }

    /** Counts distinct days this user created posts. */
    public int countActiveDays(String username) {
        Set<LocalDate> days = new HashSet<>();

        for (Post p : allPosts) {
            if (p.getAuthor() != null &&
                p.getAuthor().equalsIgnoreCase(username) &&
                p.getCreatedAt() != null) {

                days.add(p.getCreatedAt().toLocalDate());
            }
        }
        return days.size();
    }
}
