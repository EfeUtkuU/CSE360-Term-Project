package entityClasses;

import java.time.LocalDateTime;

/**
 * <p><strong>Class:</strong> Post</p>
 *
 * <p><strong>Description:</strong>
 * The {@code Post} class represents a discussion post created by a student or staff
 * within the FoundationsF25 system. Each post captures author information, textual content,
 * privacy and pinning settings, tags for categorization, and timestamps for creation
 * and modification. This class forms the backbone of the Student and Staff post-management
 * feature in the MVC architecture.
 * </p>
 *
 * <h2>Purpose</h2>
 * <ul>
 *   <li>Provides a consistent model object representing discussion posts across all GUI and database layers.</li>
 *   <li>Supports <em>Student User Story #1</em> — “As a student, I can create a new post.”</li>
 *   <li>Supports <em>Student User Story #2</em> — “As a student, I can edit or delete my own post.”</li>
 *   <li>Supports <em>Staff User Story #3</em> — “As a staff member, I can pin important posts.”</li>
 *   <li>Supports <em>System Story</em> — “As the application, I must persist posts using DB-managed IDs.”</li>
 * </ul>
 *
 * <h2>Design Decisions</h2>
 * <ul>
 *   <li>Uses <strong>two-tier ID management</strong>:
 *       <ul>
 *         <li>In-memory {@code id} for GUI logic.</li>
 *         <li>Database-managed {@code databaseId} for persistence integrity.</li>
 *       </ul></li>
 *   <li>Timestamp updates handled automatically by setters to ensure temporal traceability.</li>
 *   <li>Encapsulation ensures MVC controllers manipulate post data only through well-defined methods.</li>
 * </ul>
 *
 * @version 2.40 — 2025-10-27.
 * @author Efe Utku Ural
 */
public class Post {

    // ---------------------- Fields ----------------------

    /** 
     * In-memory unique ID used for GUI-level identification.
     * <p><strong>Rationale:</strong> Allows the application to distinguish between newly created
     * posts and posts loaded from the database before the database-assigned ID exists.</p>
     */
    private static int nextId = 1;

    /** The local (in-memory) ID assigned when a Post object is instantiated. */
    private final int id;

    /** 
     * Auto-incremented primary key managed by the H2 database. 
     * <p><strong>Source:</strong> Assigned by {@code Database.insertPost()} upon successful persistence.</p>
     */
    private int databaseId = -1;

    /** The username of the author who created the post. */
    private final String author;

    /** Indicates if the author is a staff member. */
    private final boolean authorIsStaff;

    /** Indicates if the author is a student. */
    private final boolean authorIsStudent;

    /** The title text of the post. */
    private String title;

    /** The main body content of the post. */
    private String body;

    /** Comma-separated keywords or tags describing the post topic. */
    private String tags;

    /** Marks whether this post is visible only to the creator. */
    private boolean isPrivate;

    /** Marks whether this post is pinned by staff for priority display. */
    private boolean isPinned;

    /** Timestamp of post creation, recorded when instantiated or loaded from DB. */
    private LocalDateTime createdAt;

    /** Timestamp of last update (title, body, tags, etc.). */
    private LocalDateTime updatedAt;


    // ---------------------- Constructors ----------------------

    /**
     * Constructs a new {@code Post} for entries created through the GUI and not yet saved to the database.
     * <p>This constructor is used when a student or staff member creates a new post. The system automatically
     * assigns a local ID, initializes timestamps, and stores author metadata.</p>
     *
     * @param author The {@link User} who created the post.
     * @param title The post's title.
     * @param body The post's main textual content.
     * @param tags Comma-separated tag list for filtering/search.
     * @param isPrivate Whether the post should be visible only to the author.
     * @param isPinned Whether the post should be pinned (staff only).
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

    /**
     * Constructs a {@code Post} loaded from the database.
     * <p>This constructor is used when reading rows from the {@code posts} table in H2, ensuring
     * synchronization between in-memory and persisted states.</p>
     *
     * @param databaseId Database-assigned primary key.
     * @param author Username of the author.
     * @param title Post title.
     * @param body Post body text.
     * @param tags Comma-separated tags.
     * @param isPrivate Visibility flag.
     * @param isPinned Pinned status flag.
     * @param createdAt Creation timestamp.
     * @param updatedAt Last modification timestamp.
     */
    public Post(int databaseId, String author, String title, String body, String tags,
                boolean isPrivate, boolean isPinned,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = nextId++;
        this.databaseId = databaseId;
        this.author = author;
        this.authorIsStaff = false;
        this.authorIsStudent = false;
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.isPrivate = isPrivate;
        this.isPinned = isPinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // ---------------------- Getters ----------------------

    /** 
     * Returns the in-memory ID of this post.
     * @return The in-memory ID used in the GUI layer.
     */
    public int getId() { return id; }

    /**
     * Returns the database-assigned ID for this post.
     * @return The auto-incremented database ID.
     */
    public int getDatabaseId() { return databaseId; }

    /**
     * Returns the username of the post author.
     * @return The author's username.
     */
    public String getAuthor() { return author; }

    /**
     * Checks whether the author is staff.
     * @return {@code true} if authored by a staff member.
     */
    public boolean isAuthorStaff() { return authorIsStaff; }

    /**
     * Checks whether the author is a student.
     * @return {@code true} if authored by a student.
     */
    public boolean isAuthorStudent() { return authorIsStudent; }

    /**
     * Returns the post title.
     * @return The title text.
     */
    public String getTitle() { return title; }

    /**
     * Returns the post body content.
     * @return The textual content of the post.
     */
    public String getBody() { return body; }

    /**
     * Returns the tag list.
     * @return Comma-separated tag string.
     */
    public String getTags() { return tags; }

    /**
     * Checks if the post is marked private.
     * @return {@code true} if private.
     */
    public boolean isPrivate() { return isPrivate; }

    /**
     * Checks if the post is pinned.
     * @return {@code true} if pinned.
     */
    public boolean isPinned() { return isPinned; }

    /**
     * Returns the creation timestamp.
     * @return Time the post was first created.
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Returns the last modification timestamp.
     * @return Time of the most recent update.
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }


    // ---------------------- Setters ----------------------

    /**
     * Updates the database ID after persistence.
     * <p><strong>Used by:</strong> {@code Database.insertPost()} after the record is saved.</p>
     *
     * @param databaseId The database-assigned ID.
     */
    public void setDatabaseId(int databaseId) { this.databaseId = databaseId; }

    /**
     * Updates the title and refreshes the timestamp.
     * @param title The new post title.
     */
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the body text and refreshes the timestamp.
     * @param body The new body content.
     */
    public void setBody(String body) {
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the tags and refreshes the timestamp.
     * @param tags The updated tags string.
     */
    public void setTags(String tags) {
        this.tags = tags;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Changes the privacy flag and refreshes the timestamp.
     * @param isPrivate {@code true} if the post should be private.
     */
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Changes the pinned flag and refreshes the timestamp.
     * @param isPinned {@code true} if the post should be pinned.
     */
    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the modification timestamp.
     * @param updatedAt The {@link LocalDateTime} to set.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }


    // ---------------------- Content Accessors ----------------------

    /**
     * Returns the body text (alias for {@link #getBody()}).
     * @return The post's content.
     */
    public String getContent() { return body; }

    /**
     * Sets the body text (alias for {@link #setBody(String)}).
     * @param content The content to update.
     */
    public void setContent(String content) { setBody(content); }


    // ---------------------- Utility ----------------------

    /**
     * Returns a compact string for debugging/logging purposes.
     * @return A formatted string containing key post details.
     */
    @Override
    public String toString() {
        // Requirement link: Internal consistency check for PostStore and GUI view synchronization.
        return "[DB:" + databaseId + " | Mem:" + id + "] " + title + " (by " + author + ")";
    }
}
