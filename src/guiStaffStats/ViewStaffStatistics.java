package guiStaffStats;

import entityClasses.Post;
import entityClasses.PostStore;
import entityClasses.Reply;
import entityClasses.ReplyStore;
import entityClasses.User;
import guiStaffPosts.ControllerStaffPosts;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p><strong>Class:</strong> ViewStaffStatistics</p>
 *
 * <p><strong>Description:</strong>
 * JavaFX view for displaying student-level discussion statistics to staff.
 * The layout mirrors the structure of ViewStaffPosts:
 * <ul>
 *   <li>Left column — student selector + summary metrics</li>
 *   <li>Right column — detailed per-thread activity</li>
 *   <li>Bottom — navigation buttons</li>
 * </ul>
 *
 * This class contains presentation logic only; all computation is handled by:
 * <ul>
 *   <li>{@code ControllerStaffStatistics}</li>
 *   <li>{@code ModelStaffStatistics}</li>
 *   <li>{@code ParticipationStore}</li>
 * </ul>
 *
 * 
 *
 * @version 2.00 — 2025-11-24 — Completed stats view integrated with participation scoring
 */
public class ViewStaffStatistics {

    // ---------- Shared State ----------

    protected static Stage theStage;
    private static User    theUser;

    private static final double width  = applicationMain.FoundationsMain.WINDOW_WIDTH;
    @SuppressWarnings("unused")
    private static final double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // Header (match ViewStaffPosts)
    private static final Label label_PageTitle   = new Label("Student Discussion Statistics");
    private static final Label label_UserDetails = new Label();
    private static final Line  line_Separator1   = new Line(20, 95, width - 20, 95);

    // Layout constants (reuse same thread-column width / right side start)
    private static final double LEFT_COLUMN_WIDTH  = 260.0;
    private static final double rightX             = LEFT_COLUMN_WIDTH + 30;

    // Left side: selection + summary
    private static final Label label_Students = new Label("Students");
    private static ComboBox<User> studentDropdown;
    private static Label labelTotals;

    private static VBox summaryLeftBox;    // basic participation stats
    private static VBox summaryScoresBox;  // scores out of 100
    private static VBox summaryContainer;  // wraps totals + the two columns

    // Right side: fixed-size activity panel
    private static final Label label_ActivityPanel = new Label("Student Activity");
    private static VBox       activityContainer;

    // Footer (match ViewStaffPosts)
    private static final Button button_Back   = new Button("Back");
    private static final Button button_Logout = new Button("Logout");
    private static final Button button_Quit   = new Button("Quit");

    // Scene and root (match ViewStaffPosts structure)
    private static ViewStaffStatistics theView;
    private static Scene               theScene;
    private static Pane                theRoot;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ---------- Public entry point ----------

    /**
     * Displays the statistics page for staff.
     *
     * @param ps        Primary stage
     * @param staffUser The staff user currently logged in
     */
    public static void display(Stage ps, User staffUser) {
        theStage = ps;
        theUser  = staffUser;

        if (theView == null) theView = new ViewStaffStatistics();

        label_UserDetails.setText("User: " + theUser.getUserName());

        // Load dropdown contents
        loadStudentsIntoDropdown();

        // Reset view
        labelTotals.setText("Select a student to view their activity.");
        summaryLeftBox.getChildren().clear();
        summaryScoresBox.getChildren().clear();
        activityContainer.getChildren().clear();

        theStage.setScene(theScene);
        theStage.setTitle("Student Activity Statistics");
        theStage.show();
    }

    // ---------- Constructor: Initialize GUI ----------

    /**
     * Builds the entire JavaFX layout. Parallels ViewStaffPosts to maintain a consistent UI.
     */
    private ViewStaffStatistics() {
        theRoot  = new Pane();
        theScene = new Scene(theRoot, width, height);

        // Header
        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabel(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // ----- Left: Students + Summary -----
        setupLabel(label_Students, "Arial", 18,
                LEFT_COLUMN_WIDTH - 40, Pos.BASELINE_LEFT, 20, 100);

        studentDropdown = new ComboBox<>();
        studentDropdown.setPromptText("Select a student...");
        studentDropdown.setPrefWidth(LEFT_COLUMN_WIDTH - 40);
        studentDropdown.setLayoutX(20);
        studentDropdown.setLayoutY(130);
        studentDropdown.setOnAction(e -> handleStudentSelected());

        summaryContainer = new VBox(10);
        setupVBox(summaryContainer, 20, 170, LEFT_COLUMN_WIDTH, 280);
        summaryContainer.setPadding(new Insets(5));

        labelTotals = new Label("Select a student to view their activity.");
        labelTotals.setFont(Font.font("Arial", 14));

        summaryLeftBox = new VBox(6);
        summaryScoresBox = new VBox(6);

        HBox twoColumnSummary = new HBox(40, summaryLeftBox, summaryScoresBox);
        summaryContainer.getChildren().addAll(labelTotals, twoColumnSummary);

        // ----- Right: Activity Panel -----
        label_ActivityPanel.setFont(Font.font("Arial", 18));
        label_ActivityPanel.setLayoutX(rightX);
        label_ActivityPanel.setLayoutY(100);

        activityContainer = new VBox(10);
        setupVBox(activityContainer, rightX, 130, width - rightX - 40, 350);

        ScrollPane sp = new ScrollPane(activityContainer);
        setupScroll(sp, rightX, 130, width - rightX - 40, 350);

        // ----- Footer -----
        setupButton(button_Back, "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStaff.ViewStaffHome.displayStaffHome(theStage, theUser));

        setupButton(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStaffPosts.performLogout());

        setupButton(button_Quit, "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStaffPosts.performQuit());

        theRoot.getChildren().addAll(
                label_PageTitle, label_UserDetails, line_Separator1,
                label_Students, studentDropdown, summaryContainer,
                label_ActivityPanel, sp,
                button_Back, button_Logout, button_Quit
        );
    }

    // ---------- Load students ----------

    /**
     * Loads all student users into the dropdown.
     * Uses {@code User.loadStudentUsers()} provided by your model.
     */
    private static void loadStudentsIntoDropdown() {
        studentDropdown.getItems().clear();
        List<User> allUsers = ControllerStaffStatistics.loadStudentUsers();

        for (User u : allUsers) {
            if (u.getNewStudent()) {
                studentDropdown.getItems().add(u);
            }
        }
    }

    // ---------- Selection handling ----------

    /**
     * Called when a student is selected from the dropdown.
     * Updates all UI components:
     * <ul>
     *     <li>Totals (posts + replies)</li>
     *     <li>Participation summary</li>
     *     <li>Scores</li>
     *     <li>Thread-level activity</li>
     * </ul>
     */
    private static void handleStudentSelected() {
        User selected = studentDropdown.getValue();
        ControllerStaffStatistics.selectStudent(selected);

        if (selected == null) {
            labelTotals.setText("Select a student to view their activity.");
            summaryLeftBox.getChildren().clear();
            summaryScoresBox.getChildren().clear();
            activityContainer.getChildren().clear();
            return;
        }

        int posts   = ControllerStaffStatistics.getTotalPosts();
        int replies = ControllerStaffStatistics.getTotalReplies();

        labelTotals.setText("Posts: " + posts + "       Replies: " + replies);

        renderSummary();
        renderActivity();
    }

    // ---------- Summary + Scores ----------

    /**
     * Renders:
     * <ul>
     *     <li>Participation summary (threads, days active, avg reply length)</li>
     *     <li>Scoring metrics (activity, engagement, consistency, final score)</li>
     * </ul>
     */
    private static void renderSummary() {
        summaryLeftBox.getChildren().clear();
        summaryScoresBox.getChildren().clear();

        if (ControllerStaffStatistics.getSelectedStudent() == null)
            return;

        Label partTitle = new Label("Participation Summary");
        partTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        
        Label scoresTitle = new Label("Scores");
        scoresTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        summaryLeftBox.getChildren().addAll(
                partTitle,
                new Label("Threads Participated: " +
                        ControllerStaffStatistics.getThreadsParticipated()),
                new Label("Active Days: " +
                        ControllerStaffStatistics.getActiveDays()),
                new Label("Average Reply Length: " +
                        String.format("%.1f", ControllerStaffStatistics.getAverageReplyLength())),
                scoresTitle,
                new Label("Activity Score: " +
                        String.format("%.1f / 100", ControllerStaffStatistics.getActivityScore())),
                new Label("Engagement Score: " +
                        String.format("%.1f / 100", ControllerStaffStatistics.getEngagementScore())),
                new Label("Consistency Score: " +
                        String.format("%.1f / 100", ControllerStaffStatistics.getConsistencyScore())),
                new Label("Final Score: " +
                        String.format("%.1f / 100", ControllerStaffStatistics.getFinalScore()))
        );
    }

    // ---------- Right-side Activity Panel ----------

    /**
     * Renders the full per-thread activity breakdown for the selected student.
     * Shows all posts and replies, grouped by thread.
     */
    private static void renderActivity() {
        activityContainer.getChildren().clear();

        List<ModelStaffStatistics.ThreadActivity> list =
                ControllerStaffStatistics.getActivityList();

        if (list == null || list.isEmpty()) {
            activityContainer.getChildren()
                    .add(new Label("This student has no activity."));
            return;
        }

        for (ModelStaffStatistics.ThreadActivity ta : list) {
            activityContainer.getChildren().add(buildThreadPane(ta));
        }
    }

    /**
     * Builds a collapsible pane representing activity inside a single thread.
     *
     * @param ta ThreadActivity object for the student
     * @return TitledPane summarizing all posts + replies for that thread
     */
    private static TitledPane buildThreadPane(ModelStaffStatistics.ThreadActivity ta) {

        VBox threadBox = new VBox(12);
        threadBox.setPadding(new Insets(10));

        for (Post p : ta.getPostsByStudent()) {
            threadBox.getChildren().add(buildPostCard(p));
        }

        for (Reply r : ta.getRepliesByStudent()) {
            threadBox.getChildren().add(buildReplyContextCard(r));
        }

        TitledPane tp = new TitledPane(
                ta.getThread().getTitle() + "  (" +
                        ta.getPostsByStudent().size() + " posts, " +
                        ta.getRepliesByStudent().size() + " replies)",
                threadBox
        );
        tp.setExpanded(false);

        return tp;
    }

    /**
     * Builds a card showing one post:
     * <ul>
     *     <li>Title</li>
     *     <li>Body</li>
     *     <li>Metadata (author, timestamp, edited)</li>
     *     <li>Replies under this post</li>
     * </ul>
     *
     * @param p the post
     * @return VBox representing the card
     */
    private static VBox buildPostCard(Post p) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 8;");

        Label titleLbl = new Label("Post: " + p.getTitle());
        titleLbl.setFont(Font.font("Arial", 15));
        titleLbl.setStyle("-fx-font-weight: bold;");

        Label bodyLbl = new Label(p.getBody());
        bodyLbl.setWrapText(true);
        bodyLbl.setFont(Font.font("Arial", 13));

        boolean edited = p.getUpdatedAt() != null &&
                p.getUpdatedAt().isAfter(p.getCreatedAt().plusSeconds(1));

        Label meta = new Label(
                "By " + p.getAuthor() + " • " +
                        p.getCreatedAt().format(TIME_FMT) +
                        (edited ? " (edited)" : "")
        );
        meta.setFont(Font.font("Arial", 11));
        meta.setStyle("-fx-text-fill: #666;");

        VBox repliesBox = new VBox(4);
        List<Reply> replies = ReplyStore.getInstance()
                .getRepliesForPost(p.getDatabaseId());

        for (Reply r : replies) {
            repliesBox.getChildren().add(buildReplyRow(r));
        }

        if (!repliesBox.getChildren().isEmpty()) {
            repliesBox.setPadding(new Insets(5, 0, 0, 20));
        }

        box.getChildren().addAll(titleLbl, bodyLbl, meta, repliesBox);
        return box;
    }

    /**
     * Builds a compact row showing one reply.
     *
     * @param r reply object
     * @return HBox containing reply text and timestamp
     */
    private static HBox buildReplyRow(Reply r) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.TOP_LEFT);

        boolean edited = r.getUpdatedAt().isAfter(r.getCreatedAt().plusSeconds(1));

        Label lbl = new Label(
                "↳ " + r.getAuthor() + ": " + r.getBody() +
                        "\n" + r.getCreatedAt().format(TIME_FMT) +
                        (edited ? " (edited)" : "")
        );
        lbl.setWrapText(true);
        lbl.setFont(Font.font("Arial", 12));

        row.getChildren().add(lbl);
        return row;
    }

    /**
     * Builds a card showing a reply + the post it belongs under.
     * Useful when the student replied inside a thread but did not author the post.
     *
     * @param r reply object
     * @return VBox card showing reply + parent post context
     */
    private static VBox buildReplyContextCard(Reply r) {

        Post parent = null;
        for (Post p : PostStore.getInstance().getAllPosts()) {
            if (p.getDatabaseId() == r.getPostDatabaseId()) {
                parent = p;
                break;
            }
        }

        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #eef2ff; -fx-padding: 10; -fx-background-radius: 8;");

        Label header = new Label("Reply under Post: " +
                (parent != null ? parent.getTitle() : "(unknown post)")
        );
        header.setFont(Font.font("Arial", 14));
        header.setStyle("-fx-font-weight: bold;");

        boolean edited = r.getUpdatedAt().isAfter(r.getCreatedAt().plusSeconds(1));

        Label replyLbl = new Label(
                r.getAuthor() + ": " + r.getBody() +
                        "\n" + r.getCreatedAt().format(TIME_FMT) +
                        (edited ? " (edited)" : "")
        );
        replyLbl.setWrapText(true);
        replyLbl.setFont(Font.font("Arial", 12));

        if (parent != null) {
            Label parentBody = new Label("Parent Post Body:\n" + parent.getBody());
            parentBody.setWrapText(true);
            parentBody.setFont(Font.font("Arial", 11));
            parentBody.setStyle("-fx-text-fill: #555; -fx-padding: 4 0 0 0;");

            box.getChildren().addAll(header, replyLbl, parentBody);
        } else {
            box.getChildren().addAll(header, replyLbl);
        }

        return box;
    }

    // ---------- Helper Methods (documentation only) ----------

    /**
     * Utility to configure a Label with layout + font + alignment.
     */
    private static void setupLabel(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    /**
     * Utility to configure a Button with layout + font + width.
     */
    private static void setupButton(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    /**
     * Utility to configure a VBox with fixed position + size.
     */
    private static void setupVBox(VBox v, double x, double y, double w, double h) {
        v.setLayoutX(x);
        v.setLayoutY(y);
        v.setPrefSize(w, h);
    }

    /**
     * Utility to configure a ScrollPane with fixed position + size.
     */
    private static void setupScroll(ScrollPane s, double x, double y, double w, double h) {
        s.setLayoutX(x);
        s.setLayoutY(y);
        s.setPrefSize(w, h);
        s.setFitToWidth(true);
    }
}
