package guiStaffPosts;

import entityClasses.*;
import java.util.List;

/**
 * <p><strong>Class:</strong> ModelStaffPosts</p>
 *
 * <p><strong>Description:</strong>
 * Staff-level CRUD. Staff can modify ANY post or reply across ANY thread.
 * </p>
 */
public final class ModelStaffPosts {

    private ModelStaffPosts() {}

    // --------------------------------------------------------------------
    // THREADS
    // --------------------------------------------------------------------

    /**
     * Creates a new thread plus a root post, authored by a staff user.
     *
     * @param user        Staff user
     * @param threadTitle Thread title
     * @param postTitle   Root post title
     * @param content     Root post body
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
     * ⚠️ Backwards compatible helper used by old controllers:
     * Treats "addPost" as "create a new thread with a root post".
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

    public static void editPost(User user, Post post, String newTitle, String newBody) {
        if (post == null) return;

        String t = (newTitle == null || newTitle.isBlank()) ? post.getTitle() : newTitle.trim();
        String b = (newBody == null || newBody.isBlank()) ? post.getBody() : newBody.trim();

        // 🔑 use databaseId, not in-memory id
        PostStore.getInstance().updatePost(post.getDatabaseId(), user, t, b);
        PostStore.getInstance().reloadFromDatabase();
    }

    public static void deletePost(User user, Post post) {
        if (post == null) return;
        // 🔑 use databaseId, not in-memory id
        PostStore.getInstance().deletePost(post.getDatabaseId(), user);
    }


    public static List<Post> getPostsForThread(entityClasses.Thread thread) {
        if (thread == null) return List.of();
        return PostStore.getInstance().getPostsForThread(thread.getId());
    }

    // --------------------------------------------------------------------
    // REPLIES
    // --------------------------------------------------------------------

    public static void addReply(User user, Post post, String text) {
        if (post == null || text == null || text.isBlank()) return;
        ReplyStore.getInstance().addReply(new Reply(post, user, text.trim(), false));
    }

    public static void editReply(User user, Reply reply, String newBody) {
        if (reply == null) return;
        if (newBody == null || newBody.isBlank()) return;
        ReplyStore.getInstance().updateReply(reply.getId(), user, newBody.trim());
    }

    public static void deleteReply(User user, Reply reply) {
        if (reply == null) return;
        ReplyStore.getInstance().deleteReply(reply.getId(), user);
    }
}
