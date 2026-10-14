package acs.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CrisisSimulatorApp extends Application {

    @Override
    public void start(Stage stage) {
        Label message = new Label(
                "Algorithmic Crisis Simulator - Nexora"
        );

        StackPane root = new StackPane(message);
        Scene scene = new Scene(root, 800, 500);

        stage.setTitle("Algorithmic Crisis Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}