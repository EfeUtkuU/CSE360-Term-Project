package guiPreferredName;

public class Model {

    public static String preferredErrorMessage = "";
    public static int preferredIndexOfError = -1;
    private static String inputLine = "";
    private static char currentChar;
    private static int currentCharNdx;
    private static boolean running;
    private static int state = 0;
    private static int nextState = -1;

    public static String evaluatePreferredName(String input) {
        preferredErrorMessage = "";
        preferredIndexOfError = 0;
        inputLine = input == null ? "" : input.trim();
        currentCharNdx = 0;

        if (inputLine.isEmpty()) {
            return "Preferred name cannot be empty!\n";
        }

        if (inputLine.length() > 32) {
            return "The preferred name cannot be more than 32 characters!\n";
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
                    } else {
                        preferredErrorMessage = "Preferred name must start with uppercase!\n";
                        running = false;
                    }
                    break;

                case 1:
                    if (Character.isLowerCase(currentChar)) {
                        nextState = 1; // keep reading lowercase letters
                    } else if (Character.isUpperCase(currentChar)) {
                        preferredErrorMessage = "Only the first letter can be uppercase!\n";
                        running = false;
                    } else if (Character.isDigit(currentChar)) {
                        preferredErrorMessage = "Preferred name cannot contain numbers!\n";
                        running = false;
                    } else if (!Character.isLetter(currentChar)) {
                        preferredErrorMessage = "Preferred name cannot contain special characters or spaces!\n";
                        running = false;
                    }
                    break;
            }

            if (running) {
                moveToNextCharacter();
                state = nextState;
            }
        }

        preferredIndexOfError = currentCharNdx;

        if (preferredErrorMessage.isEmpty()) {
            if (state == 1) return "";
            return "Preferred name ended in an invalid state!\n";
        }

        return preferredErrorMessage;
    }

    private static void moveToNextCharacter() {
        currentCharNdx++;
        if (currentCharNdx < inputLine.length()) {
            currentChar = inputLine.charAt(currentCharNdx);
        } else {
            running = false;
        }
    }
}
