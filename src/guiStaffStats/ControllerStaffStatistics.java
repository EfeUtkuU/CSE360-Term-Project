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

    /** Thread-level breakdown of posts and replies. */
    private static List<ModelStaffStatistics.ThreadActivity> activityList;

    /** Cached participation metrics for the selected student. */
    private static ParticipationStatistics participationStats;

    // ----------------------------------------------------------------------
    //  PUBLIC CONTROLLER API
    // ----------------------------------------------------------------------

    /**
     * Selects a student to display statistics for.
     * <p>
     * This automatically:
     * <ul>
     *     <li>Loads the student's per-thread activity breakdown</li>
     *     <li>Loads participation metrics via {@link ParticipationStore}</li>
     * </ul>
     * If {@code student == null}, all related fields reset to empty.
     *
     * @param student the student being selected, or {@code null} to clear selection
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

    /**
     * Returns the currently selected student.
     *
     * @return the selected student or {@code null} if none selected
     */
    public static User getSelectedStudent() {
        return selectedStudent;
    }

    /**
     * Returns the per-thread activity list for the selected student.
     *
     * @return list of thread activity objects
     */
    public static List<ModelStaffStatistics.ThreadActivity> getActivityList() {
        return activityList;
    }

    // ----------------------------------------------------------------------
    //  BASIC COUNTS (existing)
    // ----------------------------------------------------------------------

    /**
     * Computes how many posts the selected student has created.
     *
     * @return total number of posts
     */
    public static int getTotalPosts() {
        if (activityList == null) return 0;
        return ModelStaffStatistics.countTotalPosts(activityList);
    }

    /**
     * Computes how many replies the selected student has created.
     *
     * @return total number of replies
     */
    public static int getTotalReplies() {
        if (activityList == null) return 0;
        return ModelStaffStatistics.countTotalReplies(activityList);
    }

    // ----------------------------------------------------------------------
    //  PARTICIPATION METRICS (New)
    // ----------------------------------------------------------------------

    /**
     * Returns the full ParticipationStatistics object for the selected student.
     *
     * @return stats object or {@code null} if none loaded
     */
    public static ParticipationStatistics getParticipationStats() {
        return participationStats;
    }

    /**
     * Returns how many threads the student has participated in.
     *
     * @return thread count
     */
    public static int getThreadsParticipated() {
        return participationStats != null ? participationStats.getThreadsParticipated() : 0;
    }

    /**
     * Returns how many unique active days the student has participated on.
     *
     * @return number of active days
     */
    public static int getActiveDays() {
        return participationStats != null ? participationStats.getActiveDays() : 0;
    }

    /**
     * Returns the student's average reply length.
     *
     * @return average reply length, in characters
     */
    public static double getAverageReplyLength() {
        return participationStats != null ? participationStats.getAverageReplyLength() : 0.0;
    }

    /**
     * Returns the student's activity score (computed metric).
     *
     * @return activity score
     */
    public static double getActivityScore() {
        return participationStats != null ? participationStats.getActivityScore() : 0.0;
    }

    /**
     * Returns the student's engagement score.
     *
     * @return engagement score
     */
    public static double getEngagementScore() {
        return participationStats != null ? participationStats.getEngagementScore() : 0.0;
    }

    /**
     * Returns the student's consistency score.
     *
     * @return consistency score
     */
    public static double getConsistencyScore() {
        return participationStats != null ? participationStats.getConsistencyScore() : 0.0;
    }

    /**
     * Returns the student's final aggregated participation score.
     *
     * @return the final participation score
     */
    public static double getFinalScore() {
        return participationStats != null ? participationStats.getFinalScore() : 0.0;
    }

    /**
     * Logs out the staff user and returns to the login view.
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffStatistics.theStage);
    }

    /**
     * Quits the application entirely.
     */
    protected static void performQuit() {
        System.exit(0);
    }

    // ----------------------------------------------------------------------
    //  LOAD STUDENTS  (UNCHANGED)
    // ----------------------------------------------------------------------

    /**
     * Loads all users who have the "student" role.
     *
     * @return list of student users
     */
    public static List<User> loadStudentUsers() {
        return User.loadStudentUsers();
    }

}
