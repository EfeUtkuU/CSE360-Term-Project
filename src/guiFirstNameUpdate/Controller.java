package guiFirstNameUpdate;

public class Controller {

    static protected void handleButtonPress() {
        String firstName = View.input_NewFirst.getText();

        String errMessage = Model.evaluateFirstName(firstName);

        if (!errMessage.isEmpty()) {
            Model.showError(errMessage);
        } else {
        	View.pressFinish();
            System.out.println("Saving First Name: " + firstName);
            View.input_NewFirst.getScene().getWindow().hide();
        }
    }
}