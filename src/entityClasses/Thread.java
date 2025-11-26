package entityClasses;

import java.time.LocalDateTime;

/**
 * <h2>Class: Thread</h2>
 *
 * <p><strong>Description:</strong><br>
 * Represents a discussion thread within the forum system. A thread serves as
 * the top-level container for related posts, beginning with a root post and
 * optionally containing many additional replies.
 * </p>
 *
 * <p>
 * Each thread includes:
 * </p>
 * <ul>
 *   <li>a unique database identifier</li>
 *   <li>a title describing the topic</li>
 *   <li>the username of the creator</li>
 *   <li>creation and last-updated timestamps</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> This class is named {@code Thread} but is unrelated to
 * {@link java.lang.Thread}. It represents forum threads only.
 * </p>
 *
 * @version
 *   1.00 — 2025-11-22 — Initial forum implementation
 */
public class Thread {

    /** Unique database identifier for this thread. */
    private final int id;

    /** Title of the discussion thread. */
    private String title;

    /** Username of the user who created the thread. */
    private String author;

    /** Timestamp of when this thread was created. */
    private LocalDateTime createdAt;

    /** Timestamp of the last update made to this thread. */
    private LocalDateTime updatedAt;

    /**
     * Constructs a thread instance representing a record loaded from the database.
     *
     * @param id         unique thread identifier from the database
     * @param title      title of the thread
     * @param author     username of the thread creator
     * @param createdAt  timestamp when the thread was created
     * @param updatedAt  timestamp of the most recent update
     */
    public Thread(int id, String title, String author,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructs a new thread before it is inserted into the database.
     * <p>
     * The ID is set to {@code -1} temporarily; the database layer will assign a
     * real ID after insertion.
     * </p>
     *
     * @param title  title of the new thread
     * @param author username of the creator
     */
    public Thread(String title, String author) {
        this.id = -1;  // temporary; overwritten by DB
        this.title = title;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ------------------- Getters -------------------

    /**
     * @return the database identifier of this thread
     */
    public int getId() {
        return id;
    }

    /**
     * @return the title of this thread
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the username of the user who created the thread
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return timestamp of when the thread was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @return timestamp of the most recent modification to this thread
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ------------------- Setters -------------------

    /**
     * Sets the title of this thread.
     *
     * @param title new thread title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the time when this thread was last updated.
     *
     * @param updatedAt new update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ------------------- Display Helpers -------------------

    /**
     * Returns a short combined label for UI lists, typically displayed in the
     * left-side navigation panel of the forum interface.
     *
     * @return a formatted string including thread title and author
     */
    public String getDisplayLabel() {
        return title + "  (" + author + ")";
    }

    /**
     * Returns a string representation useful for debugging or logging.
     *
     * @return formatted representation of this thread
     */
    @Override
    public String toString() {
        return "Thread{id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt + '}';
    }
}
