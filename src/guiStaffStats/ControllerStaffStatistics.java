package guiStaffStats;

import entityClasses.User;
import guiStaff.ViewStaffHome;
import entityClasses.ParticipationStatistics;
import entityClasses.ParticipationStore;

import java.util.ArrayList;
import java.util.List;

/**
 * <p><strong>Class:</strong> ControllerStaffStatistics</p>
 *
 * <p><strong>Description:</strong>
 * Thin MVC controller that mediates between the Staff Statistics View
 * and the underlying ModelStaffStatistics + ParticipationStore logic.
 *
 * The controller NEVER touches SQL or Database directly.
 * All participation metrics come from ParticipationStore.
 * All per-thread post/reply activity comes from ModelStaffStatistics.
 *
 * </p>
 *
 * @version 2.00 — 2025-11-24 — Integrated ParticipationStore + automatic stats loading
 */
public class ControllerStaffStatistics {

    /** The student whose activity we are showing. */
    private static User selectedStudent;

    /** Thread-level breakdown of posts & replies. */
    private static List<ModelStaffStatistics.ThreadActivity> activityList;

    /** Cached participation metrics for the selected student. */
    private static ParticipationStatistics participationStats;

    // ----------------------------------------------------------------------
    //  PUBLIC CONTROLLER API
    // ----------------------------------------------------------------------

    /**
     * Called by the View when the staff selects a student.
     * Automatically loads:
     *  - per-thread post/reply activity
     *  - participation metrics (auto-compute if missing)
     */
    public static void selectStudent(User student) {
        selectedStudent = student;

        if (student != null) {
            // Load thread activity
            activityList = ModelStaffStatistics.loadActivityForStudent(student);

            // Load participation stats using ParticipationStore (OPTION A — auto compute)
            participationStats =
                    ParticipationStore.getInstance().loadStats(student.getUserName());

        } else {
            activityList = List.of();
            participationStats = null;
        }
    }

    /** @return selected student or null. */
    public static User getSelectedStudent() {
        return selectedStudent;
    }

    /** @return per-thread activity list. */
    public static List<ModelStaffStatistics.ThreadActivity> getActivityList() {
        return activityList;
    }

    // ----------------------------------------------------------------------
    //  BASIC COUNTS (existing)
    // ----------------------------------------------------------------------

    public static int getTotalPosts() {
        if (activityList == null) return 0;
        return ModelStaffStatistics.countTotalPosts(activityList);
    }

    public static int getTotalReplies() {
        if (activityList == null) return 0;
        return ModelStaffStatistics.countTotalReplies(activityList);
    }

    // ----------------------------------------------------------------------
    //  PARTICIPATION METRICS (New)
    // ----------------------------------------------------------------------

    public static ParticipationStatistics getParticipationStats() {
        return participationStats;
    }

    public static int getThreadsParticipated() {
        return participationStats != null ? participationStats.getThreadsParticipated() : 0;
    }

    public static int getActiveDays() {
        return participationStats != null ? participationStats.getActiveDays() : 0;
    }

    public static double getAverageReplyLength() {
        return participationStats != null ? participationStats.getAverageReplyLength() : 0.0;
    }

    public static double getActivityScore() {
        return participationStats != null ? participationStats.getActivityScore() : 0.0;
    }

    public static double getEngagementScore() {
        return participationStats != null ? participationStats.getEngagementScore() : 0.0;
    }

    public static double getConsistencyScore() {
        return participationStats != null ? participationStats.getConsistencyScore() : 0.0;
    }

    public static double getFinalScore() {
        return participationStats != null ? participationStats.getFinalScore() : 0.0;
    }
    
    /**
     * Logs out the current user and returns to the login page.
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffStatistics.theStage);
    }

    /**
     * Terminates the application.
     */
    protected static void performQuit() {
        System.exit(0);
    }


    // ----------------------------------------------------------------------
    //  LOAD STUDENTS  (UNCHANGED)
    //  (Your program always loads students like this — it's fine.)
    // ----------------------------------------------------------------------

    public static List<User> loadStudentUsers() {
        List<User> result = new ArrayList<>();

        try {
            database.Database db = new database.Database();
            db.connectToDatabase();
            List<String[]> rows = db.getAllStudents();

            for (String[] r : rows) {
                User u = new User(
                        r[0], "",           // username, password
                        r[1], "",           // firstName, middleName
                        r[2],               // lastName
                        r[1],               // preferred name fallback
                        r[3],               // email
                        r[4].equals("Admin"),
                        r[5].equals("Student"),
                        r[6].equals("Staff")
                );
                result.add(u);
            }

            db.closeConnection();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
