package guiStudentActivity; // replace with actual package name

import javafx.stage.Stage;
import java.util.List;

import entityClasses.Post;
import entityClasses.PostStore;
import entityClasses.Reply;
import entityClasses.ReplyStore;

public class ControllerStudentActivity {
    private ModelStudentActivity model;
    private ViewStudentActivity view;
    private Stage stage;

    public ControllerStudentActivity(Stage stage, PostStore postStore, ReplyStore replyStore) {
        this.stage = stage;
        this.model = new ModelStudentActivity(postStore, replyStore);
        this.view = new ViewStudentActivity(stage);
    }

    /**
     * Loads the data for the given username and displays it.
     */
    public void showStudentActivity(String username, Runnable onBack) {
        List<Post> posts = model.getPostsByStudent(username);
        List<Reply> replies = model.getRepliesByStudent(username);
        view.show(username, posts, replies, onBack);
    }
}
