package guiAdminHome;

import database.Database;


  // for the list of users


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
	    String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();

	    // Validate email with FSM (if invalid, stop here)
	    if (invalidEmailAddress(emailAddress)) {
	        return;
	    }

	    // Check if already used
	    if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
	        ViewAdminHome.label_EmailError.setText(
	            "An invitation has already been sent to this email address.");
	        return;
	    }

	    // Clear error label
	    ViewAdminHome.label_EmailError.setText("");

	    // Normal flow...
	    String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
	    String invitationCode = theDatabase.generateInvitationCode(emailAddress, theSelectedRole);
	    String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
	                 " was sent to: " + emailAddress;
	    System.out.println(msg);
	    ViewAdminHome.alertEmailSent.setContentText(msg);
	    ViewAdminHome.alertEmailSent.showAndWait();

	    // Reset input
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
	    guiManageInvitations.ControllerManageInvitations.performManageInvitations(ViewAdminHome.theStage);
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
		guiOneTimePassword.View.displayOneTimePassword(ViewAdminHome.theStage, ViewAdminHome.theUser);
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
	    guiDeleteUsers.ControllerDeleteUsers.performDeleteUser(ViewAdminHome.theStage);
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
	    guiListUsers.ControllerListUsers.performListUsers(ViewAdminHome.theStage);
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
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Uses FSM to validate an email address and update the error label. </p>
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
	    // Run FSM validation
	    String errMessage = guiEmailUpdate.Model.checkEmailAddress(emailAddress);

	    if (!errMessage.isEmpty()) {
	        // Display the FSM’s error message in red under the text field
	        ViewAdminHome.label_EmailError.setText(errMessage);
	        return true;
	    }

	    // Clear any old error messages
	    ViewAdminHome.label_EmailError.setText("");
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