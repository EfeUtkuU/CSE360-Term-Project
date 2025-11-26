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
 * Controller for the Staff Grading Page.
 * 
 * Matches the same static, non-instantiated MVC pattern
 * used by ControllerUserLogin.
 */
public class ControllerStaffGrading {

    /** Reference to the shared database instance */
    private static Database theDatabase = FoundationsMain.database;

    /** Reference to the JavaFX stage */
    private static Stage theStage;


    // =========================================================================
    // SETUP PAGE
    // =========================================================================
    /**
     * Called by ViewStaffHome when staff clicks "Grade Students".
     */
    public static void displayGradingPage(Stage stage, User user) {
        theStage = stage;
        ViewStaffGrading.displayStaffGrading(theStage,user);
        populateStudentList();
    }


    // =========================================================================
    // LOAD STUDENTS
    // =========================================================================
    /**
     * Loads all students into the left ListView.
     */
    public static void populateStudentList() {
        List<String[]> rows = theDatabase.getAllStudents();
        List<String> usernames = new ArrayList<>();

        for (String[] row : rows) {
            usernames.add(row[0]); // username
        }

        ViewStaffGrading.list_Students.getItems().setAll(usernames);
    }


    // =========================================================================
    // LOAD EXISTING GRADE + CHAT
    // =========================================================================
    /**
     * Called when staff selects a student from the ListView.
     */
    protected static void studentSelected() {
        String studentId = ViewStaffGrading.list_Students.getSelectionModel().getSelectedItem();
        if (studentId == null) return;

        loadStudentGrade(studentId);
        loadChatMessages(studentId);
    }


    /**
     * Loads grade + feedback into text boxes.
     */
    private static void loadStudentGrade(String studentId) {
        try {
            String[] row = theDatabase.getGradeForStudent(studentId);

            if (row == null) {
                // No grade yet
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
     * Called when staff clicks "Save Grade".
     */
    protected static void saveGrade() {
        String studentId = ViewStaffGrading.list_Students.getSelectionModel().getSelectedItem();
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
    // CHAT (STAFF <-> STUDENT)
    // =========================================================================
    /**
     * Loads all chat messages into the chat ListView.
     */
    private static void loadChatMessages(String studentId) {
        try {
            String[] grade = theDatabase.getGradeForStudent(studentId);
            if (grade == null) {
                ViewStaffGrading.list_Chat.getItems().clear();
                return;
            }

            int gradeId = Integer.parseInt(grade[0]); // grade table ID
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
     * Called when staff presses "Send Reply".
     */
    protected static void sendReply() {
        String studentId = ViewStaffGrading.list_Students.getSelectionModel().getSelectedItem();
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
    
    /**
     * Logs out the current user and returns to the login page.
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(theStage);
    }

    /**
     * Terminates the application.
     */
    protected static void performQuit() {
        System.exit(0);
    }



    // =========================================================================
    // RETURN TO STAFF HOME
    // =========================================================================
    protected static void goBack() {
        String username = theDatabase.getCurrentUsername();
        User user = new User(username);

        guiStaff.ViewStaffHome.displayStaffHome(theStage, user);
    }


}
