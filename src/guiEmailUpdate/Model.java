package guiEmailUpdate;

public class Model {

	public static String emailAddressErrorMessage = "";	
	public static String emailAddressInput = "";		
	public static int emailAddressIndexofError = -1;	
	private static int state = 0;						
	private static int nextState = 0;					
	private static boolean finalState = false;			
	private static String inputLine = "";				
	private static char currentChar;					
	private static int currentCharNdx;					
	private static boolean running;						
	private static int domainPartCounter = 0;			
	private static int lastDotIndex = -1;                // track last dot for TLD

	private static String displayInput(String input, int currentCharNdx) {
		return input.substring(0,currentCharNdx) + "?\n";
	}

	private static void displayDebuggingInfo() {
		if (currentCharNdx >= inputLine.length())
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "None");
		else
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "  " + currentChar + " " + 
					((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") + 
					nextState + "     " + domainPartCounter);
	}

	private static void moveToNextCharacter() {
		currentCharNdx++;
		if (currentCharNdx < inputLine.length())
			currentChar = inputLine.charAt(currentCharNdx);
		else {
			System.out.println("End of input was found!");
			currentChar = ' ';
			running = false;
		}
	}

	public static String checkEmailAddress(String input) {
		state = 0;							
		inputLine = input;					
		currentCharNdx = 0;					

		emailAddressInput = input;			

		if (input.length() <= 0) {
			emailAddressErrorMessage = "There was no email address found.\n";
			return emailAddressErrorMessage + displayInput(input, 0);
		}
		currentChar = input.charAt(0);		

		if (input.length() > 255) {
			emailAddressErrorMessage = "A valid email address must be no more than 255 characters.\n";
			return emailAddressErrorMessage + displayInput(input, 255);
		}
		running = true;						
		System.out.println("\nCurrent Final Input  Next  DomainName\nState   State Char  State  Size");

		while (running) {
			nextState = -1;					
			
			switch (state) {
			case 0: 
				if ((currentChar >= 'A' && currentChar <= 'Z')|| 		
						(currentChar >= 'a' && currentChar <= 'z') ||	
						(currentChar >= '0' && currentChar <= '9')) {	
					nextState = 1;
				}
				else { 
					running = false;
				}
				break;				
			
			case 1: 
				if ((currentChar >= 'A' && currentChar <= 'Z')|| 		
						(currentChar >= 'a' && currentChar <= 'z') ||	
						(currentChar >= '0' && currentChar <= '9')) {	
					nextState = 1;
				}
				else if (currentChar == '@') {
					nextState = 2;
				}
				else if (currentChar == '.' || currentChar == '_' || currentChar == '-') {
					nextState = 0;  
					domainPartCounter = 0;
				}
				else {
					running = false;
				}
				break;
							
			case 2: 
				if (Character.isLowerCase(currentChar)) { // enforce lowercase for domain
					nextState = 3; 
					domainPartCounter = 1; 
				}
				else {
					running = false;
					emailAddressErrorMessage = "Domain part must start with a lowercase letter.\n";
				}
				break;
	
			case 3:
				if (Character.isLowerCase(currentChar)) {
					nextState = 3;
					domainPartCounter++;
					if (domainPartCounter > 63) {
						running = false;
						emailAddressErrorMessage = "A domain part may not exceed 63 characters.\n";
					}
				}
				else if (currentChar == '.') {
					// mark last dot index for TLD check
					lastDotIndex = currentCharNdx;
					nextState = 2; 
					domainPartCounter = 0;
				}
				else if (currentChar == '-') {
					nextState = 4; 
				}
				else {
					running = false;
				}
				break;

			case 4: 
				if (Character.isLowerCase(currentChar) || 
					(currentChar >= '0' && currentChar <= '9')) {
					nextState = 3; 
					domainPartCounter++;
				}
				else {
					running = false;
					emailAddressErrorMessage = "A hyphen must be followed by a lowercase letter or digit.\n";
				}
				break;
			}
			
			if (running) {
				displayDebuggingInfo();
				moveToNextCharacter();
				state = nextState;
				nextState = -1;
			}
		}
		displayDebuggingInfo();
		System.out.println("The loop has ended.");

		emailAddressIndexofError = currentCharNdx;		
		
		switch (state) {
		case 0:
			emailAddressErrorMessage = "May only be alphanumeric.\n";
			return emailAddressErrorMessage;

		case 1:
			emailAddressErrorMessage = "Local part ended prematurely, missing '@'.\n";
			return emailAddressErrorMessage + displayInput(input, currentCharNdx);

		case 2:
			emailAddressErrorMessage = "Domain part must begin with a lowercase letter.\n";
			return emailAddressErrorMessage + displayInput(input, currentCharNdx);

		case 3:
			// validate final TLD
			if (lastDotIndex == -1) {
				emailAddressErrorMessage = "Domain must contain a top-level domain (e.g., .com).\n";
				return emailAddressErrorMessage;
			}
			if (domainPartCounter < 2) {
				emailAddressErrorMessage = "Top-level domain must be at least 2 characters.\n";
				return emailAddressErrorMessage;
			}
			if (domainPartCounter > 24) {
				emailAddressErrorMessage = "Top-level domain is too long.\n";
				return emailAddressErrorMessage;
			}
			if (currentCharNdx < input.length()) {
				emailAddressErrorMessage = "Unexpected characters after domain.\n";
				return emailAddressErrorMessage + displayInput(input, currentCharNdx);
			}
			emailAddressIndexofError = -1;
			emailAddressErrorMessage = "";
			return emailAddressErrorMessage;

		case 4:
			emailAddressErrorMessage = "A hyphen must be followed by a lowercase letter or digit in the domain.\n";
			return emailAddressErrorMessage + displayInput(input, currentCharNdx);

		default:
			return "";
		}
	}
}
