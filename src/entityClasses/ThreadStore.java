package entityClasses;

import database.Database;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ThreadStore {

    private static ThreadStore instance;

    private final List<Thread> threadList = new ArrayList<>();

    private ThreadStore() {}

    public static ThreadStore getInstance() {
        if (instance == null) instance = new ThreadStore();
        return instance;
    }

    // -------------------------- LOAD FROM DB --------------------------

    public void loadFromDatabase() {
        threadList.clear();

        try {
            Database db = applicationMain.FoundationsMain.database;
            db.connectToDatabase();   // safe — does nothing if already connected

            List<String[]> rows = db.getAllThreads();

            for (String[] row : rows) {
                int id = Integer.parseInt(row[0]);
                String title = row[1];
                String author = row[2];

                LocalDateTime created;
                try {
                    created = Timestamp.valueOf(row[3]).toLocalDateTime();
                } catch (Exception e) {
                    System.err.println("Failed to parse thread timestamp: " + row[3]);
                    created = LocalDateTime.now();
                }

                Thread t = new Thread(id, title, author, created, created);
                threadList.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------------- ACCESSORS --------------------------

    public List<Thread> getAllThreads() {
        return new ArrayList<>(threadList); // defensive copy
    }

    public Thread getThreadById(int id) {
        return threadList.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // -------------------------- CREATE THREAD --------------------------

    public Thread createThread(String title, String author) {
        try {
            Database db = applicationMain.FoundationsMain.database;
            db.connectToDatabase();

            int id = db.insertThread(title, author);

            // refresh list
            loadFromDatabase();

            return getThreadById(id);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // -------------------------- UPDATE THREAD --------------------------

    public boolean renameThread(int threadId, String newTitle) {
        try {
            Database db = applicationMain.FoundationsMain.database;
            db.connectToDatabase();

            db.updateThreadTitle(threadId, newTitle);

            loadFromDatabase();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -------------------------- DELETE THREAD --------------------------

    public boolean deleteThread(int threadId) {
        try {
            Database db = applicationMain.FoundationsMain.database;
            db.connectToDatabase();

            db.deleteThread(threadId);

            loadFromDatabase();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
