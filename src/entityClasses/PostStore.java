package entityClasses;

import java.util.ArrayList;
import java.util.List;

/**
 * The PostStore class acts as a central storage and manager for all Post objects.
 * 
 * This class follows a Singleton pattern — only one instance of PostStore exists
 * throughout the application. It provides methods to create, update, delete, 
 * and search for posts, enforcing simple validation and permission rules.
 * 
 * It functions as the data-handling component of the MVC model layer.
 */
public class PostStore {

    // ---------------------- Fields ----------------------
    // The single shared instance of PostStore and two collections:
    // - allPosts: stores all posts created in the system.
    // - subsetPosts: stores temporary filtered results (e.g., from search operations).

    private static PostStore instance = new PostStore();
    private final List<Post> allPosts = new ArrayList<>();
    private List<Post> subsetPosts = new ArrayList<>();

    // ---------------------- Constructor ----------------------
    // Private to prevent external instantiation (enforces Singleton design).
    private PostStore() {}

    // ---------------------- Singleton Accessor ----------------------

    /**
     * Returns the single shared instance of PostStore.
     * 
     * @return the global PostStore instance
     */
    public static PostStore getInstance() {
        return instance;
    }

    // ---------------------- Post Management Methods ----------------------

    /**
     * Adds a new post to the store after validating its title and body.
     * If the title or body is blank, the post is rejected.
     *
     * @param post the Post object to be added
     */
    public void addPost(Post post) {
        if (post.getTitle().isBlank() || post.getBody().isBlank()) {
            System.out.println("Cannot create post: title or body is empty.");
            return;
        }
        allPosts.add(post);
        System.out.println("Post added by " + post.getAuthor());
    }

    /**
     * Returns the complete list of all stored posts.
     * 
     * @return list of all posts in the system
     */
    public List<Post> getAllPosts() { 
        return allPosts; 
    }

    /**
     * Updates an existing post’s title and body if the requester is either the author
     * or a staff member. Prevents unauthorized edits.
     *
     * @param id       the ID of the post to update
     * @param user     the User attempting to perform the update
     * @param newTitle the new title for the post
     * @param newBody  the new body text for the post
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePost(int id, User user, String newTitle, String newBody) {
        for (Post p : allPosts) {
            if (p.getId() == id) {
                if (p.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    p.setTitle(newTitle);
                    p.setBody(newBody);
                    System.out.println("Post updated by " + user.getUserName());
                    return true;
                } else {
                    System.out.println("Permission denied: Only staff or the author can edit this post.");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Deletes a post if the requester is the author or a staff member.
     * Prevents unauthorized deletions.
     *
     * @param id   the ID of the post to delete
     * @param user the User attempting to delete the post
     * @return true if deletion was successful, false otherwise
     */
    public boolean deletePost(int id, User user) {
        for (Post p : allPosts) {
            if (p.getId() == id) {
                if (p.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    allPosts.remove(p);
                    System.out.println("Post deleted by " + user.getUserName());
                    return true;
                } else {
                    System.out.println("Permission denied: Only staff or the author can delete this post.");
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------- Search and Filtering ----------------------

    /**
     * Searches all posts for those containing a specific keyword
     * in their title or body (case-insensitive). The results are
     * stored in 'subsetPosts' for easy retrieval.
     *
     * @param keyword the word or phrase to search for
     * @return list of posts that match the search criteria
     */
    public List<Post> searchPosts(String keyword) {
        subsetPosts = new ArrayList<>();
        for (Post p : allPosts) {
            if (p.getTitle().toLowerCase().contains(keyword.toLowerCase())
                || p.getBody().toLowerCase().contains(keyword.toLowerCase())) {
                subsetPosts.add(p);
            }
        }
        return subsetPosts;
    }

    /**
     * Returns the most recent subset of posts found from a search.
     * 
     * @return list of filtered posts
     */
    public List<Post> getSubsetPosts() { 
        return subsetPosts; 
    }

    /**
     * Clears the current subset of filtered posts.
     */
    public void clearSubset() { 
        subsetPosts.clear(); 
    }

    // ---------------------- Utility Methods ----------------------

    /**
     * Removes a post by directly passing the Post object reference.
     * Useful when the post is already known rather than identified by ID.
     *
     * @param post the Post object to remove
     */
    public void removePost(Post post) {
        allPosts.remove(post);
        System.out.println("Post removed by reference.");
    }

    /**
     * Updates the main content of a given post directly.
     * Used for quick content edits when the Post object is already retrieved.
     *
     * @param post       the Post object to update
     * @param newContent the new body text
     */
    public void updatePostContent(Post post, String newContent) {
        post.setBody(newContent);
        System.out.println("Post content updated for " + post.getAuthor());
    }
}
