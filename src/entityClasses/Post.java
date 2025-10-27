package entityClasses;

import java.time.LocalDateTime;

/**
 * Represents a single post created by a user.
 * 
 * This class acts as the data model for posts in the system (MVC model layer).
 * Each Post object stores all relevant information such as author details, 
 * title, body content, tags, visibility options, and timestamps for creation and updates.
 */
public class Post {

    // ---------------------- Post Attributes ----------------------
    // Basic identifying and descriptive fields that define a Post.
    // Includes author details (staff/student), content information (title, body, tags),
    // visibility flags (private/pinned), and metadata (timestamps).

    private static int nextId = 1;
    private final int id;
    private final String author;
    private final boolean authorIsStaff;
    private final boolean authorIsStudent;
    private String title;
    private String body;
    private String tags;
    private boolean isPrivate;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructs a new Post with the specified details.
     * Automatically assigns a unique ID and timestamps the creation and last update.
     *
     * @param author    the User object representing the post creator
     * @param title     the post’s title
     * @param body      the main content of the post
     * @param tags      any tags or keywords associated with the post
     * @param isPrivate whether the post should be visible only to certain users
     * @param isPinned  whether the post should be pinned to the top of a feed
     */
    public Post(User author, String title, String body, String tags, boolean isPrivate, boolean isPinned) {
        this.id = nextId++;
        this.author = author.getUserName();
        this.authorIsStaff = author.getNewStaff();
        this.authorIsStudent = author.getNewStudent();
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.isPrivate = isPrivate;
        this.isPinned = isPinned;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Getters ----------------------
    // Return stored information about the post. These provide read-only access to fields
    // since the actual variables are private to maintain encapsulation.

    public int getId() { return id; }
    public String getAuthor() { return author; }
    public boolean isAuthorStaff() { return authorIsStaff; }
    public boolean isAuthorStudent() { return authorIsStudent; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getTags() { return tags; }
    public boolean isPrivate() { return isPrivate; }
    public boolean isPinned() { return isPinned; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ---------------------- Setters ----------------------
    // Methods that allow controlled modification of post fields.
    // Each setter refreshes the 'updatedAt' timestamp to reflect that a change occurred.

    /** Updates the post’s title and refreshes the update timestamp. */
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    /** Updates the post’s body text and refreshes the update timestamp. */
    public void setBody(String body) {
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }

    /** Updates the tags associated with the post and refreshes the update timestamp. */
    public void setTags(String tags) {
        this.tags = tags;
        this.updatedAt = LocalDateTime.now();
    }

    /** Changes the privacy setting (public/private) and refreshes the update timestamp. */
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        this.updatedAt = LocalDateTime.now();
    }

    /** Changes the pinned status and refreshes the update timestamp. */
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Content Accessors ----------------------

    /** Returns the main content of the post (alias for getBody). */
    public String getContent() {
        return body;
    }

    /** Updates the main post content and refreshes the update timestamp. */
    public void setContent(String content) {
        this.body = content;
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Utility Methods ----------------------

    /**
     * Returns a readable string representation of the post.
     * Useful for debugging, logging, or listing posts in brief form.
     */
    @Override
    public String toString() {
        return "[" + id + "] " + title + " (by " + author + ")";
    }
}
