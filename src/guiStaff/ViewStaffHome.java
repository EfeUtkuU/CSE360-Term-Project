package guiStaff;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;
import guiStaffGrading.ControllerStaffGrading;
import guiStaffStats.ViewStaffStatistics;


/**
 * <p><strong>Class:</strong> ViewStaffHome</p>
 *
 * <p><strong>Description:</strong>
 * Graphical UI for the Staff Home Page. Displays account info and
 * navigation options for the staff user, including account update,
 * logout, quit, and opening the staff discussion (posts) page.
 * </p>
 *
 * @version 2.00 — 2025-10-26 (Added “Open Discussion Thread” button)
 */
public class ViewStaffHome {

    private static double width  = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // GUI Area 1
    protected static Label  label_PageTitle      = new Label();
    protected static Label  label_UserDetails    = new Label();
    protected static Button button_UpdateThisUser= new Button("Account Update");
    protected static Line   line_Separator1      = new Line(20, 95, width-20, 95);

    // GUI Area 2 — Discussion Thread Access
    protected static Button button_OpenDiscussion= new Button("Open Discussion Thread");
    
    // Student Statistics Page Access
    protected static Button button_ViewStudentStatistics = new Button("View Student Statistics");
    
    // Student Grading Page Access
    protected static Button button_OpenGrading = new Button("Grade Students");




    // GUI Area 3
    protected static Line   line_Separator4      = new Line(20, 525, width-20, 525);
    protected static Button button_Logout        = new Button("Logout");
    protected static Button button_Quit          = new Button("Quit");

    // State / Scene Data
    private static ViewStaffHome theView;
    private static Database      theDatabase = applicationMain.FoundationsMain.database;
    protected static Stage       theStage;
    protected static Pane        theRootPane;
    protected static User        theUser;
    private static Scene         theStaffHomeScene;
    protected static final int   theRole = 3; // Admin: 1; Student: 2; Staff: 3

    /**
     * Displays the Staff Home page.
     *
     * @param ps the Stage to display on
     * @param user the logged-in user
     */
    public static void displayStaffHome(Stage ps, User user) {
        theStage = ps;
        theUser  = user;

        if (theView == null) theView = new ViewStaffHome();

        theDatabase.getUserAccountDetails(user.getUserName());
        applicationMain.FoundationsMain.activeHomePage = theRole;

        label_UserDetails.setText("User: " + theUser.getUserName());
        theStage.setTitle("CSE 360 Foundations: Staff Home Page");
        theStage.setScene(theStaffHomeScene);
        theStage.show();
    }

    /** Initializes the UI layout and components (only once). */
    private ViewStaffHome() {
        theRootPane = new Pane();
        theStaffHomeScene = new Scene(theRootPane, width, height);

        // Area 1
        label_PageTitle.setText("Staff Home Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        label_UserDetails.setText("User: " + theUser.getUserName());
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
        button_UpdateThisUser.setOnAction(e -> ControllerStaffHome.performUpdate());

        // Area 2 — Discussion Thread Access
        setupButtonUI(button_OpenDiscussion, "Dialog", 18, 250, Pos.CENTER, 500, 150);
        button_OpenDiscussion.setOnAction(e -> ControllerStaffHome.performOpenDiscussion(theStage, theUser));
        
        setupButtonUI(button_ViewStudentStatistics, "Dialog", 18, 250, Pos.CENTER, 500, 220);
        button_ViewStudentStatistics.setOnAction(e -> 
                ViewStaffStatistics.display(theStage, theUser)
        );
        
        setupButtonUI(button_OpenGrading, "Dialog", 18, 250, Pos.CENTER, 500, 290);
        button_OpenGrading.setOnAction(e ->
                ControllerStaffGrading.displayGradingPage(theStage, theUser)
        );



        // Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction(e -> ControllerStaffHome.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction(e -> ControllerStaffHome.performQuit());

        theRootPane.getChildren().addAll(
                label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
                button_OpenDiscussion, button_ViewStudentStatistics, button_OpenGrading,
                line_Separator4, button_Logout, button_Quit);


    }

    // Helper methods
    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
}
