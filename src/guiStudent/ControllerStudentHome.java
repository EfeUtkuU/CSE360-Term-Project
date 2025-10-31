package guiStudent;

import guiStudentPosts.ViewStudentPosts;
import entityClasses.User;
import javafx.stage.Stage;

/**
 * <p><strong>Class:</strong> ControllerStudentHome</p>
 *
 * <p><strong>Description:</strong>
 * Controller for the Student Home page.  
 * Provides actions for logout, quit, and opening the discussion (posts) thread.
 * </p>
 *
 * @version 2.00 — 2025-10-26
 */
public class ControllerStudentHome {

    /**
     * Logs out the current student and returns to the login page.
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentHome.theStage);
    }

    /**
     * Exits the application.
     */
    protected static void performQuit() {
        System.exit(0);
    }

    /**
     * Opens the Student Discussion Thread (posts GUI).
     *
     * @param stage the active JavaFX Stage
     * @param user  the logged-in student
     */
    protected static void performOpenDiscussion(Stage stage, User user) {
        ViewStudentPosts.displayStudentPosts(stage, user);
    }
}
