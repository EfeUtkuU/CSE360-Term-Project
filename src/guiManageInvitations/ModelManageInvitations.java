package guiManageInvitations;

import java.util.List;
import database.Database;

public class ModelManageInvitations {
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    // Get all invitations
    protected static List<String[]> getAllInvitations() {
        return theDatabase.getAllInvitations();
    }

    // Remove invitation by code
    protected static void removeInvitation(String code) {
        theDatabase.removeInvitationAfterUse(code);
    }

    // Get current number of invitations
    protected static int getNumberOfInvitations() {
        return theDatabase.getNumberOfInvitations();
    }
}
