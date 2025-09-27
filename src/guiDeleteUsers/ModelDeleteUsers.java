package guiDeleteUsers;

import java.util.List;
import database.Database;

public class ModelDeleteUsers {
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    // Get all users for the delete screen
    protected static List<String[]> getAllUsers() {
        return theDatabase.getAllUsers();
    }

    // Delete a user by username
    protected static boolean deleteUser(String username) {
        return theDatabase.deleteUser(username);
    }
}