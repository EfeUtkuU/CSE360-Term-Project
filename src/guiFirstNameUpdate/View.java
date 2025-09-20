package guiFirstNameUpdate;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

public class View{
	static private Label label_NewFirst = new Label();
	static protected TextField input_NewFirst = new TextField();
	
	
	static protected Label label_Requirements = new Label("");
	static protected Label label_numInName = new Label();
	static protected Label label_specialInName = new Label();
	
	static protected Button button_AcceptFirstName = new Button();
	static protected Button button_Cancel = new Button();
	
	
	public static void view(Pane theRoot) {
		double windowWidth = 250;
		
		setupLabelWidget(label_NewFirst, 10, 10, "Arial", 14, windowWidth - 15, Pos.BASELINE_LEFT);
		
		setupTextWidget(input_NewFirst, 10, 30, "Arial", 14, windowWidth - 15, Pos.BASELINE_LEFT,true);
		input_NewFirst.textProperty().addListener((observable, oldValue, newValue) -> {
		    Model.updateFirstName();
		});
		
		setupButtonWidget(button_AcceptFirstName,"Finish and Save First Name", 10, 120, "Arial", 14, windowWidth - 15,Pos.BASELINE_CENTER);
		button_AcceptFirstName.setDisable(true);
		button_AcceptFirstName.setOnAction(new EventHandler<>(){
			public void handle(ActionEvent event) {
				Controller.handleButtonPress();
			}
		});
		
		setupButtonWidget(button_Cancel, "Close the Window", 10, 150, "Arial", 14, windowWidth - 15, Pos.BASELINE_CENTER);
		button_Cancel.setOnAction(new EventHandler<>(){
			public void handle(ActionEvent event) {
				input_NewFirst.setText("");
				button_AcceptFirstName.setDisable(true);
				button_Cancel.getScene().getWindow().hide();
			}
		});
		
		theRoot.getChildren().addAll(
				label_NewFirst,input_NewFirst,label_Requirements,label_numInName,
				label_specialInName,button_AcceptFirstName,button_Cancel
				);
		
	}
	
	static private void setupLabelWidget(Label l, double x, double y, String ff, double f, double w, 
			Pos p){
		l.setLayoutX(x);
		l.setLayoutY(y);		
		l.setFont(Font.font(ff, f));	// The font face and the font size
		l.setMinWidth(w);
		l.setAlignment(p);
	}
	
	static private void setupTextWidget(TextField t, double x, double y, String ff, double f, double w, 
			Pos p, boolean e){
		t.setFont(Font.font(ff, f));	// The font face and the font size
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}

	/*
	 * Private local method to initialize the standard fields for a button
	 */
	static private void setupButtonWidget(Button b, String s, double x, double y, String ff, double f, double w, 
			Pos p){
		b.setText(s);
		b.setFont(Font.font(ff, f));	// The font face and the font size
		b.setMinWidth(w);
		b.setMaxWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
	
	private static boolean finished = false;

	public static boolean isFinished() {
	    return finished;
	}

	public static String getFirstName() {
	    return input_NewFirst.getText();
	}

	static protected void pressFinish() {
	    finished = true;
	}

}