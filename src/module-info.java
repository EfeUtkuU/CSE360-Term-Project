/**
 * Main module definition for the CSE360 Foundations F25 application.
 */
module FoundationsF25 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    
    requires org.junit.jupiter.api;

    opens applicationMain to javafx.graphics, javafx.fxml;
}
