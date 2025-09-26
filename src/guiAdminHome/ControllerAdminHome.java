package guiAdminHome;

import database.Database;
import java.util.List;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 *  
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations() {
	    // Pull invitations from DB
	    List<String[]> invitations = theDatabase.getAllInvitations();

	    // New stage for invitation management
	    javafx.stage.Stage stage = new javafx.stage.Stage();
	    stage.setTitle("Manage Invitations");
	    stage.initOwner(ViewAdminHome.theStage);

	    javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
	    double y = 20;

	    // Header
	    javafx.scene.control.Label header = new javafx.scene.control.Label("Outgoing Invitations");
	    header.setFont(javafx.scene.text.Font.font("Arial", 20));
	    header.setLayoutX(20);
	    header.setLayoutY(y);
	    pane.getChildren().add(header);

	    y += 40;

	    // Column headers
	    javafx.scene.control.Label lblEmail = new javafx.scene.control.Label("Email");
	    lblEmail.setLayoutX(20); lblEmail.setLayoutY(y);

	    javafx.scene.control.Label lblRole = new javafx.scene.control.Label("Role");
	    lblRole.setLayoutX(250); lblRole.setLayoutY(y);

	    javafx.scene.control.Label lblAccepted = new javafx.scene.control.Label("Accepted");
	    lblAccepted.setLayoutX(350); lblAccepted.setLayoutY(y);

	    javafx.scene.control.Label lblAction = new javafx.scene.control.Label("Action");
	    lblAction.setLayoutX(450); lblAction.setLayoutY(y);

	    pane.getChildren().addAll(lblEmail, lblRole, lblAccepted, lblAction);

	    y += 30;

	    // Build one row per invitation
	    for (String[] inv : invitations) {
	        String code = inv[0];
	        String email = inv[1];
	        String role = inv[2];
	        String accepted = inv[3];

	        javafx.scene.control.Label emailLbl = new javafx.scene.control.Label(email);
	        emailLbl.setLayoutX(20); emailLbl.setLayoutY(y);

	        javafx.scene.control.Label roleLbl = new javafx.scene.control.Label(role);
	        roleLbl.setLayoutX(250); roleLbl.setLayoutY(y);

	        javafx.scene.control.Label acceptedLbl = new javafx.scene.control.Label(accepted);
	        acceptedLbl.setLayoutX(350); acceptedLbl.setLayoutY(y);

	        javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("Delete");
	        deleteBtn.setLayoutX(450); deleteBtn.setLayoutY(y - 5);

	        // Handle delete
	        deleteBtn.setOnAction(e -> {
	            theDatabase.removeInvitationAfterUse(code);
	            pane.getChildren().removeAll(emailLbl, roleLbl, acceptedLbl, deleteBtn);
	            ViewAdminHome.label_NumberOfInvitations.setText(
	                "Number of outstanding invitations: " + theDatabase.getNumberOfInvitations()
	            );
	        });

	        pane.getChildren().addAll(emailLbl, roleLbl, acceptedLbl, deleteBtn);
	        y += 30;
	    }

	    // Show scene
	    javafx.scene.Scene scene = new javafx.scene.Scene(pane, 600, 400);
	    stage.setScene(scene);
	    stage.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void setOnetimePassword () {
		System.out.println("\n*** WARNING ***: One-Time Password Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("One-Time Password Issue");
		ViewAdminHome.alertNotImplemented.setContentText("One-Time Password Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void deleteUser() {
		List<String[]> users = theDatabase.getAllUsers();

	    Stage stage = new Stage();
	    stage.setTitle("Delete Users");
	    stage.initOwner(ViewAdminHome.theStage);

	    Pane pane = new Pane();
	    double y = 20;

	    // Header
	    Label header = new Label("Select a User to Delete");
	    header.setFont(javafx.scene.text.Font.font("Arial", 20));
	    header.setLayoutX(20);
	    header.setLayoutY(y);
	    pane.getChildren().add(header);

	    y += 40;

	    // Column headers
	    Label lblUser = new Label("Username");
	    lblUser.setLayoutX(20); lblUser.setLayoutY(y);

	    Label lblName = new Label("Name");
	    lblName.setLayoutX(150); lblName.setLayoutY(y);

	    Label lblEmail = new Label("Email");
	    lblEmail.setLayoutX(300); lblEmail.setLayoutY(y);

	    Label lblRoles = new Label("Roles");
	    lblRoles.setLayoutX(500); lblRoles.setLayoutY(y);

	    Label lblAction = new Label("Action");
	    lblAction.setLayoutX(650); lblAction.setLayoutY(y);

	    pane.getChildren().addAll(lblUser, lblName, lblEmail, lblRoles, lblAction);

	    y += 30;

	    // Add rows
	    for (String[] u : users) {
	        String username = u[0];
	        String name = u[1] + " " + u[2];
	        String email = u[3];
	        String roles = String.join(" ", u[4], u[5], u[6]).trim();

	        Label userLbl = new Label(username);
	        userLbl.setLayoutX(20); userLbl.setLayoutY(y);

	        Label nameLbl = new Label(name);
	        nameLbl.setLayoutX(150); nameLbl.setLayoutY(y);

	        Label emailLbl = new Label(email);
	        emailLbl.setLayoutX(300); emailLbl.setLayoutY(y);

	        Label rolesLbl = new Label(roles);
	        rolesLbl.setLayoutX(500); rolesLbl.setLayoutY(y);

	        Button deleteBtn = new Button("Delete");
	        deleteBtn.setLayoutX(650); deleteBtn.setLayoutY(y - 5);

	        deleteBtn.setOnAction(e -> {
	            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
	                    "Are you sure you want to delete user '" + username + "'?",
	                    ButtonType.YES, ButtonType.NO);
	            confirm.showAndWait().ifPresent(response -> {
	                if (response == ButtonType.YES) {
	                    boolean success = theDatabase.deleteUser(username);
	                    Alert result = new Alert(Alert.AlertType.INFORMATION);
	                    if (success) {
	                        result.setTitle("User Deleted");
	                        result.setHeaderText("Success");
	                        result.setContentText("User '" + username + "' was deleted.");
	                        result.showAndWait();
	                        stage.close();
	                        deleteUser(); // reopen to refresh
	                    } else {
	                        result.setTitle("Delete Failed");
	                        result.setHeaderText("Error");
	                        result.setContentText("Could not delete user: " + username);
	                        result.showAndWait();
	                    }
	                }
	            });
	        });

	        pane.getChildren().addAll(userLbl, nameLbl, emailLbl, rolesLbl, deleteBtn);
	        y += 25;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void listUsers() {
		 List<String[]> users = theDatabase.getAllUsers();

		    javafx.stage.Stage stage = new javafx.stage.Stage();
		    stage.setTitle("List of Users");
		    stage.initOwner(ViewAdminHome.theStage);

		    javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane();
		    double y = 20;

		    // Header
		    javafx.scene.control.Label header = new javafx.scene.control.Label("All Registered Users");
		    header.setFont(javafx.scene.text.Font.font("Arial", 20));
		    header.setLayoutX(20);
		    header.setLayoutY(y);
		    pane.getChildren().add(header);

		    y += 40;

		    // Column headers
		    javafx.scene.control.Label lblUser = new javafx.scene.control.Label("Username");
		    lblUser.setLayoutX(20); lblUser.setLayoutY(y);

		    javafx.scene.control.Label lblName = new javafx.scene.control.Label("Name");
		    lblName.setLayoutX(150); lblName.setLayoutY(y);

		    javafx.scene.control.Label lblEmail = new javafx.scene.control.Label("Email");
		    lblEmail.setLayoutX(300); lblEmail.setLayoutY(y);

		    javafx.scene.control.Label lblRoles = new javafx.scene.control.Label("Roles");
		    lblRoles.setLayoutX(500); lblRoles.setLayoutY(y);

		    pane.getChildren().addAll(lblUser, lblName, lblEmail, lblRoles);

		    y += 30;

		    // Add rows
		    for (String[] u : users) {
		        String username = u[0];
		        String name = u[1] + " " + u[2];
		        String email = u[3];
		        String roles = String.join(" ", u[4], u[5], u[6]).trim();

		        javafx.scene.control.Label userLbl = new javafx.scene.control.Label(username);
		        userLbl.setLayoutX(20); userLbl.setLayoutY(y);

		        javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(name);
		        nameLbl.setLayoutX(150); nameLbl.setLayoutY(y);

		        javafx.scene.control.Label emailLbl = new javafx.scene.control.Label(email);
		        emailLbl.setLayoutX(300); emailLbl.setLayoutY(y);

		        javafx.scene.control.Label rolesLbl = new javafx.scene.control.Label(roles);
		        rolesLbl.setLayoutX(500); rolesLbl.setLayoutY(y);

		        pane.getChildren().addAll(userLbl, nameLbl, emailLbl, rolesLbl);

		        y += 25;
		    }

		    // Close button
		    javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
		    closeBtn.setLayoutX(250);
		    closeBtn.setLayoutY(y + 20);
		    closeBtn.setOnAction(e -> stage.close());
		    pane.getChildren().add(closeBtn);

		    javafx.scene.Scene scene = new javafx.scene.Scene(pane, 700, 400);
		    stage.setScene(scene);
		    stage.showAndWait();
		
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
