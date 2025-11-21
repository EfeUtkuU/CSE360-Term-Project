package entityClasses;

import java.time.LocalDateTime;

/**
 * <p><strong>Class:</strong> Reply</p>
 *
 * <p><strong>Description:</strong>
 * Represents a single reply made to a specific {@link Post}.
 * Each reply is associated with its parent post through {@code postDatabaseId}, and stores
 * information about its author (student or staff), textual content, whether it is
 * marked as an answer, and timestamps for creation and updates.
 * </p>
 *
 * <p>This class acts as the data model for replies within the MVC structure.
 * It supports encapsulation by providing read-only access to most fields
 * and controlled updates through setter methods.</p>
 *
 * @version 2.30 — 2025-10-27 — Finalized for DB-managed IDs and H2 integration
 */
public class Reply {

    // ---------------------- Fields ----------------------

    /** Static counter for assigning unique in-memory reply IDs (UI only). */
    private static int nextId = 1;

    /** Local unique numeric identifier (for in-memory logic). */
    private final int id;

    /** Database-managed unique reply ID (AUTO_INCREMENT primary key). */
    private int databaseId = -1;

    /** Database-managed ID of the parent post (foreign key). */
    private int postDatabaseId;

    /** Username of the author who created the reply. */
    private final String author;

    /** Author role flags (not stored in DB). */
    private final boolean authorIsStaff;
    private final boolean authorIsStudent;

    /** Reply content and metadata. */
    private String body;
    private boolean isAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // ---------------------- Constructors ----------------------

    /**
     * Constructs a new {@code Reply} associated with a specific post and author.
     * Automatically assigns a unique in-memory ID and timestamps.
     *
     * @param post     the {@link Post} this reply is associated with
     * @param author   the {@link User} who created the reply
     * @param body     the textual content of the reply
     * @param isAnswer whether this reply is marked as the accepted answer
     */
    public Reply(Post post, User author, String body, boolean isAnswer) {
        this.id = nextId++;
        this.postDatabaseId = post.getDatabaseId();  // ✅ real DB foreign key
        this.author = author.getUserName();
        this.authorIsStaff = author.getNewStaff();
        this.authorIsStudent = author.getNewStudent();
        this.body = body;
        this.isAnswer = isAnswer;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructs a {@code Reply} loaded directly from the database.
     */
    public Reply(int databaseId, int postDatabaseId, String author, String body,
                 boolean isAnswer, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = nextId++;
        this.databaseId = databaseId;
        this.postDatabaseId = postDatabaseId;
        this.author = author;
        this.authorIsStaff = false;
        this.authorIsStudent = false;
        this.body = body;
        this.isAnswer = isAnswer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // ---------------------- Getters ----------------------

    public int getId() { return id; }

    /** @return database-assigned reply ID (primary key) */
    public int getDatabaseId() { return databaseId; }

    /** @return the database-managed ID of the parent post (foreign key) */
    public int getPostDatabaseId() { return postDatabaseId; }

    public String getAuthor() { return author; }
    public boolean isAuthorStaff() { return authorIsStaff; }
    public boolean isAuthorStudent() { return authorIsStudent; }
    public String getBody() { return body; }
    public boolean isAnswer() { return isAnswer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }


    // ---------------------- Setters ----------------------

    /** Set by Database.insertReply() after DB assigns an ID. */
    public void setDatabaseId(int databaseId) { this.databaseId = databaseId; }

    /** Allows manually assigning post foreign key if needed. */
    public void setPostDatabaseId(int postDatabaseId) { this.postDatabaseId = postDatabaseId; }

    public void setBody(String body) {
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }
    public String getContent() {
    	return body;
    }
    public void setAnswer(boolean isAnswer) {
        this.isAnswer = isAnswer;
        this.updatedAt = LocalDateTime.now();
    }


    // ---------------------- Utility ----------------------

    @Override
    public String toString() {
        return "[Reply DB:" + databaseId + " | Mem:" + id +
               " → Post DB:" + postDatabaseId + "] by " + author + ": " + body;
    }
}
