package guiStaffPosts;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import entityClasses.*;
import java.util.List;

/**
 * <h2>Class: ControllerStaffPosts</h2>
 *
 * <p><strong>Description:</strong><br>
 * Controller for the Staff Posts page.
 * This controller handles all staff-side actions involving:
 * </p>
 *
 * <ul>
 *   <li>creating and editing posts</li>
 *   <li>deleting posts</li>
 *   <li>creating, editing, and deleting replies</li>
 *   <li>search filtering</li>
 *   <li>navigating back to the staff home view</li>
 * </ul>
 *
 * <p>
 * It follows the same static, non-instantiated MVC pattern used throughout
 * the GUI system, meaning all controller responsibilities are implemented as
 * static methods.
 * </p>
 *
 * @version 2.10 — 2025-11-25 — Fully Javadoc-compatible version
 */
public final class ControllerStaffPosts {

    /** Current active search query; {@code null} means search is cleared. */
    private static String currentQuery = null;

    /** Private constructor; this class is not instantiated. */
    private ControllerStaffPosts() {}

    // -------------------------------------------------------------------------
    // POSTS
    // -------------------------------------------------------------------------

    /**
     * Returns the list of posts to display.
     * <p>
     * If a search query is active, the returned list is filtered. Otherwise,
     * the entire post list from {@link PostStore} is returned.
     * </p>
     *
     * @return a list of {@link Post} objects for the current view
     */
    public static List<Post> getPostsForView() {
        if (currentQuery == null || currentQuery.isBlank())
            return PostStore.getInstance().getAllPosts();

        return PostStore.getInstance().searchPosts(currentQuery);
    }

    /**
     * Creates a new post inside an existing thread.
     *
     * @param thread     the thread in which to create the post
     * @param titleField text field supplying the post title
     * @param bodyArea   text area supplying the post content
     * @param user       the staff user performing the action
     */
    public static void performPost(
            entityClasses.Thread thread,
            TextField titleField,
            TextArea bodyArea,
            User user) {

        if (thread == null) return;

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String body  = bodyArea.getText() == null ? "" : bodyArea.getText().trim();

        if (title.isBlank() || body.isBlank()) return;

        ModelStaffPosts.addPostToThread(user, thread, title, body);

        titleField.clear();
        bodyArea.clear();

        ViewStaffPosts.refreshPostsForThread(thread);
    }

    /**
     * Edits the body of an existing post.
     * Staff can edit any post regardless of authorship.
     *
     * @param user    the staff user performing the edit
     * @param post    the post to edit
     * @param newBody updated body text
     */
    public static void performPostEdit(User user, Post post, String newBody) {
        if (post == null || newBody == null || newBody.isBlank()) return;

        PostStore.getInstance().updatePost(post.getId(), user, post.getTitle(), newBody);
        PostStore.getInstance().reloadFromDatabase();

        ViewStaffPosts.refreshPosts();
    }

    /**
     * Deletes an existing post.
     * Staff can delete any post regardless of authorship.
     *
     * @param user the staff user performing the deletion
     * @param post the post to delete
     */
    public static void performDeletePost(User user, Post post) {
        if (post == null) return;

        PostStore.getInstance().deletePost(post.getId(), user);
        ViewStaffPosts.refreshPosts();
    }

    // -------------------------------------------------------------------------
    // REPLIES
    // -------------------------------------------------------------------------

    /**
     * Creates a reply to an existing post.
     * Staff override allows replying to any post.
     *
     * @param user      the staff user performing the reply
     * @param post      the post being replied to
     * @param replyArea text area containing the reply body
     */
    public static void performReply(User user, Post post, TextArea replyArea) {
        if (post == null) return;

        String text = replyArea.getText() == null ? "" : replyArea.getText().trim();
        if (text.isBlank()) return;

        ModelStaffPosts.addReply(user, post, text);
        replyArea.clear();

        ViewStaffPosts.refreshPosts();
    }

    /**
     * Edits a reply.
     *
     * @param replyId the local in-memory ID of the reply to update
     * @param user    the staff user performing the edit
     * @param newBody updated reply content
     */
    public static void performReplyEdit(int replyId, User user, String newBody) {
        if (newBody == null || newBody.isBlank()) return;

        ReplyStore.getInstance().updateReply(replyId, user, newBody);
        ViewStaffPosts.refreshPosts();
    }

    /**
     * Deletes a reply.
     *
     * @param replyId the local in-memory ID of the reply to remove
     * @param user    the staff user performing the deletion
     */
    public static void performReplyDelete(int replyId, User user) {
        ReplyStore.getInstance().deleteReply(replyId, user);
        ViewStaffPosts.refreshPosts();
    }

    /**
     * Retrieves all replies for a given post.
     *
     * @param post the post whose replies are requested
     * @return list of replies or an empty list if post is null
     */
    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }

    // -------------------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------------------

    /**
     * Performs a search across posts.
     *
     * @param query the search text; blank or null clears the search
     */
    public static void performSearch(String query) {
        currentQuery = (query == null || query.isBlank()) ? null : query.trim();
        ViewStaffPosts.refreshPosts();
    }

    /**
     * Clears any active search filter and refreshes the posts list.
     */
    public static void performClearSearch() {
        currentQuery = null;
        ViewStaffPosts.refreshPosts();
    }

    // -------------------------------------------------------------------------
    // NAVIGATION
    // -------------------------------------------------------------------------

    /**
     * Returns to the Staff Home page.
     *
     * @param user the staff user navigating back
     */
    public static void performBack(User user) {
        guiStaff.ViewStaffHome.displayStaffHome(ViewStaffPosts.theStage, user);
    }

    /**
     * Logs the user out and returns to the login screen.
     */
    public static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffPosts.theStage);
    }

    /**
     * Terminates the application immediately.
     */
    public static void performQuit() {
        System.exit(0);
    }
}
