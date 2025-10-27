package guiStudent;

public class ControllerStudentHome {

    /*-*******************************************************************************************
    User Interface Actions for this page
    This controller is not a class that gets instantiated. Rather, it is a collection of protected
    static methods that can be called by the View (which is a singleton instantiated object) and 
    the Model is often just a stub, or will be a singleton instantiated object.
    */

    /**********
     * <p> Method: performLogout() </p>
     * Logs out the current user and shows the login page
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentHome.theStage);
    }

    /**********
     * <p> Method: performQuit() </p>
     * Terminates the execution of the program
     */    
    protected static void performQuit() {
        System.exit(0);
    }
}
