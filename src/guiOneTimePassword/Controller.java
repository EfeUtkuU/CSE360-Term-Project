package guiOneTimePassword;

import database.Database;


public class Controller {
	
	/*-********************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	
	/**********
	 * <p> Method: doSelectUser() </p>
	 * 
	 * <p> Description: This method uses the ComboBox widget, fetches which item in the ComboBox
	 * was selected (a user in this case), and establishes that user and the current user, setting
	 * easily accessible values without needing to do a query. </p>
	 * 
	 */
	protected static void doSelectUser() {
	    View.theSelectedUser = (String) View.combobox_SelectUser.getValue();

	    if (View.theSelectedUser != null && !View.theSelectedUser.equals("<Select a User>")) {
	        View.button_setOneTime.setVisible(true);
	    } 
	    else {
	        View.button_setOneTime.setVisible(false);
	    }
	    theDatabase.getUserAccountDetails(View.theSelectedUser);
	    repaintTheWindow();
	}
	
	/**********
	 * <p> Method: repaintTheWindow() </p>
	 * 
	 * <p> Description: This method determines the current state of the window and then establishes
	 * the appropriate list of widgets in the Pane to show the proper set of current values. </p>
	 * 
	 */
	protected static void repaintTheWindow() {
	    View.theRootPane.getChildren().clear();

	    if (View.theSelectedUser == null || View.theSelectedUser.equals("<Select a User>")) {
	        View.theRootPane.getChildren().addAll(
	            View.label_PageTitle, View.label_UserDetails, 
	            View.button_UpdateThisUser, View.line_Separator1,
	            View.label_SelectUser, View.combobox_SelectUser, 
	            View.line_Separator4, View.button_Return,
	            View.button_Logout, View.button_Quit
	        );
	    } 
	    else {
	        View.theRootPane.getChildren().addAll(
	            View.label_PageTitle, View.label_UserDetails,
	            View.button_UpdateThisUser, View.line_Separator1,
	            View.label_SelectUser, View.combobox_SelectUser,
	            View.button_setOneTime, View.label_oneTimePassword,
	            View.line_Separator4,
	            View.button_Return, View.button_Logout, View.button_Quit
	        );
	    }

	    View.theStage.setTitle("CSE 360 Foundation Code: Admin Operations Page");
	    View.theStage.setScene(View.theAddRemoveRolesScene);
	    View.theStage.show();
	}
	
	protected static void setOneTimePassword() {
	    String username = View.theSelectedUser;
	    if (username != null && !username.equals("<Select a User>")) {
	        String otp = theDatabase.generateOneTimePassword(username);
	        System.out.println("Generated OTP for " + username + ": " + otp);
	        View.label_oneTimePassword.setText("One-Time Password for " + username + ": " + otp + " (valid 5 min)");
	    }
	}

	
	
	/**********
	 * <p> Method: performReturn() </p>
	 * 
	 * <p> Description: This method returns the user (who must be an Admin as only admins are the
	 * only users who have access to this page) to the Admin Home page. </p>
	 * 
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(View.theStage,
				View.theUser);
	}
	
	
	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(View.theStage);
	}
	
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
