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
 * - Posts by student (per thread)
 * - Replies by student (per thread)
 * - Total posts + total replies
 *
 * @version 1.00 — 2025-11-23 — Initial statistics model (read-only).
 */
public final class ModelStaffStatistics {

    private ModelStaffStatistics() {}

    /** Container for all activity by a student inside a thread */
    public static final class ThreadActivity {

        private final entityClasses.Thread thread;
        private final List<Post> postsByStudent;
        private final List<Reply> repliesByStudent;

        public ThreadActivity(entityClasses.Thread thread,
                              List<Post> postsByStudent,
                              List<Reply> repliesByStudent) {
            this.thread = thread;
            this.postsByStudent = postsByStudent;
            this.repliesByStudent = repliesByStudent;
        }

        public entityClasses.Thread getThread() { return thread; }
        public List<Post> getPostsByStudent() { return postsByStudent; }
        public List<Reply> getRepliesByStudent() { return repliesByStudent; }
    }

    // ----------------------------------------------------------------------
    // CORE API
    // ----------------------------------------------------------------------

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

    public static int countTotalPosts(List<ThreadActivity> activities) {
        int total = 0;
        for (ThreadActivity ta : activities) {
            total += ta.getPostsByStudent().size();
        }
        return total;
    }

    public static int countTotalReplies(List<ThreadActivity> activities) {
        int total = 0;
        for (ThreadActivity ta : activities) {
            total += ta.getRepliesByStudent().size();
        }
        return total;
    }
}
