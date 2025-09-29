package guiAdminHome;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;
import guiUserUpdate.ViewUserUpdate;

public class ViewAdminHome {
	
	/*-*******************************************************************************************
	Attributes
	*/
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	// GUI Area 1
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");
	private static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2
	public static Label label_NumberOfInvitations = 
			new Label("Number of Oustanding Invitations: x");
	protected static Label label_NumberOfUsers = new Label("Number of Users: x");
	private static Line line_Separator2 = new Line(20, 165, width-20, 165);

	// GUI Area 3: Invitations
	protected static Label label_Invitations = new Label("Send An Invitation");
	protected static Label label_InvitationEmailAddress = new Label("Email Address");
	protected static TextField text_InvitationEmailAddress = new TextField();
	
	// NEW: error label under the email input
	protected static Label label_EmailError = new Label("");
	
	protected static ComboBox<String> combobox_SelectRole = new ComboBox<String>();
	protected static String[] roles = {"Admin", "Student", "Staff"};
	protected static Button button_SendInvitation = new Button("Send Invitation");
	protected static Alert alertEmailError = new Alert(AlertType.INFORMATION);
	protected static Alert alertEmailSent = new Alert(AlertType.INFORMATION);
	private static Line line_Separator3 = new Line(20, 255, width-20, 255);

	// GUI Area 4
	protected static Button button_ManageInvitations = new Button("Manage Invitations");
	protected static Button button_SetOnetimePassword = new Button("Set a One-Time Password");
	protected static Button button_DeleteUser = new Button("Delete a User");
	protected static Button button_ListUsers = new Button("List All Users");
	protected static Button button_AddRemoveRoles = new Button("Add/Remove Roles");
	protected static Alert alertNotImplemented = new Alert(AlertType.INFORMATION);
	private static Line line_Separator4 = new Line(20, 525, width-20,525);

	// GUI Area 5
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	private static ViewAdminHome theView;
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	protected static Stage theStage;
	private static Pane theRootPane;
	protected static User theUser;
	private static Scene theAdminHomeScene;
	private static final int theRole = 1; // Admin

	/*-*******************************************************************************************
	Constructors
	*/
	public static void displayAdminHome(Stage ps, User user) {
		theStage = ps;
		theUser = user;
		
		if (theView == null) theView = new ViewAdminHome();
		
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;

		combobox_SelectRole.getSelectionModel().select(0);
				
		theStage.setTitle("CSE 360 Foundation Code: Admin Home Page");
		theStage.setScene(theAdminHomeScene);
		theStage.show();
	}
	
	private ViewAdminHome() {
		theRootPane = new Pane();
		theAdminHomeScene = new Scene(theRootPane, width, height);
	
		// GUI Area 1
		label_PageTitle.setText("Admin Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((event) -> 
				{ViewUserUpdate.displayUserUpdate(theStage, theUser);});
			
		// GUI Area 2
		setupLabelUI(label_NumberOfInvitations, "Arial", 20, 200, Pos.BASELINE_LEFT, 20, 105);
		label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	
		setupLabelUI(label_NumberOfUsers, "Arial", 20, 200, Pos.BASELINE_LEFT, 20, 135);
		label_NumberOfUsers.setText("Number of users: " + 
				theDatabase.getNumberOfUsers());
	
		// GUI Area 3
		setupLabelUI(label_Invitations, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 175);
		setupLabelUI(label_InvitationEmailAddress, "Arial", 16, width, Pos.BASELINE_LEFT, 20, 210);
		setupTextUI(text_InvitationEmailAddress, "Arial", 16, 360, Pos.BASELINE_LEFT, 130, 205, true);

		// NEW: Email error label, styled red, just under the input field
		label_EmailError.setTextFill(Color.RED);
		setupLabelUI(label_EmailError, "Arial", 12, 360, Pos.BASELINE_LEFT, 130, 230);
		label_EmailError.setText(""); // start empty

		setupComboBoxUI(combobox_SelectRole, "Dialog", 16, 90, 500, 205);
	
		List<String> list = new ArrayList<>();
		for (String role : roles) list.add(role);
		combobox_SelectRole.setItems(FXCollections.observableArrayList(list));
		combobox_SelectRole.getSelectionModel().select(0);
		alertEmailSent.setTitle("Invitation");
		alertEmailSent.setHeaderText("Invitation was sent");

		setupButtonUI(button_SendInvitation, "Dialog", 16, 150, Pos.CENTER, 630, 205);
		button_SendInvitation.setOnAction((event) -> {ControllerAdminHome.performInvitation(); });
	
		// GUI Area 4
		setupButtonUI(button_ManageInvitations, "Dialog", 16, 250, Pos.CENTER, 20, 270);
		button_ManageInvitations.setOnAction((event) -> {ControllerAdminHome.manageInvitations(); });
	
		setupButtonUI(button_SetOnetimePassword, "Dialog", 16, 250, Pos.CENTER, 20, 320);
		button_SetOnetimePassword.setOnAction((event) -> {ControllerAdminHome.setOnetimePassword(); });

		setupButtonUI(button_DeleteUser, "Dialog", 16, 250, Pos.CENTER, 20, 370);
		button_DeleteUser.setOnAction((event) -> {ControllerAdminHome.deleteUser(); });

		setupButtonUI(button_ListUsers, "Dialog", 16, 250, Pos.CENTER, 20, 420);
		button_ListUsers.setOnAction((event) -> {ControllerAdminHome.listUsers(); });

		setupButtonUI(button_AddRemoveRoles, "Dialog", 16, 250, Pos.CENTER, 20, 470);
		button_AddRemoveRoles.setOnAction((event) -> {ControllerAdminHome.addRemoveRoles(); });
		
		// GUI Area 5
		setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
		button_Logout.setOnAction((event) -> {ControllerAdminHome.performLogout(); });
    
		setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
		button_Quit.setOnAction((event) -> {ControllerAdminHome.performQuit(); });

		// Add everything to the root pane
		theRootPane.getChildren().addAll(
			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
    		label_NumberOfInvitations, label_NumberOfUsers,
    		line_Separator2,
    		label_Invitations, 
    		label_InvitationEmailAddress, text_InvitationEmailAddress, label_EmailError,
    		combobox_SelectRole, button_SendInvitation, line_Separator3,
    		button_ManageInvitations,
    		button_SetOnetimePassword,
    		button_DeleteUser,
    		button_ListUsers,
    		button_AddRemoveRoles,
    		line_Separator4, 
    		button_Logout,
    		button_Quit
    	);
	}

	/*-*******************************************************************************************
	Helper methods
	*/
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}	

	private void setupComboBoxUI(ComboBox <String> c, String ff, double f, double w, double x, double y){
		c.setStyle("-fx-font: " + f + " " + ff + ";");
		c.setMinWidth(w);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}
}
