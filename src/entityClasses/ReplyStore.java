package entityClasses;

import java.util.ArrayList;
import java.util.List;

/**
 * The ReplyStore class manages all Reply objects within the system.
 * 
 * This class follows a Singleton design pattern, ensuring that only one instance 
 * of the store exists at any time. It acts as the data management layer for replies — 
 * allowing the creation, modification, deletion, and searching of replies, 
 * while enforcing simple validation and permission logic.
 */
public class ReplyStore {

    // ---------------------- Fields ----------------------
    // The single shared ReplyStore instance and two collections:
    // - allReplies: stores all replies created in the system.
    // - subsetReplies: stores temporary filtered results, such as those returned by a search.

    private static ReplyStore instance = new ReplyStore();
    private final List<Reply> allReplies = new ArrayList<>();
    private List<Reply> subsetReplies = new ArrayList<>();

    // ---------------------- Constructor ----------------------
    // Private constructor prevents external instantiation,
    // enforcing the Singleton design.
    private ReplyStore() {}

    // ---------------------- Singleton Accessor ----------------------

    /**
     * Returns the single shared instance of ReplyStore.
     *
     * @return the global ReplyStore instance
     */
    public static ReplyStore getInstance() {
        return instance;
    }

    // ---------------------- Reply Management Methods ----------------------

    /**
     * Adds a new reply to the store after validating that its body is not blank.
     * 
     * @param reply the Reply object to be added
     */
    public void addReply(Reply reply) {
        if (reply.getBody().isBlank()) {
            System.out.println("Cannot add reply: text cannot be empty.");
            return;
        }
        allReplies.add(reply);
        System.out.println("Reply added by " + reply.getAuthor());
    }

    /**
     * Returns a copy of all stored replies.
     * Returning a new list helps protect the internal data structure from external modification.
     * 
     * @return a list containing all replies
     */
    public List<Reply> getAllReplies() {
        return new ArrayList<>(allReplies);
    }

    /**
     * Updates a reply’s content if the user is either the original author or a staff member.
     * Prevents unauthorized users from modifying other users’ replies.
     *
     * @param id       the ID of the reply to update
     * @param user     the User attempting to perform the update
     * @param newBody  the new reply content
     * @return true if the update was successful, false otherwise
     */
    public boolean updateReply(int id, User user, String newBody) {
        for (Reply r : allReplies) {
            if (r.getId() == id) {
                if (r.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    r.setBody(newBody);
                    System.out.println("Reply updated by " + user.getUserName());
                    return true;
                } else {
                    System.out.println("Permission denied: Only staff or the author can edit this reply.");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Deletes a reply if the user is either the author or a staff member.
     * Unauthorized users cannot delete replies created by others.
     *
     * @param id   the ID of the reply to delete
     * @param user the User attempting to delete the reply
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteReply(int id, User user) {
        for (Reply r : allReplies) {
            if (r.getId() == id) {
                if (r.getAuthor().equals(user.getUserName()) || user.getNewStaff()) {
                    allReplies.remove(r);
                    System.out.println("Reply deleted by " + user.getUserName());
                    return true;
                } else {
                    System.out.println("Permission denied: Only staff or the author can delete this reply.");
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------- Search and Filtering ----------------------

    /**
     * Searches all replies for those containing a given keyword in their body text.
     * The search is case-insensitive, and matching replies are stored in subsetReplies.
     *
     * @param keyword the word or phrase to search for
     * @return a list of replies containing the keyword
     */
    public List<Reply> searchReplies(String keyword) {
        subsetReplies = new ArrayList<>();
        for (Reply r : allReplies) {
            if (r.getBody().toLowerCase().contains(keyword.toLowerCase())) {
                subsetReplies.add(r);
            }
        }
        return subsetReplies;
    }

    /**
     * Returns the most recent set of search results.
     *
     * @return list of replies found in the last search
     */
    public List<Reply> getSubsetReplies() {
        return subsetReplies;
    }

    /**
     * Clears the currently stored subset of search results.
     */
    public void clearSubset() {
        subsetReplies.clear();
    }
}
