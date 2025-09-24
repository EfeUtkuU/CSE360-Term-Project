package guiFirstNameUpdate;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class View {
    private static Label label_Title = new Label("First Name Update");
    private static Label label_NewFirst = new Label("Enter your first name:");
    static protected TextField input_NewFirst = new TextField();

    static protected Label label_Error = new Label("");
    static protected Label label_Success = new Label("");

    static protected Button button_AcceptFirstName = new Button("Finish and Save First Name");
    static protected Button button_Cancel = new Button("Close the Window");

    private static boolean finished = false;

    public static void view(Pane theRoot) {
        double windowWidth = 500;

        input_NewFirst.setText("");
        label_Error.setText("");
        label_Success.setText("");
        setFinishFalse();
        
        // Title
        setupLabelWidget(label_Title, 10, 10, "Arial", 18, windowWidth - 15, Pos.BASELINE_CENTER);

        // Text input
        setupLabelWidget(label_NewFirst, 10, 50, "Arial", 14, windowWidth - 15, Pos.BASELINE_LEFT);
        setupTextWidget(input_NewFirst, 10, 70, "Arial", 14, windowWidth - 20, Pos.BASELINE_LEFT, true);

        // Error label (red)
        label_Error.setTextFill(Color.RED);
        setupLabelWidget(label_Error, 10, 100, "Arial", 14, windowWidth - 20, Pos.BASELINE_LEFT);

        // Success label (green)
        label_Success.setTextFill(Color.GREEN);
        setupLabelWidget(label_Success, 10, 120, "Arial", 14, windowWidth - 20, Pos.BASELINE_LEFT);

        // Buttons
        setupButtonWidget(button_AcceptFirstName, "Finish and Save First Name", 10, 160, "Arial", 14, windowWidth - 15, Pos.BASELINE_CENTER);
        button_AcceptFirstName.setOnAction(event -> Controller.handleButtonPress());

        setupButtonWidget(button_Cancel, "Close the Window", 10, 200, "Arial", 14, windowWidth - 15, Pos.BASELINE_CENTER);
        button_Cancel.setOnAction(event -> {
            input_NewFirst.setText("");
            button_Cancel.getScene().getWindow().hide();
        });

        theRoot.getChildren().addAll(label_Title, label_NewFirst, input_NewFirst,
                label_Error, label_Success, button_AcceptFirstName, button_Cancel);
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

    public static String getFirstName() {
        return input_NewFirst.getText();
    }

    static protected void pressFinish() {
        finished = true;
    }

    static protected void setFinishFalse() {
        finished = false;
    }
}
