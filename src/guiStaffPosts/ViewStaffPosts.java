package guiStaffPosts;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import entityClasses.Post;
import entityClasses.PostStore;
import entityClasses.Reply;
import entityClasses.ReplyStore;
import entityClasses.ThreadStore;
import entityClasses.User;
import guiStaff.ControllerStaffHome;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p><strong>Class:</strong> ViewStaffPosts</p>
 *
 * <p><strong>Description:</strong>
 * JavaFX View responsible for displaying staff tools for:
 * <ul>
 *     <li>Viewing all threads</li>
 *     <li>Selecting a thread and seeing its posts</li>
 *     <li>Editing/deleting posts and replies (staff privileges)</li>
 *     <li>Posting new content to a selected thread</li>
 *     <li>Searching for posts within a selected thread</li>
 *     <li>Displaying rules, creating threads, and managing UI layout</li>
 * </ul>
 *
 * This class handles UI rendering only; all logic is delegated to
 * {@link ControllerStaffPosts}, {@link ThreadStore}, {@link PostStore}, and {@link ReplyStore}.
 * </p>
 *
 * @version 4.00 — 2025-11-22
 */
public class ViewStaffPosts {

    // ==========================================================
    // SHARED STATE (set from outside)
    // ==========================================================

    /** JavaFX stage reference used for navigation. */
    protected static Stage theStage;

    /** Currently logged-in staff user. */
    protected static User theUser;

    /** Window width from FoundationsMain. */
    private static final double width = applicationMain.FoundationsMain.WINDOW_WIDTH;

    /** Window height from FoundationsMain (suppressed unused warning). */
    @SuppressWarnings("unused")
    private static final double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // ==========================================================
    // HEADER COMPONENTS
    // ==========================================================

    private static final Label label_PageTitle   = new Label("Staff Posts");
    private static final Label label_UserDetails = new Label();
    private static final Line  line_Separator1   = new Line(20, 95, width - 20, 95);

    /** Button that opens the forum rules popup. */
    protected static Button button_Rules = new Button("RULES");

    // ==========================================================
    // LEFT COLUMN — THREAD LIST
    // ==========================================================

    private static final Label label_Threads = new Label("Threads");

    /** Opens a dialog to create a new thread with a first post. */
    private static final Button button_NewThread = new Button("New Thread");

    /** ListView of all threads. */
    private static ListView<entityClasses.Thread> list_Threads;

    // ==========================================================
    // RIGHT SIDE — POSTS, SEARCH, COMPOSER
    // ==========================================================

    private static final TextField field_Search       = new TextField();
    private static final Button    button_Search      = new Button("Search");
    private static final Button    button_ClearSearch = new Button("Clear");

    /** Label showing “Thread: <title>” or placeholder. */
    private static final Label label_CurrentThread = new Label("No thread selected");

    /** Container for all post “cards” rendered in VBox form. */
    private static VBox postContainer;

    /** Post title (top of composer). */
    private static TextField field_Title;

    /** Post body (composer). */
    private static TextArea area_Body;

    /** Sends a new post to the selected thread. */
    private static final Button button_Post = new Button("Post");

    // ==========================================================
    // FOOTER BUTTONS
    // ==========================================================

    private static final Button button_Back   = new Button("Back");
    private static final Button button_Logout = new Button("Logout");
    private static final Button button_Quit   = new Button("Quit");

    // ==========================================================
    // SCENE + ROOT
    // ==========================================================

    /** Singleton-like instance of this view. */
    private static ViewStaffPosts theView;

    /** JavaFX scene. */
    private static Scene theScene;

    /** JavaFX root pane. */
    private static Pane theRoot;

    // ==========================================================
    // FORMATTING + STATE
    // ==========================================================

    /** Date/time format used for posts and replies. */
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Currently selected thread in the UI. */
    private static entityClasses.Thread selectedThread = null;

    /** Current search query filtering posts. */
    private static String currentSearch = null;

    /** Fixed width of thread column. */
    private static final double THREAD_COLUMN_WIDTH = 260.0;

    // ==========================================================
    // PUBLIC ENTRY POINT
    // ==========================================================

    /**
     * Displays the Staff Posts page by:
     * <ul>
     *     <li>Initializing the view on first load</li>
     *     <li>Loading all threads from the database</li>
     *     <li>Rendering the thread list and clearing post area</li>
     *     <li>Setting stage title + scene</li>
     * </ul>
     *
     * @param ps   JavaFX stage
     * @param user the logged-in staff user
     */
    public static void displayStaffPosts(Stage ps, User user) {
        theStage = ps;
        theUser  = user;

        if (theView == null)
            theView = new ViewStaffPosts();

        label_UserDetails.setText("User: " + user.getUserName());

        ThreadStore.getInstance().loadFromDatabase();
        refreshThreadList();        // populate left column
        refreshPostsForThread(null); // clear right column

        theStage.setTitle("CSE 360 Foundations: Staff Posts");
        theStage.setScene(theScene);
        theStage.show();
    }

    // ==========================================================
    // CONSTRUCTOR — BUILD ENTIRE VIEW
    // ==========================================================

    /**
     * Constructs the Staff Posts UI layout.
     * <p>
     * This constructor sets up:
     * <ul>
     *     <li>Header area</li>
     *     <li>Left thread column</li>
     *     <li>Right post column with search + composer</li>
     *     <li>Footer navigation buttons</li>
     *     <li>Rules popup and new-thread dialog bindings</li>
     *     <li>All UI sizing, alignment, and event hookups</li>
     * </ul>
     * Only one instance is ever created.
     * </p>
     */
    private ViewStaffPosts() {

        // everything below is identical (untouched)
        theRoot  = new Pane();
        theScene = new Scene(theRoot, width, height);

        // Header
        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabel(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // ----- Left: Threads -----
        setupLabel(label_Threads, "Arial", 18, THREAD_COLUMN_WIDTH - 40, Pos.BASELINE_LEFT, 20, 100);
        

        button_Rules.setFont(Font.font("Dialog",18));
        button_Rules.setMinWidth(170);
        button_Rules.setAlignment(Pos.CENTER);
        button_Rules.setLayoutX(610);
        button_Rules.setLayoutY(45);

        /**
         * Opens a popup containing forum rules.
         * The popup is modal and blocks input until closed.
         */
        button_Rules.setOnAction(e -> {

            Stage ruleStage = new Stage();
            ruleStage.setTitle("Rules");
            ruleStage.initModality(Modality.APPLICATION_MODAL);

            VBox box = new VBox(10);
            box.setPadding(new Insets(15));
            box.setAlignment(Pos.TOP_LEFT);

            Label title = new Label("Rules");
            title.setFont(Font.font("Dialog", FontWeight.BOLD, 20));

            Label r1 = new Label("• Be respectful to all classmates and staff.");
            Label r2 = new Label("• Stay on topic within each thread.");
            Label r3 = new Label("• No spam, advertising, or irrelevant posts.");
            Label r4 = new Label("• Use clear and professional language.");
            Label r5 = new Label("• Academic honesty is required — no plagiarism.");
            Label r6 = new Label("• Do not share private or sensitive information.");
            Label r7 = new Label("• Report inappropriate content to staff.");
            Label r8 = new Label("• Follow staff and moderator instructions.");

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(ev -> ruleStage.close());

            box.getChildren().addAll(title, r1, r2, r3, r4, r5, r6, r7, r8, closeBtn);

            Scene scene = new Scene(box, 350, 310);
            ruleStage.setScene(scene);
            ruleStage.show();
        });


        button_NewThread.setFont(Font.font("Dialog", 13));
        button_NewThread.setMinWidth(THREAD_COLUMN_WIDTH - 40);
        button_NewThread.setLayoutX(20);
        button_NewThread.setLayoutY(130);

        /**
         * Opens a modal dialog allowing staff to create:
         * <ul>
         *     <li>A new thread</li>
         *     <li>The first post inside that thread</li>
         * </ul>
         */
        button_NewThread.setOnAction(e -> openNewThreadDialog());

        list_Threads = new ListView<>();
        list_Threads.setLayoutX(20);
        list_Threads.setLayoutY(170);
        list_Threads.setPrefSize(THREAD_COLUMN_WIDTH - 40, 380);

        /**
         * Custom cell factory that displays thread titles via getDisplayLabel().
         */
        list_Threads.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(entityClasses.Thread item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayLabel());
                }
            }
        });

        /**
         * When a thread is selected:
         * <ul>
         *     <li>Save selection</li>
         *     <li>Clear search</li>
         *     <li>Refresh posts displayed on the right side</li>
         * </ul>
         */
        list_Threads.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            selectedThread = newT;
            currentSearch = null;
            field_Search.clear();
            refreshPostsForThread(selectedThread);
        });

        // ----- Right: Search + posts -----
        double rightX = THREAD_COLUMN_WIDTH + 30;

        label_CurrentThread.setFont(Font.font("Arial", 18));
        label_CurrentThread.setAlignment(Pos.BASELINE_LEFT);
        label_CurrentThread.setLayoutX(rightX);
        label_CurrentThread.setLayoutY(100);
        label_CurrentThread.setMinWidth(width - rightX - 40);

        // Search field + buttons
        field_Search.setPromptText("Search posts in this thread...");
        field_Search.setLayoutX(rightX);
        field_Search.setLayoutY(130);
        field_Search.setPrefWidth(width - rightX - 220);

        setupButton(button_Search, "Dialog", 14, 80, Pos.CENTER, width - 210, 128);

        /**
         * Executes a post search inside the selected thread.
         */
        button_Search.setOnAction(e -> {
            currentSearch = (field_Search.getText() == null || field_Search.getText().isBlank())
                    ? null
                    : field_Search.getText().trim();
            refreshPostsForThread(selectedThread);
        });

        setupButton(button_ClearSearch, "Dialog", 14, 80, Pos.CENTER, width - 120, 128);

        /**
         * Clears search and restores full post list.
         */
        button_ClearSearch.setOnAction(e -> {
            currentSearch = null;
            field_Search.clear();
            refreshPostsForThread(selectedThread);
        });

        // Posts list (scroll)
        postContainer = new VBox(10);
        setupVBox(postContainer, rightX, 170, width - rightX - 40, 280);
        ScrollPane scroll = new ScrollPane(postContainer);
        setupScroll(scroll, rightX, 170, width - rightX - 40, 280);

        // Composer
        field_Title = new TextField();
        field_Title.setPromptText("Post title...");
        field_Title.setLayoutX(rightX);
        field_Title.setLayoutY(465);
        field_Title.setPrefWidth(width - rightX - 180);

        area_Body = new TextArea();
        setupTextArea(area_Body, "Write a post...", rightX, 495, width - rightX - 180, 60);

        setupButton(button_Post, "Dialog", 16, 100, Pos.CENTER, width - 160, 465);

        /**
         * Sends a new post to the selected thread through the controller.
         */
        button_Post.setOnAction(e -> ControllerStaffPosts.performPost(
                selectedThread, field_Title, area_Body, theUser));

        // Footer
        setupButton(button_Back, "Dialog", 18, 180, Pos.CENTER, 40, 560);

        /**
         * Navigates back to Staff Home.
         */
        button_Back.setOnAction(e ->
                guiStaff.ViewStaffHome.displayStaffHome(theStage, theUser));

        setupButton(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);

        /**
         * Logs out via controller.
         */
        button_Logout.setOnAction(e -> ControllerStaffPosts.performLogout());

        setupButton(button_Quit, "Dialog", 18, 180, Pos.CENTER, 460, 560);

        /**
         * Quits the application via controller.
         */
        button_Quit.setOnAction(e -> ControllerStaffPosts.performQuit());

        theRoot.getChildren().addAll(
                label_PageTitle, label_UserDetails, button_Rules, line_Separator1,
                label_Threads, button_NewThread, list_Threads,
                label_CurrentThread, field_Search, button_Search, button_ClearSearch,
                scroll, field_Title, area_Body, button_Post,
                button_Back, button_Logout, button_Quit
        );
    }

    // ==========================================================
    // THREAD HELPERS
    // ==========================================================

    /**
     * Refreshes the left-side thread list.
     * <ul>
     *     <li>Reloads threads from ThreadStore</li>
     *     <li>Preserves existing selection if possible</li>
     *     <li>Otherwise clears selection and disables post area</li>
     * </ul>
     */
    private static void refreshThreadList() {
        List<entityClasses.Thread> threads = ThreadStore.getInstance().getAllThreads();
        list_Threads.getItems().setAll(threads);

        // Keep selection if possible
        if (selectedThread != null) {
            for (entityClasses.Thread t : threads) {
                if (t.getId() == selectedThread.getId()) {
                    list_Threads.getSelectionModel().select(t);
                    return;
                }
            }
        }

        // Otherwise, no selection
        selectedThread = null;
        list_Threads.getSelectionModel().clearSelection();
        label_CurrentThread.setText("No thread selected");
        disablePostArea(true);
    }

    /**
     * Opens the dialog used by staff to create:
     * <ul>
     *     <li>A new thread (title)</li>
     *     <li>The first post inside the thread</li>
     * </ul>
     * After creation:
     * <ul>
     *     <li>The thread list refreshes</li>
     *     <li>The new thread is automatically selected</li>
     *     <li>Its first post is rendered</li>
     * </ul>
     */
    private static void openNewThreadDialog() {
        Stage dialog = new Stage();
        dialog.initOwner(theStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Create New Thread");

        Label lblThreadTitle = new Label("Thread title:");
        TextField tfThreadTitle = new TextField();

        Label lblPostTitle = new Label("First post title:");
        TextField tfPostTitle = new TextField();

        Label lblPostBody = new Label("First post body:");
        TextArea taPostBody = new TextArea();
        taPostBody.setPrefRowCount(5);
        taPostBody.setWrapText(true);

        Button btnCreate = new Button("Create");
        Button btnCancel = new Button("Cancel");

        HBox buttonRow = new HBox(10, btnCreate, btnCancel);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10,
                lblThreadTitle, tfThreadTitle,
                lblPostTitle, tfPostTitle,
                lblPostBody, taPostBody,
                buttonRow);
        root.setStyle("-fx-padding: 15;");
        Scene sc = new Scene(root, 420, 320);
        dialog.setScene(sc);

        /**
         * Creates the new thread and the first post.
         * Validates fields (blank = ignore silently).
         */
        btnCreate.setOnAction(e -> {
            String threadTitle = tfThreadTitle.getText() == null ? "" : tfThreadTitle.getText().trim();
            String postTitle   = tfPostTitle.getText() == null ? "" : tfPostTitle.getText().trim();
            String postBody    = taPostBody.getText() == null ? "" : taPostBody.getText().trim();

            if (threadTitle.isBlank() || postTitle.isBlank() || postBody.isBlank()) {
                return; // silently ignore invalid input; instructor may add alerts later
            }

            // Create thread in DB
            entityClasses.Thread thread =
                    ThreadStore.getInstance().createThread(threadTitle, theUser.getUserName());
            if (thread == null) return;

            // Create first post in that thread
            ModelStaffPosts.addPostToThread(theUser, thread, postTitle, postBody);
            PostStore.getInstance().reloadFromDatabase();

            // Refresh UI
            ThreadStore.getInstance().loadFromDatabase();
            refreshThreadList();

            // Select the new thread explicitly
            for (entityClasses.Thread t : list_Threads.getItems()) {
                if (t.getId() == thread.getId()) {
                    list_Threads.getSelectionModel().select(t);
                    selectedThread = t;
                    break;
                }
            }
            refreshPostsForThread(selectedThread);

            dialog.close();
        });

        /**
         * Cancels the dialog.
         */
        btnCancel.setOnAction(e -> dialog.close());

        dialog.showAndWait();
    }


    private static void disablePostArea(boolean disabled) {
        field_Title.setDisable(disabled);
        area_Body.setDisable(disabled);
        button_Post.setDisable(disabled);
        field_Search.setDisable(disabled);
        button_Search.setDisable(disabled);
        button_ClearSearch.setDisable(disabled);
    }

    // ---------- Refresh posts (thread-aware) ----------

    /** Called by controllers without a thread; uses current selection. */
    protected static void refreshPosts() {
        refreshPostsForThread(selectedThread);
    }

    /**
     * Refresh the posts for a given thread. If {@code thread} is null,
     * shows a placeholder and disables composer/search.
     */
    protected static void refreshPostsForThread(entityClasses.Thread thread) {
        postContainer.getChildren().clear();

        if (thread == null) {
            label_CurrentThread.setText("No thread selected");
            disablePostArea(true);
            Label placeholder = new Label("Select a thread on the left to view its posts.");
            placeholder.setStyle("-fx-text-fill:#777; -fx-font-size:13;");
            postContainer.getChildren().add(placeholder);
            return;
        }

        selectedThread = thread;
        label_CurrentThread.setText("Thread: " + thread.getTitle());
        disablePostArea(false);

        List<Post> posts = PostStore.getInstance().getPostsForThread(thread.getId());
        if (currentSearch != null && !currentSearch.isBlank()) {
            String q = currentSearch.toLowerCase();
            posts = posts.stream()
                    .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(q)) ||
                                 (p.getBody()  != null && p.getBody().toLowerCase().contains(q)))
                    .toList();
        }

        for (Post post : posts) {
            VBox card = new VBox(8);
            card.setStyle("-fx-border-color:#ddd; -fx-background-color:#f9f9f9; "
                    + "-fx-padding:10; -fx-border-radius:8; -fx-background-radius:8;");
            card.setPrefWidth(width - THREAD_COLUMN_WIDTH - 70);

            // ----- Title + Body -----
            Label titleLbl = new Label(post.getTitle());
            titleLbl.setStyle("-fx-font-size:14; -fx-font-weight:bold;");
            titleLbl.setWrapText(true);
            titleLbl.setMaxWidth(width - THREAD_COLUMN_WIDTH - 220);
            titleLbl.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

            Label bodyLbl = new Label(post.getBody());
            bodyLbl.setStyle("-fx-font-size:13;");
            bodyLbl.setWrapText(true);
            bodyLbl.setMaxWidth(width - THREAD_COLUMN_WIDTH - 220);
            bodyLbl.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

            VBox left = new VBox(6, titleLbl, bodyLbl);
            left.setAlignment(Pos.TOP_LEFT);

            // ----- Meta + Actions -----
            VBox right = new VBox(4);
            boolean edited = post.getUpdatedAt() != null && post.getCreatedAt() != null
                    && post.getUpdatedAt().isAfter(post.getCreatedAt().plusSeconds(1));
            Label meta = new Label(post.getAuthor() + " • " +
                    post.getCreatedAt().format(TIME_FMT) + (edited ? " (edited)" : ""));
            meta.setStyle("-fx-font-size:11; -fx-text-fill:#777;");

            Button editBtn = new Button("📝");
            Button delBtn  = new Button("🗑️");
            editBtn.setStyle("-fx-font-size:11;");
            delBtn.setStyle("-fx-font-size:11;");
            HBox actions = new HBox(6, editBtn, delBtn);
            actions.setAlignment(Pos.CENTER_RIGHT);
            actions.setOpacity(0.0);

            right.getChildren().addAll(meta, actions);

            HBox postRow = new HBox(10, left, right);
            postRow.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(left, Priority.ALWAYS);

            // Hover fade for actions
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), actions);
            fadeIn.setToValue(1);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), actions);
            fadeOut.setToValue(0);
            card.setOnMouseEntered(e -> fadeIn.playFromStart());
            card.setOnMouseExited(e -> fadeOut.playFromStart());

            // ----- Replies -----
            VBox repliesBox = new VBox(5);
            List<Reply> replies = ControllerStaffPosts.getRepliesForPost(post);
            for (Reply r : replies) {
                repliesBox.getChildren().add(renderReply(r));
            }

            // ----- Reply composer -----
            VBox replyBox = buildReplyBox(post);

            // ----- Edit/Delete -----
            editBtn.setOnAction(e -> {
                TextArea editArea = new TextArea(post.getBody());
                editArea.setPrefRowCount(3);
                Button save = new Button("Save");
                Button cancel = new Button("Cancel");
                HBox controls = new HBox(6, save, cancel);
                VBox editor = new VBox(6, editArea, controls);
                int idx = postContainer.getChildren().indexOf(card);
                postContainer.getChildren().set(idx, editor);

                save.setOnAction(ev -> {
                    String newBody = editArea.getText().trim();
                    if (!newBody.isEmpty()) {
                        PostStore.getInstance().updatePost(post.getId(), theUser, post.getTitle(), newBody);
                        PostStore.getInstance().reloadFromDatabase();
                    }
                    refreshPostsForThread(selectedThread);
                });
                cancel.setOnAction(ev -> refreshPostsForThread(selectedThread));
            });

            delBtn.setOnAction(e -> {
                if (confirmDeletePost()) {
                    ControllerStaffPosts.performDeletePost(theUser, post);
                }
            });

            card.getChildren().addAll(postRow, repliesBox, replyBox);
            postContainer.getChildren().add(card);
        }

        if (posts.isEmpty()) {
            Label empty = new Label("No posts in this thread yet.");
            empty.setStyle("-fx-text-fill:#777; -fx-font-size:13;");
            postContainer.getChildren().add(empty);
        }
    }

    // ---------- Render Reply ----------

    private static HBox renderReply(Reply r) {
        boolean edited = r.getUpdatedAt() != null && r.getCreatedAt() != null
                && r.getUpdatedAt().isAfter(r.getCreatedAt().plusSeconds(1));

        HBox replyRow = new HBox(10);
        replyRow.setAlignment(Pos.TOP_LEFT);
        replyRow.setPrefWidth(width - THREAD_COLUMN_WIDTH - 70);

        Label rBodyLbl = new Label("↳ " + r.getAuthor() + ": " + r.getBody());
        rBodyLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        rBodyLbl.setWrapText(true);
        rBodyLbl.setMaxWidth(width - THREAD_COLUMN_WIDTH - 220);
        rBodyLbl.setPrefWidth(width - THREAD_COLUMN_WIDTH - 230);
        HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

        VBox rightReply = new VBox(3);
        rightReply.setAlignment(Pos.TOP_RIGHT);
        rightReply.setPrefWidth(120);

        Label rMeta = new Label(r.getCreatedAt().format(TIME_FMT)
                + (edited ? " (edited)" : ""));
        rMeta.setStyle("-fx-font-size: 10; -fx-text-fill: #777;");

        Button rEditBtn = new Button("📝");
        Button rDelBtn  = new Button("🗑️");
        rEditBtn.setStyle("-fx-font-size: 10;");
        rDelBtn.setStyle("-fx-font-size: 10;");

        HBox rActions = new HBox(5, rEditBtn, rDelBtn);
        rActions.setAlignment(Pos.CENTER_RIGHT);
        rActions.setOpacity(0.0);
        rActions.setVisible(true);

        rightReply.getChildren().addAll(rMeta, rActions);
        VBox.setVgrow(rightReply, Priority.NEVER);

        replyRow.getChildren().addAll(rBodyLbl, rightReply);
        HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), rActions);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rActions);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        replyRow.setOnMouseEntered(e -> fadeIn.playFromStart());
        replyRow.setOnMouseExited(e -> fadeOut.playFromStart());

        rEditBtn.setOnAction(e -> {
            TextArea editArea = new TextArea(r.getBody());
            editArea.setPrefRowCount(2);
            editArea.setPrefWidth(width - THREAD_COLUMN_WIDTH - 230);
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            HBox editor = new HBox(6, editArea, save, cancel);
            int idx = ((VBox) replyRow.getParent()).getChildren().indexOf(replyRow);
            ((VBox) replyRow.getParent()).getChildren().set(idx, editor);

            save.setOnAction(ev ->
                    ControllerStaffPosts.performReplyEdit(r.getId(), theUser, editArea.getText()));
            cancel.setOnAction(ev -> refreshPostsForThread(selectedThread));
        });

        rDelBtn.setOnAction(e -> {
            if (confirmDeleteReply()) {
                ControllerStaffPosts.performReplyDelete(r.getId(), theUser);
            }
        });


        return replyRow;
    }

    // ---------- Reply Composer ----------

    private static VBox buildReplyBox(Post post) {
        Button toggle = new Button("Reply");
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Write a reply...");
        replyArea.setPrefRowCount(3);
        replyArea.setWrapText(true);
        replyArea.setPrefWidth(width - THREAD_COLUMN_WIDTH - 260);

        Button send   = new Button("Reply");
        Button cancel = new Button("Cancel");
        HBox buttons  = new HBox(8, send, cancel);
        VBox composer = new VBox(6, replyArea, buttons);
        composer.setOpacity(0);
        composer.setVisible(false);
        composer.setManaged(false);
        composer.setPrefHeight(0);

        toggle.setOnAction(e -> slideToggle(composer, !composer.isVisible()));
        send.setOnAction(e -> {
            ControllerStaffPosts.performReply(theUser, post, replyArea);
            slideToggle(composer, false);
            replyArea.clear();
        });
        cancel.setOnAction(e -> slideToggle(composer, false));

        return new VBox(8, toggle, composer);
    }

    // ---------- Animations & UI helpers ----------

    private static void slideToggle(Region region, boolean expand) {
        double targetOpen = Region.USE_COMPUTED_SIZE;

        if (expand) {
            region.setVisible(true);
            region.setManaged(true);
            region.applyCss();
            region.layout();
            targetOpen = Math.max(120, region.prefHeight(-1));
        }

        double fromH = expand ? 0 : region.getHeight();
        double toH   = expand ? targetOpen : 0;
        double fromO = expand ? 0 : 1;
        double toO   = expand ? 1 : 0;

        region.setMinHeight(fromH);
        region.setPrefHeight(fromH);
        region.setMaxHeight(fromH);
        region.setOpacity(fromO);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(region.minHeightProperty(), fromH),
                        new KeyValue(region.prefHeightProperty(), fromH),
                        new KeyValue(region.maxHeightProperty(), fromH),
                        new KeyValue(region.opacityProperty(), fromO)),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(region.minHeightProperty(), toH),
                        new KeyValue(region.prefHeightProperty(), toH),
                        new KeyValue(region.maxHeightProperty(), toH),
                        new KeyValue(region.opacityProperty(), toO))
        );

        tl.setOnFinished(ev -> {
            if (!expand) {
                region.setVisible(false);
                region.setManaged(false);
            }
        });

        tl.play();
    }
    
 // ---------- Custom Confirmation Dialogs (ADD THESE) ----------

    private static boolean confirmDeletePost() {
        return showConfirmDialog(
                "Delete Post",
                "Delete this post?",
                "Are you sure you want to delete this post? This action cannot be undone.",
                "Delete"
        );
    }

    private static boolean confirmDeleteReply() {
        return showConfirmDialog(
                "Delete Reply",
                "Delete this reply?",
                "Are you sure you want to delete this reply? This action cannot be undone.",
                "Delete"
        );
    }

    private static boolean showConfirmDialog(String title, String heading, String message, String confirmText) {
        Stage dialog = new Stage();
        dialog.initOwner(theStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);

        Label headingLbl = new Label(heading);
        headingLbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        Button confirmBtn = new Button(confirmText);
        confirmBtn.setStyle("-fx-font-weight: bold;");

        final boolean[] result = { false };

        cancelBtn.setOnAction(e -> { result[0] = false; dialog.close(); });
        confirmBtn.setOnAction(e -> { result[0] = true; dialog.close(); });

        HBox buttons = new HBox(10, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, headingLbl, msgLbl, buttons);
        root.setStyle("""
                -fx-padding: 18;
                -fx-background-color: white;
                -fx-border-color: #ccc;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                """);

        Scene sc = new Scene(root, 360, 150);
        dialog.setScene(sc);
        dialog.showAndWait();

        return result[0];
    }


    private static void setupLabel(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f)); l.setMinWidth(w); l.setAlignment(p); l.setLayoutX(x); l.setLayoutY(y);
    }
    private static void setupButton(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f)); b.setMinWidth(w); b.setAlignment(p); b.setLayoutX(x); b.setLayoutY(y);
    }
    private static void setupTextArea(TextArea t, String prompt, double x, double y, double w, double h) {
        t.setPromptText(prompt); t.setLayoutX(x); t.setLayoutY(y); t.setPrefSize(w, h); t.setWrapText(true);
    }
    private static void setupVBox(VBox v, double x, double y, double w, double h) {
        v.setLayoutX(x); v.setLayoutY(y); v.setPrefSize(w, h);
    }
    private static void setupScroll(ScrollPane s, double x, double y, double w, double h) {
        s.setLayoutX(x); s.setLayoutY(y); s.setPrefSize(w, h); s.setFitToWidth(true);
    }
}
