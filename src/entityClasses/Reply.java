package entityClasses;

import java.time.LocalDateTime;

/**
 * Represents a reply made to a specific post.
 * 
 * This class serves as a model in the MVC structure, storing the data for 
 * individual replies. Each reply includes information about the author, 
 * the associated post, the content, whether it’s marked as an answer, 
 * and timestamps for when it was created or last updated.
 */
public class Reply {

    // ---------------------- Reply Attributes ----------------------
    // Basic identifying and descriptive fields for a Reply.
    // Includes IDs linking it to its parent Post, author information (staff/student),
    // reply content, answer flag, and creation/update timestamps.

    private static int nextId = 1;
    private final int id;
    private final int postId;
    private final String author;
    private final boolean authorIsStaff;
    private final boolean authorIsStudent;
    private String body;
    private boolean isAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructs a new Reply object tied to a specific Post and User.
     * Automatically assigns a unique ID and timestamps creation and update times.
     *
     * @param post      the Post object this reply is associated with
     * @param author    the User who created the reply
     * @param body      the content of the reply
     * @param isAnswer  indicates whether this reply is marked as the answer
     */
    public Reply(Post post, User author, String body, boolean isAnswer) {
        this.id = nextId++;
        this.postId = post.getId();
        this.author = author.getUserName();
        this.authorIsStaff = author.getNewStaff();
        this.authorIsStudent = author.getNewStudent();
        this.body = body;
        this.isAnswer = isAnswer;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Getters ----------------------
    // Provide access to reply data while maintaining encapsulation.

    public int getId() { return id; }
    public int getPostId() { return postId; }
    public String getAuthor() { return author; }
    public boolean isAuthorStaff() { return authorIsStaff; }
    public boolean isAuthorStudent() { return authorIsStudent; }
    public String getBody() { return body; }
    public boolean isAnswer() { return isAnswer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ---------------------- Setters ----------------------
    // Allow limited updates to mutable fields (content and answer status),
    // while refreshing the update timestamp each time a modification is made.

    /**
     * Updates the reply’s body text and refreshes the update timestamp.
     *
     * @param body the new reply content
     */
    public void setBody(String body) {
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates whether this reply is marked as an answer
     * and refreshes the update timestamp.
     *
     * @param isAnswer true if the reply should be marked as the answer
     */
    public void setAnswer(boolean isAnswer) {
        this.isAnswer = isAnswer;
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Utility Methods ----------------------

    /**
     * Returns a concise string representation of the reply,
     * including its ID, linked post ID, author, and body text.
     *
     * @return formatted string summary of the reply
     */
    @Override
    public String toString() {
        return "[" + id + "] Reply to Post " + postId + " (by " + author + "): " + body;
    }
}
