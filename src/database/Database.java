package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.sql.Timestamp;

import entityClasses.User;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.ParticipationStatistics;

/*******
 * <p> Title: Database Class. </p>
 *
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MP) for a PDF of 438 pages
 * on the H2 main page.)  This class leverages H2 and provides numerous special supporting methods.
 * </p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 3.00  2025-11-22 — Extended with forum Threads, and full Posts/Replies CRUD
 */
public class Database {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

    //  Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    //  Shared variables used within this class
    private Connection connection = null;       // Singleton to access the database
    private Statement statement = null;        // The H2 Statement is used to construct queries

    // These are the easily accessible attributes of the currently logged-in user
    // This is only useful for single user applications
    private String currentUsername;
    private String currentPassword;
    private String currentFirstName;
    private String currentMiddleName;
    private String currentLastName;
    private String currentPreferredFirstName;
    private String currentEmailAddress;
    private boolean currentAdminRole;
    private boolean currentNewStudent;
    private boolean currentNewStaff;

    /*******
     * <p> Method: Database </p>
     *
     * <p> Description: The default constructor used to establish this singleton object.</p>
     *
     */
    public Database () {
    }

    /*******
     * <p> Method: connectToDatabase </p>
     *
     * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
     *      storage.</p>
     *
     * @throws SQLException when the DriverManager is unable to establish a connection
     *
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            // You can use this command to clear the database and restart from fresh.
            // statement.execute("DROP ALL OBJECTS");
            //statement.execute("DROP TABLE IF EXISTS replies");
            //statement.execute("DROP TABLE IF EXISTS posts");
            //statement.execute("DROP TABLE IF EXISTS threads");


            createTables();              // userDB + InvitationCodes
            createPostAndReplyTables();  // threads + posts + replies
            createParticipationStatsTable();
            createGradeTables();         // NEW
            createGradeRepliesTable();   // NEW
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    /*******
     * <p> Method: createTables </p>
     *
     * <p> Description: Used to create new instances of the base tables used by this class.</p>
     *
     */
    private void createTables() throws SQLException {
        // Create the user database
        String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "firstName VARCHAR(255), "
                + "middleName VARCHAR(255), "
                + "lastName VARCHAR (255), "
                + "preferredFirstName VARCHAR(255), "
                + "emailAddress VARCHAR(255), "
                + "adminRole BOOL DEFAULT FALSE, "
                + "newStudent BOOL DEFAULT FALSE, "
                + "newStaff BOOL DEFAULT FALSE, "
                + "oneTimePassword VARCHAR(255), "
                + "otpExpiry TIMESTAMP)";
        statement.execute(userTable);

        // Create the invitation codes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "emailAddress VARCHAR(255), "
                + "role VARCHAR(10))";
        statement.execute(invitationCodesTable);
    }

    // =====================================================================
    //  Forum Tables  (Threads, Posts, Replies)
    // =====================================================================
    /**
     * <p>Creates the tables for forum Threads, Posts, and Replies.</p>
     *
     * <ul>
     *   <li><strong>threads</strong> – top-level discussion containers.</li>
     *   <li><strong>posts</strong> – messages inside a thread (root post or later posts).</li>
     *   <li><strong>replies</strong> – replies to a specific post.</li>
     * </ul>
     */
    public void createPostAndReplyTables() throws SQLException {
        // 1) Threads table (must come first, since posts references it)
        String threadsTable = "CREATE TABLE IF NOT EXISTS threads ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(255) NOT NULL, "               // Thread title (separate from post title)
                + "author VARCHAR(255) NOT NULL, "              // Username of thread creator
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (author) REFERENCES userDB(userName) ON DELETE CASCADE"
                + ")";
        statement.execute(threadsTable);

        // 2) Posts table – every post belongs to exactly one thread
        String postsTable = "CREATE TABLE IF NOT EXISTS posts ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "threadId INT NOT NULL, "
                + "author VARCHAR(255), "
                + "title VARCHAR(255), "         // Post title (can be separate from thread title)
                + "body VARCHAR(2000), "
                + "tags VARCHAR(255), "
                + "isPrivate BOOLEAN, "
                + "isPinned BOOLEAN, "
                + "createdAt TIMESTAMP, "
                + "updatedAt TIMESTAMP, "
                + "FOREIGN KEY (threadId) REFERENCES threads(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (author) REFERENCES userDB(userName) ON DELETE CASCADE"
                + ")";
        statement.execute(postsTable);

        // 3) Replies table – replies belong to posts
        String repliesTable = "CREATE TABLE IF NOT EXISTS replies ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "postId INT, "
                + "author VARCHAR(255), "
                + "body VARCHAR(2000), "
                + "isAnswer BOOLEAN, "
                + "createdAt TIMESTAMP, "
                + "updatedAt TIMESTAMP, "
                + "FOREIGN KEY (postId) REFERENCES posts(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (author) REFERENCES userDB(userName) ON DELETE CASCADE"
                + ")";
        statement.execute(repliesTable);
    }
    
	// =====================================================================
	//  PARTICIPATION STATISTICS TABLE + CRUD
	// =====================================================================
	
	/**
	 * Creates the ParticipationStats table if it does not already exist.
	 * One row per student; updated whenever participation metrics change.
	 */
    public void createParticipationStatsTable() throws SQLException {

        String sql = "CREATE TABLE IF NOT EXISTS ParticipationStats ("
                + " studentID VARCHAR(255) PRIMARY KEY, "
                + " totalPosts INT, "
                + " totalReplies INT, "
                + " threadsParticipated INT, "
                + " activeDays INT, "
                + " averageReplyLength DOUBLE, "
                + " activityScore DOUBLE, "
                + " engagementScore DOUBLE, "
                + " consistencyScore DOUBLE, "
                + " finalScore DOUBLE "
                + ");";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
    
	 // =====================================================================
	//  GRADE TABLE (Student Grades + Staff Feedback)
	// =====================================================================
	private void createGradeTables() throws SQLException {
	    String sql = "CREATE TABLE IF NOT EXISTS GRADE ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "student_id VARCHAR(255) NOT NULL, "
	            + "staff_id VARCHAR(255) NOT NULL, "
	            + "grade_value INT NOT NULL, "
	            + "feedback VARCHAR(2000), "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (student_id) REFERENCES userDB(userName) ON DELETE CASCADE, "
	            + "FOREIGN KEY (staff_id) REFERENCES userDB(userName) ON DELETE CASCADE"
	            + ");";
	
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.executeUpdate();
	    }
	}
	
	// =====================================================================
	//  GRADE_REPLIES TABLE (Student–Staff Feedback Chat Thread)
	// =====================================================================
	private void createGradeRepliesTable() throws SQLException {
	    String sql = "CREATE TABLE IF NOT EXISTS GRADE_REPLIES ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "grade_id INT NOT NULL, "
	            + "sender_type VARCHAR(10) NOT NULL, "
	            + "message VARCHAR(2000), "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (grade_id) REFERENCES GRADE(id) ON DELETE CASCADE"
	            + ");";
	
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.executeUpdate();
	    }
	}



	
	/**
	 * Inserts or updates participation statistics for a student.
	 */
    public void saveParticipationStats(ParticipationStatistics stats) throws SQLException {

        String sql =
            "MERGE INTO ParticipationStats KEY(studentID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, stats.getStudentId());
            ps.setInt(2, stats.getTotalPosts());
            ps.setInt(3, stats.getTotalReplies());
            ps.setInt(4, stats.getThreadsParticipated());
            ps.setInt(5, stats.getActiveDays());
            ps.setDouble(6, stats.getAverageReplyLength());
            ps.setDouble(7, stats.getActivityScore());
            ps.setDouble(8, stats.getEngagementScore());
            ps.setDouble(9, stats.getConsistencyScore());
            ps.setDouble(10, stats.getFinalScore());

            ps.executeUpdate();
        }
    }

	
	/**
	 * Loads participation statistics for a single student.
	 * Returns null if no stats exist.
	 */
    public ParticipationStatistics loadParticipationStatsForStudent(String studentId) throws SQLException {

        String sql = "SELECT * FROM ParticipationStats WHERE studentID = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            ParticipationStatistics stats = new ParticipationStatistics();

            stats.setStudentId(studentId);
            stats.setTotalPosts(rs.getInt("totalPosts"));
            stats.setTotalReplies(rs.getInt("totalReplies"));
            stats.setThreadsParticipated(rs.getInt("threadsParticipated"));
            stats.setActiveDays(rs.getInt("activeDays"));
            stats.setAverageReplyLength(rs.getDouble("averageReplyLength"));
            stats.setActivityScore(rs.getDouble("activityScore"));
            stats.setEngagementScore(rs.getDouble("engagementScore"));
            stats.setConsistencyScore(rs.getDouble("consistencyScore"));
            stats.setFinalScore(rs.getDouble("finalScore"));

            return stats;
        }
    }
    
	// =====================================================================
	//  GRADES: CRUD METHODS
	// =====================================================================
	
	/**
	 * Inserts a new grade or updates an existing one.
	 *
	 * If the student already has a grade row, update it.
	 * Otherwise insert a new grade.
	 */
	public void insertOrUpdateGrade(String studentId, String staffId, int gradeValue, String feedback)
	        throws SQLException {
	
	    // Check whether student already has a grade
	    String checkSql = "SELECT id FROM GRADE WHERE student_id = ?";
	
	    try (PreparedStatement check = connection.prepareStatement(checkSql)) {
	        check.setString(1, studentId);
	        ResultSet rs = check.executeQuery();
	
	        // ---------------- UPDATE ----------------
	        if (rs.next()) {
	            int gradeId = rs.getInt("id");
	
	            String updateSql = "UPDATE GRADE SET grade_value = ?, feedback = ?, "
	                    + "timestamp = CURRENT_TIMESTAMP WHERE id = ?";
	
	            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
	                update.setInt(1, gradeValue);
	                update.setString(2, feedback);
	                update.setInt(3, gradeId);
	                update.executeUpdate();
	            }
	            return;
	        }
	    }
	
	    // ---------------- INSERT ----------------
	    String insertSql = "INSERT INTO GRADE (student_id, staff_id, grade_value, feedback) "
	                     + "VALUES (?, ?, ?, ?)";
	
	    try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
	        insert.setString(1, studentId);
	        insert.setString(2, staffId);
	        insert.setInt(3, gradeValue);
	        insert.setString(4, feedback);
	        insert.executeUpdate();
	    }
	}
	
	/**
	 * Retrieves a student's grade record.
	 *
	 * @param studentId username of student
	 * @return String[] {id, studentId, staffId, gradeValue, feedback, timestamp} or null
	 */
	public String[] getGradeForStudent(String studentId) throws SQLException {
	
	    String sql = "SELECT * FROM GRADE WHERE student_id = ?";
	
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setString(1, studentId);
	        ResultSet rs = ps.executeQuery();
	
	        if (rs.next()) {
	            return new String[] {
	                    String.valueOf(rs.getInt("id")),
	                    rs.getString("student_id"),
	                    rs.getString("staff_id"),
	                    String.valueOf(rs.getInt("grade_value")),
	                    rs.getString("feedback"),
	                    rs.getTimestamp("timestamp").toString()
	            };
	        }
	    }
	    return null;
	}
	
	/**
	 * Returns ALL grade entries in the system.
	 *
	 * @return List<String[]> of rows: {id, studentId, staffId, value, feedback, timestamp}
	 */
	public List<String[]> getAllGrades() throws SQLException {
	    List<String[]> list = new ArrayList<>();
	
	    String sql = "SELECT * FROM GRADE ORDER BY timestamp DESC";
	
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ResultSet rs = ps.executeQuery();
	
	        while (rs.next()) {
	            list.add(new String[]{
	                    String.valueOf(rs.getInt("id")),
	                    rs.getString("student_id"),
	                    rs.getString("staff_id"),
	                    String.valueOf(rs.getInt("grade_value")),
	                    rs.getString("feedback"),
	                    rs.getTimestamp("timestamp").toString()
	            });
	        }
	    }
	    return list;
	}
	
	/**
	 * Deletes a grade (if needed, e.g., admin cleaning).
	 */
	public void deleteGrade(int gradeId) throws SQLException {
	    String sql = "DELETE FROM GRADE WHERE id = ?";
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setInt(1, gradeId);
	        ps.executeUpdate();
	    }
	}
	
	
	// =====================================================================
	//  GRADE REPLIES: CRUD METHODS
	// =====================================================================
	
	/**
	 * Inserts a reply in the grade feedback chat (student <-> staff).
	 */
	public void insertGradeReply(int gradeId, String senderType, String message)
	        throws SQLException {
	
	    String sql = "INSERT INTO GRADE_REPLIES (grade_id, sender_type, message) "
	               + "VALUES (?, ?, ?)";
	
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setInt(1, gradeId);
	        ps.setString(2, senderType);
	        ps.setString(3, message);
	        ps.executeUpdate();
	    }
	}
	
	/**
	 * Loads all chat messages (replies) for a specific grade id.
	 *
	 * @return List<String[]> rows: {senderType, message, timestamp}
	 */
	public List<String[]> getRepliesForGrade(int gradeId) throws SQLException {
	    List<String[]> replies = new ArrayList<>();
	
	    String sql = "SELECT sender_type, message, timestamp "
	               + "FROM GRADE_REPLIES WHERE grade_id = ? "
	               + "ORDER BY timestamp ASC";
	
	    try (PreparedStatement ps = connection.prepareStatement(sql)) {
	        ps.setInt(1, gradeId);
	        ResultSet rs = ps.executeQuery();
	
	        while (rs.next()) {
	            replies.add(new String[]{
	                    rs.getString("sender_type"),
	                    rs.getString("message"),
	                    rs.getTimestamp("timestamp").toString()
	            });
	        }
	    }
	    return replies;
	}




    // =====================================================================
    //  THREADS: CRUD + Queries
    // =====================================================================

    /**
     * Inserts a new thread.
     *
     * @param title   the thread title (shown in the left list)
     * @param author  username of the creator (for display)
     * @return the generated thread ID
     */
    public int insertThread(String title, String author) throws SQLException {
        String sql = "INSERT INTO threads (title, author, createdAt, updatedAt) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    System.out.println("Thread created: id=" + id + " title=\"" + title
                            + "\" by " + author);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert thread");
    }

    /**
     * Returns all threads, newest updated first.
     *
     * Each entry: [threadId, title, authorUserName, createdAt]
     */
    public List<String[]> getAllThreads() {
        List<String[]> threads = new ArrayList<>();
        String sql = "SELECT id, title, author, createdAt "
                   + "FROM threads ORDER BY updatedAt DESC, createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String title = rs.getString("title");
                String author = rs.getString("author");
                String createdAt = rs.getTimestamp("createdAt") != null
                        ? rs.getTimestamp("createdAt").toString()
                        : "";
                threads.add(new String[] { id, title, author, createdAt });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return threads;
    }

    /**
     * Updates a thread's title (e.g., staff edit).
     */
    public void updateThreadTitle(int threadId, String newTitle) throws SQLException {
        String sql = "UPDATE threads SET title = ?, updatedAt = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newTitle);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, threadId);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a thread and all its posts/replies via cascade.
     */
    public void deleteThread(int threadId) throws SQLException {
        String sql = "DELETE FROM threads WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, threadId);
            ps.executeUpdate();
        }
    }

    // =====================================================================
    //  POSTS: CRUD (All posts belong to a thread)
    // =====================================================================

    /**
     * Inserts a new Post into the posts table, tied to an existing thread,
     * and stores the generated database ID inside the Post object.
     *
     * @param post     the Post object to insert
     * @param threadId the thread this post belongs to
     */
    public void insertPost(Post post, int threadId) throws SQLException {
        String sql = "INSERT INTO posts (threadId, author, title, body, tags, "
                   + "isPrivate, isPinned, createdAt, updatedAt) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, threadId);
            ps.setString(2, post.getAuthor());
            ps.setString(3, post.getTitle());
            ps.setString(4, post.getBody());
            ps.setString(5, post.getTags());
            ps.setBoolean(6, post.isPrivate());
            ps.setBoolean(7, post.isPinned());
            ps.setTimestamp(8, Timestamp.valueOf(post.getCreatedAt()));
            ps.setTimestamp(9, Timestamp.valueOf(post.getUpdatedAt()));
            ps.executeUpdate();

            // Capture the generated database ID and set it in the Post object
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    post.setDatabaseId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Backward-compatible version: if no thread is specified, automatically
     * creates a new thread using the post's title and author.
     *
     * This guarantees: "All posts must be in threads".
     */
    public void insertPost(Post post) throws SQLException {
        String threadTitle = (post.getTitle() == null || post.getTitle().isBlank())
                ? "Untitled Thread"
                : post.getTitle();
        String author = post.getAuthor() != null ? post.getAuthor() : "unknown";
        int threadId = insertThread(threadTitle, author);
        insertPost(post, threadId);
    }

    /**
     * Retrieves all posts from the database (across all threads).
     *
     * @return list of all posts (latest first)
     */
    public List<Post> getAllPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Post p = new Post(
                        rs.getInt("id"),
                        rs.getString("author"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getString("tags"),
                        rs.getBoolean("isPrivate"),
                        rs.getBoolean("isPinned"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getTimestamp("updatedAt").toLocalDateTime()
                );
                posts.add(p);
            }
        }
        return posts;
    }

    /**
     * Retrieves all posts for a specific thread.
     *
     * @param threadId the thread's ID
     * @return list of Post objects ordered by creation time
     */
    public List<Post> getPostsForThread(int threadId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts WHERE threadId = ? ORDER BY createdAt ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, threadId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Post p = new Post(
                        rs.getInt("id"),
                        rs.getString("author"),
                        rs.getString("title"),
                        rs.getString("body"),
                        rs.getString("tags"),
                        rs.getBoolean("isPrivate"),
                        rs.getBoolean("isPinned"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getTimestamp("updatedAt").toLocalDateTime()
                );
                posts.add(p);
            }
        }
        return posts;
    }

    /**
     * Updates the title and body of a post.
     *
     * @param id       post ID (database id)
     * @param newTitle new title text
     * @param newBody  new body text
     */
    public void updatePost(int id, String newTitle, String newBody) throws SQLException {
        String sql = "UPDATE posts SET title = ?, body = ?, updatedAt = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newTitle);
            ps.setString(2, newBody);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a post (and its replies via cascade) using its database ID.
     *
     * @param databaseId the database-assigned ID of the post
     */
    public void deletePost(int databaseId) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, databaseId);
            ps.executeUpdate();
        }
    }

    // =====================================================================
    //  REPLIES: CRUD Methods
    // =====================================================================

    /**
     * Inserts a new Reply into the replies table.
     * Ensures referential integrity by using the post's database ID.
     *
     * @param reply the Reply object to insert
     */
    public void insertReply(Reply reply) throws SQLException {
        String sql = "INSERT INTO replies (postId, author, body, isAnswer, createdAt, updatedAt) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reply.getPostDatabaseId());
            ps.setString(2, reply.getAuthor());
            ps.setString(3, reply.getBody());
            ps.setBoolean(4, reply.isAnswer());
            ps.setTimestamp(5, Timestamp.valueOf(reply.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(reply.getUpdatedAt()));
            ps.executeUpdate();

            // capture DB-generated ID
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reply.setDatabaseId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Retrieves all replies belonging to a specific post.
     *
     * @param postDatabaseId the post's database ID
     * @return list of Reply objects
     */
    public List<Reply> getRepliesForPost(int postDatabaseId) throws SQLException {
        List<Reply> replies = new ArrayList<>();
        String sql = "SELECT * FROM replies WHERE postId = ? ORDER BY createdAt ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, postDatabaseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reply r = new Reply(
                        rs.getInt("id"),
                        rs.getInt("postId"),
                        rs.getString("author"),
                        rs.getString("body"),
                        rs.getBoolean("isAnswer"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getTimestamp("updatedAt").toLocalDateTime()
                );
                replies.add(r);
            }
        }
        return replies;
    }

    /**
     * Updates the body of an existing reply.
     *
     * @param id      reply ID
     * @param newBody new text
     */
    public void updateReply(int id, String newBody) throws SQLException {
        String sql = "UPDATE replies SET body = ?, updatedAt = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newBody);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a reply by its ID.
     *
     * @param id reply ID
     */
    public void deleteReply(int id) throws SQLException {
        String sql = "DELETE FROM replies WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // =====================================================================
    //  END OF NEW FORUM SECTION (Existing methods below are unchanged)
    // =====================================================================

    /*******
     * <p> Method: isDatabaseEmpty </p>
     *
     * <p> Description: If the user database has no rows, true is returned, else false.</p>
     *
     * @return true if the database is empty, else it returns false
     *
     */
    public boolean isDatabaseEmpty() {
        String query = "SELECT COUNT(*) AS count FROM userDB";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt("count") == 0;
            }
        }  catch (SQLException e) {
            return false;
        }
        return true;
    }

    /*******
     * <p> Method: getNumberOfUsers </p>
     *
     * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
     *
     * @return the number of user records in the database.
     *
     */
    public int getNumberOfUsers() {
        String query = "SELECT COUNT(*) AS count FROM userDB";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    /*******
     * <p> Method: register(User user) </p>
     *
     * <p> Description: Creates a new row in the database using the user parameter. </p>
     *
     * @throws SQLException when there is an issue creating the SQL command or executing it.
     *
     * @param user specifies a user object to be added to the database.
     *
     */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO userDB "
                + "(userName, password, firstName, middleName, lastName, "
                + "preferredFirstName, emailAddress, adminRole, newStudent, newStaff, "
                + "oneTimePassword, otpExpiry) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            currentUsername = user.getUserName();
            pstmt.setString(1, currentUsername);

            currentPassword = user.getPassword();
            pstmt.setString(2, currentPassword);

            currentFirstName = user.getFirstName();
            pstmt.setString(3, currentFirstName);

            currentMiddleName = user.getMiddleName();
            pstmt.setString(4, currentMiddleName);

            currentLastName = user.getLastName();
            pstmt.setString(5, currentLastName);

            currentPreferredFirstName = user.getPreferredFirstName();
            pstmt.setString(6, currentPreferredFirstName);

            currentEmailAddress = user.getEmailAddress();
            pstmt.setString(7, currentEmailAddress);

            currentAdminRole = user.getAdminRole();
            pstmt.setBoolean(8, currentAdminRole);

            currentNewStudent = user.getNewStudent();
            pstmt.setBoolean(9, currentNewStudent);

            currentNewStaff = user.getNewStaff();
            pstmt.setBoolean(10, currentNewStaff);

            pstmt.executeUpdate();
        }

        System.out.println("Registering user: " + currentUsername + " | password=" + currentPassword
                + " | adminRole=" + user.getAdminRole());
    }

    /*******
     *  <p> Method: List getUserList() </p>
     *
     *  <P> Description: Generate an List of Strings, one for each user in the database,
     *  starting with "<Select User>" at the start of the list. </p>
     *
     *  @return a list of userNames found in the database.
     */
    public List<String> getUserList () {
        List<String> userList = new ArrayList<String>();
        userList.add("<Select a User>");
        String query = "SELECT userName FROM userDB";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userList.add(rs.getString("userName"));
            }
        } catch (SQLException e) {
            return null;
        }
        return userList;
    }

    // ---------------------------------------------------------------
    // Login / Roles / Invitation / Profile methods (unchanged)
    // ---------------------------------------------------------------

    // Check login with either password or OTP
    public boolean loginWithPasswordOrOtp(String username, String passwordOrOtp) {
        String query = "SELECT password, oneTimePassword, otpExpiry FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String storedOtp = rs.getString("oneTimePassword");
                Timestamp expiry = rs.getTimestamp("otpExpiry");

                if (storedPassword != null && storedPassword.equals(passwordOrOtp)) {
                    return true;
                }

                if (storedOtp != null && storedOtp.equals(passwordOrOtp) && expiry != null) {
                    if (expiry.toInstant().isAfter(java.time.Instant.now())) {
                        clearOtp(username);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearOtp(String username) {
        String query = "UPDATE userDB SET oneTimePassword = NULL, otpExpiry = NULL WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean loginAdmin(User user) {
        String query = "SELECT * FROM userDB WHERE userName = ? AND adminRole = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return loginWithPasswordOrOtp(user.getUserName(), user.getPassword());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loginStudent(User user) {
        String query = "SELECT * FROM userDB WHERE userName = ? AND newStudent = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return loginWithPasswordOrOtp(user.getUserName(), user.getPassword());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loginStaff(User user) {
        String query = "SELECT * FROM userDB WHERE userName = ? AND newStaff = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return loginWithPasswordOrOtp(user.getUserName(), user.getPassword());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // If an error occurs, assume user doesn't exist
    }

    public int getNumberOfRoles (User user) {
        int numberOfRoles = 0;
        if (user.getAdminRole()) numberOfRoles++;
        if (user.getNewStudent()) numberOfRoles++;
        if (user.getNewStaff()) numberOfRoles++;
        return numberOfRoles;
    }

    public String generateInvitationCode(String emailAddress, String role) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        String query = "INSERT INTO InvitationCodes (code, emailaddress, role) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.setString(2, emailAddress);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    public int getNumberOfInvitations() {
        String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        } catch  (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean emailaddressHasBeenUsed(String emailAddress) {
        String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, emailAddress);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getRoleGivenAnInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getEmailAddressUsingCode (String code ) {
        String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("emailAddress");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void removeInvitationAfterUse(String code) {
        String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int counter = rs.getInt(1);
                if (counter > 0) {
                    query = "DELETE FROM InvitationCodes WHERE code = ?";
                    try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
                        pstmt2.setString(1, code);
                        pstmt2.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getFirstName(String username) {
        String query = "SELECT firstName FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("firstName");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateFirstName(String username, String firstName) {
        String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            currentFirstName = firstName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getMiddleName(String username) {
        String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("middleName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMiddleName(String username, String middleName) {
        String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, middleName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            currentMiddleName = middleName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLastName(String username) {
        String query = "SELECT LastName FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("lastName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateLastName(String username, String lastName) {
        String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, lastName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            currentLastName = lastName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPreferredFirstName(String username) {
        String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("firstName");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePreferredFirstName(String username, String preferredFirstName) {
        String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, preferredFirstName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            currentPreferredFirstName = preferredFirstName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getEmailAddress(String username) {
        String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("emailAddress");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOneTimePassword(String username) {
        String query = "SELECT oneTimePassword FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("oneTimePassword");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateEmailAddress(String username, String emailAddress) {
        String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, emailAddress);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            currentEmailAddress = emailAddress;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getUserAccountDetails(String username) {
        String query = "SELECT * FROM userDB WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            currentUsername = rs.getString(2);
            currentPassword = rs.getString(3);
            currentFirstName = rs.getString(4);
            currentMiddleName = rs.getString(5);
            currentLastName = rs.getString(6);
            currentPreferredFirstName = rs.getString(7);
            currentEmailAddress = rs.getString(8);
            currentAdminRole = rs.getBoolean(9);
            currentNewStudent = rs.getBoolean(10);
            currentNewStaff = rs.getBoolean(11);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateUserRole(String username, String role, String value) {
        if (role.compareTo("Admin") == 0) {
            String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, value);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
                currentAdminRole = value.compareTo("true") == 0;
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        if (role.compareTo("Student") == 0) {
            String query = "UPDATE userDB SET newStudent = ? WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, value);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
                currentNewStudent = value.compareTo("true") == 0;
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        if (role.compareTo("Staff") == 0) {
            String query = "UPDATE userDB SET newStaff = ? WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, value);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
                currentNewStaff = value.compareTo("true") == 0;
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    public String getCurrentUsername() { return currentUsername; }
    public String getCurrentPassword() { return currentPassword; }
    public String getCurrentFirstName() { return currentFirstName; }
    public String getCurrentMiddleName() { return currentMiddleName; }
    public String getCurrentLastName() { return currentLastName; }
    public String getCurrentPreferredFirstName() { return currentPreferredFirstName; }
    public String getCurrentEmailAddress() { return currentEmailAddress; }
    public boolean getCurrentAdminRole() { return currentAdminRole; }
    public boolean getCurrentNewStudent() { return currentNewStudent; }
    public boolean getCurrentNewStaff() { return currentNewStaff; }

    public void dump() throws SQLException {
        String query = "SELECT * FROM userDB";
        ResultSet resultSet = statement.executeQuery(query);
        ResultSetMetaData meta = resultSet.getMetaData();
        while (resultSet.next()) {
            for (int i = 0; i < meta.getColumnCount(); i++) {
                System.out.println(
                        meta.getColumnLabel(i + 1) + ": " +
                                resultSet.getString(i + 1));
            }
            System.out.println();
        }
        resultSet.close();
    }

    public void closeConnection() {
        try{
            if(statement!=null) statement.close();
        } catch(SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if(connection!=null) connection.close();
        } catch(SQLException se){
            se.printStackTrace();
        }
    }

    public void updateUsername(String oldUsername, String newUsername) {
        String query = "UPDATE userDB SET userName = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, oldUsername);
            pstmt.executeUpdate();
            currentUsername = newUsername;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePassword(String username, String newPassword) {
        String query = "UPDATE userDB SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            currentPassword = newPassword;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAllInvitations() {
        List<String[]> invitations = new ArrayList<>();
        String query = "SELECT code, emailAddress, role FROM InvitationCodes";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String code = rs.getString("code");
                String email = rs.getString("emailAddress");
                String role = rs.getString("role");

                boolean accepted = false;
                String checkUser = "SELECT COUNT(*) FROM userDB WHERE emailAddress = ?";
                try (PreparedStatement checkStmt = connection.prepareStatement(checkUser)) {
                    checkStmt.setString(1, email);
                    ResultSet userRs = checkStmt.executeQuery();
                    if (userRs.next()) {
                        accepted = userRs.getInt(1) > 0;
                    }
                }

                invitations.add(new String[] { code, email, role, accepted ? "Yes" : "No" });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invitations;
    }

    public List<String[]> getAllUsers() {
        List<String[]> users = new ArrayList<>();
        String query = "SELECT userName, firstName, lastName, emailAddress, "
                + "adminRole, newStudent, newStaff FROM userDB";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new String[] {
                        rs.getString("userName"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("emailAddress"),
                        rs.getBoolean("adminRole") ? "Admin" : "",
                        rs.getBoolean("newStudent") ? "Student" : "",
                        rs.getBoolean("newStaff") ? "Staff" : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Returns all users who have the 'newStudent' flag set to TRUE.
     *
     * Each entry: [username, firstName, lastName, emailAddress, adminRole, newStudent, newStaff]
     */
    public List<String[]> getAllStudents() {
        List<String[]> students = new ArrayList<>();

        String query = "SELECT userName, firstName, lastName, emailAddress, "
                     + "adminRole, newStudent, newStaff "
                     + "FROM userDB WHERE newStudent = TRUE";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(new String[]{
                        rs.getString("userName"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("emailAddress"),
                        rs.getBoolean("adminRole") ? "Admin" : "",
                        rs.getBoolean("newStudent") ? "Student" : "",
                        rs.getBoolean("newStaff") ? "Staff" : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    
    public static List<User> loadStudentUsers() {
        List<User> result = new ArrayList<>();

        try {
            Database db = new Database();
            db.connectToDatabase();

            List<String[]> rows = db.getAllStudents();

            for (String[] row : rows) {
                User u = new User(
                        row[0],     // username
                        "",         // password not needed
                        row[1],     // firstName
                        "",         // middle name not stored
                        row[2],     // lastName
                        row[1],     // preferred name fallback
                        row[3],     // email
                        row[4].equals("Admin"),
                        row[5].equals("Student"),
                        row[6].equals("Staff")
                );
                result.add(u);
            }

            db.closeConnection();

        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
        }

        return result;
    }


    public boolean deleteUser(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM userDB WHERE userName = ?");
            ps.setString(1, username);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String generateOneTimePassword(String username) {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        String otp = sb.toString();

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        String query = "UPDATE userDB SET oneTimePassword = ?, otpExpiry = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, otp);
            pstmt.setTimestamp(2, Timestamp.valueOf(expiry));
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return otp;
    }

    public boolean validateOneTimePassword(String username, String otp) {
        String query = "SELECT oneTimePassword, otpExpiry FROM userDB WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedOtp = rs.getString("oneTimePassword");
                Timestamp expiry = rs.getTimestamp("otpExpiry");

                if (storedOtp != null && storedOtp.equals(otp) && expiry != null) {

                    if (expiry.toInstant().isAfter(java.time.Instant.now())) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Connection getConnection() throws SQLException {
        return this.connection;
    }
}
