package guiLastNameUpdate;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Controller {

    static protected void handleButtonPress() {
        String lastName = View.input_NewLast.getText();

        String errMessage = Model.evaluateLastName(lastName);

        if (!errMessage.isEmpty()) {
            View.label_Error.setText(errMessage);
            View.label_Success.setText("");
        } 
        else {
            View.pressFinish();
            View.label_Error.setText("");
            View.label_Success.setText("Last name is valid!");
            System.out.println("Saving Last Name: " + lastName);
            View.input_NewLast.getScene().getWindow().hide();
            View.setFinishFalse();
        }
    }

    public static void openUpdateWindow(User theUser, Label label_CurrentLastName, Database theDatabase) {
        Stage lnStage = new Stage();
        lnStage.setTitle("Update Last Name");
        Pane lnRoot = new Pane();
        View.view(lnRoot);
        Scene lnScene = new Scene(lnRoot, 500, 250);
        lnStage.setScene(lnScene);

        lnStage.setOnHidden(e -> {
            if (View.isFinished()) {
                String newLast = View.getLastName();
                if (newLast != null && !newLast.isEmpty()) {
                    theUser.setLastName(newLast);
                    label_CurrentLastName.setText(newLast);
                    theDatabase.updateLastName(theUser.getUserName(), newLast);
                    System.out.println("Last name changed successfully!");
                }
            } 
            else {
                System.out.println("Last name change cancelled.");
            }
        });

        lnStage.show();
    }
}
