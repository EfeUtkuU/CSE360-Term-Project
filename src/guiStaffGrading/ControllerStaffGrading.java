package guiStaffGrading;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.User;
import guiStaff.ViewStaffHome;
import javafx.stage.Stage;

/**
 * <h2>Class: ControllerStaffGrading</h2>
 *
 * <p><strong>Description:</strong><br>
 * Controller for the Staff Grading page in the MVC system.
 * This controller handles all interactions between:
 * </p>
 *
 * <ul>
 *   <li>the grading GUI ({@link ViewStaffGrading})</li>
 *   <li>the shared {@link Database}</li>
 *   <li>staff actions such as selecting a student, grading, and replying</li>
 * </ul>
 *
 * <p>
 * This class follows the same static, non-instantiated MVC pattern used
 * by {@code ControllerUserLogin}, meaning no objects of this class are
 * created—only static methods are invoked.
 * </p>
 *
 * @version 1.00 — 2025-11-25 — Initial Javadoc-complete controller version
 */
public class ControllerStaffGrading {

    /** Reference to the shared database instance. */
    private static Database theDatabase = FoundationsMain.database;

    /** The primary JavaFX stage for the grading window. */
    private static Stage theStage;


    // =========================================================================
    // INITIAL PAGE SETUP
    // =========================================================================

    /**
     * Displays the Staff Grading Page.
     * <p>
     * This method is called by {@link ViewStaffHome} when the staff member
     * clicks the <em>Grade Students</em> button.
     * </p>
     *
     * @param stage the JavaFX stage where the grading view will be displayed
     * @param user  the currently logged-in staff member
     */
    public static void displayGradingPage(Stage stage, User user) {
        theStage = stage;
        ViewStaffGrading.displayStaffGrading(theStage, user);
        populateStudentList();
    }


    // =========================================================================
    // LOAD STUDENT LIST
    // =========================================================================

    /**
     * Loads all student usernames into the grading page’s student ListView.
     * <p>
     * The method retrieves all student records from the database and extracts
     * the username field for display.
     * </p>
     */
    public static void populateStudentList() {
        List<String[]> rows = theDatabase.getAllStudents();
        List<String> usernames = new ArrayList<>();

        for (String[] row : rows) {
            usernames.add(row[0]);
        }

        ViewStaffGrading.list_Students.getItems().setAll(usernames);
    }


    // =========================================================================
    // LOAD SELECTED STUDENT'S DATA
    // =========================================================================

    /**
     * Handles the event where a staff member selects a student
     * from the student ListView.
     * <p>
     * This triggers loading:
     * </p>
     * <ul>
     *   <li>the student's grade and feedback</li>
     *   <li>the chat (staff–student replies)</li>
     * </ul>
     */
    protected static void studentSelected() {
        String studentId = ViewStaffGrading.list_Students.getSelectionModel()
                .getSelectedItem();

        if (studentId == null) return;

        loadStudentGrade(studentId);
        loadChatMessages(studentId);
    }

    /**
     * Loads an existing grade and feedback for a specific student
     * into the grading text fields.
     *
     * @param studentId the selected student's username
     */
    private static void loadStudentGrade(String studentId) {
        try {
            String[] row = theDatabase.getGradeForStudent(studentId);

            if (row == null) {
                ViewStaffGrading.text_Grade.setText("");
                ViewStaffGrading.text_Feedback.setText("");
                return;
            }

            ViewStaffGrading.text_Grade.setText(row[3]);     // grade_value
            ViewStaffGrading.text_Feedback.setText(row[4]);  // feedback

        } catch (SQLException e) {
            System.err.println("Error loading grade: " + e.getMessage());
        }
    }


    // =========================================================================
    // SAVE GRADE
    // =========================================================================

    /**
     * Saves the grade and feedback entered by the staff member.
     * <p>
     * Validation is performed to ensure the grade is a valid integer.
     * On success, the grade is inserted or updated in the database.
     * </p>
     */
    protected static void saveGrade() {
        String studentId = ViewStaffGrading.list_Students.getSelectionModel()
                .getSelectedItem();
        if (studentId == null) return;

        String gradeStr = ViewStaffGrading.text_Grade.getText();
        String feedback = ViewStaffGrading.text_Feedback.getText();

        int gradeValue;
        try {
            gradeValue = Integer.parseInt(gradeStr);
        } catch (NumberFormatException e) {
            ViewStaffGrading.alert.setContentText("Grade must be an integer!");
            ViewStaffGrading.alert.showAndWait();
            return;
        }

        String staffId = theDatabase.getCurrentUsername();

        try {
            theDatabase.insertOrUpdateGrade(studentId, staffId, gradeValue, feedback);
            ViewStaffGrading.alert.setContentText("Grade saved successfully.");
            ViewStaffGrading.alert.showAndWait();
        } catch (SQLException e) {
            ViewStaffGrading.alert.setContentText("Error saving grade: " + e.getMessage());
            ViewStaffGrading.alert.showAndWait();
        }

        loadStudentGrade(studentId);
    }


    // =========================================================================
    // CHAT (STAFF ↔ STUDENT)
    // =========================================================================

    /**
     * Loads all staff–student chat messages associated with the student’s grade
     * and displays them in the chat ListView.
     *
     * @param studentId the selected student's username
     */
    private static void loadChatMessages(String studentId) {
        try {
            String[] grade = theDatabase.getGradeForStudent(studentId);
            if (grade == null) {
                ViewStaffGrading.list_Chat.getItems().clear();
                return;
            }

            int gradeId = Integer.parseInt(grade[0]);
            List<String[]> rows = theDatabase.getRepliesForGrade(gradeId);

            List<String> formatted = new ArrayList<>();
            for (String[] r : rows) {
                formatted.add(r[0].toUpperCase() + ": " + r[1] + " (" + r[2] + ")");
            }

            ViewStaffGrading.list_Chat.getItems().setAll(formatted);

        } catch (Exception e) {
            System.err.println("Error loading chat: " + e.getMessage());
        }
    }

    /**
     * Sends a new chat reply from the staff member to the selected student.
     * <p>
     * After insertion, the chat list is refreshed and the reply input box is cleared.
     * </p>
     */
    protected static void sendReply() {
        String studentId = ViewStaffGrading.list_Students.getSelectionModel()
                .getSelectedItem();
        if (studentId == null) return;

        String message = ViewStaffGrading.text_Reply.getText().trim();
        if (message.isEmpty()) return;

        try {
            String[] grade = theDatabase.getGradeForStudent(studentId);
            if (grade == null) return;

            int gradeId = Integer.parseInt(grade[0]);

            theDatabase.insertGradeReply(gradeId, "staff", message);

            ViewStaffGrading.text_Reply.clear();
            loadChatMessages(studentId);

        } catch (Exception e) {
            System.err.println("Error sending reply: " + e.getMessage());
        }
    }


    // =========================================================================
    // LOGOUT + QUIT
    // =========================================================================

    /**
     * Logs out the current staff user and returns to the User Login page.
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(theStage);
    }

    /**
     * Terminates the entire application.
     * <p><strong>Warning:</strong> This exits immediately without confirmation.</p>
     */
    protected static void performQuit() {
        System.exit(0);
    }


    // =========================================================================
    // RETURN TO STAFF HOME
    // =========================================================================

    /**
     * Navigates back to the staff home page.
     * <p>
     * This method reloads the current staff user from the database
     * and reopens the home view.
     * </p>
     */
    protected static void goBack() {
        String username = theDatabase.getCurrentUsername();
        User user = new User(username);

        ViewStaffHome.displayStaffHome(theStage, user);
    }
}
