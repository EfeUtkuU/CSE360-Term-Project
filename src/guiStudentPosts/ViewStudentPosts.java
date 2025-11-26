package guiStudentPosts;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import database.Database;
import entityClasses.Post;
import entityClasses.PostStore;
import entityClasses.Reply;
import entityClasses.ReplyStore;
import entityClasses.ThreadStore;
import entityClasses.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p><b>Class:</b> ViewStudentPosts</p>
 *
 * <p><b>Description:</b>
 * JavaFX GUI for Student discussion posts with thread support.
 * Left side shows a list of threads; right side shows posts and replies
 * for the selected thread. Students can create/edit/delete their own
 * posts and replies (staff users still have broader permissions).
 *
 * New posts are only allowed within a selected thread. To create a thread,
 * a popup asks for: thread title, first post title, and first post body.
 * </p>
 *
 * @version 4.10 — 2025-11-23 — Added custom-confirmation dialogs for post/reply delete
 */
public class ViewStudentPosts {

    // ---------- Stage / user ----------

    protected static Stage theStage;
    protected static User  theUser;
    @SuppressWarnings("unused")
    private static final Database theDatabase = applicationMain.FoundationsMain.database;

    // ---------- Layout constants ----------

    private static final double width  = applicationMain.FoundationsMain.WINDOW_WIDTH;
    @SuppressWarnings("unused")
    private static final double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    private static final double THREAD_COLUMN_WIDTH = 260.0;

    // ---------- Header ----------

    protected static final Label label_PageTitle   = new Label("Posts");
    protected static final Label label_UserDetails = new Label();
    protected static final Line  line_Separator1   = new Line(20, 95, width - 20, 95);
    protected static final Button button_Rules = new Button("RULES");

    // ---------- Threads (left) ----------

    private static final Label  label_Threads    = new Label("Threads");
    private static final Button button_NewThread = new Button("New Thread");
    private static ListView<entityClasses.Thread> list_Threads;

    // ---------- Posts + Search (right) ----------

    protected static final TextField field_Search       = new TextField();
    protected static final Button    button_Search      = new Button("Search");
    protected static final Button    button_ClearSearch = new Button("Clear");
    private static final Label       label_CurrentThread = new Label("No thread selected");

    protected static VBox      postContainer;
    protected static TextField field_Title;
    protected static TextArea  area_Body;
    protected static final Button button_Post = new Button("Post");

    // ---------- Footer ----------

    protected static final Button button_Back   = new Button("Back");
    protected static final Button button_Logout = new Button("Logout");
    protected static final Button button_Quit   = new Button("Quit");

    // ---------- Scene / root ----------

    private static ViewStudentPosts theView;
    private static Scene            theScene;
    private static Pane             theRoot;

    // ---------- Meta ----------

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Currently selected thread, or null if none. */
    private static entityClasses.Thread selectedThread = null;

    /** Current search string within the selected thread. */
    private static String currentSearch = null;

    // ---------- Display ----------

    /**
     * Display the Student Posts page.
     *
     * @param ps   the primary stage
     * @param user the logged-in user
     */
    public static void displayStudentPosts(Stage ps, User user) {
        theStage = ps;
        theUser  = user;

        if (theView == null) theView = new ViewStudentPosts();

        label_UserDetails.setText("User: " + user.getUserName());

        // Load threads and show
        ThreadStore.getInstance().loadFromDatabase();
        refreshThreadList();
        refreshPostsForThread(null); // no thread selected initially

        theStage.setTitle("CSE 360 Foundations: Student Posts");
        theStage.setScene(theScene);
        theStage.show();
    }

    // ---------- Constructor: initialize static GUI ----------

    private ViewStudentPosts() {
        theRoot  = new Pane();
        theScene = new Scene(theRoot, width, height);

        // Header
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // ----- Left column: Threads -----
        setupLabelUI(label_Threads, "Arial", 18, THREAD_COLUMN_WIDTH - 40,
                Pos.BASELINE_LEFT, 20, 100);
        
        button_Rules.setFont(Font.font("Dialog",18));
        button_Rules.setMinWidth(170);
        button_Rules.setAlignment(Pos.CENTER);
        button_Rules.setLayoutX(610);
        button_Rules.setLayoutY(45);
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

            Scene scene = new Scene(box, 350, 250);
            ruleStage.setScene(scene);
            ruleStage.show();
        });

        button_NewThread.setFont(Font.font("Dialog", 13));
        button_NewThread.setMinWidth(THREAD_COLUMN_WIDTH - 40);
        button_NewThread.setLayoutX(20);
        button_NewThread.setLayoutY(130);
        button_NewThread.setOnAction(e -> openNewThreadDialog());

        list_Threads = new ListView<>();
        list_Threads.setLayoutX(20);
        list_Threads.setLayoutY(170);
        list_Threads.setPrefSize(THREAD_COLUMN_WIDTH - 40, 380);

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

        list_Threads.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            selectedThread = newT;
            currentSearch = null;
            field_Search.clear();
            refreshPostsForThread(selectedThread);
        });

        // ----- Right column: search + posts -----
        double rightX = THREAD_COLUMN_WIDTH + 30;

        label_CurrentThread.setFont(Font.font("Arial", 18));
        label_CurrentThread.setAlignment(Pos.BASELINE_LEFT);
        label_CurrentThread.setLayoutX(rightX);
        label_CurrentThread.setLayoutY(100);
        label_CurrentThread.setMinWidth(width - rightX - 40);

        field_Search.setPromptText("Search posts in this thread...");
        field_Search.setLayoutX(rightX);
        field_Search.setLayoutY(130);
        field_Search.setPrefWidth(width - rightX - 220);

        setupButtonUI(button_Search, "Dialog", 14, 80, Pos.CENTER, width - 210, 128);
        button_Search.setOnAction(e -> {
            currentSearch = (field_Search.getText() == null || field_Search.getText().isBlank())
                    ? null
                    : field_Search.getText().trim();
            refreshPostsForThread(selectedThread);
        });

        setupButtonUI(button_ClearSearch, "Dialog", 14, 80, Pos.CENTER, width - 120, 128);
        button_ClearSearch.setOnAction(e -> {
            currentSearch = null;
            field_Search.clear();
            refreshPostsForThread(selectedThread);
        });

        // Posts scroll area
        postContainer = new VBox(10);
        setupVBoxUI(postContainer, rightX, 170, width - rightX - 40, 280);
        ScrollPane scroll = new ScrollPane(postContainer);
        setupScrollPaneUI(scroll, rightX, 170, width - rightX - 40, 280);

        // Composer
        field_Title = new TextField();
        field_Title.setPromptText("Post title...");
        field_Title.setLayoutX(rightX);
        field_Title.setLayoutY(465);
        field_Title.setPrefWidth(width - rightX - 180);

        area_Body = new TextArea();
        setupTextAreaUI(area_Body, "Write a post...", rightX, 495, width - rightX - 180, 60);

        setupButtonUI(button_Post, "Dialog", 16, 100, Pos.CENTER, width - 160, 465);
        button_Post.setOnAction(e ->
                ControllerStudentPosts.performPost(selectedThread, field_Title, area_Body, theUser));

        // Footer: Back / Logout / Quit
        setupButtonUI(button_Back, "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStudent.ViewStudentHome.displayStudentHome(theStage, theUser));

        setupButtonUI(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStudentPosts.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStudentPosts.performQuit());

        theRoot.getChildren().addAll(
                label_PageTitle, label_UserDetails, button_Rules, line_Separator1,
                label_Threads, button_NewThread, list_Threads,
                label_CurrentThread, field_Search, button_Search, button_ClearSearch,
                scroll, field_Title, area_Body, button_Post,
                button_Back, button_Logout, button_Quit
        );
    }

    // ---------- Threads helpers ----------

    private static void refreshThreadList() {
        List<entityClasses.Thread> threads = ThreadStore.getInstance().getAllThreads();
        list_Threads.getItems().setAll(threads);

        if (selectedThread != null) {
            for (entityClasses.Thread t : threads) {
                if (t.getId() == selectedThread.getId()) {
                    list_Threads.getSelectionModel().select(t);
                    return;
                }
            }
        }
        selectedThread = null;
        list_Threads.getSelectionModel().clearSelection();
        label_CurrentThread.setText("No thread selected");
        disablePostArea(true);
    }

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
        HBox buttonRow   = new HBox(10, btnCreate, btnCancel);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10,
                lblThreadTitle, tfThreadTitle,
                lblPostTitle, tfPostTitle,
                lblPostBody, taPostBody,
                buttonRow);
        root.setStyle("-fx-padding: 15;");
        Scene sc = new Scene(root, 420, 320);
        dialog.setScene(sc);

        btnCreate.setOnAction(e -> {
            String threadTitle = tfThreadTitle.getText() == null ? "" : tfThreadTitle.getText().trim();
            String postTitle   = tfPostTitle.getText() == null ? "" : tfPostTitle.getText().trim();
            String postBody    = taPostBody.getText() == null ? "" : taPostBody.getText().trim();

            if (threadTitle.isBlank() || postTitle.isBlank() || postBody.isBlank()) {
                return;
            }

            // Create thread
            entityClasses.Thread thread =
                    ThreadStore.getInstance().createThread(threadTitle, theUser.getUserName());
            if (thread == null) return;

            // Create initial post in that thread
            ModelStudentPosts.addPostToThread(theUser, thread, postTitle, postBody);
            PostStore.getInstance().reloadFromDatabase();

            ThreadStore.getInstance().loadFromDatabase();
            refreshThreadList();

            // Select the new thread
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

    // ---------- Refresh posts ----------

    /** For existing controller calls without explicit thread. */
    protected static void refreshPosts() {
        refreshPostsForThread(selectedThread);
    }

    /**
     * Refresh the posts list; builds post cards with hover actions, inline edit,
     * visible “(edited)” tag, and a hidden reply composer that slides down.
     * Students can edit/delete only their own posts/replies; staff users can
     * edit/delete anything.
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
            card.setStyle("-fx-border-color: #ddd; -fx-background-color: #f9f9f9; "
                    + "-fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");
            card.setPrefWidth(width - THREAD_COLUMN_WIDTH - 70);

            // MAIN POST
            HBox postRow = new HBox(10);
            postRow.setAlignment(Pos.TOP_LEFT);
            postRow.setFillHeight(false);

            // LEFT: TITLE + BODY
            VBox leftBox = new VBox(6);
            leftBox.setAlignment(Pos.TOP_LEFT);

            Label titleLbl = new Label(post.getTitle());
            titleLbl.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            titleLbl.setWrapText(true);
            titleLbl.setMaxWidth(width - THREAD_COLUMN_WIDTH - 220);
            titleLbl.setMinWidth(300);
            titleLbl.setPrefWidth(width - THREAD_COLUMN_WIDTH - 260);
            titleLbl.setMinHeight(Region.USE_PREF_SIZE);
            titleLbl.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

            Label bodyLbl = new Label(post.getBody());
            bodyLbl.setStyle("-fx-font-size: 13;");
            bodyLbl.setWrapText(true);
            bodyLbl.setMaxWidth(width - THREAD_COLUMN_WIDTH - 220);
            bodyLbl.setMinWidth(300);
            bodyLbl.setPrefWidth(width - THREAD_COLUMN_WIDTH - 260);
            bodyLbl.setMinHeight(Region.USE_PREF_SIZE);
            bodyLbl.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

            leftBox.getChildren().addAll(titleLbl, bodyLbl);
            HBox.setHgrow(leftBox, Priority.ALWAYS);

            // RIGHT: META + BUTTONS
            VBox rightBox = new VBox(4);
            rightBox.setAlignment(Pos.TOP_RIGHT);

            boolean edited = post.getUpdatedAt() != null && post.getCreatedAt() != null
                    && post.getUpdatedAt().isAfter(post.getCreatedAt().plusSeconds(1));

            Label meta = new Label(post.getAuthor() + " • "
                    + post.getCreatedAt().format(TIME_FMT)
                    + (edited ? " (edited)" : ""));
            meta.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");

            boolean canEditPost = post.getAuthor().equals(theUser.getUserName()) || theUser.getNewStaff();

            Button editBtn = new Button("📝");
            Button delBtn  = new Button("🗑️");
            editBtn.setStyle("-fx-font-size: 11;");
            delBtn.setStyle("-fx-font-size: 11;");

            HBox actions = new HBox(6, editBtn, delBtn);
            actions.setAlignment(Pos.CENTER_RIGHT);
            actions.setOpacity(0.0);
            actions.setVisible(canEditPost);

            rightBox.getChildren().addAll(meta, actions);
            VBox.setVgrow(rightBox, Priority.NEVER);

            postRow.getChildren().addAll(leftBox, rightBox);
            HBox.setHgrow(leftBox, Priority.ALWAYS);

            // Hover
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), actions);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), actions);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            card.setOnMouseEntered(e -> { if (canEditPost) fadeIn.playFromStart(); });
            card.setOnMouseExited(e -> { if (canEditPost) fadeOut.playFromStart(); });

            // REPLIES
            VBox repliesBox = new VBox(6);
            List<Reply> replies = ControllerStudentPosts.getRepliesForPost(post);

            for (Reply r : replies) {
                VBox replyCard = new VBox(4);
                replyCard.setStyle("-fx-padding: 4 0 4 0;");
                replyCard.setPrefWidth(width - THREAD_COLUMN_WIDTH - 70);

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

                boolean rEdited = r.getUpdatedAt() != null && r.getCreatedAt() != null
                        && r.getUpdatedAt().isAfter(r.getCreatedAt().plusSeconds(1));

                Label rMeta = new Label(r.getCreatedAt().format(TIME_FMT)
                        + (rEdited ? " (edited)" : ""));
                rMeta.setStyle("-fx-font-size: 10; -fx-text-fill: #777;");

                boolean canEditReply = r.getAuthor().equals(theUser.getUserName()) || theUser.getNewStaff();

                Button rEditBtn = new Button("📝");
                Button rDelBtn  = new Button("🗑️");
                rEditBtn.setStyle("-fx-font-size: 10;");
                rDelBtn.setStyle("-fx-font-size: 10;");

                HBox rActions = new HBox(5, rEditBtn, rDelBtn);
                rActions.setAlignment(Pos.CENTER_RIGHT);
                rActions.setOpacity(0.0);
                rActions.setVisible(canEditReply);

                rightReply.getChildren().addAll(rMeta, rActions);
                VBox.setVgrow(rightReply, Priority.NEVER);

                replyRow.getChildren().addAll(rBodyLbl, rightReply);
                HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

                FadeTransition rFadeIn = new FadeTransition(Duration.millis(200), rActions);
                FadeTransition rFadeOut = new FadeTransition(Duration.millis(200), rActions);
                rFadeIn.setFromValue(0); rFadeIn.setToValue(1);
                rFadeOut.setFromValue(1); rFadeOut.setToValue(0);
                replyRow.setOnMouseEntered(e -> { if (canEditReply) rFadeIn.playFromStart(); });
                replyRow.setOnMouseExited(e -> { if (canEditReply) rFadeOut.playFromStart(); });

                // ----- Reply EDIT -----
                rEditBtn.setOnAction(e -> {
                    TextArea editArea = new TextArea(r.getBody());
                    editArea.setPrefRowCount(2);
                    editArea.setPrefWidth(width - THREAD_COLUMN_WIDTH - 260);
                    Button save = new Button("Save");
                    Button cancel = new Button("Cancel");
                    HBox editor = new HBox(6, editArea, save, cancel);
                    int idx = repliesBox.getChildren().indexOf(replyCard);
                    repliesBox.getChildren().set(idx, editor);

                    save.setOnAction(ev ->
                            ControllerStudentPosts.performReplyEdit(r.getId(), theUser, editArea.getText()));
                    cancel.setOnAction(ev -> refreshPostsForThread(selectedThread));
                });

                // ----- Reply DELETE with confirmation -----
                rDelBtn.setOnAction(e -> {
                    if (confirmDeleteReply()) {
                        ControllerStudentPosts.performReplyDelete(r.getId(), theUser);
                    }
                });

                replyCard.getChildren().add(replyRow);
                repliesBox.getChildren().add(replyCard);
            }

            // REPLY BOX
            VBox replyBox = buildHiddenReplyBox(post);

            // POST EDIT / DELETE
            editBtn.setOnAction(e -> {
                TextArea editArea = new TextArea(post.getBody());
                editArea.setPrefRowCount(3);
                Button save = new Button("Save");
                Button cancel = new Button("Cancel");
                HBox editControls = new HBox(6, save, cancel);
                VBox editor = new VBox(6, editArea, editControls);

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

            // ----- Post DELETE with confirmation -----
            delBtn.setOnAction(e -> {
                if (confirmDeletePost()) {
                    ControllerStudentPosts.performDeletePost(theUser, post);
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

    // ---------- helpers ----------

    private static VBox buildHiddenReplyBox(Post post) {
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
        composer.setMinHeight(0);
        composer.setMaxHeight(0);

        toggle.setOnAction(e -> {
            boolean currentlyVisible = composer.isVisible();
            slideToggle(composer, !currentlyVisible);
        });

        send.setOnAction(e -> {
            ControllerStudentPosts.performReply(theUser, post, replyArea);
            slideToggle(composer, false);
            replyArea.clear();
        });
        cancel.setOnAction(e -> slideToggle(composer, false));

        return new VBox(8, toggle, composer);
    }

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

    // ---------- Custom confirmation dialogs (View-only UI) ----------

    /**
     * Generic custom confirmation dialog used by post/reply delete.
     */
    private static boolean showConfirmDialog(String title, String heading, String message, String confirmText) {
        Stage dialog = new Stage();
        dialog.initOwner(theStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);

        Label headingLbl = new Label(heading);
        headingLbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label msgLbl = new Label(message);
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 13;");

        Button cancelBtn = new Button("Cancel");
        Button confirmBtn = new Button(confirmText);

        cancelBtn.setStyle("-fx-font-size: 13;");
        confirmBtn.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        final boolean[] result = new boolean[]{false};

        cancelBtn.setOnAction(e -> {
            result[0] = false;
            dialog.close();
        });

        confirmBtn.setOnAction(e -> {
            result[0] = true;
            dialog.close();
        });

        HBox buttons = new HBox(10, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, headingLbl, msgLbl, buttons);
        root.setStyle(
                "-fx-padding: 18;" +
                "-fx-background-color: white;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Scene scene = new Scene(root, 360, 150);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result[0];
    }

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

    // UI helpers

    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f)); l.setMinWidth(w); l.setAlignment(p); l.setLayoutX(x); l.setLayoutY(y);
    }
    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f)); b.setMinWidth(w); b.setAlignment(p); b.setLayoutX(x); b.setLayoutY(y);
    }
    private static void setupTextAreaUI(TextArea t, String prompt, double x, double y, double w, double h) {
        t.setPromptText(prompt); t.setLayoutX(x); t.setLayoutY(y); t.setPrefSize(w, h); t.setWrapText(true);
    }
    private static void setupVBoxUI(VBox v, double x, double y, double w, double h) {
        v.setLayoutX(x); v.setLayoutY(y); v.setPrefSize(w, h);
    }
    private static void setupScrollPaneUI(ScrollPane s, double x, double y, double w, double h) {
        s.setLayoutX(x); s.setLayoutY(y); s.setPrefSize(w, h); s.setFitToWidth(true);
    }
}
