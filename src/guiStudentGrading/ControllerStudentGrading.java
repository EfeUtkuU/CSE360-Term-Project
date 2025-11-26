package guiStudentGrading;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.User;
import guiStaffStats.ControllerStaffStatistics;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p><strong>Class:</strong> ControllerStudentGrading</p>
 *
 * <p><strong>Description:</strong>
 * MVC controller for the student-facing grading page.
 * Responsible for:
 * <ul>
 *   <li>Loading participation statistics for the logged-in student
 *       (reuses staff-side statistics engine)</li>
 *   <li>Loading grade + staff feedback from the database</li>
 *   <li>Loading chat conversation for this specific grade</li>
 *   <li>Sending student messages in the feedback conversation</li>
 * </ul>
 * This controller never performs SQL directly; it delegates to {@link Database}.
 * 
 */
public class ControllerStudentGrading {

    /** Shared database instance (provided by FoundationsMain). */
    private static final Database theDatabase = FoundationsMain.database;

    /** Stage reference for navigation. */
    private static Stage theStage;

    /** Logged-in student user. */
    private static User  theUser;

    // --------------------------------------------------------------------
    // ENTRY POINT
    // --------------------------------------------------------------------

    /**
     * Displays the student grading/feedback page.
     *
     * @param stage JavaFX stage
     * @param user  logged-in student
     */
    public static void displayStudentGrading(Stage stage, User user) {
        theStage = stage;
        theUser  = user;
        ViewStudentGrading.displayStudentGrading(theStage, theUser);
    }

    /**
     * Called by the View immediately after the scene is constructed.
     * Loads:
     * <ul>
     *     <li>All discussion statistics</li>
     *     <li>Grade + feedback</li>
     *     <li>Chat messages</li>
     * </ul>
     */
    protected static void refreshAll() {
        refreshStatistics();
        refreshGrade();
        refreshChat();
    }

    // --------------------------------------------------------------------
    // STATISTICS
    // --------------------------------------------------------------------

    /**
     * Loads this student's per-thread activity + participation metrics.
     * Relies on {@link ControllerStaffStatistics} to avoid duplicating logic.
     */
    private static void refreshStatistics() {
        if (theUser == null) return;

        // Reuse staff statistics pipeline
        ControllerStaffStatistics.selectStudent(theUser);

        int totalPosts      = ControllerStaffStatistics.getTotalPosts();
        int totalReplies    = ControllerStaffStatistics.getTotalReplies();
        int threads         = ControllerStaffStatistics.getThreadsParticipated();
        int activeDays      = ControllerStaffStatistics.getActiveDays();
        double avgLen       = ControllerStaffStatistics.getAverageReplyLength();
        double activity     = ControllerStaffStatistics.getActivityScore();
        double engagement   = ControllerStaffStatistics.getEngagementScore();
        double consistency  = ControllerStaffStatistics.getConsistencyScore();
        double finalScore   = ControllerStaffStatistics.getFinalScore();

        ViewStudentGrading.updateStatistics(
                totalPosts, totalReplies, threads, activeDays,
                avgLen, activity, engagement, consistency, finalScore
        );
    }

    // --------------------------------------------------------------------
    // GRADE + FEEDBACK
    // --------------------------------------------------------------------

    /**
     * Loads the student's grade + staff feedback.
     *
     * <p>Database row layout:</p>
     * <ul>
     *   <li>{@code row[3]} → grade_value</li>
     *   <li>{@code row[4]} → feedback text</li>
     * </ul>
     */
    private static void refreshGrade() {
        if (theUser == null) return;

        try {
            String[] row = theDatabase.getGradeForStudent(theUser.getUserName());
            if (row == null) {
                ViewStudentGrading.updateGradeAndFeedback(
                        "No grade yet", "No feedback available yet.");
            } else {
                String grade    = row[3];
                String feedback = row[4];
                ViewStudentGrading.updateGradeAndFeedback(grade, feedback);
            }
        } catch (SQLException e) {
            ViewStudentGrading.alert.setContentText(
                    "Error loading grade: " + e.getMessage());
            ViewStudentGrading.alert.showAndWait();
        }
    }

    // --------------------------------------------------------------------
    // CHAT / FEEDBACK THREAD
    // --------------------------------------------------------------------

    /**
     * Loads the entire feedback conversation for this student's grading entry.
     * If the student has no grade yet, a placeholder message is shown.
     */
    private static void refreshChat() {
        if (theUser == null) return;

        try {
            String[] grade = theDatabase.getGradeForStudent(theUser.getUserName());
            if (grade == null) {
                // No grade means no chat thread exists yet
                ViewStudentGrading.updateChatMessages(List.of(
                        "No grade yet — conversation will appear here once your work is graded."
                ));
                return;
            }

            int gradeId = Integer.parseInt(grade[0]);
            List<String[]> rows = theDatabase.getRepliesForGrade(gradeId);

            List<String> formatted = new ArrayList<>();
            for (String[] r : rows) {
                // Format: ROLE: message (timestamp)
                formatted.add(r[0].toUpperCase() + ": " + r[1] + " (" + r[2] + ")");
            }

            ViewStudentGrading.updateChatMessages(formatted);

        } catch (Exception e) {
            ViewStudentGrading.alert.setContentText(
                    "Error loading conversation: " + e.getMessage());
            ViewStudentGrading.alert.showAndWait();
        }
    }

    /**
     * Called when the student clicks “Send”.
     * Inserts a new reply into the grade-level conversation.
     *
     * <p>Notes:</p>
     * <ul>
     *   <li>Students can only send messages if they already have a grade entry.</li>
     *   <li>Staff replies use the role "staff"; student replies use "student".</li>
     * </ul>
     */
    protected static void sendReply() {
        if (theUser == null) return;

        String message = ViewStudentGrading.field_Reply.getText();
        if (message == null) message = "";
        message = message.trim();
        if (message.isEmpty()) return;

        try {
            String[] grade = theDatabase.getGradeForStudent(theUser.getUserName());
            if (grade == null) {
                ViewStudentGrading.alert.setContentText(
                        "You don't have a grade yet, so you can't reply to feedback.");
                ViewStudentGrading.alert.showAndWait();
                return;
            }

            int gradeId = Integer.parseInt(grade[0]);

            // Insert reply with role="student"
            theDatabase.insertGradeReply(gradeId, "student", message);

            ViewStudentGrading.field_Reply.clear();
            refreshChat();

        } catch (Exception e) {
            ViewStudentGrading.alert.setContentText(
                    "Error sending reply: " + e.getMessage());
            ViewStudentGrading.alert.showAndWait();
        }
    }
}
