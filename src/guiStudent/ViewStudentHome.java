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
import guiStudentPosts.*;
import guiUserUpdate.ViewUserUpdate;

public class ViewStudentHome {

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // GUI Area 1
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();
    protected static Button button_UpdateThisUser = new Button("Account Update");

    protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

    // GUI Area 2: Discussion Thread button
    protected static Button button_OpenDiscussion = new Button("Open Discussion Thread");

    // GUI Area 3: Logout/Quit
    protected static Line line_Separator4 = new Line(20, 525, width-20,525);
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");

    private static ViewStudentHome theView;
    protected static Stage theStage;
    protected static Pane theRootPane;
    protected static User theUser;

    private static Scene theViewStudentHomeScene;
    protected static final int theRole = 2;  // Student role

    // -------------------------------------------------
    // Constructor and display method
    // -------------------------------------------------

    public static void displayStudentHome(Stage ps, User user) {
        theStage = ps;
        theUser = user;

        if (theView == null) theView = new ViewStudentHome();

        label_UserDetails.setText("User: " + theUser.getUserName());
        theStage.setTitle("CSE 360 Foundations: Student Home Page");
        theStage.setScene(theViewStudentHomeScene);
        theStage.show();
    }

    private ViewStudentHome() {
        theRootPane = new Pane();
        theViewStudentHomeScene = new Scene(theRootPane, width, height);

        // GUI Area 1
        label_PageTitle.setText("Student Home Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        label_UserDetails.setText("User: " + theUser.getUserName());
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
        button_UpdateThisUser.setOnAction(event ->
            ViewUserUpdate.displayUserUpdate(theStage, theUser)
        );

        // GUI Area 2: Open Discussion Thread
        setupButtonUI(button_OpenDiscussion, "Dialog", 18, 250, Pos.CENTER, 500, 150);
        button_OpenDiscussion.setOnAction(event -> openDiscussionThread());

        // GUI Area 3: Logout/Quit
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction(event -> ControllerStudentHome.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction(event -> ControllerStudentHome.performQuit());

        theRootPane.getChildren().addAll(
            label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
            button_OpenDiscussion, line_Separator4, button_Logout, button_Quit
        );
    }

    // -------------------------------------------------
    // Open Posts System
    // -------------------------------------------------
    private static void openDiscussionThread() {
        ControllerPosts controller = new ControllerPosts(theUser);
        controller.openDiscussionThread();
    }


    // -------------------------------------------------
    // Helper methods
    // -------------------------------------------------
    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);        
    }

    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);        
    }
}
