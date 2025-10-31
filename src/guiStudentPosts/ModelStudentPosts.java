package guiStudentPosts;

import entityClasses.*;
import java.util.List;

/**
 * <p><strong>Class:</strong> ModelStudentPosts</p>
 *
 * <p><strong>Description:</strong>
 * The model layer for the Student Posts page. This class wraps {@link PostStore}
 * and {@link ReplyStore} to perform create, read, update, and delete (CRUD) operations
 * while enforcing student-level permissions.
 * </p>
 *
 * <p><strong>Key Rules:</strong></p>
 * <ul>
 *   <li>Students may edit or delete only their own posts and replies.</li>
 *   <li>Timestamps are automatically handled by {@link Post} and {@link Reply} setters.</li>
 * </ul>
 *
 * @version 2.10 — 2025-10-27 — Updated for DB-managed post/reply IDs
 */
public final class ModelStudentPosts {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ModelStudentPosts() { }

    // ---------------------- Posts ----------------------

    /**
     * Adds a new post on behalf of a student.
     * Automatically sanitizes title and body fields.
     *
     * @param user the current user creating the post
     * @param title the title of the post; if blank, defaults to "Student Post"
     * @param content the body content of the post
     */
    public static void addPost(User user, String title, String content) {
        if (content == null || content.isBlank()) return;
        String safeTitle = (title == null || title.isBlank()) ? "Student Post" : title.trim();
        Post newPost = new Post(user, safeTitle, content.trim(), "", false, false);
        PostStore.getInstance().addPost(newPost);
    }

    /**
     * Edits an existing post if the current user is the author.
     *
     * @param user the user requesting the edit
     * @param postId the ID of the post to edit
     * @param newTitle the new title; if blank, retains the old one
     * @param newBody the new body content; if {@code null}, retains the old one
     */
    public static void editPost(User user, int postId, String newTitle, String newBody) {
        PostStore store = PostStore.getInstance();
        for (Post p : store.getAllPosts()) {
            if (p.getId() == postId) {
                if (p.getAuthor().equals(user.getUserName())) {
                    String t = (newTitle == null || newTitle.isBlank()) ? p.getTitle() : newTitle.trim();
                    String b = (newBody == null) ? p.getBody() : newBody.trim();
                    store.updatePost(postId, user, t, b);
                }
                return;
            }
        }
    }

    /**
     * Deletes a post if the current user is the author.
     *
     * @param user the user requesting deletion
     * @param postId the ID of the post to delete
     */
    public static void deletePost(User user, int postId) {
        PostStore store = PostStore.getInstance();
        for (Post p : store.getAllPosts()) {
            if (p.getId() == postId && p.getAuthor().equals(user.getUserName())) {
                store.deletePost(postId, user);
                return;
            }
        }
    }

    /**
     * Retrieves all posts currently available.
     *
     * @return a list of all {@link Post} objects
     */
    public static List<Post> getAllPosts() {
        return PostStore.getInstance().getAllPosts();
    }

    /**
     * Searches posts by a given keyword.
     *
     * @param keyword the search keyword; if blank, returns all posts
     * @return a list of {@link Post} objects matching the search
     */
    public static List<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllPosts();
        return PostStore.getInstance().searchPosts(keyword.trim());
    }

    // ---------------------- Replies ----------------------

    /**
     * Adds a new reply for a given post.
     * Uses the post’s database ID to maintain referential integrity.
     *
     * @param user the current user creating the reply
     * @param post the parent post to which the reply belongs
     * @param text the reply content text
     */
    public static void addReply(User user, Post post, String text) {
        if (post == null || text == null || text.isBlank()) return;
        Reply reply = new Reply(post, user, text.trim(), false);
        ReplyStore.getInstance().addReply(reply);
    }

    /**
     * Edits an existing reply if the user is the author.
     *
     * @param user the user requesting the edit
     * @param replyId the ID of the reply to edit
     * @param newBody the updated body text
     */
    public static void editReply(User user, int replyId, String newBody) {
        if (newBody == null || newBody.isBlank()) return;
        ReplyStore.getInstance().updateReply(replyId, user, newBody.trim());
    }

    /**
     * Deletes a reply if the user is the author.
     *
     * @param user the user requesting deletion
     * @param replyId the ID of the reply to delete
     */
    public static void deleteReply(User user, int replyId) {
        ReplyStore.getInstance().deleteReply(replyId, user);
    }

    /**
     * Loads all replies for a given post directly from the database.
     *
     * @param post the parent post whose replies should be loaded
     * @return a list of {@link Reply} objects belonging to the post,
     *         or an empty list if {@code post} is {@code null}
     */
    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }
}
