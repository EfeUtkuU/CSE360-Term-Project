package entityClasses;

import database.Database;

/**
 * <h2>Class: ParticipationCalculator</h2>
 *
 * <p><strong>Description:</strong><br>
 * Computes participation statistics for a student and produces
 * normalized scores in the 0–100 range for:
 * <ul>
 *     <li>Activity Score</li>
 *     <li>Engagement Score</li>
 *     <li>Consistency Score</li>
 *     <li>Final Composite Score</li>
 * </ul>
 *
 * The calculator gathers raw metrics from PostStore and ReplyStore,
 * computes raw scores, normalizes them to a 0–100 range, and saves
 * the resulting ParticipationStatistics into the database.
 *
 * Normalization boundaries (maximums):
 * <ul>
 *     <li><strong>Activity:</strong> maxPosts=10, maxReplies=20 → maxRaw=50</li>
 *     <li><strong>Engagement:</strong> maxThreads=10, maxAvgReply=200 → maxRaw=40</li>
 *     <li><strong>Consistency:</strong> maxActiveDays=14 → maxRaw=28</li>
 * </ul>
 *
 * @author
 *   Efe Utku Ural
 *
 * @version
 *   2.00 — 2025-11-24 — Updated scoring system (0–100 normalized)
 */
public class ParticipationCalculator {

    // ---------------------------------------------------------------------
    // NORMALIZATION CONSTANTS
    // ---------------------------------------------------------------------

    /** Max possible raw activity = (10 posts * 3) + (20 replies * 1) = 50 */
    private static final double MAX_ACTIVITY_RAW = 50.0;

    /** Max possible raw engagement = (10 threads * 2) + (200 avgReply/10) = 40 */
    private static final double MAX_ENGAGEMENT_RAW = 40.0;

    /** Max possible raw consistency = (14 days * 2) = 28 */
    private static final double MAX_CONSISTENCY_RAW = 28.0;

    /**
     * Compute full participation metrics and save to DB.
     */
    public static ParticipationStatistics computeAndSave(User student, Database database) {

        ParticipationStatistics stats = new ParticipationStatistics();
        stats.setStudentId(student.getUserName());
        String username = student.getUserName();

        // ---------------------------------------------------------
        // RAW METRICS
        // ---------------------------------------------------------
        int totalPosts =
                PostStore.getInstance().countPostsByAuthor(username);

        int totalReplies =
                ReplyStore.getInstance().countRepliesByAuthor(username);

        int threadsParticipated =
                PostStore.getInstance().countThreadsParticipated(username);

        int activePostDays =
                PostStore.getInstance().countActiveDays(username);

        int activeReplyDays =
                ReplyStore.getInstance().countReplyActiveDays(username);

        int activeDays = Math.max(activePostDays, activeReplyDays);

        double avgReplyLength =
                ReplyStore.getInstance().computeAverageReplyLength(username);

        // Save raw values
        stats.setTotalPosts(totalPosts);
        stats.setTotalReplies(totalReplies);
        stats.setThreadsParticipated(threadsParticipated);
        stats.setActiveDays(activeDays);
        stats.setAverageReplyLength(avgReplyLength);

        // ---------------------------------------------------------
        // RAW SCORES (BEFORE NORMALIZATION)
        // ---------------------------------------------------------
        double rawActivity =
                (totalPosts * 3) + (totalReplies * 1);

        double rawEngagement =
                (threadsParticipated * 2) + (avgReplyLength / 10.0);

        double rawConsistency =
                activeDays * 2;

        // ---------------------------------------------------------
        // NORMALIZED (0–100) SCORES
        // ---------------------------------------------------------
        double activityScore =
                Math.min(100.0, (rawActivity / MAX_ACTIVITY_RAW) * 100.0);

        double engagementScore =
                Math.min(100.0, (rawEngagement / MAX_ENGAGEMENT_RAW) * 100.0);

        double consistencyScore =
                Math.min(100.0, (rawConsistency / MAX_CONSISTENCY_RAW) * 100.0);

        stats.setActivityScore(activityScore);
        stats.setEngagementScore(engagementScore);
        stats.setConsistencyScore(consistencyScore);

        // ---------------------------------------------------------
        // FINAL SCORE (WEIGHTED 0–100)
        // ---------------------------------------------------------
        double finalScore =
                (activityScore * 0.50) +
                (engagementScore * 0.30) +
                (consistencyScore * 0.20);

        stats.setFinalScore(finalScore);

        // ---------------------------------------------------------
        // SAVE TO DB
        // ---------------------------------------------------------
        try {
            database.saveParticipationStats(stats);
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save participation statistics: " + e.getMessage(), e
            );
        }

        return stats;
    }

    private ParticipationCalculator() {}
}
