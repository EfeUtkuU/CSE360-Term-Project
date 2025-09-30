package guiUserLogin;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;



public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/


	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	private static Stage theStage;	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid.  If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doLogin(Stage ts) {
	    theStage = ts;
	    String username = ViewUserLogin.text_Username.getText();
	    String passwordOrOtp = ViewUserLogin.text_Password.getText();
	    boolean loginResult = false;

	    // Basic input validation
	    String userError = Model.checkForValidUserName(username);
	    if (!userError.isEmpty()) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(userError);
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    String passError = Model.evaluatePassword(passwordOrOtp);
	    if (!passError.isEmpty()) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(passError);
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    // Fetch user details from DB
	    if (!theDatabase.getUserAccountDetails(username)) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	                "Incorrect username/password. Try again!");
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }
	    System.out.println("*** Username is valid");

	    // Validate password or OTP in one place
	    if (!theDatabase.loginWithPasswordOrOtp(username, passwordOrOtp)) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	                "Incorrect username/password. Try again!");
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }
	    System.out.println("*** Login credentials are valid");

	    // Create User object with details from DB
	    User user = new User(username, theDatabase.getCurrentPassword(),
	            theDatabase.getCurrentFirstName(),
	            theDatabase.getCurrentMiddleName(),
	            theDatabase.getCurrentLastName(),
	            theDatabase.getCurrentPreferredFirstName(),
	            theDatabase.getCurrentEmailAddress(),
	            theDatabase.getCurrentAdminRole(),
	            theDatabase.getCurrentNewStudent(),
	            theDatabase.getCurrentNewStaff());

	    // Decide which home page to show
	    int numberOfRoles = theDatabase.getNumberOfRoles(user);
	    System.out.println("*** The number of roles: " + numberOfRoles);

	    if (numberOfRoles == 1) {
	        if (user.getAdminRole()) {
	            loginResult = theDatabase.loginAdmin(user);
	            if (loginResult) guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
	        } else if (user.getNewStudent()) {
	            loginResult = theDatabase.loginStudent(user);
	            if (loginResult) guiStudent.ViewStudentHome.displayStudentHome(theStage, user);
	        } else if (user.getNewStaff()) {
	            loginResult = theDatabase.loginStaff(user);
	            if (loginResult) guiStaff.ViewStaffHome.displayStaffHome(theStage, user);
	        } else {
	            System.out.println("***** UserLogin goToUserHome request has an invalid role");
	        }
	    } else if (numberOfRoles > 1) {
	        System.out.println("*** Going to displayMultipleRoleDispatch");
	        guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(theStage, user);
	    }
	}

	
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content.</p>
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	

}
