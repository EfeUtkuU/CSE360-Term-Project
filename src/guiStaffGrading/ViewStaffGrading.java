package guiStaffGrading;

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

/**
 * <h2>Class: ViewStaffGrading</h2>
 *
 * <p><strong>Description:</strong><br>
 * JavaFX view class responsible for rendering the Staff Grading interface.
 * This view provides three primary panels:
 * </p>
 *
 * <ul>
 *   <li><strong>Left Panel:</strong> List of students</li>
 *   <li><strong>Center Panel:</strong> Grade + Feedback editor</li>
 *   <li><strong>Right Panel:</strong> Staff–Student chat messages</li>
 * </ul>
 *
 * <p>
 * All events and interactions are delegated to
 * {@link ControllerStaffGrading}, following the static MVC pattern used
 * throughout the application.
 * </p>
 *
 * @version 1.00 — 2025-11-25 — Full Javadoc-compatible version
 */
public class ViewStaffGrading {

    // -------------------------------------------------------------------------
    // Shared State (Stage + Logged-in User)
    // -------------------------------------------------------------------------

    /** The JavaFX stage used to display the grading view. */
    protected static Stage theStage;

    /** The currently logged-in staff user. */
    protected static User theUser;

    /** Window dimensions (inherited from FoundationsMain). */
    private static final double width  = FoundationsMain.WINDOW_WIDTH;
    private static final double height = FoundationsMain.WINDOW_HEIGHT;

    // -------------------------------------------------------------------------
    // UI Components — Header
    // -------------------------------------------------------------------------

    private static final Label label_PageTitle   = new Label("Staff Grading");
    private static final Label label_UserDetails = new Label();
    private static final Line  line_Separator1   = new Line(20, 95, width - 20, 95);

    // -------------------------------------------------------------------------
    // UI Components — Left Column (Students)
    // -------------------------------------------------------------------------

    private static final Label label_Students = new Label("Students");

    /** List of student usernames. */
    public static ListView<String> list_Students;

    // -------------------------------------------------------------------------
    // UI Components — Center Column (Grade + Feedback)
    // -------------------------------------------------------------------------

    private static final Label label_Grade = new Label("Grade (0–100)");

    /** Text field for entering a numeric grade. */
    public static TextField text_Grade;

    private static final Label label_Feedback = new Label("Staff Feedback to Student");

    /** Text area for entering staff feedback. */
    public static TextArea text_Feedback;

    private static final Button button_SaveGrade = new Button("Save Grade");

    // -------------------------------------------------------------------------
    // UI Components — Right Column (Chat)
    // -------------------------------------------------------------------------

    private static final Label label_Chat = new Label("Feedback Conversation");

    /** Chat window displaying staff–student conversation. */
    public static ListView<String> list_Chat;

    /** Text field where staff type replies. */
    public static TextField text_Reply;

    private static final Button button_Send = new Button("Send");

    // -------------------------------------------------------------------------
    // Footer Buttons + Alert
    // -------------------------------------------------------------------------

    private static final Button button_Back   = new Button("Back");
    private static final Button button_Logout = new Button("Logout");
    private static final Button button_Quit   = new Button("Quit");

    /** Shared confirmation/information alert used throughout the view. */
    public static Alert alert = new Alert(Alert.AlertType.INFORMATION);

    // -------------------------------------------------------------------------
    // Internal Scene + Root Pane
    // -------------------------------------------------------------------------

    private static ViewStaffGrading theView;
    private static Scene            theScene;
    private static Pane             theRoot;

    // Layout constants
    private static final double LEFT_COLUMN_WIDTH   = 220.0;
    private static final double CENTER_COLUMN_WIDTH = 260.0;

    // -------------------------------------------------------------------------
    // Public Entry Point
    // -------------------------------------------------------------------------

    /**
     * Displays the Staff Grading page for the given user on the provided stage.
     *
     * @param ps   the JavaFX stage
     * @param user the staff user currently logged in
     */
    public static void displayStaffGrading(Stage ps, User user) {
        theStage = ps;
        theUser  = user;

        if (theView == null) {
            theView = new ViewStaffGrading();
        }

        label_UserDetails.setText("User: " + user.getUserName());

        ControllerStaffGrading.populateStudentList();

        theStage.setTitle("CSE 360 Foundations: Staff Grading");
        theStage.setScene(theScene);
        theStage.show();
    }

    // -------------------------------------------------------------------------
    // Constructor — Builds Entire GUI
    // -------------------------------------------------------------------------

    /**
     * Constructs the Staff Grading GUI layout, initializes all components,
     * and arranges them into three vertical columns.
     */
    private ViewStaffGrading() {
        theRoot  = new Pane();
        theScene = new Scene(theRoot, width, height);

        setupHeader();
        setupMiddleColumns();
        setupFooter();

        theRoot.getChildren().addAll(
                label_PageTitle, label_UserDetails, line_Separator1,
                label_Students, list_Students,
                label_Grade, text_Grade,
                label_Feedback, text_Feedback, button_SaveGrade,
                label_Chat, list_Chat, text_Reply, button_Send,
                button_Back, button_Logout, button_Quit
        );
    }

    // -------------------------------------------------------------------------
    // HEADER SETUP
    // -------------------------------------------------------------------------

    /** Initializes the page header. */
    private void setupHeader() {
        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabel(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
    }

    // -------------------------------------------------------------------------
    // MIDDLE SECTION (Three Columns)
    // -------------------------------------------------------------------------

    /** Assembles the students, grading, and chat columns. */
    private void setupMiddleColumns() {
        double contentTopY   = 110;
        double contentHeight = 400;
        double gap           = 30;

        double leftX   = 20;
        double centerX = leftX + LEFT_COLUMN_WIDTH + gap;
        double rightX  = centerX + CENTER_COLUMN_WIDTH + 10;
        double rightWidth = width - rightX - 20;

        // Left — Students
        label_Students.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        setupLabel(label_Students, "Arial", 16, LEFT_COLUMN_WIDTH - 20,
                Pos.BASELINE_LEFT, leftX, contentTopY);

        list_Students = new ListView<>();
        list_Students.setOnMouseClicked(e -> ControllerStaffGrading.studentSelected());
        setupVBox(list_Students, leftX, contentTopY + 30,
                LEFT_COLUMN_WIDTH - 20, contentHeight - 40);

        // Center — Grade + Feedback
        setupCenterColumn(centerX, contentTopY);

        // Right — Chat
        setupRightColumn(rightX, contentTopY, rightWidth, contentHeight);
    }

    /**
     * Assembles the center column (grade + feedback).
     */
    private void setupCenterColumn(double centerX, double yTop) {
        label_Grade.setFont(Font.font("Arial", 14));
        label_Grade.setLayoutX(centerX);
        label_Grade.setLayoutY(yTop);

        text_Grade = new TextField();
        text_Grade.setPromptText("Enter grade...");
        text_Grade.setPrefWidth(CENTER_COLUMN_WIDTH - 40);
        text_Grade.setLayoutX(centerX);
        text_Grade.setLayoutY(yTop + 25);

        label_Feedback.setFont(Font.font("Arial", 14));
        label_Feedback.setLayoutX(centerX);
        label_Feedback.setLayoutY(yTop + 70);

        text_Feedback = new TextArea();
        text_Feedback.setWrapText(true);
        text_Feedback.setPrefSize(CENTER_COLUMN_WIDTH - 40, 180);
        text_Feedback.setLayoutX(centerX);
        text_Feedback.setLayoutY(yTop + 95);

        button_SaveGrade.setFont(Font.font("Dialog", 16));
        button_SaveGrade.setMinWidth(140);
        button_SaveGrade.setLayoutX(centerX);
        button_SaveGrade.setLayoutY(yTop + 295);
        button_SaveGrade.setOnAction(e -> ControllerStaffGrading.saveGrade());
    }

    /**
     * Assembles the right column (chat conversation).
     */
    private void setupRightColumn(double rightX, double yTop, double width, double height) {
        label_Chat.setFont(Font.font("Arial", 14));
        label_Chat.setLayoutX(rightX);
        label_Chat.setLayoutY(yTop);

        list_Chat = new ListView<>();
        setupVBox(list_Chat, rightX, yTop + 25, width, height - 80);

        text_Reply = new TextField();
        text_Reply.setPromptText("Type a reply...");
        text_Reply.setPrefWidth(width - 90);
        text_Reply.setLayoutX(rightX);
        text_Reply.setLayoutY(yTop + height - 45);

        button_Send.setFont(Font.font("Dialog", 14));
        button_Send.setMinWidth(70);
        button_Send.setLayoutX(rightX + width - 80);
        button_Send.setLayoutY(yTop + height - 47);
        button_Send.setOnAction(e -> ControllerStaffGrading.sendReply());
    }

    // -------------------------------------------------------------------------
    // FOOTER
    // -------------------------------------------------------------------------

    /** Configures the bottom navigation buttons. */
    private void setupFooter() {
        setupButton(button_Back,   "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStaff.ViewStaffHome.displayStaffHome(theStage, theUser));

        setupButton(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStaffPosts.performLogout());

        setupButton(button_Quit,   "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStaffPosts.performQuit());
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Utility method for positioning and styling labels.
     */
    private static void setupLabel(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    /**
     * Utility method for positioning and styling buttons.
     */
    private static void setupButton(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    /**
     * Utility method that configures size and placement of list views and input fields.
     */
    private static void setupVBox(Region r, double x, double y, double w, double h) {
        r.setLayoutX(x);
        r.setLayoutY(y);
        r.setPrefSize(w, h);
        if (r instanceof Pane p) {
            p.setPadding(new Insets(5));
        }
    }
}
