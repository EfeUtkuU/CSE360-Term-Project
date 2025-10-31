package entityClasses;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import database.Database;

/**
 * <p><strong>Class:</strong> PostStore</p>
 *
 * <p><strong>Description:</strong>
 * The {@code PostStore} class acts as a central manager for all {@link Post} objects.
 * It follows the Singleton design pattern, ensuring that only one shared instance
 * exists throughout the application. It provides CRUD operations (create, read, update,
 * delete) and search functionality for posts.</p>
 *
 * <p>This class synchronizes posts with the underlying database (via {@link Database})
 * and also keeps an in-memory list for quick access and filtering.</p>
 *
 * @version 2.30 — 2025-10-27 — Finalized for DB-managed IDs and live synchronization
 */
public class PostStore {

    // ---------------------- Fields ----------------------

    private static final PostStore instance = new PostStore();

    private final List<Post> allPosts = new ArrayList<>();
    private List<Post> subsetPosts = new ArrayList<>();
    private Database db;


    // ---------------------- Constructor ----------------------

    private PostStore() {
        try {
            db = new Database();
            db.connectToDatabase();
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

    // ---------------------- DB Sync Helpers ----------------------

    /** Reload all posts from the database, replacing the in-memory list. */
    public void reloadFromDatabase() {
        try {
            allPosts.clear();
            allPosts.addAll(db.getAllPosts());
        } catch (SQLException e) {
            System.err.println("Error reloading posts: " + e.getMessage());
        }
    }

    // ---------------------- CRUD: CREATE ----------------------

    public void addPost(Post post) {
        if (post.getTitle().isBlank() || post.getBody().isBlank()) {
            System.out.println("Cannot create post: title or body is empty.");
            return;
        }
        try {
            db.insertPost(post);  // assigns databaseId internally
            allPosts.add(post);
            System.out.println("Post added by " + post.getAuthor() +
                               " (DB ID: " + post.getDatabaseId() + ")");
        } catch (SQLException e) {
            System.err.println("Database error while adding post: " + e.getMessage());
        }
    }

    // ---------------------- CRUD: READ ----------------------

    public List<Post> getAllPosts() {
        return new ArrayList<>(allPosts);
    }

    // ---------------------- CRUD: UPDATE ----------------------

    public boolean updatePost(int localId, User user, String newTitle, String newBody) {
        for (Post p : allPosts) {
            if (p.getId() == localId) {
                if (p.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    p.setTitle(newTitle);
                    p.setBody(newBody);
                    try {
                        db.updatePost(p.getDatabaseId(), newTitle, newBody);
                        System.out.println("Post updated by " + user.getUserName());
                    } catch (SQLException e) {
                        System.err.println("Database error while updating post: " + e.getMessage());
                    }
                    return true;
                } else {
                    System.out.println("Permission denied: Only staff or the author can edit this post.");
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------- CRUD: DELETE ----------------------

    public boolean deletePost(int localId, User user) {
        for (Post p : new ArrayList<>(allPosts)) {
            if (p.getId() == localId) {
                if (p.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    try {
                        db.deletePost(p.getDatabaseId());
                        allPosts.remove(p);
                        System.out.println("Post deleted by " + user.getUserName() +
                                           " (DB ID: " + p.getDatabaseId() + ")");
                        return true;
                    } catch (SQLException e) {
                        System.err.println("Database error while deleting post: " + e.getMessage());
                        return false;
                    }
                } else {
                    System.out.println("Permission denied: Only staff or the author can delete this post.");
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------- Search and Filtering ----------------------

    public List<Post> searchPosts(String keyword) {
        subsetPosts = new ArrayList<>();
        for (Post p : allPosts) {
            if (p.getTitle().toLowerCase().contains(keyword.toLowerCase())
                    || p.getBody().toLowerCase().contains(keyword.toLowerCase())) {
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

    // ---------------------- Utility Methods ----------------------

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
        allPosts.clear();          // or execute DELETE FROM posts in DB test mode
    }
}
