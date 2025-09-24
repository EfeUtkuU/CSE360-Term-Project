package guiStaff;

public class ControllerStaffHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	**********************************************************************************************/
	
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewStaffHome.theStage, ViewStaffHome.theUser);
	}	

	
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffHome.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}

}
