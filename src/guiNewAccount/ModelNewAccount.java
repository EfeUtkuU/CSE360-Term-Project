package guiNewAccount;


/*******
 * <p> Title: ModelNewAccount Class. </p>
 * 
 * <p> Description: The NewAccount Page Model.  This class is not used as there is no
 * data manipulated by this MVC beyond accepting role information and saving it in the
 * database.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-15 Initial version
 *  
 */
public class ModelNewAccount {

	
	/*-********************************************************************************************
	 * 
	 * Attributes used by the Finite State Machine to inform the user about what was and was not
	 * valid and point to the character of the error.  This will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	public static boolean foundShortEnough = false;		// Flag for the 32 character maximization
	
	public static String usernameErrorMessage = "";
	public static String usernameInput = "";
	public static int usernameIndexofError = -1;
	private static int state = 0;
	private static int nextState = 0;
	private static boolean finalState = false;
	private static int usernameSize = 0;
	
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	/*
	 * This private method displays the input line and then on a line under it displays the input
	 * up to the point of the error.  At that point, a question mark is place and the rest of the 
	 * input is ignored. This method is designed to be used to display information to make it clear
	 * to the user where the error in the input can be found, and show that on the console 
	 * terminal.
	 * 
	 */

	private static void displayInputState() {
		// Display the entire input line
		System.out.println(inputLine);
		System.out.println(inputLine.substring(0,currentCharNdx) + "?");
		System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
				currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
	}
	
	
	/*
	 * This private method checks each of the requirements and if one is satisfied, it changes the
	 * the text to tell the user of this fact and changes the text color from red to green.
	 * 
	 */
	


	/**********
	 * <p> Title: evaluatePassword - Public Method </p>
	 * 
	 * <p> Description: This method is a mechanical transformation of a Directed Graph diagram 
	 * into a Java method. This method is used by both the GUI version of the application as well
	 * as the testing automation version.
	 * 
	 * @param input		The input string evaluated by the directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a helpful description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) {
			return "*** Error *** The password is empty!";
		}
		
		// Added length checker in order to verify that the password is no longer than 32 characters.
		if (input.length() > 32) {
			foundShortEnough = false; //explicit fail
			return "*** Error *** The password cannot be more than 32 characters!";
		}
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state.  This
		// local variable is a working copy of the input.
		passwordInput = input;				// Save a copy of the input
		
		// The following are the attributes associated with each of the requirements
		foundUpperCase = false;				// Reset the Boolean flag
		foundLowerCase = false;				// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundSpecialChar = false;			// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundLongEnough = false;			// Reset the Boolean flag
		foundShortEnough = input.length() <= 32;			// Reset the Boolean flag
		
		// This flag determines whether the directed graph (FSM) loop is operating or not
		running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some
		// state the current character does not match any valid transition
		while (running) {
			displayInputState();
			// The cascading if statement sequentially tries the current character against all of
			// the valid transitions, each associated with one of the requirements
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
				System.out.println("Special character found");
				foundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				return "*** Error *** An invalid character has been found!";
			}
			if (currentCharNdx >= 7) {
				System.out.println("At least 8 characters found");
				foundLongEnough = true;
			}
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
			
			System.out.println();
		}
		
		// Construct a String with a list of the requirement elements that were found.
		String errMessage = "";
		if (!foundUpperCase) {
			errMessage += "At least one upper case letter is needed!\n";
		}
		if (!foundLowerCase) {
			errMessage += "At least one lower case letter is needed!\n";
		}
		if (!foundNumericDigit) {
			errMessage += "At least one numeric digit is needed!\n";
		}
		if (!foundSpecialChar) {
			errMessage += "At least one special character is needed!\n";
		}
		if (!foundLongEnough)
			errMessage += "The password is not long enough!\n";
		
		if (!foundShortEnough) {
			errMessage += "The password is too long!\n";
		}
		if (errMessage == "")
			return "";
		
		// If it gets here, there something was not found, so return an appropriate message
		passwordIndexofError = currentCharNdx;
		return errMessage;
	}
	
	// Private method to display debugging data
		private static void displayDebuggingInfo() {
			// Display the current state of the FSM as part of an execution trace
			if (currentCharNdx >= inputLine.length())
				// display the line with the current state numbers aligned
				System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
						((finalState) ? "       F   " : "           ") + "None");
			else
				System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "  " + currentChar + " " + 
					((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") + 
					nextState + "     " + usernameSize);
		}
		
		// Private method to move to the next character within the limits of the input line
		private static void moveToNextCharacter() {
			currentCharNdx++;
			if (currentCharNdx < inputLine.length())
				currentChar = inputLine.charAt(currentCharNdx);
			else {
				currentChar = ' ';
				running = false;
			}
		}
	
	public static String checkForValidUserName(String input) {
		// Check to ensure that there is input to process
		if(input.length() <= 0) {
			usernameIndexofError = 0;	// Error at first character;
			return "\n*** ERROR *** The input is empty";
		}
		
		// The local variables used to perform the Finite State Machine simulation
		state = 0;							// This is the FSM state number
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		currentChar = input.charAt(0);		// The current character from above indexed position

		// The Finite State Machines continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

		usernameInput = input;	// Save a copy of the input
		running = true;						// Start the loop
		nextState = -1;						// There is no next state
		System.out.println("\nCurrent Final Input  Next  Date\nState   State Char  State  Size");
		
		// This is the place where semantic actions for a transition to the initial state occur
		
		usernameSize = 0;					// Initialize the UserName size

		// The Finite State Machines continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		while (running) {
			// The switch statement takes the execution to the code for the current state, where
			// that code sees whether or not the current character is valid to transition to a
			// next state
			switch (state) {
			case 0: 
				// State 0 has 1 valid transition that is addressed by an if statement.
				
				// The current character is checked against A-Z, a-z, 0-9. If any are matched
				// the FSM goes to state 1
				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' )) {	// Check for a-z
					nextState = 1;
					
					// Count the character 
					usernameSize++;
					
					// This only occurs once, so there is no need to check for the size getting
					// too large.
				}
				// If it is none of those characters, the FSM halts
				else 
					running = false;
				
				// The execution of this state is finished
				break;
			
			case 1: 
				// State 1 has two valid transitions, 
				//	1: a A-Z, a-z, 0-9 that transitions back to state 1
				//  2: a period that transitions to state 2 

				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the character
					usernameSize++;
				}
				// . -> State 2
				else if (currentChar == '.' || currentChar == '-' || currentChar == '_') {	// Check for . - _ between characters
					nextState = 2;
					
					// Count the .
					usernameSize++;
				}				
				// If it is none of those characters, the FSM halts
				else
					running = false;
				
				// The execution of this state is finished
				// If the size is larger than 16, the loop must stop
				if (usernameSize > 16)
					running = false;
				break;			
				
			case 2: 
				// State 2 deals with a character after a period in the name.
				
				// A-Z, a-z, 0-9 -> State 1
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||		// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||	// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {	// Check for 0-9
					nextState = 1;
					
					// Count the odd digit
					usernameSize++;
					
				}
				// If it is none of those characters, the FSM halts
				else 
					running = false;

				// The execution of this state is finished
				// If the size is larger than 16, the loop must stop
				if (usernameSize > 16)
					running = false;
				break;			
			}
			
			if (running) {
				displayDebuggingInfo();
				// When the processing of a state has finished, the FSM proceeds to the next
				// character in the input and if there is one, it fetches that character and
				// updates the currentChar.  If there is no next character the currentChar is
				// set to a blank.
				moveToNextCharacter();

				// Move to the next state
				state = nextState;
				
				// Is the new state a final state?  If so, signal this fact.
				if (state == 1) finalState = true;

				// Ensure that one of the cases sets this to a valid value
				nextState = -1;
			}
			// Should the FSM get here, the loop starts again
	
		}
		displayDebuggingInfo();
		
		System.out.println("The loop has ended.");
		
		// When the FSM halts, we must determine if the situation is an error or not.  That depends
		// of the current state of the FSM and whether or not the whole string has been consumed.
		// This switch directs the execution to separate code for each of the FSM states and that
		// makes it possible for this code to display a very specific error message to improve the
		// user experience.
		usernameIndexofError = currentCharNdx;	// Set index of a possible error;
		usernameErrorMessage = "\n*** ERROR *** ";
		
		// The following code is a slight variation to support just console output.
		switch (state) {
		case 0:
			// State 0 is not a final state, so we can return a very specific error message
			usernameErrorMessage += "A UserName must start with A-Z or a-z.\n"; // Changed the error log as well
			return usernameErrorMessage;

		case 1:
			// State 1 is a final state.  Check to see if the UserName length is valid.  If so we
			// we must ensure the whole string has been consumed.

			if (usernameSize < 4) {
				// UserName is too small
				usernameErrorMessage += "A UserName must have at least 4 characters.\n";
				return usernameErrorMessage;
			}
			else if (usernameSize > 16) {
				// UserName is too long
				usernameErrorMessage += 
					"A UserName must have no more than 16 characters.\n";
				return usernameErrorMessage;
			}
			else if (currentCharNdx < input.length()) {
				// There are characters remaining in the input, so the input is not valid
				usernameErrorMessage += 
					"A UserName character may only contain the characters A-Z, a-z, 0-9.\n";
				return usernameErrorMessage;
			}
			else {
					// UserName is valid
					usernameIndexofError = -1;
					usernameErrorMessage = "";
					return usernameErrorMessage;
			}

		case 2:
			// State 2 is not a final state, so we can return a very specific error message
			usernameErrorMessage +=
				"A UserName character after a period must be A-Z, a-z, 0-9.\n";
			return usernameErrorMessage;
			
		default:
			// This is for the case where we have a state that is outside of the valid range.
			// This should not happen
			return "";
		}
	}
}

