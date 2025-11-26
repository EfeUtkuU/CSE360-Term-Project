package guiStudentPosts;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import entityClasses.*;
import java.util.List;

/**
 * Controller for the Student Posts page.
 */
public class ControllerStudentPosts {

    private static String currentQuery = null;

    public ControllerStudentPosts() {}

    // -------------------- Post Retrieval --------------------

    public static List<Post> getPostsForView() {
        if (currentQuery == null || currentQuery.isBlank())
            return PostStore.getInstance().getAllPosts();

        return PostStore.getInstance().searchPosts(currentQuery);
    }

    // -------------------- Search --------------------

    public static void performSearch(String query) {
        currentQuery = (query == null || query.isBlank()) ? null : query.trim();
        ViewStudentPosts.refreshPosts();
    }

    public static void performClearSearch() {
        currentQuery = null;
        ViewStudentPosts.refreshPosts();
    }

    // -------------------- Session --------------------

    public static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentPosts.theStage);
    }

    public static void performQuit() {
        System.exit(0);
    }

    // -------------------- Posts --------------------

    /**
     * Student posting INSIDE a thread.
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

        ModelStudentPosts.addPostToThread(user, thread, title, body);

        titleField.clear();
        bodyArea.clear();

        ViewStudentPosts.refreshPostsForThread(thread);
    }

    /** Students can delete their own posts OR staff can override. */
    public static void performDeletePost(User user, Post post) {
        if (post == null) return;

        if (post.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
            PostStore.getInstance().deletePost(post.getId(), user);
            PostStore.getInstance().reloadFromDatabase();
            ViewStudentPosts.refreshPosts();
        }
    }

    // -------------------- Replies --------------------

    public static void performReply(User user, Post post, TextArea replyArea) {
        if (post == null) return;

        String text = replyArea.getText() == null ? "" : replyArea.getText().trim();
        if (text.isBlank()) return;

        ModelStudentPosts.addReply(user, post, text);
        replyArea.clear();

        ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
        ViewStudentPosts.refreshPosts();
    }

    /** Student edits only their own replies; staff override allowed. */
    public static void performReplyEdit(int replyId, User user, String newBody) {
        if (newBody == null || newBody.isBlank()) return;

        ReplyStore.getInstance().updateReply(replyId, user, newBody);
        ViewStudentPosts.refreshPosts();
    }

    /** Student deletes own replies; staff override allowed. */
    public static void performReplyDelete(int replyId, User user) {
        ReplyStore.getInstance().deleteReply(replyId, user);
        ViewStudentPosts.refreshPosts();
    }

    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }
}
