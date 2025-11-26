package guiStaffPosts;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import entityClasses.*;
import java.util.List;

/**
 * Controller for the Staff Posts page.
 */
public final class ControllerStaffPosts {

    /** Current active search query; null means no filtering. */
    private static String currentQuery = null;

    private ControllerStaffPosts() {}

    // -------------------- POSTS --------------------

    /** Returns posts for search mode OR entire DB when no search is active. */
    public static List<Post> getPostsForView() {
        if (currentQuery == null || currentQuery.isBlank())
            return PostStore.getInstance().getAllPosts();

        return PostStore.getInstance().searchPosts(currentQuery);
    }

    /**
     * Adds a new post inside an existing thread.
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

    /** Staff can edit any post. */
    public static void performPostEdit(User user, Post post, String newBody) {
        if (post == null || newBody == null || newBody.isBlank()) return;

        PostStore.getInstance().updatePost(post.getId(), user, post.getTitle(), newBody);
        PostStore.getInstance().reloadFromDatabase();

        ViewStaffPosts.refreshPosts();
    }

    /** Staff can delete any post. */
    public static void performDeletePost(User user, Post post) {
        if (post == null) return;

        PostStore.getInstance().deletePost(post.getId(), user);
        ViewStaffPosts.refreshPosts();
    }

    // -------------------- REPLIES --------------------

    /** Add reply to a post (staff override applies). */
    public static void performReply(User user, Post post, TextArea replyArea) {
        if (post == null) return;

        String text = replyArea.getText() == null ? "" : replyArea.getText().trim();
        if (text.isBlank()) return;

        ModelStaffPosts.addReply(user, post, text);
        replyArea.clear();

        ViewStaffPosts.refreshPosts();
    }

    /** Staff can edit any reply. */
    public static void performReplyEdit(int replyId, User user, String newBody) {
        if (newBody == null || newBody.isBlank()) return;

        ReplyStore.getInstance().updateReply(replyId, user, newBody);
        ViewStaffPosts.refreshPosts();
    }

    /** Staff can delete any reply. */
    public static void performReplyDelete(int replyId, User user) {
        ReplyStore.getInstance().deleteReply(replyId, user);
        ViewStaffPosts.refreshPosts();
    }

    /** Get replies for a post. */
    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }

    // -------------------- SEARCH --------------------

    public static void performSearch(String query) {
        currentQuery = (query == null || query.isBlank()) ? null : query.trim();
        ViewStaffPosts.refreshPosts();
    }

    public static void performClearSearch() {
        currentQuery = null;
        ViewStaffPosts.refreshPosts();
    }

    // -------------------- NAVIGATION --------------------

    public static void performBack(User user) {
        guiStaff.ViewStaffHome.displayStaffHome(ViewStaffPosts.theStage, user);
    }

    public static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffPosts.theStage);
    }

    public static void performQuit() {
        System.exit(0);
    }
}
