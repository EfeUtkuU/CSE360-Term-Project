package guiFirstNameUpdate;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Controller {

	static protected void handleButtonPress() {
        String firstName = View.input_NewFirst.getText();

        String errMessage = Model.evaluateFirstName(firstName);

        if (!errMessage.isEmpty()) {
            View.label_Error.setText(errMessage);
            View.label_Success.setText("");
        } else {
            // Success
            View.pressFinish();
            View.label_Error.setText("");
            View.label_Success.setText("First name is valid!");
            System.out.println("Saving First Name: " + firstName);
            View.input_NewFirst.getScene().getWindow().hide();
            View.setFinishFalse();
        }
    }

    // New method to open first name update window
    public static void openUpdateWindow(User theUser, Label label_CurrentFirstName, Database theDatabase) {
        Stage fnStage = new Stage();
        fnStage.setTitle("Update First Name");
        Pane fnRoot = new Pane();
        View.view(fnRoot);
        Scene fnScene = new Scene(fnRoot, 500, 250);
        fnStage.setScene(fnScene);

        fnStage.setOnHidden(e -> {
            if (guiFirstNameUpdate.View.isFinished()) {
                String newFirstName = guiFirstNameUpdate.View.getFirstName();
                if (newFirstName != null && !newFirstName.isEmpty()) {
                    theUser.setFirstName(newFirstName);
                    label_CurrentFirstName.setText(newFirstName);
                    theDatabase.updateFirstName(theUser.getUserName(), newFirstName);
                    System.out.println("First name changed successfully!");
                }
            } else {
                System.out.println("First Name change cancelled.");
            }
        });

        fnStage.show();
    }
}
