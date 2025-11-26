package guiStudentGrading;

import applicationMain.FoundationsMain;
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

import java.util.List;

/**
 * <p><strong>Class:</strong> ViewStudentGrading</p>
 *
 * <p><strong>Description:</strong>
 * Student-facing GUI for:
 * <ul>
 *     <li>Displaying discussion statistics for the logged-in student</li>
 *     <li>Showing the grade given by staff</li>
 *     <li>Showing the staff’s written feedback</li>
 *     <li>Displaying a chat-style conversation between staff and student</li>
 *     <li>Allowing the student to send replies in that conversation</li>
 * </ul>
 *
 * This view mirrors the visual structure of the staff-side pages:
 * left-side summary panel + right-side chat panel + footer navigation buttons.
 *
 * <strong>No SQL or business logic exists here.</strong>
 * All data comes from {@link ControllerStudentGrading}.
 * 
 */
public class ViewStudentGrading {

    /** General-purpose alert box used for errors/info. */
    public static Alert alert = new Alert(Alert.AlertType.INFORMATION);

    /** Chat list (conversation messages). Updated by controller. */
    public static ListView<String> list_Chat;

    /** Field for the student to type a reply message. */
    public static TextField field_Reply;

    // Labels updated by controller
    private static Label lblPosts;
    private static Label lblReplies;
    private static Label lblThreads;
    private static Label lblActiveDays;
    private static Label lblAvgLen;
    private static Label lblActivity;
    private static Label lblEngagement;
    private static Label lblConsistency;
    private static Label lblFinalScore;

    private static Label lblGradeValue;
    private static Label lblStaffFeedback;

    // Internal view state
    private static Stage theStage;
    private static User theUser;
    private static Pane theRoot;
    private static Scene theScene;

    private static final double WIDTH  = FoundationsMain.WINDOW_WIDTH;
    private static final double HEIGHT = FoundationsMain.WINDOW_HEIGHT;

    // ---------------------------------------------------------------------
    // ENTRY POINT
    // ---------------------------------------------------------------------

    /**
     * Displays the Student Grading screen.
     * Builds UI and then asks {@link ControllerStudentGrading} to load all data.
     */
    public static void displayStudentGrading(Stage stage, User user) {
        theStage = stage;
        theUser  = user;

        buildUI();

        theStage.setScene(theScene);
        theStage.setTitle("CSE 360 Foundations: Student Grading");
        theStage.show();

        // Controller populates all fields after UI exists
        ControllerStudentGrading.refreshAll();
    }

    // ---------------------------------------------------------------------
    // BUILD UI (entire screen)
    // ---------------------------------------------------------------------

    /**
     * Builds the whole JavaFX interface for the student grading page.
     * Mirrors the layout conventions of the staff pages.
     */
    private static void buildUI() {
        theRoot = new Pane();
        theScene = new Scene(theRoot, WIDTH, HEIGHT);

        /* ------------------------------------------------------
           HEADER: Page Title + Username + Top Separator
        ------------------------------------------------------ */
        Label label_PageTitle = new Label("Student Grading & Feedback");
        setupLabel(label_PageTitle, "Arial", 28, WIDTH, Pos.CENTER, 0, 5);

        Label label_UserDetails = new Label("User: " + theUser.getUserName());
        setupLabel(label_UserDetails, "Arial", 20, WIDTH, Pos.BASELINE_LEFT, 20, 55);

        Line separatorTop = new Line(20, 95, WIDTH - 20, 95);

        /* ------------------------------------------------------
           LEFT PANE — Statistics, Grade, and Staff Feedback
        ------------------------------------------------------ */

        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.setLayoutX(20);
        leftPane.setLayoutY(110);
        leftPane.setPrefWidth(350);
        leftPane.setPrefHeight(420);
        leftPane.setStyle("-fx-background-color:#f5f5f5; -fx-padding:15; -fx-background-radius:8;");

        // Header for statistics
        Label lblHeaderStats = new Label("Your Statistics");
        lblHeaderStats.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Labels that will be filled by ControllerStudentGrading.updateStatistics
        lblPosts       = new Label();
        lblReplies     = new Label();
        lblThreads     = new Label();
        lblActiveDays  = new Label();
        lblAvgLen      = new Label();
        lblActivity    = new Label();
        lblEngagement  = new Label();
        lblConsistency = new Label();
        lblFinalScore  = new Label();

        VBox statsBox = new VBox(5,
                lblPosts, lblReplies, lblThreads, lblActiveDays,
                lblAvgLen, lblActivity, lblEngagement, lblConsistency, lblFinalScore
        );

        // Grade header and grade value
        Label lblHeaderGrade = new Label("Your Grade");
        lblHeaderGrade.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        lblGradeValue = new Label("Loading...");
        lblGradeValue.setStyle("-fx-font-size: 16px;");

        // Staff feedback label
        Label lblHeaderFeedback = new Label("Staff Feedback");
        lblHeaderFeedback.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        lblStaffFeedback = new Label("Loading...");
        lblStaffFeedback.setWrapText(true);
        lblStaffFeedback.setStyle("-fx-font-size: 14px;");

        leftPane.getChildren().addAll(
                lblHeaderStats, statsBox,
                lblHeaderGrade, lblGradeValue,
                lblHeaderFeedback, lblStaffFeedback
        );

        /* ------------------------------------------------------
           RIGHT PANE — Conversation with Staff
        ------------------------------------------------------ */

        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        rightPane.setLayoutX(400);
        rightPane.setLayoutY(110);
        rightPane.setPrefWidth(WIDTH - 430);
        rightPane.setPrefHeight(420);
        rightPane.setStyle("-fx-background-color:#eef2ff; -fx-padding:15; -fx-background-radius:8;");

        Label lblChatHeader = new Label("Conversation With Staff");
        lblChatHeader.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        list_Chat = new ListView<>();
        list_Chat.setPrefHeight(330);

        field_Reply = new TextField();
        field_Reply.setPromptText("Write a reply...");

        Button btnSend = new Button("Send");
        btnSend.setOnAction(e -> ControllerStudentGrading.sendReply());

        HBox replyBox = new HBox(10, field_Reply, btnSend);

        rightPane.getChildren().addAll(lblChatHeader, list_Chat, replyBox);

        /* ------------------------------------------------------
           FOOTER — Navigation (matches staff layout)
        ------------------------------------------------------ */

        Button button_Back   = new Button("Back");
        Button button_Logout = new Button("Logout");
        Button button_Quit   = new Button("Quit");

        setupButton(button_Back, "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStudent.ViewStudentHome.displayStudentHome(theStage, theUser));

        setupButton(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStaffPosts.performLogout());

        setupButton(button_Quit, "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStaffPosts.performQuit());

        Line separatorBottom = new Line(20, 545, WIDTH - 20, 545);

        /* ------------------------------------------------------
           Add everything to the root
        ------------------------------------------------------ */
        theRoot.getChildren().addAll(
                label_PageTitle, label_UserDetails, separatorTop,
                leftPane, rightPane,
                separatorBottom,
                button_Back, button_Logout, button_Quit
        );
    }

    // ---------------------------------------------------------------------
    // UPDATE METHODS (called by ControllerStudentGrading)
    // ---------------------------------------------------------------------

    /**
     * Updates all statistic labels in the left panel.
     */
    protected static void updateStatistics(
            int posts, int replies, int threads, int activeDays,
            double avgLen, double activity, double engagement,
            double consistency, double finalScore
    ) {
        lblPosts.setText("Total Posts: " + posts);
        lblReplies.setText("Total Replies: " + replies);
        lblThreads.setText("Threads Participated: " + threads);
        lblActiveDays.setText("Active Days: " + activeDays);
        lblAvgLen.setText("Average Reply Length: " + String.format("%.1f", avgLen));
        lblActivity.setText("Activity Score: " + String.format("%.1f / 100", activity));
        lblEngagement.setText("Engagement Score: " + String.format("%.1f / 100", engagement));
        lblConsistency.setText("Consistency Score: " + String.format("%.1f / 100", consistency));
        lblFinalScore.setText("Final Score: " + String.format("%.1f / 100", finalScore));
    }

    /**
     * Updates the grade (0–100) and staff feedback text.
     */
    protected static void updateGradeAndFeedback(String grade, String feedback) {
        lblGradeValue.setText(grade);
        lblStaffFeedback.setText(feedback);
    }

    /**
     * Replaces the entire message list in the chat panel.
     */
    protected static void updateChatMessages(List<String> msgs) {
        list_Chat.getItems().setAll(msgs);
    }

    // ---------------------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------------------

    private static void setupLabel(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    private static void setupButton(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
}
