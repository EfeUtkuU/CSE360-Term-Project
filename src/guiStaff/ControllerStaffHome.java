package guiStaff;

import guiStaffPosts.ViewStaffPosts;
import entityClasses.User;
import javafx.stage.Stage;

/**
 * <p><strong>Class:</strong> ControllerStaffHome</p>
 *
 * <p><strong>Description:</strong>
 * Controller for the Staff Home page.  Handles button actions such as
 * account update, logout, quitting the application, and opening the
 * staff discussion (posts) page.
 * </p>
 *
 * @version 2.00 — 2025-10-26
 */
public class ControllerStaffHome {

    /**
     * Opens the account update page for the current staff user.
     */
    protected static void performUpdate() {
        guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewStaffHome.theStage, ViewStaffHome.theUser);
    }

    /**
     * Logs out the current user and returns to the login page.
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffHome.theStage);
    }

    /**
     * Terminates the application.
     */
    protected static void performQuit() {
        System.exit(0);
    }

    /**
     * Opens the Staff Posts (discussion) interface.
     *
     * @param stage the primary Stage
     * @param user  the logged-in staff user
     */
    protected static void performOpenDiscussion(Stage stage, User user) {
        ViewStaffPosts.displayStaffPosts(stage, user);
    }
}
