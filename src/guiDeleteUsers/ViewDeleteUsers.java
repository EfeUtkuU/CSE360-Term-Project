package guiDeleteUsers;

import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ViewDeleteUsers {

    public static void displayDeleteUsers(Stage ownerStage, List<String[]> users) {
        Stage stage = new Stage();
        stage.setTitle("Delete Users");
        stage.initOwner(ownerStage);

        Pane pane = new Pane();
        double y = 20;

        // Header
        Label header = new Label("Select a User to Delete");
        header.setFont(Font.font("Arial", 20));
        header.setLayoutX(20);
        header.setLayoutY(y);
        pane.getChildren().add(header);

        y += 40;

        // Column headers
        Label lblUser = new Label("Username");
        lblUser.setLayoutX(20); lblUser.setLayoutY(y);

        Label lblName = new Label("Name");
        lblName.setLayoutX(150); lblName.setLayoutY(y);

        Label lblEmail = new Label("Email");
        lblEmail.setLayoutX(300); lblEmail.setLayoutY(y);

        Label lblRoles = new Label("Roles");
        lblRoles.setLayoutX(500); lblRoles.setLayoutY(y);

        Label lblAction = new Label("Action");
        lblAction.setLayoutX(650); lblAction.setLayoutY(y);

        pane.getChildren().addAll(lblUser, lblName, lblEmail, lblRoles, lblAction);

        y += 30;

        // Add rows
        for (String[] u : users) {
            String username = u[0];
            String name = u[1] + " " + u[2];
            String email = u[3];
            String roles = String.join(" ", u[4], u[5], u[6]).trim();

            Label userLbl = new Label(username);
            userLbl.setLayoutX(20); userLbl.setLayoutY(y);

            Label nameLbl = new Label(name);
            nameLbl.setLayoutX(150); nameLbl.setLayoutY(y);

            Label emailLbl = new Label(email);
            emailLbl.setLayoutX(300); emailLbl.setLayoutY(y);

            Label rolesLbl = new Label(roles);
            rolesLbl.setLayoutX(500); rolesLbl.setLayoutY(y);

            Button deleteBtn = new Button("Delete");
            deleteBtn.setLayoutX(650); deleteBtn.setLayoutY(y - 5);

            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete user '" + username + "'?",
                        ButtonType.YES, ButtonType.NO);

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        boolean success = ControllerDeleteUsers.handleDelete(username);

                        Alert result = new Alert(Alert.AlertType.INFORMATION);
                        if (success) {
                            result.setTitle("User Deleted");
                            result.setHeaderText("Success");
                            result.setContentText("User '" + username + "' was deleted.");
                            result.showAndWait();
                            stage.close();
                            // Refresh window after deletion
                            ControllerDeleteUsers.performDeleteUser(ownerStage);
                        } else {
                            result.setTitle("Delete Failed");
                            result.setHeaderText("Error");
                            result.setContentText("Could not delete user: " + username);
                            result.showAndWait();
                        }
                    }
                });
            });

            pane.getChildren().addAll(userLbl, nameLbl, emailLbl, rolesLbl, deleteBtn);
            y += 25;
        }

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setLayoutX(300);
        closeBtn.setLayoutY(y + 20);
        closeBtn.setOnAction(e -> ControllerDeleteUsers.performClose(stage));
        pane.getChildren().add(closeBtn);

        Scene scene = new Scene(pane, 800, 500);
        stage.setScene(scene);
        stage.showAndWait();
    }
}