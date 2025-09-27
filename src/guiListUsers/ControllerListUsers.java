package guiListUsers;

import javafx.stage.Stage;

public class ControllerListUsers {

    // Show the List Users window
    public static void performListUsers(Stage ownerStage) {
        ViewListUsers.displayListUsers(ownerStage, ModelListUsers.getAllUsers());
    }

    // Close the List Users window
    protected static void performClose(Stage stage) {
        stage.close();
    }
}
