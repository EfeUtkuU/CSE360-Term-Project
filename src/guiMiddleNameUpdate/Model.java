package guiMiddleNameUpdate;

public class Model {

    public static String middlenameErrorMessage = "";
    public static int middlenameIndexofError = -1;
    private static String inputLine = "";
    private static char currentChar;
    private static int currentCharNdx;
    private static boolean running;
    private static int state = 0;
    private static int nextState = -1;

    public static String evaluateMiddleName(String input) {
        middlenameErrorMessage = "";
        middlenameIndexofError = 0;
        inputLine = input == null ? "" : input;
        currentCharNdx = 0;


        if (inputLine.isEmpty()) {
            return "";
        }

        if (inputLine.length() > 32) {
            return "The middle name cannot be more than 32 characters!\n";
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
                        middlenameErrorMessage = "Middle name must start with uppercase!\n";
                        running = false;
                    }
                    break;

                case 1:
                    if (Character.isLowerCase(currentChar)) {
                        nextState = 1;
                    } 
                    else if (currentChar == ' ') {
                        nextState = 2;
                    } 
                    else if (Character.isUpperCase(currentChar)) {
                        middlenameErrorMessage = "Only the first letter can be uppercase!\n";
                        running = false;
                    } 
                    else {
                        middlenameErrorMessage = "Invalid character in middle name!\n";
                        running = false;
                    }
                    break;

                case 2:
                    if (Character.isUpperCase(currentChar)) {
                        nextState = 1;
                    } 
                    else {
                        middlenameErrorMessage = "Each middle name part must start uppercase!\n";
                        running = false;
                    }
                    break;
            }

            if (running) {
                moveToNextCharacter();
                state = nextState;
            }
        }

        middlenameIndexofError = currentCharNdx;

        if (middlenameErrorMessage.isEmpty()) {
            if (state == 1) return "";
            if (state == 2) return "Middle name cannot end with a space!\n";
        }

        return middlenameErrorMessage;
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
