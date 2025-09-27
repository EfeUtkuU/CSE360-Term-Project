package guiListUsers;

import java.util.List;
import database.Database;

public class ModelListUsers {
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    // Returns a list of all users from the DB
    protected static List<String[]> getAllUsers() {
        return theDatabase.getAllUsers();
    }
}