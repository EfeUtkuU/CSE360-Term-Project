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

public class ViewStaffGrading {

    // ---------- Shared State ----------
    protected static Stage theStage;
    protected static User  theUser;

    private static final double width  = FoundationsMain.WINDOW_WIDTH;
    private static final double height = FoundationsMain.WINDOW_HEIGHT;

    // Header
    private static final Label label_PageTitle   = new Label("Staff Grading");
    private static final Label label_UserDetails = new Label();
    private static final Line  line_Separator1   = new Line(20, 95, width - 20, 95);

    // Middle – Left column (Students)
    private static final Label       label_Students = new Label("Students");
    public  static       ListView<String> list_Students;

    // Middle – Center column (Grade + Feedback)
    private static final Label      label_Grade     = new Label("Grade (0–100)");
    public  static       TextField  text_Grade;
    private static final Label      label_Feedback  = new Label("Staff Feedback to Student");
    public  static       TextArea   text_Feedback;
    private static final Button     button_SaveGrade = new Button("Save Grade");

    // Middle – Right column (Chat)
    private static final Label      label_Chat      = new Label("Feedback Conversation");
    public  static       ListView<String> list_Chat;
    public  static       TextField text_Reply;
    private static final Button     button_Send     = new Button("Send");

    // Footer
    private static final Button button_Back   = new Button("Back");
    private static final Button button_Logout = new Button("Logout");
    private static final Button button_Quit   = new Button("Quit");

    // Alert
    public static Alert alert = new Alert(Alert.AlertType.INFORMATION);

    // Scene and root
    private static ViewStaffGrading theView;
    private static Scene            theScene;
    private static Pane             theRoot;

    // Layout constants (similar idea to THREAD_COLUMN_WIDTH in posts view)
    private static final double LEFT_COLUMN_WIDTH   = 220.0; // students
    private static final double CENTER_COLUMN_WIDTH = 260.0; // grade + feedback

    // ---------- Public entry point ----------
    public static void displayStaffGrading(Stage ps, User user) {
        theStage = ps;
        theUser  = user;

        if (theView == null) {
            theView = new ViewStaffGrading();
        }

        label_UserDetails.setText("User: " + user.getUserName());

        // Ask controller to load student list once the view is ready
        ControllerStaffGrading.populateStudentList();

        theStage.setTitle("CSE 360 Foundations: Staff Grading");
        theStage.setScene(theScene);
        theStage.show();
    }

    // ---------- Constructor: Initialize GUI ----------
    private ViewStaffGrading() {
        theRoot  = new Pane();
        theScene = new Scene(theRoot, width, height);

        // ----- Header -----
        setupLabel(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
        setupLabel(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // line_Separator1 already has proper X/Y in its constructor
        // (20, 95) to (width-20, 95)

        // ----- Middle band coordinates -----
        // Everything between y ~ 110 and y ~ 520 should stay inside this band
        double contentTopY   = 110;
        double contentHeight = 400;  // fixed band (no dynamic resize)
        double gapBetweenColumns = 30;

        // Left column (Students) X/Y
        double leftX = 20;

        // Center column (Grade + Feedback)
        double centerX = leftX + LEFT_COLUMN_WIDTH + gapBetweenColumns;

        // Right column (Chat)
        double rightX = centerX + CENTER_COLUMN_WIDTH + 10;
        double rightWidth = width - rightX - 20;

        // ----- Left: Students -----
        label_Students.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        setupLabel(label_Students, "Arial", 16, LEFT_COLUMN_WIDTH - 20,
                Pos.BASELINE_LEFT, leftX, contentTopY);

        list_Students = new ListView<>();
        list_Students.setOnMouseClicked(e -> ControllerStaffGrading.studentSelected());
        setupVBox(list_Students, leftX, contentTopY + 30,
                LEFT_COLUMN_WIDTH - 20, contentHeight - 40);

        // ----- Center: Grade + Feedback -----
        label_Grade.setStyle("-fx-font-weight: bold;");
        label_Grade.setFont(Font.font("Arial", 14));
        label_Grade.setLayoutX(centerX);
        label_Grade.setLayoutY(contentTopY);

        text_Grade = new TextField();
        text_Grade.setPromptText("Enter grade...");
        text_Grade.setPrefWidth(CENTER_COLUMN_WIDTH - 40);
        text_Grade.setLayoutX(centerX);
        text_Grade.setLayoutY(contentTopY + 25);

        label_Feedback.setStyle("-fx-font-weight: bold;");
        label_Feedback.setFont(Font.font("Arial", 14));
        label_Feedback.setLayoutX(centerX);
        label_Feedback.setLayoutY(contentTopY + 70);

        text_Feedback = new TextArea();
        text_Feedback.setWrapText(true);
        // Keep this moderate height so we don't push bottom band
        text_Feedback.setPrefSize(CENTER_COLUMN_WIDTH - 40, 180);
        text_Feedback.setLayoutX(centerX);
        text_Feedback.setLayoutY(contentTopY + 95);

        button_SaveGrade.setText("Save Grade");
        button_SaveGrade.setOnAction(e -> ControllerStaffGrading.saveGrade());
        button_SaveGrade.setFont(Font.font("Dialog", 16));
        button_SaveGrade.setMinWidth(140);
        button_SaveGrade.setLayoutX(centerX);
        button_SaveGrade.setLayoutY(contentTopY + 295);

        // ----- Right: Chat -----
        label_Chat.setStyle("-fx-font-weight: bold;");
        label_Chat.setFont(Font.font("Arial", 14));
        label_Chat.setLayoutX(rightX);
        label_Chat.setLayoutY(contentTopY);

        list_Chat = new ListView<>();
        setupVBox(list_Chat, rightX, contentTopY + 25,
                rightWidth, contentHeight - 80);

        text_Reply = new TextField();
        text_Reply.setPromptText("Type a reply...");
        text_Reply.setPrefWidth(rightWidth - 90);
        text_Reply.setLayoutX(rightX);
        text_Reply.setLayoutY(contentTopY + contentHeight - 45);

        button_Send.setText("Send");
        button_Send.setOnAction(e -> ControllerStaffGrading.sendReply());
        button_Send.setFont(Font.font("Dialog", 14));
        button_Send.setMinWidth(70);
        button_Send.setLayoutX(rightX + rightWidth - 80);
        button_Send.setLayoutY(contentTopY + contentHeight - 47);

        // ----- Footer (exactly like StaffPosts) -----
        setupButton(button_Back,   "Dialog", 18, 180, Pos.CENTER, 40, 560);
        button_Back.setOnAction(e ->
                guiStaff.ViewStaffHome.displayStaffHome(theStage, theUser));

        setupButton(button_Logout, "Dialog", 18, 180, Pos.CENTER, 250, 560);
        button_Logout.setOnAction(e -> ControllerStaffPosts.performLogout());

        setupButton(button_Quit,   "Dialog", 18, 180, Pos.CENTER, 460, 560);
        button_Quit.setOnAction(e -> ControllerStaffPosts.performQuit());

        // ----- Add everything to theRoot -----
        theRoot.getChildren().addAll(
                label_PageTitle, label_UserDetails, line_Separator1,
                label_Students, list_Students,
                label_Grade, text_Grade,
                label_Feedback, text_Feedback, button_SaveGrade,
                label_Chat, list_Chat, text_Reply, button_Send,
                button_Back, button_Logout, button_Quit
        );
    }

    // ---------- Helper Methods (same style as ViewStaffPosts) ----------

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

    private static void setupVBox(Region r, double x, double y, double w, double h) {
        r.setLayoutX(x);
        r.setLayoutY(y);
        r.setPrefSize(w, h);
        if (r instanceof Pane p) {
            p.setPadding(new Insets(5));
        }
    }
}
