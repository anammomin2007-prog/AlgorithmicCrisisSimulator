package acs.ui;

import acs.simulation.SimulationEngine;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CrisisSimulatorApp extends Application {

    @Override
    public void start(Stage stage) {
        SimulationEngine simulationEngine = new SimulationEngine();

        DashboardView dashboard = new DashboardView(simulationEngine);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        double initialWidth = Math.min(1600, bounds.getWidth());
        double initialHeight = Math.min(900, bounds.getHeight());

        Scene scene = new Scene(
                dashboard.createView(),
                initialWidth,
                initialHeight
        );

        stage.setTitle("Algorithmic Crisis Simulator - Nexora");
        stage.setScene(scene);

        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        stage.centerOnScreen();
        stage.show();
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
