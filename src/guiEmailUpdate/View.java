package guiEmailUpdate;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class View {
    private static Label label_Title = new Label("Email Address Update");
    private static Label label_NewEmail = new Label("Enter your email address:");
    static protected TextField text_EmailAddress = new TextField();

    static protected Label label_errEmailAddress = new Label("");
    static protected Label validEmailAddress = new Label("");

    static protected Button button_AcceptEmail = new Button("Finish and Save Email");
    static protected Button button_Cancel = new Button("Close the Window");

    private static boolean finished = false;

    public static void view(Pane theRoot) {
        double windowWidth = 500;

        // Reset each time window is opened
        text_EmailAddress.setText("");
        label_errEmailAddress.setText("");
        validEmailAddress.setText("");
        setFinishFalse();

        // Title
        setupLabelWidget(label_Title, 10, 10, "Arial", 18, windowWidth - 15, Pos.BASELINE_CENTER);

        // Text input
        setupLabelWidget(label_NewEmail, 10, 50, "Arial", 14, windowWidth - 15, Pos.BASELINE_LEFT);
        setupTextWidget(text_EmailAddress, 10, 70, "Arial", 14, windowWidth - 20, Pos.BASELINE_LEFT, true);

        // Error label (red)
        label_errEmailAddress.setTextFill(Color.RED);
        setupLabelWidget(label_errEmailAddress, 10, 100, "Arial", 14, windowWidth - 20, Pos.BASELINE_LEFT);

        // Success label (green)
        validEmailAddress.setTextFill(Color.GREEN);
        setupLabelWidget(validEmailAddress, 10, 120, "Arial", 14, windowWidth - 20, Pos.BASELINE_LEFT);

        // Buttons
        setupButtonWidget(button_AcceptEmail, "Finish and Save Email", 10, 160, "Arial", 14, windowWidth - 15, Pos.BASELINE_CENTER);
        button_AcceptEmail.setOnAction(event -> Controller.handleButtonPress());

        setupButtonWidget(button_Cancel, "Close the Window", 10, 200, "Arial", 14, windowWidth - 15, Pos.BASELINE_CENTER);
        button_Cancel.setOnAction(event -> {
            text_EmailAddress.setText("");
            button_Cancel.getScene().getWindow().hide();
        });

        theRoot.getChildren().addAll(label_Title, label_NewEmail, text_EmailAddress,
                label_errEmailAddress, validEmailAddress, button_AcceptEmail, button_Cancel);
    }

    // --- Utility setup methods ---
    private static void setupLabelWidget(Label l, double x, double y, String ff, double f, double w, Pos p) {
        l.setLayoutX(x);
        l.setLayoutY(y);
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
    }

    private static void setupTextWidget(TextField t, double x, double y, String ff, double f, double w,
                                        Pos p, boolean e) {
        t.setFont(Font.font(ff, f));
        t.setMinWidth(w);
        t.setMaxWidth(w);
        t.setAlignment(p);
        t.setLayoutX(x);
        t.setLayoutY(y);
        t.setEditable(e);
    }

    private static void setupButtonWidget(Button b, String s, double x, double y, String ff, double f, double w,
                                          Pos p) {
        b.setText(s);
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setMaxWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    public static boolean isFinished() {
        return finished;
    }

    public static String getEmailAddress() {
        return text_EmailAddress.getText();
    }

    static protected void pressFinish() {
        finished = true;
    }

    static protected void setFinishFalse() {
        finished = false;
    }
}
