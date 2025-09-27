package guiManageInvitations;

import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import guiAdminHome.ViewAdminHome;

public class ViewManageInvitations {

    public static void displayManageInvitations(Stage ownerStage, List<String[]> invitations) {
        Stage stage = new Stage();
        stage.setTitle("Manage Invitations");
        stage.initOwner(ownerStage);

        Pane pane = new Pane();
        double y = 20;

        // Header
        Label header = new Label("Outgoing Invitations");
        header.setFont(Font.font("Arial", 20));
        header.setLayoutX(20);
        header.setLayoutY(y);
        pane.getChildren().add(header);

        y += 40;

        // Column headers
        Label lblEmail = new Label("Email");
        lblEmail.setLayoutX(20); lblEmail.setLayoutY(y);

        Label lblRole = new Label("Role");
        lblRole.setLayoutX(250); lblRole.setLayoutY(y);

        Label lblAccepted = new Label("Accepted");
        lblAccepted.setLayoutX(350); lblAccepted.setLayoutY(y);

        Label lblAction = new Label("Action");
        lblAction.setLayoutX(450); lblAction.setLayoutY(y);

        pane.getChildren().addAll(lblEmail, lblRole, lblAccepted, lblAction);

        y += 30;

        // Build one row per invitation
        for (String[] inv : invitations) {
            String code = inv[0];
            String email = inv[1];
            String role = inv[2];
            String accepted = inv[3];

            Label emailLbl = new Label(email);
            emailLbl.setLayoutX(20); emailLbl.setLayoutY(y);

            Label roleLbl = new Label(role);
            roleLbl.setLayoutX(250); roleLbl.setLayoutY(y);

            Label acceptedLbl = new Label(accepted);
            acceptedLbl.setLayoutX(350); acceptedLbl.setLayoutY(y);

            Button deleteBtn = new Button("Delete");
            deleteBtn.setLayoutX(450); deleteBtn.setLayoutY(y - 5);

            // Handle delete
            deleteBtn.setOnAction(e -> {
                ControllerManageInvitations.handleDelete(code);

                // Remove row from UI
                pane.getChildren().removeAll(emailLbl, roleLbl, acceptedLbl, deleteBtn);

                // Update invitation counter in Admin Home
                ViewAdminHome.label_NumberOfInvitations.setText(
                    "Number of outstanding invitations: " + 
                    ControllerManageInvitations.getInvitationCount()
                );
            });

            pane.getChildren().addAll(emailLbl, roleLbl, acceptedLbl, deleteBtn);
            y += 30;
        }

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setLayoutX(250);
        closeBtn.setLayoutY(y + 20);
        closeBtn.setOnAction(e -> ControllerManageInvitations.performClose(stage));
        pane.getChildren().add(closeBtn);

        Scene scene = new Scene(pane, 600, 400);
        stage.setScene(scene);
        stage.showAndWait();
    }
}