package guiPasswordUpdate;

import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Controller {

    static protected void handleButtonPress() {
        String thePassword = View.text_Password.getText();
        System.out.println("Saving password: " + thePassword);
        View.text_Password.getScene().getWindow().hide();
    }

    // New method to open password update window
    public static void openUpdateWindow(User theUser, Label label_CurrentPassword, Database theDatabase) {
        Stage pwStage = new Stage();
        pwStage.setTitle("Update Password");
        Pane pwRoot = new Pane();
        guiPasswordUpdate.View.view(pwRoot);
        Scene pwScene = new Scene(pwRoot, 500, 500);
        pwStage.setScene(pwScene);

        pwStage.setOnHidden(e -> {
            if (guiPasswordUpdate.View.isFinishEnabled()) {
                String newPassword = guiPasswordUpdate.View.getPassword();
                if (newPassword != null && !newPassword.isEmpty()) {
                    theUser.setPassword(newPassword);
                    label_CurrentPassword.setText(newPassword);
                    theDatabase.updatePassword(theUser.getUserName(), newPassword);
                    System.out.println("Password changed successfully!");
                }
            } else {
                System.out.println("Password change cancelled.");
            }
        });

        pwStage.show();
    }
}
