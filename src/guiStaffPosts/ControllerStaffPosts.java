package guiStaffPosts;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import entityClasses.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><strong>Class:</strong> ControllerStaffPosts</p>
 *
 * <p><strong>Description:</strong>
 * Controller for the Staff Posts page. Handles CRUD operations and search for posts
 * and replies. Staff users can edit or delete any post or reply regardless of author.
 * Uses DB-managed IDs for persistence.</p>
 *
 * @version 2.20 — 2025-11-10 — Added “Search by User” filtering
 */
public final class ControllerStaffPosts {

    /** Current active search query; null means no filtering. */
    private static String currentQuery = null;

    private ControllerStaffPosts() {}

    // -------------------- POSTS --------------------

    /** Returns all posts or the filtered list if a search is active. */
    public static List<Post> getPostsForView() {
        if (currentQuery == null || currentQuery.isBlank())
            return PostStore.getInstance().getAllPosts();

        String q = currentQuery.trim();

        // NEW: Handle "Search by username only" queries
        if (q.toLowerCase().startsWith("user:")) {
            String username = q.substring(5).trim().toLowerCase();
            return PostStore.getInstance().getAllPosts()
                    .stream()
                    .filter(p -> p.getAuthor() != null
                            && p.getAuthor().toLowerCase().contains(username))
                    .collect(Collectors.toList());
        }

        // Default: keyword search (title/body/etc.)
        return PostStore.getInstance().searchPosts(q);
    }

    /** Adds a new post to the database. */
    public static void performPost(TextField titleField, TextArea bodyArea, User user) {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String body  = bodyArea.getText() == null ? "" : bodyArea.getText().trim();
        if (title.isBlank() || body.isBlank()) return;

        ModelStaffPosts.addPost(user, title, body);
        titleField.clear();
        bodyArea.clear();
        ViewStaffPosts.refreshPosts();
    }

    /** Updates the contents of a post. Staff can edit any post. */
    public static void performPostEdit(User user, Post post, String newBody) {
        if (post == null || newBody == null || newBody.isBlank()) return;
        PostStore.getInstance().updatePost(post.getId(), user, post.getTitle(), newBody);
        PostStore.getInstance().reloadFromDatabase();
        ViewStaffPosts.refreshPosts();
    }

    /** Deletes a post by its database ID (staff override). */
    public static void performDeletePost(User user, Post post) {
        if (post == null) return;
        PostStore.getInstance().deletePost(post.getId(), user);
        ViewStaffPosts.refreshPosts();
    }

    // -------------------- REPLIES --------------------

    /** Adds a reply to a specific post. */
    public static void performReply(User user, Post post, TextArea replyArea) {
        String text = replyArea.getText() == null ? "" : replyArea.getText().trim();
        if (text.isBlank() || post == null) return;

        ModelStaffPosts.addReply(user, post, text);
        replyArea.clear();
        ViewStaffPosts.refreshPosts();
    }

    /** Updates an existing reply’s body (staff override). */
    public static void performReplyEdit(int replyId, User user, String newBody) {
        if (newBody == null || newBody.isBlank()) return;
        ReplyStore.getInstance().updateReply(replyId, user, newBody);
        ViewStaffPosts.refreshPosts();
    }

    /** Deletes any reply (staff override). */
    public static void performReplyDelete(int replyId, User user) {
        ReplyStore.getInstance().deleteReply(replyId, user);
        ViewStaffPosts.refreshPosts();
    }

    /** Returns all replies for a given post (by DB ID). */
    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }

    // -------------------- SEARCH --------------------

    /** Applies a search query and refreshes the posts view. */
    public static void performSearch(String query) {
        currentQuery = (query == null || query.isBlank()) ? null : query.trim();
        ViewStaffPosts.refreshPosts();
    }

    /** Clears the current search query and refreshes the posts view. */
    public static void performClearSearch() {
        currentQuery = null;
        ViewStaffPosts.refreshPosts();
    }

    // -------------------- NAVIGATION --------------------

    /** Returns to staff home. */
    public static void performBack(User user) {
        guiStaff.ViewStaffHome.displayStaffHome(ViewStaffPosts.theStage, user);
    }

    /** Logs out the current user. */
    public static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffPosts.theStage);
    }

    /** Quits the application. */
    public static void performQuit() {
        System.exit(0);
    }
}
