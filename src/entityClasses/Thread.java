package entityClasses;

import java.time.LocalDateTime;

/**
 * <p><strong>Class:</strong> Thread</p>
 *
 * <p><strong>Description:</strong>
 * Represents a forum thread. This is the top-level container
 * holding multiple posts (the first of which is the “root post”).
 * Each thread has a title, creator, and timestamps.
 * </p>
 *
 * @version 1.00 — 2025-11-22 — Initial forum implementation
 */
public class Thread {

    private final int id;                  // database ID
    private String title;                  // thread title
    private String author;                 // username creating thread
    private LocalDateTime createdAt;       // timestamp
    private LocalDateTime updatedAt;       // timestamp

    /** Constructor used when loading from database */
    public Thread(int id, String title, String author,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Constructor used before DB insert (ID assigned by DB) */
    public Thread(String title, String author) {
        this.id = -1;  // temporary, DB assigns real value
        this.title = title;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ------------------- Getters -------------------

    public int getId() { return id; }

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ------------------- Setters -------------------

    public void setTitle(String title) { this.title = title; }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ------------------- Display Helpers -------------------

    /** Shown in left-side UI thread list */
    public String getDisplayLabel() {
        return title + "  (" + author + ")";
    }

    @Override
    public String toString() {
        return "Thread{id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt + '}';
    }
}
