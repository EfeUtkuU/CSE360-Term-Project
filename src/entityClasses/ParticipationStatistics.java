package entityClasses;

/**
 * <p><strong>Class:</strong> ParticipationStatistics</p>
 *
 * <p><strong>Description:</strong>
 * Immutable-style snapshot of a single student's discussion participation.
 * Holds both raw activity counts (posts, replies, threads, etc.) and
 * derived scores (activity, engagement, consistency, and overall
 * participation score).
 *
 * These objects are:
 * <ul>
 *   <li>Computed by a calculator class (e.g., ParticipationCalculator)</li>
 *   <li>Stored/loaded by the database layer (ParticipationStatsDAO)</li>
 *   <li>Displayed in the Student Discussion Statistics GUI and other views</li>
 * </ul>
 * </p>
 *
 * @author
 *   Efe Utku Ural
 * @version
 *   1.01 — 2025-11-24 — Updated studentId to String (username identifier)
 */
public class ParticipationStatistics {

    // ---------------------- Identity ----------------------

    /** Unique identifier for the student (username). */
    private String studentId;

    // ---------------------- Raw activity counts ----------------------

    /** Total number of top-level posts created by this student. */
    private int totalPosts;

    /** Total number of replies created by this student. */
    private int totalReplies;

    /** Number of distinct threads in which this student participated. */
    private int threadsParticipated;

    /**
     * Number of distinct days (calendar days) on which this student
     * posted or replied within the selected date range.
     */
    private int activeDays;

    /**
     * Average length of replies (for this student) measured in characters.
     */
    private double averageReplyLength;

    // ---------------------- Derived scores ----------------------

    /** Score that reflects pure volume of activity (posts + replies). */
    private double activityScore;

    /** Score that reflects depth/diversity/quality of engagement. */
    private double engagementScore;

    /** Score that reflects consistency of participation across time. */
    private double consistencyScore;

    /** Final weighted participation score combining all sub-scores. */
    private double finalScore;

    // ---------------------- Constructors ----------------------

    /** No-arg constructor for frameworks, DB, and default usage. */
    public ParticipationStatistics() {
        // Intentionally empty.
    }

    /**
     * Convenience constructor for quickly building a fully-specified
     * statistics snapshot in custom code or tests.
     */
    public ParticipationStatistics(
            String studentId,
            int totalPosts,
            int totalReplies,
            int threadsParticipated,
            int activeDays,
            double averageReplyLength,
            double activityScore,
            double engagementScore,
            double consistencyScore,
            double finalScore) {

        this.studentId = studentId;
        this.totalPosts = totalPosts;
        this.totalReplies = totalReplies;
        this.threadsParticipated = threadsParticipated;
        this.activeDays = activeDays;
        this.averageReplyLength = averageReplyLength;
        this.activityScore = activityScore;
        this.engagementScore = engagementScore;
        this.consistencyScore = consistencyScore;
        this.finalScore = finalScore;
    }

    // ---------------------- Getters and setters ----------------------

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getTotalReplies() {
        return totalReplies;
    }

    public void setTotalReplies(int totalReplies) {
        this.totalReplies = totalReplies;
    }

    public int getThreadsParticipated() {
        return threadsParticipated;
    }

    public void setThreadsParticipated(int threadsParticipated) {
        this.threadsParticipated = threadsParticipated;
    }

    public int getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }

    public double getAverageReplyLength() {
        return averageReplyLength;
    }

    public void setAverageReplyLength(double averageReplyLength) {
        this.averageReplyLength = averageReplyLength;
    }

    public double getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(double activityScore) {
        this.activityScore = activityScore;
    }

    public double getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(double engagementScore) {
        this.engagementScore = engagementScore;
    }

    public double getConsistencyScore() {
        return consistencyScore;
    }

    public void setConsistencyScore(double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    // ---------------------- Utility ----------------------

    @Override
    public String toString() {
        return "ParticipationStatistics{" +
               "studentId='" + studentId + '\'' +
               ", totalPosts=" + totalPosts +
               ", totalReplies=" + totalReplies +
               ", threadsParticipated=" + threadsParticipated +
               ", activeDays=" + activeDays +
               ", averageReplyLength=" + averageReplyLength +
               ", activityScore=" + activityScore +
               ", engagementScore=" + engagementScore +
               ", consistencyScore=" + consistencyScore +
               ", finalScore=" + finalScore +
               '}';
    }
}
