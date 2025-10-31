package guiStudentPosts;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import entityClasses.*;
import database.Database;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p><b>Class:</b> ViewStudentPosts</p>
 *
 * <p><b>Description:</b> JavaFX GUI for Student discussion posts. Students can
 * search, create, edit and delete <i>their own</i> posts and replies. The layout mirrors
 * the staff page with a Back button and animated, hidden reply composer. Staff users
 * visiting this page would still have broader permissions through their role checks.</p>
 *
 * @version 3.2 — 2025-10-26
 */
public class ViewStudentPosts {

    /** Stage/user references set by the display method. */
    protected static Stage theStage;
    protected static User theUser;
    private static final Database theDatabase = applicationMain.FoundationsMain.database;

    /** Window sizing from app constants. */
    private static final double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static final double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    /** Header UI. */
    protected static final Label label_PageTitle = new Label("Posts");
    protected static final Label label_UserDetails = new Label();
    protected static final Line line_Separator1 = new Line(20, 95, width - 20, 95);

    /** Search UI. */
    protected static final TextField field_Search = new TextField();
    protected static final Button button_Search = new Button("Search");
    protected static final Button button_ClearSearch = new Button("Clear");

    /** Posts list + composer. */
    protected static VBox postContainer;
    protected static TextField field_Title;
    protected static TextArea area_Body;
    protected static final Button button_Post = new Button("Post");

    /** Footer controls. */
    protected static final Button button_Back = new Button("Back");
    protected static final Button button_Logout = new Button("Logout");
    protected static final Button button_Quit = new Button("Quit");

    private static ViewStudentPosts theView;
    private static Scene theScene;
    private static Pane theRoot;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Display the Student Posts page.
     *
     * @param ps   the primary stage
     * @param user the logged-in user
     */
    public static void displayStudentPosts(Stage ps, User user) {
        theStage = ps;
        theUser = user;

        if (theView == null) theView = new ViewStudentPosts();

        theDatabase.getUserAccountDetails(user.getUserName());
        label_UserDetails.setText("User: " + user.getUserName());
        refreshPosts();

        theStage.setTitle("CSE 360 Foundations: Student Posts");
        theStage.setScene(theScene);
        theStage.show();
    }

    /** Initialize the static GUI. */
    private ViewStudentPosts() {
        theRoot = new Pane();
        theScene = new Scene(theRoot, width, height);

        // Header
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // Search bar
        field_Search.setPromptText("Search posts...");
        field_Search.setLayoutX(40);
        field_Search.setLayoutY(100);
        field_Search.setPrefWidth(width - 260);

        setupButtonUI(button_Search, "Dialog", 14, 80, Pos.CENTER, width - 210, 98);
        button_Search.setOnAction(e ->
                ControllerStudentPosts.performSearch(field_Search.getText()));

        setupButtonUI(button_ClearSearch, "Dialog", 14, 80, Pos.CENTER, width - 120, 98);
        button_ClearSearch.setOnAction(e -> {
            field_Search.clear();
            ControllerStudentPosts.performClearSearch();
        });

        // Posts scroll area
        postContainer = new VBox(10);
        setupVBoxUI(postContainer, 40, 140, width - 80, 300);
        ScrollPane scroll = new ScrollPane(postContainer);
        setupScrollPaneUI(scroll, 40, 140, width - 80, 300);

        // Composer
        field_Title = new TextField();
        field_Title.setPromptText("Post title...");
        field_Title.setLayoutX(40);
        field_Title.setLayoutY(455);
        field_Title.setPrefWidth(width - 220);

        area_Body = new TextArea();
        setupTextAreaUI(area_Body, "Write a post...", 40, 485, width - 220, 60);

        setupButtonUI(button_Post, "Dialog", 16, 100, Pos.CENTER, width - 160, 455);
        button_Post.setOnAction(e ->
                ControllerStudentPosts.performPost(field_Title, area_Body, theUser));

        // Footer: Back / Logout / Quit
        setupButtonUI(button_Back, "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStudent.ViewStudentHome.displayStudentHome(theStage, theUser));

        setupButtonUI(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStudentPosts.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStudentPosts.performQuit());

        theRoot.getChildren().addAll(label_PageTitle, label_UserDetails, line_Separator1,
                field_Search, button_Search, button_ClearSearch,
                scroll, field_Title, area_Body, button_Post,
                button_Back, button_Logout, button_Quit);
    }

    /**
     * Refresh the posts list; builds post cards with hover actions, inline edit,
     * visible “(edited)” tag, and a hidden reply composer that slides down.
     * Students can edit/delete only their own posts/replies.
     */
    protected static void refreshPosts() {
        postContainer.getChildren().clear();
        List<Post> posts = ControllerStudentPosts.getPostsForView();

        for (Post post : posts) {
            VBox card = new VBox(8);
            card.setStyle("-fx-border-color: #ddd; -fx-background-color: #f9f9f9; "
                    + "-fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");
            card.setPrefWidth(width - 100);

            // =============================
            //   MAIN POST (TITLE + BODY + META + ACTIONS)
            // =============================
            HBox postRow = new HBox(10);
            postRow.setAlignment(Pos.TOP_LEFT);
            postRow.setFillHeight(false);

            // ----- LEFT SIDE: TITLE + BODY -----
            VBox leftBox = new VBox(6);
            leftBox.setAlignment(Pos.TOP_LEFT);

            Label titleLbl = new Label(post.getTitle());
            titleLbl.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            titleLbl.setWrapText(true);
            titleLbl.setMaxWidth(width - 350);
            titleLbl.setMinWidth(300);
            titleLbl.setPrefWidth(width - 400);
            titleLbl.setMinHeight(Region.USE_PREF_SIZE);
            titleLbl.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

            Label bodyLbl = new Label(post.getContent());
            bodyLbl.setStyle("-fx-font-size: 13;");
            bodyLbl.setWrapText(true);
            bodyLbl.setMaxWidth(width - 350);
            bodyLbl.setMinWidth(300);
            bodyLbl.setPrefWidth(width - 400);
            bodyLbl.setMinHeight(Region.USE_PREF_SIZE);
            bodyLbl.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);

            leftBox.getChildren().addAll(titleLbl, bodyLbl);
            HBox.setHgrow(leftBox, Priority.ALWAYS);

            // ----- RIGHT SIDE: META + BUTTONS -----
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
            Button delBtn = new Button("🗑️");
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

            // =============================
            //   FADE-IN / FADE-OUT EFFECTS
            // =============================
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), actions);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), actions);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            card.setOnMouseEntered(e -> { if (canEditPost) fadeIn.playFromStart(); });
            card.setOnMouseExited(e -> { if (canEditPost) fadeOut.playFromStart(); });

	       // =============================
	       //   REPLIES SECTION (Styled Cards)
	       // =============================
            VBox repliesBox = new VBox(6);
            List<Reply> replies = ControllerStudentPosts.getRepliesForPost(post);

            for (Reply r : replies) {

                // Outer container (no border box)
                VBox replyCard = new VBox(4);
                replyCard.setStyle("-fx-padding: 4 0 4 0;");
                replyCard.setPrefWidth(width - 80);

                // ===== Row: Left (↳ author: text) | Right (timestamp + buttons) =====
                HBox replyRow = new HBox(10);
                replyRow.setAlignment(Pos.TOP_LEFT);
                replyRow.setPrefWidth(width-100);

                // LEFT SIDE — author + reply text
                Label rBodyLbl = new Label("↳ " + r.getAuthor() + ": " + r.getBody());
                rBodyLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
                rBodyLbl.setWrapText(true);
                rBodyLbl.setMaxWidth(width - 250);
                rBodyLbl.setPrefWidth(width - 260);
                HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

                // RIGHT SIDE — timestamp (top) + edit/delete (below)
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

                // Stack timestamp above buttons
                rightReply.getChildren().addAll(rMeta, rActions);
                VBox.setVgrow(rightReply, Priority.NEVER);

                replyRow.getChildren().addAll(rBodyLbl, rightReply);
                HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

                // ===== Hover fade effect for edit/delete =====
                FadeTransition rFadeIn = new FadeTransition(Duration.millis(200), rActions);
                FadeTransition rFadeOut = new FadeTransition(Duration.millis(200), rActions);
                rFadeIn.setFromValue(0); rFadeIn.setToValue(1);
                rFadeOut.setFromValue(1); rFadeOut.setToValue(0);
                replyRow.setOnMouseEntered(e -> { if (canEditReply) rFadeIn.playFromStart(); });
                replyRow.setOnMouseExited(e -> { if (canEditReply) rFadeOut.playFromStart(); });

                // ===== Edit logic =====
                rEditBtn.setOnAction(e -> {
                    TextArea editArea = new TextArea(r.getBody());
                    editArea.setPrefRowCount(2);
                    editArea.setPrefWidth(width - 420);
                    Button save = new Button("Save");
                    Button cancel = new Button("Cancel");
                    HBox editor = new HBox(6, editArea, save, cancel);
                    int idx = repliesBox.getChildren().indexOf(replyCard);
                    repliesBox.getChildren().set(idx, editor);

                    save.setOnAction(ev ->
                            ControllerStudentPosts.performReplyEdit(r.getId(), theUser, editArea.getText()));
                    cancel.setOnAction(ev -> refreshPosts());
                });

                // ===== Delete logic =====
                rDelBtn.setOnAction(e ->
                        ControllerStudentPosts.performReplyDelete(r.getId(), theUser));

                // Add composed row to replyCard
                replyCard.getChildren().add(replyRow);
                repliesBox.getChildren().add(replyCard);
            }




            // =============================
            //   REPLY BOX
            // =============================
            VBox replyBox = buildHiddenReplyBox(post);

            // =============================
            //   POST EDIT / DELETE LOGIC
            // =============================
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
                    refreshPosts();
                });
                cancel.setOnAction(ev -> refreshPosts());
            });

            delBtn.setOnAction(e -> {
                ControllerStudentPosts.performDeletePost(theUser, post);
            });


            delBtn.setOnAction(e ->
                    ControllerStudentPosts.performDeletePost(theUser, post));

            // =============================
            //   ADD TO CARD
            // =============================
            card.getChildren().addAll(postRow, repliesBox, replyBox);
            postContainer.getChildren().add(card);
        }
    }


    // ---------- helpers ----------

    /**
     * Build a hidden reply box that slides down/up when toggled.
     *
     * @param post the post for which the reply box is built
     * @return the constructed VBox containing the reply UI
     */
    private static VBox buildHiddenReplyBox(Post post) {
        Button toggle = new Button("Reply");

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Write a reply...");
        replyArea.setPrefRowCount(3);
        replyArea.setWrapText(true);
        replyArea.setPrefWidth(width - 300);

        Button send = new Button("Reply");
        Button cancel = new Button("Cancel");

        HBox buttons = new HBox(8, send, cancel);

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

    /**
     * Slide a Region open/closed by animating height + opacity.
     *
     * @param region  the Region to animate
     * @param expand  true to expand (show), false to collapse (hide)
     */
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

    /**
     * Label styling helper.
     *
     * @param l the label
     * @param ff the font family
     * @param f the font size
     * @param w the width
     * @param p the alignment
     * @param x the X layout position
     * @param y the Y layout position
     */
    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f)); l.setMinWidth(w); l.setAlignment(p); l.setLayoutX(x); l.setLayoutY(y);
    }
    /**
     * Button styling helper.
     *
     * @param b the button
     * @param ff the font family
     * @param f the font size
     * @param w the width
     * @param p the alignment
     * @param x the X layout position
     * @param y the Y layout position
     */
    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f)); b.setMinWidth(w); b.setAlignment(p); b.setLayoutX(x); b.setLayoutY(y);
    }
    /**
     * TextArea styling helper.
     *
     * @param t the TextArea
     * @param prompt the placeholder text
     * @param x the X layout position
     * @param y the Y layout position
     * @param w the width
     * @param h the height
     */
    private static void setupTextAreaUI(TextArea t, String prompt, double x, double y, double w, double h) {
        t.setPromptText(prompt); t.setLayoutX(x); t.setLayoutY(y); t.setPrefSize(w, h); t.setWrapText(true);
    }
    /**
     * VBox styling helper.
     *
     * @param v the VBox
     * @param x the X layout position
     * @param y the Y layout position
     * @param w the width
     * @param h the height
     */
    private static void setupVBoxUI(VBox v, double x, double y, double w, double h) {
        v.setLayoutX(x); v.setLayoutY(y); v.setPrefSize(w, h);
    }
    /**
     * ScrollPane styling helper.
     *
     * @param s the ScrollPane
     * @param x the X layout position
     * @param y the Y layout position
     * @param w the width
     * @param h the height
     */
    private static void setupScrollPaneUI(ScrollPane s, double x, double y, double w, double h) {
        s.setLayoutX(x); s.setLayoutY(y); s.setPrefSize(w, h); s.setFitToWidth(true);
    }
}
