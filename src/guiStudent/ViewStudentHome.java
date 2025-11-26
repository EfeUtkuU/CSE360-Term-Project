package guiStudent;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import entityClasses.User;
import guiUserUpdate.ViewUserUpdate;

/**
 * <p><strong>Class:</strong> ViewStudentHome</p>
 *
 * <p><strong>Description:</strong>
 * JavaFX GUI for the Student Home Page. Displays account details and
 * provides navigation to account update, discussion thread, logout,
 * and quit options.  
 * Layout matches the Staff Home page for UI consistency.
 * </p>
 *
 * @version 2.00 — 2025-10-26 (Added “Open Discussion Thread” button)
 */
public class ViewStudentHome {

    private static double width  = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // GUI Area 1
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();
    protected static Button button_UpdateThisUser = new Button("Account Update");
    protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

    // GUI Area 2 — Discussion Thread Button
    protected static Button button_OpenDiscussion = new Button("Open Discussion Thread");
    protected static Button button_ViewGrade = new Button("View Grade & Feedback");

    // GUI Area 3
    protected static Line line_Separator4 = new Line(20, 525, width-20, 525);
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");

    private static ViewStudentHome theView;
    protected static Stage theStage;
    protected static Pane theRootPane;
    protected static User theUser;
    private static Scene theScene;
    protected static final int theRole = 2;

    /**
     * Displays the Student Home page.
     *
     * @param ps the JavaFX Stage
     * @param user the current student user
     */
    public static void displayStudentHome(Stage ps, User user) {
        theStage = ps;
        theUser = user;

        if (theView == null) theView = new ViewStudentHome();

        label_UserDetails.setText("User: " + theUser.getUserName());
        theStage.setTitle("CSE 360 Foundations: Student Home Page");
        theStage.setScene(theScene);
        theStage.show();
    }

    /** Initializes the UI elements for the Student Home page (singleton). */
    private ViewStudentHome() {
        theRootPane = new Pane();
        theScene = new Scene(theRootPane, width, height);

        // Area 1
        label_PageTitle.setText("Student Home Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        label_UserDetails.setText("User: " + theUser.getUserName());
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
        button_UpdateThisUser.setOnAction(e -> ViewUserUpdate.displayUserUpdate(theStage, theUser));

        // Area 2 — Discussion Thread
        setupButtonUI(button_OpenDiscussion, "Dialog", 18, 250, Pos.CENTER, 500, 150);
        button_OpenDiscussion.setOnAction(e -> ControllerStudentHome.performOpenDiscussion(theStage, theUser));
        
        setupButtonUI(button_ViewGrade, "Dialog", 18, 250, Pos.CENTER, 500, 220);
        button_ViewGrade.setOnAction(e -> guiStudentGrading.ControllerStudentGrading.displayStudentGrading(theStage, theUser));


        // Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction(e -> ControllerStudentHome.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction(e -> ControllerStudentHome.performQuit());

        theRootPane.getChildren().addAll(
                label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
                button_OpenDiscussion, button_ViewGrade, line_Separator4, button_Logout, button_Quit);
    }

    // Helpers
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
