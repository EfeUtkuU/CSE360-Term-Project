package guiStaffPosts;

import entityClasses.*;
import java.util.List;

/**
 * <p><strong>Class:</strong> ModelStaffPosts</p>
 *
 * <p><strong>Description:</strong>
 * Model layer for the Staff Posts page. Handles database persistence through
 * {@link PostStore} and {@link ReplyStore}. Staff users have full permissions to
 * edit or delete any post or reply. All IDs are managed by the database itself.
 * </p>
 *
 * @version 3.00 — 2025-10-27 — Updated for DB-managed IDs and full staff privileges
 */
public final class ModelStaffPosts {

    private ModelStaffPosts() {}

    // -------------------- POSTS --------------------

    /** Add a new post authored by the staff user. */
    public static void addPost(User user, String title, String content) {
        if (content == null || content.isBlank()) return;
        String safeTitle = (title == null || title.isBlank()) ? "Staff Post" : title.trim();
        Post post = new Post(user, safeTitle, content.trim(), "", false, false);
        PostStore.getInstance().addPost(post);
    }

    /** Edit any post (staff can edit all). */
    public static void editPost(User user, Post post, String newTitle, String newBody) {
        if (post == null) return;
        String updatedTitle = (newTitle == null || newTitle.isBlank()) ? post.getTitle() : newTitle.trim();
        String updatedBody  = (newBody == null || newBody.isBlank()) ? post.getBody() : newBody.trim();

        PostStore.getInstance().updatePost(post.getId(), user, updatedTitle, updatedBody);
        PostStore.getInstance().reloadFromDatabase();
    }

    /** Delete any post (staff override). */
    public static void deletePost(User user, Post post) {
        if (post == null) return;
        PostStore.getInstance().deletePost(post.getId(), user);
    }

    /** Retrieve all posts from database. */
    public static List<Post> getAllPosts() {
        return PostStore.getInstance().getAllPosts();
    }

    /** Search posts by keyword. */
    public static List<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.isBlank())
            return getAllPosts();
        return PostStore.getInstance().searchPosts(keyword.trim());
    }

    // -------------------- REPLIES --------------------

    /** Add a reply to a specific post (staff can reply to any). */
    public static void addReply(User user, Post post, String text) {
        if (post == null || text == null || text.isBlank()) return;
        Reply reply = new Reply(post, user, text.trim(), false);
        ReplyStore.getInstance().addReply(reply);
    }

    /** Edit any reply (staff override). */
    public static void editReply(User user, int replyId, String newBody) {
        if (newBody == null || newBody.isBlank()) return;
        ReplyStore.getInstance().updateReply(replyId, user, newBody.trim());
    }

    /** Delete any reply (staff override). */
    public static void deleteReply(User user, int replyId) {
        ReplyStore.getInstance().deleteReply(replyId, user);
    }
}
