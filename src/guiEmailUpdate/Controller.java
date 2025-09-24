package guiEmailUpdate;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Controller {

    static protected void handleButtonPress() {
        String email = View.text_EmailAddress.getText();

        String errMessage = Model.checkEmailAddress(email);

        if (!errMessage.isEmpty()) {
            // FSM error → show error on popup
            View.label_errEmailAddress.setText(errMessage);
            View.validEmailAddress.setText("");
        } else {
            // Success
            View.pressFinish();
            View.label_errEmailAddress.setText("");
            View.validEmailAddress.setText("Email is valid!");
            System.out.println("Saving Email: " + email);
            View.text_EmailAddress.getScene().getWindow().hide();
            View.setFinishFalse();
        }
    }

    // New method to open email update window
    public static void openUpdateWindow(User theUser, Label label_CurrentEmail, Database theDatabase) {
        Stage emailStage = new Stage();
        emailStage.setTitle("Update Email Address");
        Pane emailRoot = new Pane();
        View.view(emailRoot);
        Scene emailScene = new Scene(emailRoot, 500, 250);
        emailStage.setScene(emailScene);

        emailStage.setOnHidden(e -> {
            if (View.isFinished()) {
                String newEmail = View.getEmailAddress();
                if (newEmail != null && !newEmail.isEmpty()) {
                    theUser.setEmailAddress(newEmail);
                    label_CurrentEmail.setText(newEmail);
                    theDatabase.updateEmailAddress(theUser.getUserName(), newEmail);
                    System.out.println("Email changed successfully!");
                }
            } else {
                System.out.println("Email change cancelled.");
            }
        });

        emailStage.show();
    }
}
