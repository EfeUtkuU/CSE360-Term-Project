package guiMiddleNameUpdate;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Controller {

    static protected void handleButtonPress() {
        String middleName = View.input_NewMiddle.getText();

        String errMessage = Model.evaluateMiddleName(middleName);

        if (!errMessage.isEmpty()) {
            View.label_Error.setText(errMessage);
            View.label_Success.setText("");
        } 
        else {
            View.pressFinish();
            View.label_Error.setText("");
            View.label_Success.setText("Middle name is valid!");
            System.out.println("Saving Middle Name: " + middleName);
            View.input_NewMiddle.getScene().getWindow().hide();
            View.setFinishFalse();
        }
    }

    public static void openUpdateWindow(User theUser, Label label_CurrentMiddle, Database theDatabase) {
        Stage mnStage = new Stage();
        mnStage.setTitle("Update Middle Name");
        Pane mnRoot = new Pane();
        View.view(mnRoot);
        Scene mnScene = new Scene(mnRoot, 500, 250);
        mnStage.setScene(mnScene);

        mnStage.setOnHidden(e -> {
            if (View.isFinished()) {
                String newMiddle = View.getMiddleName();
                if (newMiddle != null) {
                    theUser.setMiddleName(newMiddle);
                    label_CurrentMiddle.setText(newMiddle);
                    theDatabase.updateMiddleName(theUser.getUserName(), newMiddle);
                    System.out.println("Middle name changed successfully!");
                }
            } 
            else {
                System.out.println("Middle name change cancelled.");
            }
        });

        mnStage.show();
    }
}
