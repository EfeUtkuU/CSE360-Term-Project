package guiFirstAdmin;

import java.sql.SQLException;
import database.Database;
import entityClasses.User;
import javafx.stage.Stage;


public class ControllerFirstAdmin {
	/*-********************************************************************************************
	
	The controller attributes for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	private static String adminUsername = "";
	private static String adminPassword1 = "";
	private static String adminPassword2 = "";		
	protected static Database theDatabase = applicationMain.FoundationsMain.database;		

	/*-********************************************************************************************
	
	The User Interface Actions for this page
	
	*/
	
	
	/**********
	 * <p> Method: setAdminUsername() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the username field in the
	 * View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminUsername() {
		adminUsername = ViewFirstAdmin.text_AdminUsername.getText();
	}
	
	
	/**********
	 * <p> Method: setAdminPassword1() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 1 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword1() {
		adminPassword1 = ViewFirstAdmin.text_AdminPassword1.getText();
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: setAdminPassword2() </p>
	 * 
	 * <p> Description: This method is called when the user adds text to the password 2 field in
	 * the View.  A private local copy of what was last entered is kept here.</p>
	 * 
	 */
	protected static void setAdminPassword2() {
		adminPassword2 = ViewFirstAdmin.text_AdminPassword2.getText();		
		ViewFirstAdmin.label_PasswordsDoNotMatch.setText("");
	}
	
	
	/**********
	 * <p> Method: doSetupAdmin() </p>
	 * 
	 * <p> Description: This method is called when the user presses the button to set up the Admin
	 * account.  It start by trying to establish a new user and placing that user into the
	 * database.  If that is successful, we proceed to the UserUpdate page.</p>
	 * 
	 */
	protected static void doSetupAdmin(Stage ps, int r) {
	    String adminUsername = ViewFirstAdmin.text_AdminUsername.getText();
	    String adminPassword1 = ViewFirstAdmin.text_AdminPassword1.getText();
	    String adminPassword2 = ViewFirstAdmin.text_AdminPassword2.getText();

	    System.out.println("DEBUG: Admin Username=" + adminUsername);
	    System.out.println("DEBUG: Password1=" + adminPassword1);
	    System.out.println("DEBUG: Password2=" + adminPassword2);

	    String userError = ModelFirstAdmin.checkForValidUserName(adminUsername);
	    if (!userError.isEmpty()) {
	        ViewFirstAdmin.label_PasswordsDoNotMatch.setText(userError);
	        return;
	    }

	    String passError = ModelFirstAdmin.evaluatePassword(adminPassword1);
	    if (!passError.isEmpty()) {
	        ViewFirstAdmin.label_PasswordsDoNotMatch.setText(passError);
	        return;
	    }

	    if (adminPassword1.equals(adminPassword2)) {
	        User user = new User(adminUsername, adminPassword1, "", "", "", "", "", true, false, false);
	        try {
	            theDatabase.register(user);
	        } catch (SQLException e) {
	            System.err.println("*** ERROR *** Database error trying to register a user: " + e.getMessage());
	            e.printStackTrace();
	            System.exit(0);
	        }

	        guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewFirstAdmin.theStage, user);
	    } else {
	        ViewFirstAdmin.text_AdminPassword1.setText("");
	        ViewFirstAdmin.text_AdminPassword2.setText("");
	        ViewFirstAdmin.label_PasswordsDoNotMatch.setText("The two passwords must match. Please try again!");
	    }
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
		System.out.println("Perform Quit");
		System.exit(0);
	}	
}

