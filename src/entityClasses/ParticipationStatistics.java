package entityClasses;

/**
 * <h2>Class: ParticipationStatistics</h2>
 *
 * <p><strong>Description:</strong><br>
 * Represents a snapshot of a single student's participation statistics in the
 * discussion system. This class stores both:
 * </p>
 *
 * <ul>
 *   <li>Raw activity counts (posts, replies, threads participated, etc.)</li>
 *   <li>Derived normalized scores (activity, engagement, consistency, and
 *       final composite score)</li>
 * </ul>
 *
 * <p>
 * Instances of this class are typically:
 * </p>
 * <ul>
 *   <li>Computed by {@code ParticipationCalculator}</li>
 *   <li>Stored and retrieved by the database layer (e.g., {@code ParticipationStatsDAO})</li>
 *   <li>Displayed in the Student Discussion Statistics GUI and related views</li>
 * </ul>
 *
 * @author
 *   Efe Utku Ural
 *
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
     * Number of distinct calendar days on which this student
     * posted or replied during the evaluated period.
     */
    private int activeDays;

    /** Average reply length for this student, measured in characters. */
    private double averageReplyLength;

    // ---------------------- Derived scores ----------------------

    /** Score reflecting the volume of activity (posts + replies). */
    private double activityScore;

    /** Score reflecting depth, diversity, or quality of engagement. */
    private double engagementScore;

    /** Score reflecting consistency of activity over time. */
    private double consistencyScore;

    /** Final weighted participation score combining all sub-scores. */
    private double finalScore;

    // ---------------------- Constructors ----------------------

    /**
     * No-argument constructor.
     * <p>
     * Used primarily by frameworks, database loaders, and serialization.
     * </p>
     */
    public ParticipationStatistics() {
        // Intentionally empty.
    }

    /**
     * Constructs a fully-initialized statistics snapshot.
     *
     * @param studentId            unique username of the student
     * @param totalPosts           total number of posts
     * @param totalReplies         total number of replies
     * @param threadsParticipated  number of threads the student participated in
     * @param activeDays           number of active calendar days
     * @param averageReplyLength   average reply length in characters
     * @param activityScore        normalized activity score
     * @param engagementScore      normalized engagement score
     * @param consistencyScore     normalized consistency score
     * @param finalScore           final weighted participation score
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

    // ---------------------- Getters and Setters ----------------------

    /**
     * @return the student's username
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Sets the student's username.
     *
     * @param studentId the new student identifier
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * @return total number of posts created by the student
     */
    public int getTotalPosts() {
        return totalPosts;
    }

    /**
     * Sets the total number of posts.
     *
     * @param totalPosts number of posts
     */
    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    /**
     * @return total number of replies created by the student
     */
    public int getTotalReplies() {
        return totalReplies;
    }

    /**
     * Sets the total number of replies.
     *
     * @param totalReplies number of replies
     */
    public void setTotalReplies(int totalReplies) {
        this.totalReplies = totalReplies;
    }

    /**
     * @return number of distinct threads the student participated in
     */
    public int getThreadsParticipated() {
        return threadsParticipated;
    }

    /**
     * Sets the thread participation count.
     *
     * @param threadsParticipated number of threads
     */
    public void setThreadsParticipated(int threadsParticipated) {
        this.threadsParticipated = threadsParticipated;
    }

    /**
     * @return number of calendar days with activity
     */
    public int getActiveDays() {
        return activeDays;
    }

    /**
     * Sets the number of active days.
     *
     * @param activeDays count of active days
     */
    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }

    /**
     * @return average reply length in characters
     */
    public double getAverageReplyLength() {
        return averageReplyLength;
    }

    /**
     * Sets the average reply length.
     *
     * @param averageReplyLength reply length in characters
     */
    public void setAverageReplyLength(double averageReplyLength) {
        this.averageReplyLength = averageReplyLength;
    }

    /**
     * @return normalized activity score
     */
    public double getActivityScore() {
        return activityScore;
    }

    /**
     * Sets the activity score.
     *
     * @param activityScore normalized value
     */
    public void setActivityScore(double activityScore) {
        this.activityScore = activityScore;
    }

    /**
     * @return normalized engagement score
     */
    public double getEngagementScore() {
        return engagementScore;
    }

    /**
     * Sets the engagement score.
     *
     * @param engagementScore normalized value
     */
    public void setEngagementScore(double engagementScore) {
        this.engagementScore = engagementScore;
    }

    /**
     * @return normalized consistency score
     */
    public double getConsistencyScore() {
        return consistencyScore;
    }

    /**
     * Sets the consistency score.
     *
     * @param consistencyScore normalized value
     */
    public void setConsistencyScore(double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }

    /**
     * @return final weighted participation score
     */
    public double getFinalScore() {
        return finalScore;
    }

    /**
     * Sets the final participation score.
     *
     * @param finalScore weighted final score
     */
    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    // ---------------------- Utility ----------------------

    /**
     * Returns a string summary of this statistics snapshot.
     *
     * @return formatted string representation
     */
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
