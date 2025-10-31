package guiStaffPosts;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import entityClasses.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p><strong>Class:</strong> ViewStaffPosts</p>
 *
 * <p><strong>Description:</strong>
 * Staff version of the discussion posts page. Identical to the student layout but
 * with full privileges: staff can edit or delete <i>any</i> post or reply.
 * Uses database-managed IDs for persistence.</p>
 *
 * @version 3.00 — 2025-10-27 — Unified with StudentPosts (DB + staff privileges)
 */
public class ViewStaffPosts {

    protected static Stage theStage;
    protected static User theUser;

    private static final double width  = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static final double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // Header + layout components
    private static final Label label_PageTitle = new Label("Staff Posts");
    private static final Label label_UserDetails = new Label();
    private static final Line  line_Separator1 = new Line(20, 95, width - 20, 95);

    private static final TextField field_Search = new TextField();
    private static final Button button_Search = new Button("Search");
    private static final Button button_ClearSearch = new Button("Clear");

    private static VBox postContainer;
    private static TextField field_Title;
    private static TextArea area_Body;
    private static final Button button_Post = new Button("Post");

    private static final Button button_Back = new Button("Back");
    private static final Button button_Logout = new Button("Logout");
    private static final Button button_Quit = new Button("Quit");

    private static ViewStaffPosts theView;
    private static Scene theScene;
    private static Pane theRoot;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ---------- Display ----------

    public static void displayStaffPosts(Stage ps, User user) {
        theStage = ps;
        theUser = user;
        if (theView == null) theView = new ViewStaffPosts();

        label_UserDetails.setText("User: " + user.getUserName());
        refreshPosts();

        theStage.setTitle("CSE 360 Foundations: Staff Posts");
        theStage.setScene(theScene);
        theStage.show();
    }

    // ---------- Initialize ----------

    private ViewStaffPosts() {
        theRoot = new Pane();
        theScene = new Scene(theRoot, width, height);

        // Header
        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabel(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // Search
        field_Search.setPromptText("Search posts...");
        field_Search.setLayoutX(40);
        field_Search.setLayoutY(100);
        field_Search.setPrefWidth(width - 260);

        setupButton(button_Search, "Dialog", 14, 80, Pos.CENTER, width - 210, 98);
        button_Search.setOnAction(e -> ControllerStaffPosts.performSearch(field_Search.getText()));

        setupButton(button_ClearSearch, "Dialog", 14, 80, Pos.CENTER, width - 120, 98);
        button_ClearSearch.setOnAction(e -> {
            field_Search.clear();
            ControllerStaffPosts.performClearSearch();
        });

        // Posts list
        postContainer = new VBox(10);
        setupVBox(postContainer, 40, 140, width - 80, 300);
        ScrollPane scroll = new ScrollPane(postContainer);
        setupScroll(scroll, 40, 140, width - 80, 300);

        // Composer
        field_Title = new TextField();
        field_Title.setPromptText("Post title...");
        field_Title.setLayoutX(40);
        field_Title.setLayoutY(455);
        field_Title.setPrefWidth(width - 220);

        area_Body = new TextArea();
        setupTextArea(area_Body, "Write a post...", 40, 485, width - 220, 60);

        setupButton(button_Post, "Dialog", 16, 100, Pos.CENTER, width - 160, 455);
        button_Post.setOnAction(e ->
                ControllerStaffPosts.performPost(field_Title, area_Body, theUser));

        // Footer
        setupButton(button_Back, "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStaff.ViewStaffHome.displayStaffHome(theStage, theUser));

        setupButton(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStaffPosts.performLogout());

        setupButton(button_Quit, "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStaffPosts.performQuit());

        theRoot.getChildren().addAll(label_PageTitle, label_UserDetails, line_Separator1,
                field_Search, button_Search, button_ClearSearch,
                scroll, field_Title, area_Body, button_Post,
                button_Back, button_Logout, button_Quit);
    }

    // ---------- Refresh ----------

    protected static void refreshPosts() {
        postContainer.getChildren().clear();
        List<Post> posts = ControllerStaffPosts.getPostsForView();

        for (Post post : posts) {
            VBox card = new VBox(8);
            card.setStyle("-fx-border-color:#ddd; -fx-background-color:#f9f9f9; "
                    + "-fx-padding:10; -fx-border-radius:8; -fx-background-radius:8;");
            card.setPrefWidth(width - 100);

            // ----- Title + Body -----
            Label titleLbl = new Label(post.getTitle());
            titleLbl.setStyle("-fx-font-size:14; -fx-font-weight:bold;");
            Label bodyLbl = new Label(post.getBody());
            bodyLbl.setStyle("-fx-font-size:13;");

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

            // Fade-in/out on hover
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
                    refreshPosts();
                });
                cancel.setOnAction(ev -> refreshPosts());
            });

            delBtn.setOnAction(e -> {
                ControllerStaffPosts.performDeletePost(theUser, post);
            });

            // ----- Combine -----
            card.getChildren().addAll(postRow, repliesBox, replyBox);
            postContainer.getChildren().add(card);
        }
    }

    // ---------- Render Reply ----------
    private static HBox renderReply(Reply r) {
        boolean edited = r.getUpdatedAt() != null && r.getCreatedAt() != null
                && r.getUpdatedAt().isAfter(r.getCreatedAt().plusSeconds(1));

        // ===== Outer layout =====
        HBox replyRow = new HBox(10);
        replyRow.setAlignment(Pos.TOP_LEFT);
        replyRow.setPrefWidth(width - 100);

        // ===== LEFT SIDE — reply text =====
        Label rBodyLbl = new Label("↳ " + r.getAuthor() + ": " + r.getBody());
        rBodyLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        rBodyLbl.setWrapText(true);
        rBodyLbl.setMaxWidth(width - 250);
        rBodyLbl.setPrefWidth(width - 260);
        HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

        // ===== RIGHT SIDE — timestamp (top) + buttons (below) =====
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
        rActions.setOpacity(0.0); // hidden initially
        rActions.setVisible(true);

        rightReply.getChildren().addAll(rMeta, rActions);
        VBox.setVgrow(rightReply, Priority.NEVER);

        replyRow.getChildren().addAll(rBodyLbl, rightReply);
        HBox.setHgrow(rBodyLbl, Priority.ALWAYS);

        // ===== Hover fade effect =====
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), rActions);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rActions);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        replyRow.setOnMouseEntered(e -> fadeIn.playFromStart());
        replyRow.setOnMouseExited(e -> fadeOut.playFromStart());

        // ===== Edit logic =====
        rEditBtn.setOnAction(e -> {
            TextArea editArea = new TextArea(r.getBody());
            editArea.setPrefRowCount(2);
            editArea.setPrefWidth(width - 420);
            Button save = new Button("Save");
            Button cancel = new Button("Cancel");
            HBox editor = new HBox(6, editArea, save, cancel);
            int idx = ((VBox) replyRow.getParent()).getChildren().indexOf(replyRow);
            ((VBox) replyRow.getParent()).getChildren().set(idx, editor);

            save.setOnAction(ev -> {
                ControllerStaffPosts.performReplyEdit(r.getId(), theUser, editArea.getText());
                refreshPosts();
            });
            cancel.setOnAction(ev -> refreshPosts());
        });

        // ===== Delete logic =====
        rDelBtn.setOnAction(e -> {
            ControllerStaffPosts.performReplyDelete(r.getId(), theUser);
            refreshPosts();
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
        replyArea.setPrefWidth(width - 300);

        Button send = new Button("Reply");
        Button cancel = new Button("Cancel");
        HBox buttons = new HBox(8, send, cancel);
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
        double from = expand ? 0 : region.getHeight();
        double to   = expand ? 120 : 0;
        double fromO = expand ? 0 : 1;
        double toO   = expand ? 1 : 0;

        region.setVisible(true);
        region.setManaged(true);
        region.setOpacity(fromO);
        region.setMinHeight(from);
        region.setPrefHeight(from);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(region.opacityProperty(), fromO),
                        new KeyValue(region.prefHeightProperty(), from)),
                new KeyFrame(Duration.millis(220),
                        new KeyValue(region.opacityProperty(), toO),
                        new KeyValue(region.prefHeightProperty(), to))
        );
        tl.setOnFinished(ev -> {
            if (!expand) {
                region.setVisible(false);
                region.setManaged(false);
            }
        });
        tl.play();
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
