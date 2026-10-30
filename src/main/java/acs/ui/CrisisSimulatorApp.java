package acs.ui;

import acs.config.NexoraCity;
import acs.graph.CityGraph;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CrisisSimulatorApp extends Application {

    @Override
    public void start(Stage stage) {
        CityGraph cityGraph = NexoraCity.createCity();

        DashboardView dashboard = new DashboardView(cityGraph);

        Scene scene = new Scene(
                dashboard.createView(),
                1200,
                800
        );

        stage.setTitle("Algorithmic Crisis Simulator - Nexora");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}