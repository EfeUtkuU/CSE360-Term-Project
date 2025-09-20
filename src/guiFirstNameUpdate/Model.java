package guiFirstNameUpdate;

public class Model{
	
	protected static void updateFirstName() {
	    String firstName = View.input_NewFirst.getText();
	    
	    String errMessage = evaluateFirstName(firstName);
	    if (!errMessage.isEmpty()) {
	    	System.out.println(errMessage);

	    }
	    else {
	    	System.out.println("Success! The first name satisfies requirements!");
	        View.button_AcceptFirstName.setDisable(false);
	    }
	}

	protected static void showError(String message) {
	    javafx.scene.control.Alert alert =
	        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
	    alert.setTitle("Input Error");
	    alert.setHeaderText("Invalid First Name");
	    alert.setContentText(message);
	    alert.showAndWait();
	}
	
	public static String firstnameErrorMessage = "";		// The error message text
	public static String firstnameInput = "";			// The input being processed
	public static int firstnameIndexofError = -1;		// The index where the error was located
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;	
	
    private static boolean tooLong;
    private static boolean emptyInput;
    private static boolean invalidUppercase = false;
    private static boolean invalidUpperCaseStart = false;
    private static boolean invalidChar = false;
	
	public static String evaluateFirstName(String input) {
	    firstnameErrorMessage = "";
	    firstnameIndexofError = 0;
	    inputLine = input;
	    currentCharNdx = 0;
	    
	    tooLong = false;
	    emptyInput = false;
	    invalidUppercase = false;
	    invalidUpperCaseStart = false;

	    if (input == null || input.isEmpty()) {
	        emptyInput = true;
	        return "The name cannot be empty!\n";
	    }

	    if (input.length() > 32) {
	        tooLong = true;
	    }

	    running = true;
	    boolean expectUppercase = true;
	    currentChar = input.charAt(0);

	    while (running) {
	        if (Character.isLetter(currentChar)) {
	            if (Character.isUpperCase(currentChar)) {
	                if (!expectUppercase) {
	                    invalidUppercase = true;
	                }
	                expectUppercase = false;
	            } else if (Character.isLowerCase(currentChar)) {
	                if (expectUppercase) {
	                    invalidUpperCaseStart = true;
	                }
	            }
	        } else if (currentChar == ' ') {
	            expectUppercase = true;
	        } else {
	            invalidChar = true;
	        }

	        currentCharNdx++;
	        if (currentCharNdx >= inputLine.length()) {
	            running = false;
	        } else {
	            currentChar = input.charAt(currentCharNdx);
	        }
	    }

	    String errMessage = "";
	    if (tooLong) errMessage += "The name cannot be more than 32 characters!\n";
	    if (invalidUppercase) errMessage += "Only the first letter of each word can be uppercase!\n";
	    if (invalidUpperCaseStart) errMessage += "Each word must start with an uppercase letter!\n";
	    if (invalidChar) errMessage += "Digits and special characters are not allowed in names!\n";

	    return errMessage;
	}


	
}