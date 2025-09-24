package guiLastNameUpdate;

public class Model {

    public static String lastnameErrorMessage = "";
    public static int lastnameIndexofError = -1;
    private static String inputLine = "";
    private static char currentChar;
    private static int currentCharNdx;
    private static boolean running;
    private static int state = 0;
    private static int nextState = -1;

    public static String evaluateLastName(String input) {
        lastnameErrorMessage = "";
        lastnameIndexofError = 0;
        inputLine = input == null ? "" : input;
        currentCharNdx = 0;

        if (inputLine.isEmpty()) {
            return "Last name cannot be empty!\n";
        }

        if (inputLine.length() > 32) {
            return "The last name cannot be more than 32 characters!\n";
        }

        state = 0;
        running = true;
        currentChar = inputLine.charAt(0);

        System.out.println("\nState   Char   -> Next");

        while (running) {
            nextState = -1;
            switch (state) {
                case 0:
                    if (Character.isUpperCase(currentChar)) {
                        nextState = 1;
                    } 
                    else {
                        lastnameErrorMessage = "Last name must start with uppercase!\n";
                        running = false;
                    }
                    break;

                case 1:
                    if (Character.isLowerCase(currentChar)) {
                        nextState = 1;
                    } 
                    else if (currentChar == '-' || currentChar == '\'') {
                        nextState = 2;
                    } 
                    else if (Character.isUpperCase(currentChar)) {
                        lastnameErrorMessage = "Only the first letter can be uppercase!\n";
                        running = false;
                    } 
                    else {
                        lastnameErrorMessage = "Invalid character in last name!\n";
                        running = false;
                    }
                    break;

                case 2:
                    if (Character.isUpperCase(currentChar)) {
                        nextState = 1;
                    } 
                    else {
                        lastnameErrorMessage = "Hyphen/apostrophe must be followed by uppercase!\n";
                        running = false;
                    }
                    break;
            }

            if (running) {
                moveToNextCharacter();
                state = nextState;
            }
        }

        lastnameIndexofError = currentCharNdx;

        if (lastnameErrorMessage.isEmpty()) {
            if (state == 1) return "";
            if (state == 2) return "Last name cannot end with hyphen or apostrophe!\n";
        }

        return lastnameErrorMessage;
    }

    private static void moveToNextCharacter() {
        currentCharNdx++;
        if (currentCharNdx < inputLine.length()) {
            currentChar = inputLine.charAt(currentCharNdx);
        } 
        else {
            running = false;
        }
    }
}
