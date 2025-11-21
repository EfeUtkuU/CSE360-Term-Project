package guiStudentActivity; // replace with actual package name

import java.util.ArrayList;
import java.util.List;

import entityClasses.Post;
import entityClasses.PostStore;
import entityClasses.Reply;
import entityClasses.ReplyStore;

public class ModelStudentActivity {
    private PostStore postStore;
    private ReplyStore replyStore;

    public ModelStudentActivity(PostStore postStore, ReplyStore replyStore) {
        this.postStore = postStore;
        this.replyStore = replyStore;
    }

    /**
     * Retrieves all posts made by a given student username.
     */
    public List<Post> getPostsByStudent(String username) {
        List<Post> allPosts = postStore.getAllPosts();
        List<Post> userPosts = new ArrayList<>();
        for (Post p : allPosts) {
            if (p.getAuthor().equalsIgnoreCase(username)) {
                userPosts.add(p);
            }
        }
        return userPosts;
    }

    /**
     * Retrieves all replies made by a given student username.
     */
    public List<Reply> getRepliesByStudent(String username) {
        List<Reply> allReplies = replyStore.getAllReplies();
        List<Reply> userReplies = new ArrayList<>();
        for (Reply r : allReplies) {
            if (r.getAuthor().equalsIgnoreCase(username)) {
                userReplies.add(r);
            }
        }
        return userReplies;
    }
}
