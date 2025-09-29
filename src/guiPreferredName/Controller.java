package guiPreferredName;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Controller {

    static protected void handleButtonPress() {
        String preferredName = View.input_NewPreferred.getText();

        // Run FSM validation from Model
        String errMessage = Model.evaluatePreferredName(preferredName);

        if (!errMessage.isEmpty()) {
            View.label_Error.setText(errMessage);
            View.label_Success.setText("");
        } 
        else {
            View.pressFinish();
            View.label_Error.setText("");
            View.label_Success.setText("Preferred name is valid!");
            System.out.println("Saving Preferred Name: " + preferredName);
            View.input_NewPreferred.getScene().getWindow().hide();
            View.setFinishFalse();
        }
    }

    public static void openUpdateWindow(User theUser, Label label_CurrentPreferred, Database theDatabase) {
        Stage pnStage = new Stage();
        pnStage.setTitle("Update Preferred Name");
        Pane pnRoot = new Pane();
        View.view(pnRoot);
        Scene pnScene = new Scene(pnRoot, 500, 250);
        pnStage.setScene(pnScene);

        pnStage.setOnHidden(e -> {
            if (View.isFinished()) {
                String newPreferred = View.getPreferredName();
                if (newPreferred != null) {
                    theUser.setPreferredFirstName(newPreferred);
                    label_CurrentPreferred.setText(newPreferred);
                    theDatabase.updatePreferredFirstName(theUser.getUserName(), newPreferred);
                    System.out.println("Preferred name changed successfully!");
                }
            } 
            else {
                System.out.println("Preferred name change cancelled.");
            }
        });

        pnStage.show();
    }
}
