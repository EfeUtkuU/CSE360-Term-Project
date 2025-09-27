package guiUserLogin;

public class Model {

    /*-********************************************************************************************
     * Simplified login validation for username and password.
     * At login, major companies only check if inputs are not empty
     * and have reasonable length (e.g., 3–32 characters).
     * Strong complexity checks are only enforced at registration/change.
     ********************************************************************************************/

    // Validate username at login
    public static String checkForValidUserName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "*** Error *** The username is empty!";
        }
        if (input.length() < 3) {
            return "*** Error *** The username must be at least 3 characters!";
        }
        if (input.length() > 32) {
            return "*** Error *** The username cannot be more than 32 characters!";
        }
        return "";
    }

    // Validate password at login
    public static String evaluatePassword(String input) {
        if (input == null || input.isEmpty()) {
            return "*** Error *** The password is empty!";
        }
        if (input.length() < 3) {
            return "*** Error *** The password must be at least 3 characters!";
        }
        if (input.length() > 32) {
            return "*** Error *** The password cannot be more than 32 characters!";
        }
        return "";
    }
}
