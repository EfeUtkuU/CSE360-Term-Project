package guiStudentPosts;

import entityClasses.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * The ViewPosts class defines the graphical user interface (GUI) for the discussion thread system.
 * It serves as the “View” component in the MVC architecture, displaying posts and their replies,
 * and allowing users to interact with the system through actions like adding, editing, deleting,
 * and replying to posts.
 *
 * This class dynamically updates the display when posts or replies are created, modified, or removed,
 * and supports searching and filtering (e.g., showing only the current user’s posts).
 */
public class ViewPosts {

    // ---------------------- Fields ----------------------

    private Stage stage;                 // The main window displaying the discussion thread
    private User currentUser;            // The currently logged-in user
    private ModelPosts model;            // The data model handling post/reply operations

    // UI components for layout and interaction
    private VBox postsContainer;         // Container that holds all post boxes
    private ScrollPane scrollPane;       // Enables scrolling through the post list
    private TextField searchField;       // Text input for keyword-based searching
    private Button searchButton;         // Executes the search operation
    private Button showMyPostsButton;    // Toggles between showing all posts or only user’s posts
    private boolean showOnlyMyPosts = false; // State flag for filtering posts by author

    // ---------------------- Constructor ----------------------

    /**
     * Constructs a new ViewPosts window for the given user and model.
     * Initializes the UI and displays the discussion thread in a new stage.
     *
     * @param user  the currently logged-in user
     * @param model the data model used to manage posts and replies
     */
    public ViewPosts(User user, ModelPosts model) {
        this.currentUser = user;
        this.model = model;
        this.stage = new Stage();
        setupUI();
    }

    // ---------------------- UI Setup ----------------------

    /**
     * Builds and initializes all UI components, including buttons, search fields,
     * post listings, and layout containers. Also sets up event listeners for user interactions.
     */
    private void setupUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Discussion Thread");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Main top buttons
        Button addPostButton = new Button("Add New Post");
        addPostButton.setOnAction(e -> showAddPostDialog());

        showMyPostsButton = new Button("Show My Posts");
        showMyPostsButton.setOnAction(e -> toggleShowMyPosts());

        HBox topButtons = new HBox(10, addPostButton, showMyPostsButton);
        topButtons.setAlignment(Pos.CENTER);

        // Search bar
        HBox searchBox = new HBox(5);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Search by keyword in title...");
        searchButton = new Button("Search");
        searchButton.setOnAction(e -> refreshPostsWithSearch(searchField.getText()));
        searchBox.getChildren().addAll(searchField, searchButton);

        // Scrollable post area
        postsContainer = new VBox(10);
        scrollPane = new ScrollPane(postsContainer);
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(title, topButtons, searchBox, scrollPane);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Discussion Thread");
        stage.show();

        refreshPosts();
    }

    // ---------------------- Post Filtering ----------------------

    /**
     * Toggles between showing only the current user’s posts and showing all posts.
     * Updates the button label accordingly and refreshes the post view.
     */
    private void toggleShowMyPosts() {
        showOnlyMyPosts = !showOnlyMyPosts;
        showMyPostsButton.setText(showOnlyMyPosts ? "Show All Posts" : "Show My Posts");
        refreshPostsWithSearch(searchField.getText());
    }

    // ---------------------- Post Refresh Methods ----------------------

    /**
     * Refreshes and reloads the entire post list without any search filter.
     */
    private void refreshPosts() {
        refreshPostsWithSearch("");
    }

    /**
     * Refreshes the post list based on a search keyword and/or author filter.
     * @param keyword the search keyword to filter posts by title
     */
    private void refreshPostsWithSearch(String keyword) {
        postsContainer.getChildren().clear();

        List<Post> allPosts = new ArrayList<>(model.getAllPosts());
        List<Post> matchingPosts = new ArrayList<>();
        List<Post> otherPosts = new ArrayList<>();
        List<Post> sortedPosts = new ArrayList<>();

        // Separate posts by keyword match or author
        for (Post post : allPosts) {
            boolean matchesKeyword = keyword != null && !keyword.isBlank()
                    && post.getTitle().toLowerCase().contains(keyword.toLowerCase());

            if (showOnlyMyPosts) {
                if (post.getAuthor().equals(currentUser.getUserName())) {
                    sortedPosts.add(post);
                }
            } else {
                if (matchesKeyword) matchingPosts.add(post);
                else otherPosts.add(post);
            }
        }

        if (!showOnlyMyPosts) {
            sortedPosts.addAll(matchingPosts);
            sortedPosts.addAll(otherPosts);
        }

        // Render each post into a displayable UI component
        for (Post post : sortedPosts) {
            VBox postBox = createPostBox(post);
            postsContainer.getChildren().add(postBox);
        }
    }

    // ---------------------- Post Box Creation ----------------------

    /**
     * Creates a styled, interactive VBox element representing a single post.
     * Includes buttons for editing, deleting, viewing replies, and adding new replies.
     *
     * @param post the post to display
     * @return a VBox containing the rendered post
     */
    private VBox createPostBox(Post post) {
        VBox postBox = new VBox(5);
        postBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: #f0f0f0;");
        postBox.setPrefWidth(750);

        Label postTitle = new Label(post.getTitle() + " (by " + post.getAuthor() + ")");
        postTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Label postBody = new Label(post.getBody());
        postBody.setWrapText(true);

        // Edit/Delete controls (permission-based)
        HBox postButtons = new HBox(10);
        postButtons.setAlignment(Pos.CENTER_LEFT);
        Button editPostBtn = new Button("Edit Post");
        Button deletePostBtn = new Button("Delete Post");

        if (!post.getAuthor().equals(currentUser.getUserName()) && !currentUser.getNewStaff()) {
            editPostBtn.setDisable(true);
            deletePostBtn.setDisable(true);
        }

        editPostBtn.setOnAction(e -> showEditPostDialog(post));
        deletePostBtn.setOnAction(e -> {
            model.deletePost(post.getId(), currentUser);
            refreshPostsWithSearch(searchField.getText());
        });
        postButtons.getChildren().addAll(editPostBtn, deletePostBtn);

        // Replies section
        VBox repliesBox = new VBox(5);
        repliesBox.setPadding(new Insets(5, 10, 5, 20));
        repliesBox.setVisible(false);

        Button showRepliesBtn = new Button("Show Replies");
        showRepliesBtn.setOnAction(e -> {
            if (!repliesBox.isVisible()) {
                repliesBox.getChildren().clear();
                List<Reply> replies = model.getAllRepliesForPost(post.getId());
                for (Reply reply : replies) {
                    VBox replyBox = new VBox(2);
                    replyBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5;");
                    Label replyLabel = new Label(reply.getAuthor() + ": " + reply.getBody());
                    replyLabel.setWrapText(true);
                    replyBox.getChildren().add(replyLabel);
                    repliesBox.getChildren().add(replyBox);
                }
                repliesBox.setVisible(true);
                showRepliesBtn.setText("Hide Replies");
            } else {
                repliesBox.setVisible(false);
                showRepliesBtn.setText("Show Replies");
            }
        });

        // Add reply section
        HBox addReplyBox = new HBox(5);
        addReplyBox.setAlignment(Pos.CENTER_LEFT);
        TextField replyField = new TextField();
        replyField.setPromptText("Write a reply...");
        Button addReplyBtn = new Button("Reply");

        addReplyBtn.setOnAction(e -> {
            if (!replyField.getText().isBlank()) {
                Reply reply = new Reply(post, currentUser, replyField.getText(), false);
                model.addReply(reply);
                refreshPostsWithSearch(searchField.getText());
            }
        });

        addReplyBox.getChildren().addAll(replyField, addReplyBtn);

        postBox.getChildren().addAll(postTitle, postBody, postButtons, showRepliesBtn, repliesBox, addReplyBox);
        return postBox;
    }

    // ---------------------- Add/Edit Post Dialogs ----------------------

    /**
     * Opens a dialog allowing the user to create a new post by entering a title and body.
     * On confirmation, the new post is added to the model and the UI is refreshed.
     */
    private void showAddPostDialog() {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Add New Post");

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();
        Label bodyLabel = new Label("Body:");
        TextArea bodyArea = new TextArea();

        VBox vbox = new VBox(10, titleLabel, titleField, bodyLabel, bodyArea);
        dialog.getDialogPane().setContent(vbox);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Post(currentUser, titleField.getText(), bodyArea.getText(), "", false, false);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(post -> {
            model.addPost(post);
            refreshPostsWithSearch(searchField.getText());
        });
    }

    /**
     * Opens a dialog allowing the user to edit an existing post.
     * The user can modify the title or body, and the changes are saved to the model.
     *
     * @param post the post object to edit
     */
    private void showEditPostDialog(Post post) {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Edit Post");

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(post.getTitle());
        Label bodyLabel = new Label("Body:");
        TextArea bodyArea = new TextArea(post.getBody());

        VBox vbox = new VBox(10, titleLabel, titleField, bodyLabel, bodyArea);
        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                post.setTitle(titleField.getText());
                post.setBody(bodyArea.getText());
                return post;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> refreshPostsWithSearch(searchField.getText()));
    }
}
