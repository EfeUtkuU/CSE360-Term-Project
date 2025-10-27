package guiStudentPosts;

import entityClasses.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The ModelPosts class represents the data and logic layer of the MVC structure 
 * for managing posts and replies. It interacts directly with the PostStore and 
 * ReplyStore singletons to perform CRUD operations.
 * 
 * This class acts as a bridge between the data layer (entityClasses) and 
 * the controller/view layers in the guiPosts package.
 */
public class ModelPosts {

    // ---------------------- Fields ----------------------
    // Handles all post-related storage and logic operations.
    private final PostStore postStore;

    // Handles all reply-related storage and logic operations.
    private final ReplyStore replyStore;

    // ---------------------- Constructor ----------------------

    /**
     * Initializes the model by obtaining instances of PostStore and ReplyStore.
     * These singletons ensure consistent access to shared post and reply data.
     */
    public ModelPosts() {
        postStore = PostStore.getInstance();
        replyStore = ReplyStore.getInstance();
    }

    // ---------------------- Post Operations ----------------------

    /**
     * Adds a new post to the PostStore.
     * @param post the post object to be added
     */
    public void addPost(Post post) { postStore.addPost(post); }

    /**
     * Retrieves all posts from the PostStore.
     * @return a list containing all posts
     */
    public List<Post> getAllPosts() { return postStore.getAllPosts(); }

    /**
     * Updates an existing post if the user has the necessary permissions.
     * @param id the ID of the post to update
     * @param user the user attempting the update
     * @param title the new title
     * @param body the new body text
     * @return true if the update succeeds, false otherwise
     */
    public boolean updatePost(int id, User user, String title, String body) {
        return postStore.updatePost(id, user, title, body);
    }

    /**
     * Deletes a post if the user is authorized.
     * @param id the ID of the post to delete
     * @param user the user attempting deletion
     * @return true if deletion is successful, false otherwise
     */
    public boolean deletePost(int id, User user) { return postStore.deletePost(id, user); }

    /**
     * Searches for posts containing the specified keyword in their title or body.
     * @param keyword the search term to match
     * @return a list of posts containing the keyword
     */
    public List<Post> searchPosts(String keyword) { return postStore.searchPosts(keyword); }

    // ---------------------- Reply Operations ----------------------

    /**
     * Adds a new reply to the ReplyStore.
     * @param reply the reply object to be added
     */
    public void addReply(Reply reply) { replyStore.addReply(reply); }

    /**
     * Retrieves all replies associated with a specific post.
     * @param postId the ID of the post to get replies for
     * @return a filtered list of replies belonging to the given post
     */
    public List<Reply> getRepliesForPost(int postId) {
        List<Reply> all = replyStore.getAllReplies();
        all.removeIf(r -> r.getPostId() != postId);
        return all;
    }

    /**
     * Alternative method to retrieve replies for a specific post.
     * Uses a separate filtered list for clarity and immutability.
     * @param postId the ID of the post to retrieve replies for
     * @return a list of replies associated with the post
     */
    public List<Reply> getAllRepliesForPost(int postId) {
        List<Reply> all = replyStore.getAllReplies();
        List<Reply> filtered = new ArrayList<>();
        for (Reply r : all) {
            if (r.getPostId() == postId) filtered.add(r);
        }
        return filtered;
    }
}
