package guiStudentActivity; // replace with actual package name

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

import entityClasses.Post;
import entityClasses.Reply;

public class ViewStudentActivity {
    private Stage stage;
    private VBox layout;
    private Button backButton;

    public ViewStudentActivity(Stage stage) {
        this.stage = stage;
        this.layout = new VBox(15);
        this.layout.setPadding(new Insets(20));
        this.backButton = new Button("Back");
    }

    /**
     * Displays the student activity page for the given username.
     */
    public void show(String username, List<Post> posts, List<Reply> replies, Runnable onBack) {
        layout.getChildren().clear();

        Label header = new Label("Activity for " + username);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox postsBox = new VBox(10);
        postsBox.setPadding(new Insets(10));

        // Add posts
        if (posts.isEmpty() && replies.isEmpty()) {
            postsBox.getChildren().add(new Label("No posts or replies found for this user."));
        } else {
            for (Post p : posts) {
                VBox postBox = new VBox(5);
                postBox.setStyle("-fx-border-color: gray; -fx-padding: 8;");
                Label postTitle = new Label("Post: " + p.getTitle());
                Label postContent = new Label(p.getContent());
                postBox.getChildren().addAll(postTitle, postContent);
                postsBox.getChildren().add(postBox);
            }

            for (Reply r : replies) {
                VBox replyBox = new VBox(5);
                replyBox.setStyle("-fx-border-color: lightgray; -fx-padding: 8; -fx-background-color: #f9f9f9;");
                Label replyLabel = new Label("Reply to Post #" + r.getId() + ":");
                Label replyContent = new Label(r.getContent());
                replyBox.getChildren().addAll(replyLabel, replyContent);
                postsBox.getChildren().add(replyBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(postsBox);
        scrollPane.setFitToWidth(true);

        backButton.setOnAction(e -> onBack.run());

        layout.getChildren().addAll(header, scrollPane, backButton);

        Scene scene = new Scene(layout, 600, 600);
        stage.setScene(scene);
        stage.setTitle("Student Activity");
        stage.show();
    }
}
