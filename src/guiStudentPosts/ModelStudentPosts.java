package guiStudentPosts;

import entityClasses.*;
import java.util.List;

/**
 * <p><strong>Class:</strong> ModelStudentPosts</p>
 *
 * <p><strong>Description:</strong>
 * Handles student-level CRUD for posts & replies *within threads*.
 * Students may:
 *   • Create new threads
 *   • Create posts inside existing threads
 *   • Edit/delete only their own content
 * </p>
 */
public final class ModelStudentPosts {

    private ModelStudentPosts() {}

    // --------------------------------------------------------------------
    //  THREAD CREATION BY STUDENT
    // --------------------------------------------------------------------

    /**
     * Creates a new thread and inserts the root post.
     *
     * Students supply:
     *   • threadTitle  (shown in thread list)
     *   • postTitle    (root post title)
     *   • content      (root post body)
     *
     * @return the newly created thread, or null if content is blank
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

        // Create thread in DB
        entityClasses.Thread thread = ThreadStore.getInstance()
                .createThread(safeThreadTitle, user.getUserName());

        if (thread == null) return null;

        // Add the root post inside the new thread
        PostStore.getInstance()
                .addPostToThread(thread.getId(), user, safePostTitle, content.trim());

        return thread;
    }

    /**
     * ⚠️ Backwards compatible helper used by old controllers:
     * Treats a "post" as a new thread + root post.
     *
     * Old GUI calls:
     *   ModelStudentPosts.addPost(user, title, body)
     *
     * New behavior:
     *   • Creates a new thread whose title = title (or "Student Post" if blank)
     *   • Root post title = same as thread title
     *   • Root post body = content
     */
    public static void addPost(User user, String title, String content) {
        if (content == null || content.isBlank()) return;

        String safeTitle = (title == null || title.isBlank())
                ? "Student Post"
                : title.trim();

        createNewThread(user, safeTitle, safeTitle, content.trim());
    }

    // --------------------------------------------------------------------
    //  POSTS
    // --------------------------------------------------------------------

    /**
     * Adds a new post inside an existing thread.
     * (You can use this later if you support multi-post threads.)
     */
    public static void addPostToThread(
            User user,
            entityClasses.Thread thread,
            String title,
            String content
    ) {
        if (thread == null || content == null || content.isBlank()) return;

        String safeTitle = (title == null || title.isBlank())
                ? "Student Post"
                : title.trim();

        PostStore.getInstance()
                .addPostToThread(thread.getId(), user, safeTitle, content.trim());
    }

    /**
     * Student edits their own post only.
     */
    public static void editPost(User user, Post post, String newTitle, String newBody) {
        if (post == null) return;
        if (!post.getAuthor().equals(user.getUserName())) return;

        String t = (newTitle == null || newTitle.isBlank()) ? post.getTitle() : newTitle.trim();
        String b = (newBody == null || newBody.isBlank()) ? post.getBody() : newBody.trim();

        PostStore.getInstance().updatePost(post.getDatabaseId(), user, t, b);
    }

    /**
     * Student deletes only their own post.
     */
    public static void deletePost(User user, Post post) {
        if (post == null) return;
        if (!post.getAuthor().equals(user.getUserName())) return;

        PostStore.getInstance().deletePost(post.getDatabaseId(), user);
    }


    /**
     * Get all posts for a thread.
     */
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
        if (!reply.getAuthor().equals(user.getUserName())) return;
        if (newBody == null || newBody.isBlank()) return;

        ReplyStore.getInstance().updateReply(reply.getId(), user, newBody.trim());
    }

    public static void deleteReply(User user, Reply reply) {
        if (reply == null) return;
        if (!reply.getAuthor().equals(user.getUserName())) return;

        ReplyStore.getInstance().deleteReply(reply.getId(), user);
    }

    public static List<Reply> getRepliesForPost(Post post) {
        if (post == null) return List.of();
        return ReplyStore.getInstance().loadRepliesForPost(post.getDatabaseId());
    }
}
