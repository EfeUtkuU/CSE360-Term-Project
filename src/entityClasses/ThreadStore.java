package entityClasses;

import database.Database;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Class: ThreadStore</h2>
 *
 * <p><strong>Description:</strong><br>
 * Centralized manager for all {@link Thread} objects in the system.
 * This class loads, caches, creates, updates, and deletes discussion
 * threads, acting as the integration point between the GUI and the
 * underlying H2 database (via {@link Database}).
 * </p>
 *
 * <p>This class follows the Singleton design pattern, ensuring only one
 * thread list exists in memory:</p>
 *
 * <ul>
 *   <li>All thread data is loaded from the database on demand</li>
 *   <li>The store maintains an in-memory cache for fast GUI access</li>
 *   <li>Modifying operations automatically refresh the cache</li>
 * </ul>
 *
 * @version
 *   1.00 — 2025-11-22 — Initial version with full DB synchronization and basic CRUD
 */
public final class ThreadStore {

    // -------------------------- Fields --------------------------

    /** Singleton instance of the ThreadStore. */
    private static ThreadStore instance;

    /** In-memory list of all forum threads. */
    private final List<Thread> threadList = new ArrayList<>();

    /** Private constructor to enforce Singleton usage. */
    private ThreadStore() {}

    // -------------------------- Singleton Accessor --------------------------

    /**
     * Returns the shared singleton instance of the {@code ThreadStore}.
     *
     * @return the global {@code ThreadStore} instance
     */
    public static ThreadStore getInstance() {
        if (instance == null) instance = new ThreadStore();
        return instance;
    }

    // -------------------------- LOAD FROM DB --------------------------

    /**
     * Loads all threads from the database into the in-memory cache.
     * <p>
     * Existing cached threads are cleared before reloading.
     * Each row returned from the database is converted into a {@link Thread}.
     * </p>
     */
    public void loadFromDatabase() {
        threadList.clear();

        try {
            Database db = applicationMain.FoundationsMain.database;
            db.connectToDatabase(); // safe if already connected

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

    /**
     * Returns a defensive copy of all currently cached threads.
     *
     * @return a list of all {@link Thread} objects
     */
    public List<Thread> getAllThreads() {
        return new ArrayList<>(threadList);
    }

    /**
     * Retrieves a thread by its database ID.
     *
     * @param id the thread's unique identifier
     * @return the matching {@link Thread}, or {@code null} if not found
     */
    public Thread getThreadById(int id) {
        return threadList.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // -------------------------- CREATE THREAD --------------------------

    /**
     * Creates a new discussion thread and persists it to the database.
     * <p>
     * After insertion, the thread list is fully reloaded and the new
     * thread is returned.
     * </p>
     *
     * @param title  the title of the new thread
     * @param author the username of the creator
     * @return the newly created {@link Thread}, or {@code null} on failure
     */
    public Thread createThread(String title, String author) {
        try {
            Database db = applicationMain.FoundationsMain.database;
            db.connectToDatabase();

            int id = db.insertThread(title, author);

            loadFromDatabase();
            return getThreadById(id);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // -------------------------- UPDATE THREAD --------------------------

    /**
     * Renames an existing thread.
     *
     * @param threadId the ID of the thread to rename
     * @param newTitle the new thread title
     * @return {@code true} if renamed successfully; {@code false} otherwise
     */
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

    /**
     * Deletes a thread (and its associated posts/replies as defined in the schema).
     * <p>
     * After deletion, the in-memory thread list is refreshed.
     * </p>
     *
     * @param threadId the ID of the thread to delete
     * @return {@code true} if deleted successfully; {@code false} otherwise
     */
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
