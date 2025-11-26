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
 * Controller for the student grade/feedback page.
 *  • Loads statistics for the logged-in student (reuses staff statistics logic)
 *  • Loads grade + staff feedback from the database
 *  • Loads and sends chat messages between student and staff
 * </p>
 */
public class ControllerStudentGrading {

    private static final Database theDatabase = FoundationsMain.database;

    private static Stage theStage;
    private static User  theUser;

    // ---------- Entry from Student Home ----------
    public static void displayStudentGrading(Stage stage, User user) {
        theStage = stage;
        theUser  = user;
        ViewStudentGrading.displayStudentGrading(theStage, theUser);
    }

    /** Called by the View when it is first constructed to load all data. */
    protected static void refreshAll() {
        refreshStatistics();
        refreshGrade();
        refreshChat();
    }

    // ---------- Statistics ----------
    private static void refreshStatistics() {
        if (theUser == null) return;

        // Reuse staff statistics calculation logic for this student
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

    // ---------- Grade & Feedback ----------
    private static void refreshGrade() {
        if (theUser == null) return;

        try {
            String[] row = theDatabase.getGradeForStudent(theUser.getUserName());
            if (row == null) {
                ViewStudentGrading.updateGradeAndFeedback(
                        "No grade yet", "No feedback available yet.");
            } else {
                String grade    = row[3];  // grade_value
                String feedback = row[4];  // feedback
                ViewStudentGrading.updateGradeAndFeedback(grade, feedback);
            }
        } catch (SQLException e) {
            ViewStudentGrading.alert.setContentText(
                    "Error loading grade: " + e.getMessage());
            ViewStudentGrading.alert.showAndWait();
        }
    }

    // ---------- Chat ----------
    private static void refreshChat() {
        if (theUser == null) return;

        try {
            String[] grade = theDatabase.getGradeForStudent(theUser.getUserName());
            if (grade == null) {
                ViewStudentGrading.updateChatMessages(List.of(
                        "No grade yet — conversation will appear here once your work is graded."
                ));
                return;
            }

            int gradeId = Integer.parseInt(grade[0]); // grade table ID
            List<String[]> rows = theDatabase.getRepliesForGrade(gradeId);

            List<String> formatted = new ArrayList<>();
            for (String[] r : rows) {
                // r[0] = sender role, r[1] = message, r[2] = timestamp
                formatted.add(r[0].toUpperCase() + ": " + r[1] + " (" + r[2] + ")");
            }

            ViewStudentGrading.updateChatMessages(formatted);

        } catch (Exception e) {
            ViewStudentGrading.alert.setContentText(
                    "Error loading conversation: " + e.getMessage());
            ViewStudentGrading.alert.showAndWait();
        }
    }

    /** Called when the student presses the "Send" button in the chat panel. */
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

            // role is "student" here; staff uses "staff" in ControllerStaffGrading
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
