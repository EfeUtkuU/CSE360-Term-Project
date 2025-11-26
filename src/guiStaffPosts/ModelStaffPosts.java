package guiStaffPosts;

import entityClasses.*;
import java.util.List;

/**
 * <h2>Class: ModelStaffPosts</h2>
 *
 * <p><strong>Description:</strong><br>
 * Model layer for the Staff Posts subsystem. This class contains all
 * staff-level CRUD logic related to:
 * </p>
 *
 * <ul>
 *   <li>creating new threads</li>
 *   <li>creating, editing, and deleting posts</li>
 *   <li>creating, editing, and deleting replies</li>
 *   <li>full staff override permissions (staff may modify any content)</li>
 * </ul>
 *
 * <p>
 * Staff users have full authority to modify any post or reply, regardless of
 * the original author. This model acts as the business logic layer beneath
 * the {@link guiStaffPosts.ControllerStaffPosts} controller.
 * </p>
 *
 * @version 1.00 — 2025-11-25 — Fully Javadoc-compliant version
 */
public final class ModelStaffPosts {

    /** Private constructor; class is static and not instantiated. */
    private ModelStaffPosts() {}

    // --------------------------------------------------------------------
    // THREADS
    // --------------------------------------------------------------------

    /**
     * Creates a new thread and a root post, authored by a staff user.
     * <p>
     * This is the preferred modern method for thread creation.
     * The thread title, post title, and body are sanitized (falling back
     * to default values if blank).
     * </p>
     *
     * @param user        staff user creating the thread
     * @param threadTitle title for the new thread (defaults to "New Thread")
     * @param postTitle   title for the root post (defaults to "Root Post")
     * @param content     body of the root post
     * @return the created {@link entityClasses.Thread}, or {@code null} if creation fails
     */
    public static entityClasses.Thread createNewThread(
            User user,
            String threadTitle,
            String postTitle,
            String content
    ) {
        if (content == null || content.isBlank()) return null;

        String safeThreadTitle = (threadTitle == null || threadTitle.isBlank())
                ? "New Thread"
                : threadTitle.trim();

        String safePostTitle = (postTitle == null || postTitle.isBlank())
                ? "Root Post"
                : postTitle.trim();

        entityClasses.Thread thread = ThreadStore.getInstance()
                .createThread(safeThreadTitle, user.getUserName());

        if (thread == null) return null;

        PostStore.getInstance()
                .addPostToThread(thread.getId(), user, safePostTitle, content.trim());

        return thread;
    }

    /**
     * Backwards-compatible helper retained for older controllers.
     * <p>
     * Treats a post creation as a request to create a new thread whose root
     * post carries the given title and content.
     * </p>
     *
     * @param user    staff user creating the post
     * @param title   thread + post title
     * @param content post body
     */
    public static void addPost(User user, String title, String content) {
        if (content == null || content.isBlank()) return;

        String safeTitle = (title == null || title.isBlank())
                ? "Staff Post"
                : title.trim();

        createNewThread(user, safeTitle, safeTitle, content.trim());
    }

    // --------------------------------------------------------------------
    // POSTS
    // --------------------------------------------------------------------

    /**
     * Adds a post to an existing thread.
     *
     * @param user    staff user performing the action
     * @param thread  the thread to post in
     * @param title   post title (defaults to "Staff Post")
     * @param content post body text
     */
    public static void addPostToThread(
            User user,
            entityClasses.Thread thread,
            String title,
            String content
    ) {
        if (thread == null || content == null || content.isBlank()) return;

        String safe = (title == null || title.isBlank()) ? "Staff Post" : title.trim();

        PostStore.getInstance()
                .addPostToThread(thread.getId(), user, safe, content.trim());
    }

    /**
     * Edits an existing post. Staff has full override permissions.
     * <p>
     * If a field is blank, the original value is kept.
     * This method uses the post's <strong>database ID</strong> for updates.
     * </p>
     *
     * @param user     staff user editing the post
     * @param post     the post to edit
     * @param newTitle new title (or null/blank to keep original)
     * @param newBody  new body text (or null/blank to keep original)
     */
    public static void editPost(User user, Post post, String newTitle, String newBody) {
        if (post == null) return;

        String t = (newTitle == null || newTitle.isBlank()) ? post.getTitle() : newTitle.trim();
        String b = (newBody == null || newBody.isBlank()) ? post.getBody()  : newBody.trim();

        PostStore.getInstance().updatePost(post.getDatabaseId(), user, t, b);
        PostStore.getInstance().reloadFromDatabase();
    }

    /**
     * Deletes a post. Staff can remove any post across the entire system.
     * Uses the post's database ID.
     *
     * @param user staff user performing the deletion
     * @param post post to delete
     */
    public static void deletePost(User user, Post post) {
        if (post == null) return;
        PostStore.getInstance().deletePost(post.getDatabaseId(), user);
    }

    /**
     * Returns the list of posts for a specified thread.
     *
     * @param thread the thread whose posts should be returned
     * @return list of matching posts; empty list if thread is null
     */
    public static List<Post> getPostsForThread(entityClasses.Thread thread) {
        if (thread == null) return List.of();
        return PostStore.getInstance().getPostsForThread(thread.getId());
    }

    // --------------------------------------------------------------------
    // REPLIES
    // --------------------------------------------------------------------

    /**
     * Adds a new reply to a post on behalf of a staff user.
     *
     * @param user staff user creating the reply
     * @param post post being replied to
     * @param text reply body
     */
    public static void addReply(User user, Post post, String text) {
        if (post == null || text == null || text.isBlank()) return;
        ReplyStore.getInstance().addReply(new Reply(post, user, text.trim(), false));
    }

    /**
     * Edits an existing reply. Staff has full override permissions.
     *
     * @param user    staff user editing the reply
     * @param reply   reply to edit
     * @param newBody updated body text
     */
    public static void editReply(User user, Reply reply, String newBody) {
        if (reply == null) return;
        if (newBody == null || newBody.isBlank()) return;
        ReplyStore.getInstance().updateReply(reply.getId(), user, newBody.trim());
    }

    /**
     * Deletes a reply. Staff may delete any reply across any thread.
     *
     * @param user  staff user performing the deletion
     * @param reply reply to delete
     */
    public static void deleteReply(User user, Reply reply) {
        if (reply == null) return;
        ReplyStore.getInstance().deleteReply(reply.getId(), user);
    }
}
