package guiDeleteUsers;

import javafx.stage.Stage;

public class ControllerDeleteUsers {

    // Entry point to show the Delete Users UI
    public static void performDeleteUser(Stage ownerStage) {
        ViewDeleteUsers.displayDeleteUsers(ownerStage, ModelDeleteUsers.getAllUsers());
    }

    // Delete user via the model
    protected static boolean handleDelete(String username) {
        return ModelDeleteUsers.deleteUser(username);
    }

    // Close the delete window
    protected static void performClose(Stage stage) {
        stage.close();
    }
}
