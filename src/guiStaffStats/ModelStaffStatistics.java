package guiStaffStats;

import entityClasses.Post;
import entityClasses.PostStore;
import entityClasses.Reply;
import entityClasses.ReplyStore;
import entityClasses.ThreadStore;
import entityClasses.User;

import java.util.ArrayList;
import java.util.List;

/**
 * <p><strong>Class:</strong> ModelStaffStatistics</p>
 *
 * <p><strong>Description:</strong>
 * Pure logic/model class for computing per-student discussion activity.
 * It does NOT contain any JavaFX code.
 *
 * Computes:
 * <ul>
 *     <li>Posts by student (per thread)</li>
 *     <li>Replies by student (per thread)</li>
 *     <li>Total posts</li>
 *     <li>Total replies</li>
 * </ul>
 *
 * This class is completely read-only relative to the database —
 * it only queries PostStore / ReplyStore / ThreadStore.
 *
 * @version 1.00 — 2025-11-23 — Initial statistics model (read-only).
 */
public final class ModelStaffStatistics {

    private ModelStaffStatistics() {}

    /**
     * <p><strong>Class:</strong> ThreadActivity</p>
     *
     * <p><strong>Description:</strong>
     * A structured container describing all activity performed
     * by a single student inside a specific thread.
     * <br>
     * Contains:
     * <ul>
     *     <li>The thread itself</li>
     *     <li>A list of the student's posts in that thread</li>
     *     <li>A list of the student's replies in that thread</li>
     * </ul>
     * 
     */
    public static final class ThreadActivity {

        private final entityClasses.Thread thread;
        private final List<Post> postsByStudent;
        private final List<Reply> repliesByStudent;

        /**
         * Constructs a ThreadActivity object.
         *
         * @param thread the thread being analyzed
         * @param postsByStudent list of posts authored by the student in this thread
         * @param repliesByStudent list of replies authored by the student in this thread
         */
        public ThreadActivity(entityClasses.Thread thread,
                              List<Post> postsByStudent,
                              List<Reply> repliesByStudent) {
            this.thread = thread;
            this.postsByStudent = postsByStudent;
            this.repliesByStudent = repliesByStudent;
        }

        /** @return the thread this activity pertains to */
        public entityClasses.Thread getThread() { return thread; }

        /** @return all posts the student wrote inside this thread */
        public List<Post> getPostsByStudent() { return postsByStudent; }

        /** @return all replies the student wrote inside this thread */
        public List<Reply> getRepliesByStudent() { return repliesByStudent; }
    }

    // ----------------------------------------------------------------------
    // CORE API
    // ----------------------------------------------------------------------

    /**
     * Computes the complete activity breakdown for a specific student.
     * <p>
     * For every thread:
     * <ul>
     *     <li>Collect all posts authored by that student</li>
     *     <li>Collect all replies authored by that student</li>
     * </ul>
     * If a thread contains no activity by that student, it is skipped.
     *
     * @param student the student whose activity is being analyzed
     * @return list of ThreadActivity objects (one per thread with student activity)
     */
    public static List<ThreadActivity> loadActivityForStudent(User student) {

        String username = student.getUserName();

        List<ThreadActivity> activities = new ArrayList<>();

        PostStore postStore = PostStore.getInstance();
        ReplyStore replyStore = ReplyStore.getInstance();
        List<entityClasses.Thread> threads = ThreadStore.getInstance().getAllThreads();

        for (entityClasses.Thread t : threads) {

            List<Post> postsInThread = postStore.getPostsForThread(t.getId());

            List<Post> postsByStudent = new ArrayList<>();
            List<Reply> repliesByStudent = new ArrayList<>();

            for (Post p : postsInThread) {

                if (username.equals(p.getAuthor())) {
                    postsByStudent.add(p);
                }

                List<Reply> repliesForPost =
                        replyStore.getRepliesForPost(p.getDatabaseId());

                for (Reply r : repliesForPost) {
                    if (username.equals(r.getAuthor())) {
                        repliesByStudent.add(r);
                    }
                }
            }

            if (!postsByStudent.isEmpty() || !repliesByStudent.isEmpty()) {
                activities.add(new ThreadActivity(t, postsByStudent, repliesByStudent));
            }
        }

        return activities;
    }

    /**
     * Counts the total number of posts across all ThreadActivity entries.
     *
     * @param activities list of activity objects (usually from loadActivityForStudent)
     * @return the total count of posts authored by the student
     */
    public static int countTotalPosts(List<ThreadActivity> activities) {
        int total = 0;
        for (ThreadActivity ta : activities) {
            total += ta.getPostsByStudent().size();
        }
        return total;
    }

    /**
     * Counts the total number of replies across all ThreadActivity entries.
     *
     * @param activities list of activity objects (usually from loadActivityForStudent)
     * @return the total count of replies authored by the student
     */
    public static int countTotalReplies(List<ThreadActivity> activities) {
        int total = 0;
        for (ThreadActivity ta : activities) {
            total += ta.getRepliesByStudent().size();
        }
        return total;
    }
}
