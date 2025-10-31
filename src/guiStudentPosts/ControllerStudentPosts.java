package guiStudentPosts;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import entityClasses.*;
import java.util.List;

/**
 * <p><strong>Class:</strong> ControllerStudentPosts</p>
 *
 * <p><strong>Description:</strong>
 * Controller for the Student Posts page. Handles searching, creating, editing,
 * and deleting of posts and replies. It delegates persistence operations to
 * {@link PostStore} and {@link ReplyStore} through {@link ModelStudentPosts}.
 * Students may edit or delete only their own posts and replies.
 * </p>
 *
 * @version 2.20 — 2025-10-27 — Finalized for DB-managed IDs and full DB synchronization
 */
public class ControllerStudentPosts {

    /** Current search query; {@code null} or blank means no filtering. */
    private static String currentQuery = null;

    /** Default constructor for {@code ControllerStudentPosts}. */
    public ControllerStudentPosts() { }

    // -------------------- Post Retrieval --------------------

    /**
     * Returns posts appropriate for the current search state.
     *
     * @return a list of {@link Post} objects filtered by the current query,
     *         or all posts if no query is active
     */
    public static List<Post> getPostsForView() {
        if (currentQuery == null || currentQuery.isBlank())
            return PostStore.getInstance().getAllPosts();
        return PostStore.getInstance().searchPosts(currentQuery);
    }

    // -------------------- Search --------------------

    /**
     * Performs a search operation and updates the view.
     *
     * @param query the user-entered search string; if {@code null} or blank,
     *              the search is cleared
     */
    public static void performSearch(String query) {
        currentQuery = (query == null || query.isBlank()) ? null : query.trim();
        ViewStudentPosts.refreshPosts();
    }

    /**
     * Clears the current search filter and reloads all posts.
     */
    public static void performClearSearch() {
        currentQuery = null;
        ViewStudentPosts.refreshPosts();
    }

    // -------------------- Session --------------------

    /**
     * Logs the user out and returns to the login screen.
     */
    public static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentPosts.theStage);
    }

    /**
     * Exits the application.
     */
    public static void performQuit() {
        System.exit(0);
    }

    // -------------------- Posts --------------------

    /**
     * Creates a new post and saves it through the model layer.
     *
     * @param titleField the text field containing the post title
     * @param bodyArea the text area containing the post body
     * @param user the currently logged-in user creating the post
     */
    public static void performPost(TextField titleField, TextArea bodyArea, User user) {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String body  = bodyArea.getText() == null ? "" : bodyArea.getText().trim();
        if (title.isBlank() || body.isBlank()) return;

        ModelStudentPosts.addPost(user, title, body);

        titleField.clear();
        bodyArea.clear();

        // Reload posts from the database to ensure updated IDs
        PostStore.getInstance().reloadFromDatabase();
        ViewStudentPosts.refreshPosts();
    }

    /**
     * Deletes the specified post if the user is the author or has staff privileges.
     *
     * @param user the current user requesting deletion
     * @param post the post to delete
     */
    public static void performDeletePost(User user, Post post) {
        if (post == null) return;

        if (post.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
            PostStore.getInstance().deletePost(post.getId(), user);
            PostStore.getInstance().reloadFromDatabase();
            ViewStudentPosts.refreshPosts();
        }
    }

    // -------------------- Replies --------------------

    /**
     * Adds a reply to a specific post.
     *
     * @param user the current user creating the reply
     * @param post the parent post to which the reply is added
     * @param replyArea the text area containing the reply text
     */
    public static void performReply(User user, Post post, TextArea replyArea) {
        String text = replyArea.getText() == null ? "" : replyArea.getText().trim();
        if (text.isBlank() || post == null) return;

        ModelStudentPosts.addReply(user, post, text);
        replyArea.clear();

        // Reload replies for consistency with the database
        ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
        ViewStudentPosts.refreshPosts();
    }

    /**
     * Edits a reply if the user is the author or has staff privileges.
     *
     * @param replyId the ID of the reply to edit
     * @param user the current user requesting the edit
     * @param newBody the updated reply text
     */
    public static void performReplyEdit(int replyId, User user, String newBody) {
        if (newBody == null || newBody.isBlank()) return;
        ReplyStore.getInstance().updateReply(replyId, user, newBody);
        ViewStudentPosts.refreshPosts();
    }

    /**
     * Deletes a reply if the user is the author or has staff privileges.
     *
     * @param replyId the ID of the reply to delete
     * @param user the current user requesting deletion
     */
    public static void performReplyDelete(int replyId, User user) {
        ReplyStore.getInstance().deleteReply(replyId, user);
        ViewStudentPosts.refreshPosts();
    }

    /**
     * Retrieves all replies for the specified post.
     *
     * @param post the parent post whose replies are to be fetched
     * @return a list of {@link Reply} objects associated with the post,
     *         or an empty list if {@code post} is {@code null}
     */
    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }
}
