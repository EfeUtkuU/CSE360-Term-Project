package guiManageInvitations;

import javafx.stage.Stage;

public class ControllerManageInvitations {

    // Show the invitation management window
    public static void performManageInvitations(Stage ownerStage) {
        ViewManageInvitations.displayManageInvitations(
            ownerStage, 
            ModelManageInvitations.getAllInvitations()
        );
    }

    // Handle deleting an invitation
    protected static void handleDelete(String code) {
        ModelManageInvitations.removeInvitation(code);
    }

    // Get current invitation count
    protected static int getInvitationCount() {
        return ModelManageInvitations.getNumberOfInvitations();
    }

    // Close window
    protected static void performClose(Stage stage) {
        stage.close();
    }
}