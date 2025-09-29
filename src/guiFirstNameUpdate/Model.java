package guiFirstNameUpdate;

public class Model {

    public static String firstnameErrorMessage = "";     // The error message text
    public static String firstnameInput = "";           // The input being processed
    public static int firstnameIndexofError = -1;       // The index where the error was located
    private static String inputLine = "";               // The input line
    private static char currentChar;                    // The current character in the line
    private static int currentCharNdx;                  // The index of the current character
    private static boolean running;	
    private static int state = 0;
    private static int nextState = -1;

    protected static void updateFirstName() {
        String firstName = View.input_NewFirst.getText();
        String errMessage = evaluateFirstName(firstName);

        if (!errMessage.isEmpty()) {
            System.out.println(errMessage);
        } 
        else {
            System.out.println("Success! The first name satisfies requirements!");
        }
    }

    private static void displayDebuggingInfo() {
        if (currentCharNdx >= inputLine.length()) {
            System.out.println(" " + state + "       " + "None");
        } 
        else {
            System.out.println(" " + state + "   " + currentChar + " -> " + nextState);
        }
    }

    private static void moveToNextCharacter() {
        currentCharNdx++;
        if (currentCharNdx < inputLine.length()) {
            currentChar = inputLine.charAt(currentCharNdx);
        } 
        else {
            running = false; // end of input
        }
    }

    public static String evaluateFirstName(String input) {
        firstnameErrorMessage = "";
        firstnameIndexofError = 0;
        inputLine = input == null ? "" : input;
        currentCharNdx = 0;

        if (inputLine.isEmpty()) {
            return "The name cannot be empty!\n";
        }

        if (inputLine.length() > 32) {
            return "The name cannot be more than 32 characters!\n";
        }

        state = 0; // start state
        running = true;
        currentChar = inputLine.charAt(0);

        System.out.println("\nState   Char   -> Next");

        while (running) {
            nextState = -1;

            switch (state) {
                case 0:
                    if (Character.isUpperCase(currentChar)) {
                        nextState = 1; // valid start
                    } else {
                        firstnameErrorMessage = "The name must start with an uppercase letter!\n";
                        running = false;
                    }
                    break;

                case 1:
                    if (Character.isLowerCase(currentChar)) {
                        nextState = 1; // keep reading lowercase letters
                    } else if (Character.isUpperCase(currentChar)) {
                        firstnameErrorMessage = "Only the first letter can be uppercase!\n";
                        running = false;
                    } else if (Character.isDigit(currentChar)) {
                        firstnameErrorMessage = "Digits are not allowed in names!\n";
                        running = false;
                    } else if (!Character.isLetter(currentChar)) {
                        firstnameErrorMessage = "Special characters and spaces are not allowed in names!\n";
                        running = false;
                    }
                    break;
            }

            displayDebuggingInfo();

            if (running) {
                moveToNextCharacter();
                state = nextState;
            }
        }

        firstnameIndexofError = currentCharNdx;

        if (firstnameErrorMessage.isEmpty()) {
            if (state == 1) {
                return ""; // valid
            } else {
                return "Invalid name format!\n";
            }
        }

        return firstnameErrorMessage;
    }
}
