package entityClasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.SQLException;
import java.time.LocalDate;
import database.Database;

/**
 * <h2>Class: PostStore</h2>
 *
 * <p><strong>Description:</strong><br>
 * Centralized manager for all {@link Post} objects in the system. This store
 * maintains:
 * </p>
 *
 * <ul>
 *   <li>a master list of all posts loaded from the database</li>
 *   <li>a subset list representing the currently active view (e.g., posts for a selected thread)</li>
 * </ul>
 *
 * <p>
 * Edit and delete operations are keyed by the <strong>local in-memory ID</strong>
 * ({@link Post#getId()}) to preserve compatibility with existing GUI code.
 * Each {@link Post} also stores a database ID internally, which is used for
 * persistence.
 * </p>
 *
 * @version
 *   2.60 — 2025-11-22 — Restored legacy signatures/behavior so GUI methods such
 *   as searchPosts, getAllPosts, and post editing/deleting remain compatible.
 */
public class PostStore {

    // ---------------------- Fields ----------------------

    /** Singleton instance of this store. */
    private static final PostStore instance = new PostStore();

    /** All posts known to the application (across all threads). */
    private final List<Post> allPosts = new ArrayList<>();

    /** Posts currently visible in the user interface. */
    private List<Post> subsetPosts = new ArrayList<>();

    /** Database reference for persistence operations. */
    private Database db;

    // ---------------------- Constructor ----------------------

    /**
     * Private constructor.
     * <p>
     * Initializes the database connection, ensures table creation,
     * and loads all posts into memory.
     * </p>
     */
    private PostStore() {
        try {
            db = applicationMain.FoundationsMain.database;
            db.createPostAndReplyTables();

            reloadFromDatabase();
            System.out.println("Loaded " + allPosts.size() + " posts from database.");

        } catch (SQLException e) {
            System.err.println("Error initializing PostStore: " + e.getMessage());
        }
    }

    // ---------------------- Singleton Accessor ----------------------

    /**
     * @return the single shared instance of {@code PostStore}
     */
    public static PostStore getInstance() {
        return instance;
    }

    // ---------------------- PARTICIPATION UPDATE HOOK ----------------------

    /**
     * Recomputes participation statistics for the given user.
     * Triggered whenever the user adds, edits, or deletes a post.
     *
     * @param user the user whose participation stats are affected
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

    /**
     * Reloads all posts from the database into the in-memory list.
     * This completely replaces any existing in-memory state.
     */
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

    /**
     * Adds a newly created standalone post (also creates a thread internally).
     *
     * @param post the post object to insert
     */
    public void addPost(Post post) {
        if (post.getTitle().isBlank() || post.getBody().isBlank()) {
            System.out.println("Cannot create post: title or body is empty.");
            return;
        }
        try {
            db.insertPost(post);
            allPosts.add(post);

            System.out.println("Post added by " + post.getAuthor() +
                    " (DB ID: " + post.getDatabaseId() + ")");

            updateParticipationFor(new User(post.getAuthor()));

        } catch (SQLException e) {
            System.err.println("Database error while adding post: " + e.getMessage());
        }
    }

    /**
     * Adds a post to an existing thread.
     *
     * @param threadId the database ID of the thread
     * @param user     the user creating the post
     * @param title    optional title
     * @param content  body text of the post
     */
    public void addPostToThread(int threadId, User user, String title, String content) {
        if (content == null || content.isBlank()) {
            System.out.println("Cannot create post: body is empty.");
            return;
        }

        String safeTitle = (title == null || title.isBlank()) ? "Post" : title.trim();

        Post post = new Post(user, safeTitle, content.trim(), "", false, false);

        try {
            db.insertPost(post, threadId);
            allPosts.add(post);

            System.out.println("Post added to thread " + threadId +
                    " by " + post.getAuthor() +
                    " (DB ID: " + post.getDatabaseId() + ")");

            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while adding post to thread: " + e.getMessage());
        }
    }

    // ---------------------- READ ----------------------

    /**
     * @return a new list containing all posts in the system
     */
    public List<Post> getAllPosts() {
        return new ArrayList<>(allPosts);
    }

    /**
     * Loads posts belonging to a specific thread.
     *
     * @param threadId the thread identifier
     * @return the list of posts for the thread
     */
    public List<Post> getPostsForThread(int threadId) {
        subsetPosts = new ArrayList<>();
        try {
            List<Post> loaded = db.getPostsForThread(threadId);
            subsetPosts.addAll(loaded);

            // Merge into master list, preventing duplicates.
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

    /**
     * Updates the content of an existing post.
     *
     * @param localId the in-memory ID of the post
     * @param user    the user performing the edit
     * @param newTitle optional new title
     * @param newBody  optional new body
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean updatePost(int localId, User user, String newTitle, String newBody) {
        System.out.println("updatePost called with localId=" + localId +
                ", user=" + user.getUserName());

        Post target = null;

        // Search subset first
        for (Post p : subsetPosts) {
            if (p.getId() == localId) {
                target = p;
                break;
            }
        }

        // Fallback search in master list
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

        // Permission check
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

            updateParticipationFor(user);

        } catch (SQLException e) {
            System.err.println("Database error while updating post: " + e.getMessage());
        }
        return true;
    }

    // ---------------------- DELETE ----------------------

    /**
     * Deletes a post by local ID.
     *
     * @param localId the in-memory post ID
     * @param user    user performing the action
     * @return {@code true} if successful
     */
    public boolean deletePost(int localId, User user) {
        System.out.println("deletePost called with localId=" + localId +
                ", user=" + user.getUserName());

        Post target = null;

        // Search subset first
        for (Post p : subsetPosts) {
            if (p.getId() == localId) {
                target = p;
                break;
            }
        }

        // Fallback search in master list
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

            updateParticipationFor(user);
            return true;

        } catch (SQLException e) {
            System.err.println("Database error while deleting post: " + e.getMessage());
            return false;
        }
    }

    // ---------------------- Search ----------------------

    /**
     * Searches posts by keyword in title or body.
     *
     * @param keyword substring to match (case-insensitive)
     * @return list of matching posts
     */
    public List<Post> searchPosts(String keyword) {
        subsetPosts = new ArrayList<>();
        for (Post p : allPosts) {
            boolean titleMatch =
                    p.getTitle() != null &&
                    p.getTitle().toLowerCase().contains(keyword.toLowerCase());

            boolean bodyMatch =
                    p.getBody() != null &&
                    p.getBody().toLowerCase().contains(keyword.toLowerCase());

            if (titleMatch || bodyMatch) {
                subsetPosts.add(p);
            }
        }
        return subsetPosts;
    }

    /**
     * @return current subset list used by the GUI
     */
    public List<Post> getSubsetPosts() {
        return subsetPosts;
    }

    /** Clears the current subset view. */
    public void clearSubset() {
        subsetPosts.clear();
    }

    // ---------------------- Utility ----------------------

    /**
     * Removes a post and deletes it from the database.
     *
     * @param post the post to remove
     */
    public void removePost(Post post) {
        allPosts.remove(post);
        try {
            db.deletePost(post.getDatabaseId());
            System.out.println("Post removed (DB ID: " + post.getDatabaseId() + ")");
        } catch (SQLException e) {
            System.err.println("Database error while removing post: " + e.getMessage());
        }
    }

    /**
     * Updates only the body content of a post.
     *
     * @param post       the post to update
     * @param newContent the new content text
     */
    public void updatePostContent(Post post, String newContent) {
        post.setBody(newContent);
        try {
            db.updatePost(post.getDatabaseId(), post.getTitle(), newContent);
            System.out.println("Post content updated for " + post.getAuthor());
        } catch (SQLException e) {
            System.err.println("Database error while updating post content: " + e.getMessage());
        }
    }

    /** Clears all posts from memory (does not affect database). */
    public void clearAllPosts() {
        allPosts.clear();
    }

    // =====================================================================
    //  PARTICIPATION STATISTICS HELPERS
    // =====================================================================

    /**
     * Counts how many posts were authored by the given username.
     *
     * @param username user to count posts for
     * @return number of posts authored
     */
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

    /**
     * Counts the number of unique threads in which the given user has posted.
     *
     * @param username the user
     * @return count of unique threads participated in
     */
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
                    System.err.println(
                        "Error fetching threadId for post " +
                        p.getDatabaseId() + ": " + e.getMessage());
                }
            }
        }

        return threadIds.size();
    }

    /**
     * Counts distinct calendar days on which the user created posts.
     *
     * @param username the user
     * @return number of unique posting days
     */
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
