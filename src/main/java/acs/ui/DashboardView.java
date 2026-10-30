package acs.ui;

import acs.graph.CityGraph;
import acs.model.Location;
import acs.model.Road;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.Map;

public class DashboardView {

    private final CityGraph cityGraph;

    private static final String BACKGROUND = "#101827";
    private static final String CARD = "#1d293b";
    private static final String TEXT = "#e6edf5";
    private static final String MUTED = "#9caec5";
    private static final String ACCENT = "#7fa88a";

    public DashboardView(CityGraph cityGraph) {
        this.cityGraph = cityGraph;
    }

    public BorderPane createView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");

        // Header
        Label title = new Label("NEXORA");
        title.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "ALGORITHMIC CRISIS SIMULATOR | EMERGENCY RESPONSE SYSTEM"
        );
        subtitle.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(header);

        // Left information panel
        Label overviewTitle = createHeading("CITY OVERVIEW");

        Label locations = createInfo(
                "Locations: " + cityGraph.getLocationIds().size()
        );

        Label roads = createInfo(
                "Roads: " + cityGraph.getRoads().size()
        );

        Label systemTitle = createHeading("SYSTEM STATUS");

        Label graphStatus = createInfo("City graph: Initialized");
        Label routingStatus = createInfo("Route engine: Available");

        VBox overviewCard = new VBox(
                14,
                overviewTitle,
                locations,
                roads,
                createDivider(),
                systemTitle,
                graphStatus,
                routingStatus
        );

        overviewCard.setPadding(new Insets(20));
        overviewCard.setPrefWidth(235);
        overviewCard.setStyle(
                "-fx-background-color: " + CARD + ";" +
                        "-fx-background-radius: 12;"
        );

        // City map
        Label mapTitle = createHeading("NEXORA NETWORK MAP");

        Pane cityMap = createCityMap();
        cityMap.setMinSize(500, 350);
        cityMap.setPrefSize(760, 510);
        cityMap.setStyle(
                "-fx-background-color: " + CARD + ";" +
                        "-fx-background-radius: 12;"
        );

        VBox mapPanel = new VBox(14, mapTitle, cityMap);
        mapPanel.setPadding(new Insets(20));
        mapPanel.setStyle(
                "-fx-background-color: " + CARD + ";" +
                        "-fx-background-radius: 12;"
        );

        HBox content = new HBox(20, overviewCard, mapPanel);
        content.setAlignment(Pos.TOP_LEFT);

        root.setCenter(content);

        Label footer = new Label(
                "NEXORA RESPONSE NETWORK | INITIAL DASHBOARD"
        );
        footer.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 11px;"
        );
        footer.setPadding(new Insets(20, 0, 0, 0));

        root.setBottom(footer);

        return root;
    }

    private Pane createCityMap() {
        Pane map = new Pane();

        Map<String, double[]> positions = new HashMap<>();

        double width = 760;
        double height = 510;

        // Arrange 24 locations in a six-column, four-row layout.
        for (int i = 1; i <= 24; i++) {
            String locationId = String.format("LOC_%02d", i);

            int column = (i - 1) % 6;
            int row = (i - 1) / 6;

            double x = 65 + column * 125;
            double y = 65 + row * 125;

            positions.put(locationId, new double[]{x, y});
        }

        // Draw roads first so that locations appear above them.
        // Draw roads first so that locations appear above them.
        for (Road road : cityGraph.getRoads()) {
            double[] start = positions.get(road.getFromLocationId());
            double[] end = positions.get(road.getToLocationId());

            // Skip roads whose endpoints are not on the map.
            if (start == null || end == null) {
                continue;
            }

            Line line = new Line(
                    start[0], start[1],
                    end[0], end[1]
            );

            line.setStroke(javafx.scene.paint.Color.web("#52647a"));
            line.setStrokeWidth(2);

            map.getChildren().add(line);
        }

        // Draw location nodes.
        for (String locationId : cityGraph.getLocationIds()) {
            double[] position = positions.get(locationId);

            if (position == null) {
                continue;
            }

            Circle node = new Circle(
                    position[0],
                    position[1],
                    12
            );

            node.setFill(
                    javafx.scene.paint.Color.web(ACCENT)
            );

            node.setStroke(
                    javafx.scene.paint.Color.web("#e6edf5")
            );

            node.setStrokeWidth(1.5);

            Label locationLabel = new Label(locationId);

            locationLabel.setLayoutX(position[0] - 25);
            locationLabel.setLayoutY(position[1] + 15);

            locationLabel.setStyle(
                    "-fx-text-fill: " + TEXT + ";" +
                            "-fx-font-size: 10px;"
            );

            map.getChildren().addAll(node, locationLabel);
        }

        return map;
    }

    private Label createHeading(String text) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createInfo(String text) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        return label;
    }

    private javafx.scene.shape.Line createDivider() {
        javafx.scene.shape.Line divider =
                new javafx.scene.shape.Line(0, 0, 190, 0);

        divider.setStroke(
                javafx.scene.paint.Color.web("#52647a")
        );

        return divider;
    }
}