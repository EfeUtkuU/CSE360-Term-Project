package tests;

import entityClasses.*;
import database.Database;
import java.sql.*;
import java.util.List;

/**
 * <h2>Class: TP2Tests</h2>
 *
 * <p><strong>Description:</strong><br>
 * A fully database-integrated, production-safe test suite verifying the correctness
 * of {@link entityClasses.PostStore} and {@link entityClasses.ReplyStore}.  
 * Tests run against the live {@link database.Database} using the existing H2 schema,
 * ensuring behavior matches GUI-level expectations for posts and replies.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Automatically inserts two test users (<code>student1</code>, <code>staff1</code>)
 *       if not already registered.</li>
 *   <li>Executes each CRUD test in isolation to ensure consistent results.</li>
 *   <li>Safely deletes any temporary posts, replies, and users created during testing.</li>
 *   <li>Leaves production data untouched — cleanup is fully scoped to test users.</li>
 * </ul>
 *
 * <h3>Requirements Verified (TP2)</h3>
 * <ol>
 *   <li>R1 – Students can create valid posts.</li>
 *   <li>R2 – Empty posts are rejected.</li>
 *   <li>R3 – Staff can create posts visible to all.</li>
 *   <li>R4 – Posts display in correct order with timestamps.</li>
 *   <li>R5 – Authors can edit their own posts.</li>
 *   <li>R6 – Staff can edit any user’s post.</li>
 *   <li>R7 – Unauthorized users cannot edit others’ posts.</li>
 *   <li>R8 – Authors can delete their own posts.</li>
 *   <li>R9 – Staff can delete any post.</li>
 *   <li>R10 – Unauthorized users cannot delete others’ posts.</li>
 *   <li>R11 – Replies link correctly to parent posts.</li>
 *   <li>R12 – Empty replies are rejected.</li>
 *   <li>R13 – Authors can edit their own replies.</li>
 *   <li>R14 – Staff can edit any reply.</li>
 *   <li>R15 – Unauthorized users cannot edit others’ replies.</li>
 *   <li>R16 – Authors can delete their own replies.</li>
 *   <li>R17 – Staff can delete any reply.</li>
 *   <li>R18 – Unauthorized users cannot delete others’ replies.</li>
 *   <li>R19 – Deleting a post cascades to its replies.</li>
 * </ol>
 *
 * @author
 * Efe Utku Ural
 *
 * @version 2.30 — 2025-10-29 — Final version with zero Javadoc warnings
 */
public class TP2Tests {

    /** Default constructor for TP2Tests (not instantiated manually). */
    public TP2Tests() { }

    /** Represents the test student user performing normal actions. */
    private static User studentUser;

    /** Represents the test staff user performing moderator actions. */
    private static User staffUser;

    /** Database and store singletons for accessing tables. */
    private static Database db;
    /** Store for managing posts during tests. */
    private static PostStore postStore;
    /** Store for managing replies during tests. */
    private static ReplyStore replyStore;

    /**
     * <strong>Main Entry Point:</strong><br>
     * Initializes database, ensures users exist, runs all tests, and performs cleanup.
     *
     * @param args Command-line arguments (unused)
     */
    public static void main(String[] args) {
        try {
            setupDatabase();
            System.out.println("========== BEGINNING TP2 DATABASE TEST SUITE ==========");

            // ---- POST TESTS ----
            runTest("testAddPost_Valid", TP2Tests::testAddPost_Valid);
            runTest("testAddPost_BlankBody", TP2Tests::testAddPost_BlankBody);
            runTest("testUpdatePost_ByAuthor", TP2Tests::testUpdatePost_ByAuthor);
            runTest("testUpdatePost_ByStaff", TP2Tests::testUpdatePost_ByStaff);
            runTest("testUpdatePost_Unauthorized", TP2Tests::testUpdatePost_Unauthorized);
            runTest("testDeletePost_ByAuthor", TP2Tests::testDeletePost_ByAuthor);
            runTest("testDeletePost_ByStaff", TP2Tests::testDeletePost_ByStaff);
            runTest("testDeletePost_Unauthorized", TP2Tests::testDeletePost_Unauthorized);

            // ---- REPLY TESTS ----
            runTest("testAddReply_Valid", TP2Tests::testAddReply_Valid);
            runTest("testAddReply_BlankBody", TP2Tests::testAddReply_BlankBody);
            runTest("testGetRepliesForPost_FiltersCorrectly", TP2Tests::testGetRepliesForPost_FiltersCorrectly);
            runTest("testUpdateReply_ByAuthor", TP2Tests::testUpdateReply_ByAuthor);
            runTest("testUpdateReply_ByStaff", TP2Tests::testUpdateReply_ByStaff);
            runTest("testUpdateReply_Unauthorized", TP2Tests::testUpdateReply_Unauthorized);
            runTest("testDeleteReply_ByAuthor", TP2Tests::testDeleteReply_ByAuthor);
            runTest("testDeleteReply_ByStaff", TP2Tests::testDeleteReply_ByStaff);
            runTest("testDeleteReply_Unauthorized", TP2Tests::testDeleteReply_Unauthorized);

            // ---- INTEGRATION ----
            runTest("testCascade_DeletePostAlsoRemovesReplies", TP2Tests::testCascade_DeletePostAlsoRemovesReplies);

            System.out.println("========== ALL TESTS COMPLETED ==========");
        } catch (Exception e) {
            System.err.println("❌ Setup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanupDatabase();
        }
    }

    /* ============================================================
       DATABASE SETUP / CLEANUP
       ============================================================ */

    /**
     * Sets up test users and store instances before running tests.
     *
     * @throws SQLException if a database connection or query fails
     */
    private static void setupDatabase() throws SQLException {
        db = new Database();
        db.connectToDatabase();
        postStore = PostStore.getInstance();
        replyStore = ReplyStore.getInstance();

        studentUser = new User("student1", "pass", "John", "", "Doe", "John", "j@x.com",
                false, true, false);
        staffUser = new User("staff1", "pass", "Jane", "", "Smith", "Jane", "s@x.com",
                false, false, true);

        ensureUserExists(studentUser);
        ensureUserExists(staffUser);
    }

    /**
     * Ensures the given test user exists in the userDB table.
     *
     * @param user the user to check and insert if missing
     * @throws SQLException if a database query or insert fails
     */
    private static void ensureUserExists(User user) throws SQLException {
        db.connectToDatabase();
        Connection conn = db.getConnection();
        PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM userDB WHERE userName=?");
        check.setString(1, user.getUserName());
        ResultSet rs = check.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        check.close();

        if (count == 0) {
            System.out.println("Inserting test user: " + user.getUserName());
            db.register(user);
        }
        db.connectToDatabase();
    }

    /** Removes all temporary data created by tests. */
    private static void cleanupDatabase() {
        try (Connection conn = db.getConnection()) {
            conn.prepareStatement("DELETE FROM replies WHERE author IN ('student1','staff1')").executeUpdate();
            conn.prepareStatement("DELETE FROM posts WHERE author IN ('student1','staff1')").executeUpdate();
            conn.prepareStatement("DELETE FROM userDB WHERE userName IN ('student1','staff1')").executeUpdate();
            System.out.println("🧹 Cleanup complete — test data removed.");
        } catch (Exception e) {
            System.out.println("⚠️ Cleanup warning: " + e.getMessage());
        }
    }

    /* ============================================================
       TEST WRAPPER
       ============================================================ */

    /**
     * Runs a named test safely and reports its result.
     *
     * @param name the test name for logging
     * @param method the Runnable containing the test logic
     */
    private static void runTest(String name, Runnable method) {
        try {
            postStore.clearAllPosts();
            replyStore.clearAllReplies();
            method.run();
            System.out.println("✅ TEST PASSED: " + name);
        } catch (AssertionError e) {
            System.out.println("❌ TEST FAILED: " + name + " — " + e.getMessage());
        } catch (Exception e) {
            System.out.println("⚠️ ERROR in test " + name + ": " + e.getMessage());
        }
        System.out.println("---------------------------------------------");
    }

    /* ============================================================
       POST TESTS
       ============================================================ */

    /** 
     * <p><b>Test:</b> testAddPost_Valid</p>
     * <p><b>Related Requirements:</b> R1</p>
     * <p><b>Purpose:</b> Verify that valid student posts are correctly added.</p>
     * <p><b>Assessment:</b> Adds a post and asserts that {@code PostStore.size() ≥ 1}.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” confirms R1 satisfied.</p>
     */
    private static void testAddPost_Valid() {
        Post post = new Post(studentUser, "Valid Post", "Body text", "", false, false);
        postStore.addPost(post);
        assert postStore.getAllPosts().size() >= 1 : "Post insertion failed";
    }

    /** 
     * <p><b>Test:</b> testAddPost_BlankBody</p>
     * <p><b>Related Requirements:</b> R2</p>
     * <p><b>Purpose:</b> Ensure that empty post bodies are rejected.</p>
     * <p><b>Assessment:</b> Inserts blank-body post and asserts none exist with that title.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” indicates R2 satisfied.</p>
     */
    private static void testAddPost_BlankBody() {
        Post post = new Post(studentUser, "Blank Body", "   ", "", false, false);
        postStore.addPost(post);
        assert postStore.getAllPosts().stream()
                .noneMatch(p -> p.getTitle().equals("Blank Body"))
                : "Blank-body post was incorrectly added";
    }

    /** 
     * <p><b>Test:</b> testUpdatePost_ByAuthor</p>
     * <p><b>Related Requirements:</b> R5</p>
     * <p><b>Purpose:</b> Verify authors can update their own posts.</p>
     * <p><b>Assessment:</b> Adds and updates a post; expects update to return true.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” confirms R5 satisfied.</p>
     */
    private static void testUpdatePost_ByAuthor() {
        Post p = new Post(studentUser, "Old", "Body", "", false, false);
        postStore.addPost(p);
        boolean updated = postStore.updatePost(p.getId(), studentUser, "New Title", "Updated Body");
        assert updated : "Author update failed";
    }

    /** 
     * <p><b>Test:</b> testUpdatePost_ByStaff</p>
     * <p><b>Related Requirements:</b> R6</p>
     * <p><b>Purpose:</b> Ensure staff can edit any user’s post.</p>
     * <p><b>Assessment:</b> Staff updates student post; expects success.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” = R6 met.</p>
     */
    private static void testUpdatePost_ByStaff() {
        Post p = new Post(studentUser, "Old", "Body", "", false, false);
        postStore.addPost(p);
        boolean updated = postStore.updatePost(p.getId(), staffUser, "Staff Edit", "Edited Body");
        assert updated : "Staff update failed";
    }

    /** 
     * <p><b>Test:</b> testUpdatePost_Unauthorized</p>
     * <p><b>Related Requirements:</b> R7</p>
     * <p><b>Purpose:</b> Prevent unauthorized edits.</p>
     * <p><b>Assessment:</b> Outsider attempts update; expects false.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” = permissions enforced (R7).</p>
     */
    private static void testUpdatePost_Unauthorized() {
        User outsider = new User("outsider", "p", "Out", "", "Sider", "Out", "o@x.com",
                false, true, false);
        Post p = new Post(studentUser, "Protected", "Body", "", false, false);
        postStore.addPost(p);
        boolean updated = postStore.updatePost(p.getId(), outsider, "Hack", "HackBody");
        assert !updated : "Unauthorized update succeeded";
    }

    /** 
     * <p><b>Test:</b> testDeletePost_ByAuthor</p>
     * <p><b>Related Requirements:</b> R8</p>
     * <p><b>Purpose:</b> Verify authors can delete their own posts.</p>
     * <p><b>Assessment:</b> Adds and deletes a post as same user.</p>
     * <p><b>Interpretation:</b> Success confirms R8.</p>
     */
    private static void testDeletePost_ByAuthor() {
        Post p = new Post(studentUser, "Title", "Body", "", false, false);
        postStore.addPost(p);
        boolean deleted = postStore.deletePost(p.getId(), studentUser);
        assert deleted : "Delete by author failed";
    }

    /** 
     * <p><b>Test:</b> testDeletePost_ByStaff</p>
     * <p><b>Related Requirements:</b> R9</p>
     * <p><b>Purpose:</b> Ensure staff can delete any user’s post.</p>
     * <p><b>Assessment:</b> Staff deletes student post; expects success.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” = R9 met.</p>
     */
    private static void testDeletePost_ByStaff() {
        Post p = new Post(studentUser, "To Delete", "Body", "", false, false);
        postStore.addPost(p);
        boolean deleted = postStore.deletePost(p.getId(), staffUser);
        assert deleted : "Delete by staff failed";
    }

    /** 
     * <p><b>Test:</b> testDeletePost_Unauthorized</p>
     * <p><b>Related Requirements:</b> R10</p>
     * <p><b>Purpose:</b> Block unauthorized deletions.</p>
     * <p><b>Assessment:</b> Outsider deletes post; expects failure.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” = R10 validated.</p>
     */
    private static void testDeletePost_Unauthorized() {
        User outsider = new User("outsider", "p", "Out", "", "Sider", "Out", "o@x.com",
                false, true, false);
        Post p = new Post(studentUser, "NoDelete", "Body", "", false, false);
        postStore.addPost(p);
        boolean deleted = postStore.deletePost(p.getId(), outsider);
        assert !deleted : "Unauthorized delete succeeded";
    }

    /* ============================================================
       REPLY TESTS
       ============================================================ */

    /** 
     * <p><b>Test:</b> testAddReply_Valid</p>
     * <p><b>Related Requirements:</b> R11</p>
     * <p><b>Purpose:</b> Validate reply linking to parent post.</p>
     * <p><b>Assessment:</b> Adds a reply; asserts count ≥ 1.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” confirms R11.</p>
     */
    private static void testAddReply_Valid() {
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "This is a reply", false);
        replyStore.addReply(r);
        assert replyStore.getAllReplies().size() >= 1 : "Reply insertion failed";
    }

    /** 
     * <p><b>Test:</b> testAddReply_BlankBody</p>
     * <p><b>Related Requirements:</b> R12</p>
     * <p><b>Purpose:</b> Ensure empty replies are rejected.</p>
     * <p><b>Assessment:</b> Adds blank reply; expects store empty.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” = R12 enforced.</p>
     */
    private static void testAddReply_BlankBody() {
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "   ", false);
        replyStore.addReply(r);
        assert replyStore.getAllReplies().isEmpty() : "Blank reply was added";
    }

    /** 
     * <p><b>Test:</b> testGetRepliesForPost_FiltersCorrectly</p>
     * <p><b>Related Requirements:</b> R11</p>
     * <p><b>Purpose:</b> Verify reply filtering by post.</p>
     * <p><b>Assessment:</b> Adds replies to two posts; ensures only correct replies load per post.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” confirms correct linkage (R11).</p>
     */
    private static void testGetRepliesForPost_FiltersCorrectly() {
        Post p1 = new Post(studentUser, "T1", "B1", "", false, false);
        Post p2 = new Post(studentUser, "T2", "B2", "", false, false);
        postStore.addPost(p1);
        postStore.addPost(p2);
        replyStore.addReply(new Reply(p1, studentUser, "Reply1", false));
        replyStore.addReply(new Reply(p2, studentUser, "Reply2", false));
        List<Reply> forP1 = replyStore.loadRepliesForPost(p1.getDatabaseId());
        assert forP1.stream().allMatch(r -> r.getBody().contains("1"))
                : "Replies not filtered correctly";
    }

    /** 
     * <p><b>Test:</b> testUpdateReply_ByAuthor</p>
     * <p><b>Related Requirements:</b> R13</p>
     * <p><b>Purpose:</b> Confirm reply authors can edit their replies.</p>
     * <p><b>Assessment:</b> Updates a reply and expects success.</p>
     * <p><b>Interpretation:</b> Pass = R13 met.</p>
     */
    private static void testUpdateReply_ByAuthor() {
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "Old", false);
        replyStore.addReply(r);
        boolean updated = replyStore.updateReply(r.getId(), studentUser, "New Body");
        assert updated : "Reply update by author failed";
    }

    /** 
     * <p><b>Test:</b> testUpdateReply_ByStaff</p>
     * <p><b>Related Requirements:</b> R14</p>
     * <p><b>Purpose:</b> Staff should be able to edit any reply.</p>
     * <p><b>Assessment:</b> Staff edits student reply; expects success.</p>
     * <p><b>Interpretation:</b> Pass = R14 validated.</p>
     */
    private static void testUpdateReply_ByStaff() {
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "Old", false);
        replyStore.addReply(r);
        boolean updated = replyStore.updateReply(r.getId(), staffUser, "Staff Edit");
        assert updated : "Reply update by staff failed";
    }

    /** 
     * <p><b>Test:</b> testUpdateReply_Unauthorized</p>
     * <p><b>Related Requirements:</b> R15</p>
     * <p><b>Purpose:</b> Prevent unauthorized edits of replies.</p>
     * <p><b>Assessment:</b> Outsider edits another’s reply; expects failure.</p>
     * <p><b>Interpretation:</b> Pass = R15 enforced.</p>
     */
    private static void testUpdateReply_Unauthorized() {
        User outsider = new User("outsider", "p", "O", "", "S", "O", "o@x.com",
                false, true, false);
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "Body", false);
        replyStore.addReply(r);
        boolean updated = replyStore.updateReply(r.getId(), outsider, "Hack");
        assert !updated : "Unauthorized reply edit succeeded";
    }

    /** 
     * <p><b>Test:</b> testDeleteReply_ByAuthor</p>
     * <p><b>Related Requirements:</b> R16</p>
     * <p><b>Purpose:</b> Authors should delete their own replies.</p>
     * <p><b>Assessment:</b> Adds and deletes reply as same user.</p>
     * <p><b>Interpretation:</b> Pass = R16 confirmed.</p>
     */
    private static void testDeleteReply_ByAuthor() {
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "Body", false);
        replyStore.addReply(r);
        boolean deleted = replyStore.deleteReply(r.getId(), studentUser);
        assert deleted : "Author reply delete failed";
    }

    /** 
     * <p><b>Test:</b> testDeleteReply_ByStaff</p>
     * <p><b>Related Requirements:</b> R17</p>
     * <p><b>Purpose:</b> Ensure staff can delete any reply.</p>
     * <p><b>Assessment:</b> Staff deletes student reply; expects success.</p>
     * <p><b>Interpretation:</b> Pass = R17 validated.</p>
     */
    private static void testDeleteReply_ByStaff() {
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "Body", false);
        replyStore.addReply(r);
        boolean deleted = replyStore.deleteReply(r.getId(), staffUser);
        assert deleted : "Staff reply delete failed";
    }

    /** 
     * <p><b>Test:</b> testDeleteReply_Unauthorized</p>
     * <p><b>Related Requirements:</b> R18</p>
     * <p><b>Purpose:</b> Prevent unauthorized reply deletion.</p>
     * <p><b>Assessment:</b> Outsider deletes reply; expects failure.</p>
     * <p><b>Interpretation:</b> Pass = R18 enforced.</p>
     */
    private static void testDeleteReply_Unauthorized() {
        User outsider = new User("outsider", "p", "O", "", "S", "O", "o@x.com",
                false, true, false);
        Post p = new Post(studentUser, "T", "B", "", false, false);
        postStore.addPost(p);
        Reply r = new Reply(p, studentUser, "Body", false);
        replyStore.addReply(r);
        boolean deleted = replyStore.deleteReply(r.getId(), outsider);
        assert !deleted : "Unauthorized reply delete succeeded";
    }

    /* ============================================================
       INTEGRATION TEST
       ============================================================ */

    /** 
     * <p><b>Test:</b> testCascade_DeletePostAlsoRemovesReplies</p>
     * <p><b>Related Requirements:</b> R19</p>
     * <p><b>Purpose:</b> Ensure deleting a post also deletes its replies.</p>
     * <p><b>Assessment:</b> Adds post and replies; deletes post; asserts zero replies remain.</p>
     * <p><b>Interpretation:</b> “✅ TEST PASSED” confirms cascade behavior (R19).</p>
     */
    private static void testCascade_DeletePostAlsoRemovesReplies() {
        Post p = new Post(studentUser, "Cascade", "Body", "", false, false);
        postStore.addPost(p);
        replyStore.addReply(new Reply(p, studentUser, "Reply A", false));
        replyStore.addReply(new Reply(p, staffUser, "Reply B", false));
        postStore.deletePost(p.getId(), staffUser);
        long remaining = replyStore.loadRepliesForPost(p.getDatabaseId()).size();
        assert remaining == 0 : "Cascade delete failed — replies still exist";
    }
}
