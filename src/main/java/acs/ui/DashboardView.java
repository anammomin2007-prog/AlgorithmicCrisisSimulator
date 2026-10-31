package acs.ui;

import acs.graph.CityGraph;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import acs.simulation.SimulationEngine;
import acs.model.Emergency;
import acs.model.EmergencyStatus;
import acs.model.EmergencyVehicle;
import acs.model.Road;
import acs.model.enums.TrafficLevel;
import acs.model.enums.VehicleType;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.input.MouseButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Tooltip;

import java.util.HashMap;
import java.util.Map;

public class DashboardView {

    private final SimulationEngine simulationEngine;
    private final CityGraph cityGraph;

    private static final String BG = "#060e17";
    private static final String PANEL = "#0a1724";
    private static final String PANEL_ALT = "#0d1e30";
    private static final String BORDER = "#1a3149";
    private static final String TEXT = "#edf5ff";
    private static final String MUTED = "#768c9e";
    private static final String GREEN = "#34d399";
    private static final String BLUE = "#38bdf8";
    private static final String RED = "#f87171";
    private static final String AMBER = "#fbbf24";
    private static final String PURPLE = "#a78bfa";

    private final Map<String, double[]> positions = new HashMap<>();

    private final VBox incidentList = new VBox(8);
    private final VBox vehicleList = new VBox(7);

    // Actual computer clock. Simulation time is shown separately in controls.
    private final Label clockValue = new Label("--:--:--");
    private final Label timeValue = new Label("00:00:00");
    private final Label durationValue = new Label("30 simulated min");
    private final Timeline liveClock = new Timeline();
    private final Timeline autoSimulation = new Timeline();
    private boolean simulationRunning = false;
    private final Label pendingValue = new Label("2");
    private final Label respondingValue = new Label("1");
    private final Label onSceneValue = new Label("0");
    private final Label resolvedValue = new Label("1");

    private final Label avgResponseMetric = new Label("8.4 min");
    private final Label totalIncidentsMetric = new Label("4");
    private final Label activeResponsesMetric = new Label("5");
    private final Label resolvedMetric = new Label("1");

    private final ComboBox<CrisisType> typeSelector = new ComboBox<>();
    private final ComboBox<String> locationSelector = new ComboBox<>();
    private final ComboBox<CrisisSeverity> severitySelector =
            new ComboBox<>();

    private final Spinner<Double> advanceSpinner = new Spinner<>();

    private final Pane cityMap = new Pane();
    private final Label simulationClockLabel = new Label("Simulation: 0.0 min • real-time playback");

    public DashboardView(SimulationEngine simulationEngine) {
        if (simulationEngine == null) {
            throw new IllegalArgumentException(
                    "SimulationEngine cannot be null."
            );
        }

        this.simulationEngine = simulationEngine;
        this.cityGraph = simulationEngine.getCityGraph();

        initializeMockPositions();
    }

    private void initializeMockPositions() {
        // Deliberately irregular, deterministic positions so Nexora looks like a city,
        // not a spreadsheet. Coordinates are normalized to the available map size.
        double[][] points = {
                {0.08,0.18},{0.23,0.10},{0.35,0.24},{0.52,0.12},
                {0.70,0.20},{0.89,0.12},{0.15,0.39},{0.40,0.39},
                {0.58,0.34},{0.78,0.39},{0.28,0.53},{0.49,0.52},
                {0.67,0.56},{0.90,0.53},{0.08,0.70},{0.31,0.73},
                {0.52,0.78},{0.73,0.72},{0.88,0.82},{0.18,0.90},
                {0.39,0.91},{0.60,0.92},{0.78,0.93},{0.96,0.91}
        };
        for (int i = 1; i <= points.length; i++) {
            positions.put(String.format("LOC_%02d", i), points[i - 1]);
        }
    }

    public BorderPane createView() {
        BorderPane root = new BorderPane();

        root.setStyle("-fx-background-color: " + BG + ";");
        root.setPadding(new Insets(12));

        HBox header = createHeader();
        root.setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 12, 0));

        HBox workspace = new HBox(12);
        workspace.setAlignment(Pos.TOP_LEFT);
        workspace.setFillHeight(true);
        workspace.setStyle("-fx-background-color: transparent;");

        VBox leftColumn = createLeftColumn();
        VBox centerColumn = createCenterColumn();
        VBox rightColumn = createRightColumn();

        leftColumn.setMinWidth(210);
        leftColumn.setPrefWidth(240);
        leftColumn.setMaxWidth(280);

        centerColumn.setMinWidth(400);
        HBox.setHgrow(centerColumn, Priority.ALWAYS);
        centerColumn.setMaxWidth(Double.MAX_VALUE);

        rightColumn.setMinWidth(240);
        rightColumn.setPrefWidth(280);
        rightColumn.setMaxWidth(340);

        workspace.getChildren().addAll(
                leftColumn,
                centerColumn,
                rightColumn
        );

        ScrollPane workspaceScroll = new ScrollPane(workspace);
        workspaceScroll.setFitToWidth(true);
        workspaceScroll.setFitToHeight(true);
        workspaceScroll.setPannable(true);
        workspaceScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        workspaceScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        workspaceScroll.setStyle(
                "-fx-background: " + BG + ";" +
                        " -fx-background-color: " + BG + ";" +
                        " -fx-border-color: transparent;"
        );

        root.setCenter(workspaceScroll);

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 4, 0, 4));

        Label footerLabel = new Label(
                "NEXORA RESPONSE NETWORK  |  " +
                        "SMARTER DECISIONS. SAFER COMMUNITIES."
        );
        footerLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 10px;" +
                        " -fx-font-weight: bold;" +
                        " -fx-font-family: 'Segoe UI';"
        );

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Circle onlineDot = new Circle(4, Color.web(GREEN));

        Label onlineLabel = new Label("System Online");
        onlineLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 10px;" +
                        " -fx-font-weight: bold;"
        );

        footer.getChildren().addAll(
                footerLabel,
                footerSpacer,
                onlineDot,
                onlineLabel
        );

        root.setBottom(footer);

        refreshDashboard();
        startLiveClock();

        root.getStylesheets().add(
                getClass().getResource("/dashboard.css").toExternalForm()
        );

        return root;
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setStyle(panelStyle());

        StackPane logoIcon = new StackPane();
        logoIcon.setMinSize(24, 24);
        logoIcon.setPrefSize(24, 24);

        Line line1 = new Line(-8, 0, 0, -8);
        Line line2 = new Line(0, -8, 8, 0);
        Line line3 = new Line(8, 0, 0, 8);
        Line line4 = new Line(0, 8, -8, 0);

        for (Line line : new Line[]{line1, line2, line3, line4}) {
            line.setStroke(Color.web(GREEN));
            line.setStrokeWidth(2.5);
        }

        logoIcon.getChildren().addAll(line1, line2, line3, line4);

        Label title = new Label("NEXORA");
        title.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-size: 20px;" +
                        " -fx-font-weight: 900;" +
                        " -fx-font-family: 'Segoe UI';"
        );

        Label subtitle = new Label(
                "Algorithmic Crisis Simulator | Emergency Response System"
        );
        subtitle.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 12px;" +
                        " -fx-font-family: 'Segoe UI';"
        );
        subtitle.setWrapText(true);

        HBox logoContainer = new HBox(8, logoIcon, title, subtitle);
        logoContainer.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button startButton = new Button("▶ Start");
        startButton.setStyle(
                "-fx-background-color: #065f46;" +
                        " -fx-text-fill: #a7f3d0;" +
                        " -fx-font-weight: bold;" +
                        " -fx-background-radius: 4;" +
                        " -fx-padding: 6 12 6 12;"
        );

        Button pauseButton = new Button("⏸ Pause");
        pauseButton.setStyle(buttonStyle());

        Button resetButton = new Button("⟳ Reset");
        resetButton.setStyle(buttonStyle());

        startButton.setOnAction(event -> startSimulation(startButton, pauseButton));
        pauseButton.setOnAction(event -> pauseSimulation(startButton, pauseButton));
        resetButton.setOnAction(event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Reset simulation");
            confirm.setHeaderText("Start Nexora from a clean state?");
            confirm.setContentText("This clears incidents and restores all vehicles and roads.");
            confirm.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
                autoSimulation.stop();
                simulationRunning = false;
                simulationEngine.reset();
                simulationClockLabel.setText("Simulation: 0.0 min • real-time playback");
                startButton.setDisable(false);
                pauseButton.setDisable(true);
                refreshDashboard();
                renderNetworkMapGraphics();
            });
        });
        pauseButton.setDisable(true);

        header.getChildren().addAll(
                logoContainer,
                spacer,
                startButton,
                pauseButton,
                resetButton
        );

        return header;
    }

    // ---------------------------------------------------------
    // LEFT COLUMN
    // ---------------------------------------------------------

    private VBox createLeftColumn() {
        VBox container = new VBox(12);
        container.setPrefWidth(240);
        container.setStyle("-fx-background-color: transparent;");

        VBox navigationBox = new VBox(4);
        navigationBox.setPadding(new Insets(8));
        navigationBox.setStyle(panelStyle());

        String[] navigationItems = {
                "🏠 Dashboard", "⚠ Incidents", "🚒 Vehicles", "📊 Analytics", "⚙ Settings"
        };
        for (int i = 0; i < navigationItems.length; i++) {
            final String itemName = navigationItems[i];
            Button item = new Button(itemName);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(8, 12, 8, 12));
            item.setStyle(i == 0
                    ? "-fx-background-color: #112942; -fx-text-fill: " + GREEN + "; -fx-font-weight: bold; -fx-background-radius: 4;"
                    : "-fx-background-color: transparent; -fx-text-fill: " + MUTED + "; -fx-font-weight: bold;");
            item.setOnAction(event -> showNavigationSection(itemName));
            navigationBox.getChildren().add(item);
        }

        VBox creationBox = new VBox(10);
        creationBox.setPadding(new Insets(14));
        creationBox.setStyle(panelStyle());

        Label creationTitle = sectionTitle("➕ Create Emergency");

        typeSelector.setItems(
                FXCollections.observableArrayList(CrisisType.values())
        );
        typeSelector.getSelectionModel().select(CrisisType.FIRE);
        typeSelector.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(typeSelector);

        locationSelector.setItems(FXCollections.observableArrayList(
                cityGraph.getLocationIds().stream().sorted().toList()));
        locationSelector.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : locationIcon(item) + " " + locationName(item) + " (" + item + ")");
            }
        });
        locationSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : locationIcon(item) + " " + locationName(item));
            }
        });
        locationSelector.getSelectionModel().select("LOC_08");
        locationSelector.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(locationSelector);

        severitySelector.setItems(
                FXCollections.observableArrayList(
                        CrisisSeverity.values()
                )
        );
        severitySelector.getSelectionModel().select(
                CrisisSeverity.CRITICAL
        );
        severitySelector.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(severitySelector);

        Button createButton = new Button("Create Emergency");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setStyle(
                "-fx-background-color: " + GREEN + ";" +
                        " -fx-text-fill: #042f2e;" +
                        " -fx-font-weight: bold;" +
                        " -fx-background-radius: 4;" +
                        " -fx-padding: 8 0 8 0;"
        );
        createButton.setOnAction(event -> {
            CrisisType type = typeSelector.getValue();
            String location = locationSelector.getValue();
            CrisisSeverity severity = severitySelector.getValue();

            if (type == null || location == null || severity == null) {
                return;
            }

            try {
                simulationEngine.createEmergency(type, location, severity);
                simulationEngine.allocatePendingEmergencies();
                refreshDashboard();
                renderNetworkMapGraphics();
            } catch (IllegalArgumentException exception) {
                showError("Could not create emergency", exception.getMessage());
            }
        });

        creationBox.getChildren().addAll(
                creationTitle,
                createFormLabel("Type"),
                typeSelector,
                createFormLabel("Location"),
                locationSelector,
                createFormLabel("Severity"),
                severitySelector,
                createButton
        );

        VBox statusBox = new VBox(8);
        statusBox.setPadding(new Insets(12));
        statusBox.setStyle(panelStyle());

        statusBox.getChildren().addAll(
                sectionTitle("Response Timing"),
                createTimeDisplayRow("🕒 Current Local Time", clockValue, GREEN),
                createTimeDisplayRow("⏱ Response Elapsed", timeValue, BLUE),
                createTimeDisplayRow("⌛ Estimated Full Journey", durationValue, PURPLE)
        );

        VBox summaryBox = new VBox(8);
        summaryBox.setPadding(new Insets(12));
        summaryBox.setStyle(panelStyle());

        summaryBox.getChildren().addAll(
                sectionTitle("Emergency Summary"),
                createSummaryCounter("⏳ Pending", pendingValue, AMBER),
                createSummaryCounter("🚒 Responding", respondingValue, BLUE),
                createSummaryCounter("✓ On Scene", onSceneValue, PURPLE),
                createSummaryCounter("🏆 Resolved", resolvedValue, GREEN)
        );

        container.getChildren().addAll(
                navigationBox,
                creationBox,
                statusBox,
                summaryBox
        );

        return container;
    }

    // ---------------------------------------------------------
    // CENTRE COLUMN
    // ---------------------------------------------------------

    private VBox createCenterColumn() {
        VBox container = new VBox(12);
        container.setMinWidth(400);
        container.setStyle("-fx-background-color: transparent;");

        VBox mapWrapper = new VBox(8);
        mapWrapper.setPadding(new Insets(12));
        mapWrapper.setStyle(panelStyle());
        VBox.setVgrow(mapWrapper, Priority.ALWAYS);

        Label mapTitle = sectionTitle("Nexora Network Map");

        HBox legendRow = new HBox(12);
        legendRow.setAlignment(Pos.CENTER_LEFT);
        legendRow.getChildren().addAll(
                createLegendIcon("Location", GREEN, false),
                createLegendIcon("Fire", RED, true),
                createLegendIcon("Accident", AMBER, true),
                createLegendIcon("Flood", BLUE, true),
                createLegendIcon("Low traffic", "#2dd4bf", false),
                createLegendIcon("Medium", AMBER, false),
                createLegendIcon("High", "#fb7185", false)
        );

        HBox mapHeader = new HBox(12, mapTitle, new Region(), legendRow);
        mapHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(mapHeader.getChildren().get(1), Priority.ALWAYS);

        cityMap.setMinSize(300, 250);
        cityMap.setPrefSize(700, 450);
        cityMap.setStyle(
                "-fx-background-color: " + BG + ";" +
                        " -fx-background-radius: 4;" +
                        " -fx-border-color: " + BORDER + ";" +
                        " -fx-border-width: 1;"
        );

        VBox.setVgrow(cityMap, Priority.ALWAYS);

        cityMap.widthProperty().addListener(
                (obs, oldValue, newValue) -> renderNetworkMapGraphics()
        );
        cityMap.heightProperty().addListener(
                (obs, oldValue, newValue) -> renderNetworkMapGraphics()
        );

        Label mapHelp = new Label("Road controls: left-click to cycle traffic • right-click to close/reopen • dashed gray = closed");
        mapHelp.setWrapText(true);
        mapHelp.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 10px;");
        mapWrapper.getChildren().addAll(mapHeader, cityMap, mapHelp);

        HBox performanceAndControls = new HBox(12);
        performanceAndControls.setMinHeight(145);
        performanceAndControls.setPrefHeight(160);

        VBox performanceBox = new VBox(8);
        performanceBox.setPadding(new Insets(12));
        performanceBox.setStyle(panelStyle());
        HBox.setHgrow(performanceBox, Priority.ALWAYS);

        Label performanceTitle = sectionTitle("📊 Response Performance");

        HBox metricsGrid = new HBox(10);
        metricsGrid.setAlignment(Pos.CENTER_LEFT);
        metricsGrid.setPadding(new Insets(6, 0, 0, 0));

        metricsGrid.getChildren().addAll(
                createMetricCard("Avg. Response Time", avgResponseMetric, "⏱"),
                createMetricCard("Total Incidents", totalIncidentsMetric, "🔺"),
                createMetricCard("Resolved Cases", resolvedMetric, "✓"),
                createMetricCard("Active Fleet Units", activeResponsesMetric, "🎛")
        );

        for (Node node : metricsGrid.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setStyle(node.getStyle());
        }

        performanceBox.getChildren().addAll(performanceTitle, metricsGrid);

        VBox controlsBox = new VBox(8);
        controlsBox.setPadding(new Insets(12));
        controlsBox.setPrefWidth(220);
        controlsBox.setMinWidth(190);
        controlsBox.setStyle(panelStyle());

        Label controlTitle = sectionTitle("▶ Simulation Controls");
        Label advanceLabel = createFormLabel("Advance by (minutes)");

        advanceSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        0.5, 10.0, 1.0, 0.5
                )
        );
        advanceSpinner.setEditable(true);
        advanceSpinner.setMaxWidth(Double.MAX_VALUE);

        simulationClockLabel.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 9px;");
        Button advanceButton = new Button("Advance");
        advanceButton.setMaxWidth(Double.MAX_VALUE);
        advanceButton.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                        " -fx-text-fill: #034f74;" +
                        " -fx-font-weight: bold;" +
                        " -fx-background-radius: 4;" +
                        " -fx-padding: 6 0 6 0;"
        );
        advanceButton.setOnAction(event -> {
            try {
                double minutes = advanceSpinner.getValue();

                simulationEngine.advanceTime(minutes);
                simulationClockLabel.setText(String.format(
                        "Simulation: %.2f min • real-time playback",
                        simulationEngine.getSimulationTime()));

                refreshDashboard();
                renderNetworkMapGraphics();

            } catch (Exception exception) {
                exception.printStackTrace();

                showError(
                        "Could not advance simulation",
                        exception.getMessage() == null
                                ? exception.getClass().getSimpleName()
                                : exception.getMessage()
                );
            }
        });

        controlsBox.getChildren().addAll(
                controlTitle,
                advanceLabel,
                advanceSpinner,
                advanceButton,
                simulationClockLabel
        );

        performanceAndControls.getChildren().addAll(
                performanceBox,
                controlsBox
        );

        container.getChildren().addAll(mapWrapper, performanceAndControls);
        VBox.setVgrow(mapWrapper, Priority.ALWAYS);

        return container;
    }

    // ---------------------------------------------------------
    // RIGHT COLUMN
    // ---------------------------------------------------------
    private final Label incidentCount = new Label("0");
    private final Label vehicleCount = new Label("0");

    private VBox createRightColumn() {
        VBox container = new VBox(12);
        container.setPrefWidth(280);
        container.setStyle("-fx-background-color: transparent;");

        VBox incidentsBox = new VBox(8);
        incidentsBox.setPadding(new Insets(12));
        incidentsBox.setStyle(panelStyle());
        VBox.setVgrow(incidentsBox, Priority.ALWAYS);

        Label incidentTitle = sectionTitle("🚨 Active Incidents");
        incidentCount.setText("0");
        incidentCount.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-weight: bold;"
        );

        HBox incidentHeader = new HBox(
                incidentTitle, new Region(), incidentCount
        );
        incidentHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(incidentHeader.getChildren().get(1), Priority.ALWAYS);

        ScrollPane incidentScroll = new ScrollPane(incidentList);
        setupScrollPane(incidentScroll);
        VBox.setVgrow(incidentScroll, Priority.ALWAYS);

        incidentsBox.getChildren().addAll(incidentHeader, incidentScroll);

        VBox vehiclesBox = new VBox(8);
        vehiclesBox.setPadding(new Insets(12));
        vehiclesBox.setStyle(panelStyle());
        VBox.setVgrow(vehiclesBox, Priority.ALWAYS);

        Label vehicleTitle = sectionTitle("🚒 Vehicle Status");
        vehicleCount.setText("0");
        vehicleCount.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-weight: bold;"
        );

        HBox vehicleHeader = new HBox(
                vehicleTitle, new Region(), vehicleCount
        );
        vehicleHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(vehicleHeader.getChildren().get(1), Priority.ALWAYS);

        ScrollPane vehicleScroll = new ScrollPane(vehicleList);
        setupScrollPane(vehicleScroll);
        VBox.setVgrow(vehicleScroll, Priority.ALWAYS);

        vehiclesBox.getChildren().addAll(vehicleHeader, vehicleScroll);

        VBox overviewBox = new VBox(8);
        overviewBox.setPadding(new Insets(12));
        overviewBox.setStyle(panelStyle());
        overviewBox.getChildren().addAll(
                sectionTitle("System Overview"),
                createMetaCountRow("📍 Locations", String.valueOf(cityGraph.getLocationIds().size())),
                createMetaCountRow("🛣 Roads", String.valueOf(cityGraph.getRoads().size()))
        );

        container.getChildren().addAll(incidentsBox, vehiclesBox, overviewBox);
        return container;
    }

    // ---------------------------------------------------------
    // NETWORK MAP
    // ---------------------------------------------------------

    private void renderNetworkMapGraphics() {
        double width = cityMap.getWidth();
        double height = cityMap.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        cityMap.getChildren().clear();

        double horizontalPadding = Math.min(45, width * 0.08);
        double verticalPadding = Math.min(40, height * 0.10);

        double usableWidth = Math.max(1, width - horizontalPadding * 2);
        double usableHeight = Math.max(1, height - verticalPadding * 2);

        Map<String, double[]> screenPositions = new HashMap<>();

        positions.forEach((locationId, normalized) -> {
            double x = horizontalPadding + normalized[0] * usableWidth;
            double y = verticalPadding + normalized[1] * usableHeight;

            screenPositions.put(locationId, new double[]{x, y});
        });

        // Draw the actual graph roads, with color reflecting traffic and closures.
        for (Road road : cityGraph.getRoads()) {
            drawRoad(
                    screenPositions.get(road.getFromLocationId()),
                    screenPositions.get(road.getToLocationId()),
                    road
            );
        }

        Map<String, List<String>> activeRoutes = simulationEngine.getActiveRouteLocations();
        for (Emergency emergency : simulationEngine.getEmergencies()) {
            List<String> routeLocations = activeRoutes.get(emergency.getId());
            if (routeLocations == null || routeLocations.size() < 2) continue;
            String routeColor = switch (emergency.getType()) {
                case FIRE -> RED;
                case ACCIDENT -> BLUE;
                case FLOOD -> PURPLE;
            };
            for (int i = 0; i < routeLocations.size() - 1; i++) {
                double[] from = screenPositions.get(routeLocations.get(i));
                double[] to = screenPositions.get(routeLocations.get(i + 1));
                if (from == null || to == null) continue;
                Line routeLine = new Line(from[0], from[1], to[0], to[1]);
                routeLine.setStroke(Color.web(routeColor));
                routeLine.setStrokeWidth(4);
                routeLine.setOpacity(0.9);
                routeLine.getStrokeDashArray().addAll(7.0, 4.0);
                routeLine.setMouseTransparent(true);
                cityMap.getChildren().add(routeLine);
            }
        }

        // Draw compact, explicitly sized location markers and separate name labels.
        // Keeping the labels outside the StackPane prevents JavaFX from squeezing
        // long names into a tiny marker (which previously rendered as "...").
        for (Map.Entry<String, double[]> entry : screenPositions.entrySet()) {
            String locationId = entry.getKey();
            double[] coordinates = entry.getValue();
            String icon = locationIcon(locationId);
            String displayName = locationName(locationId);
            String shortName = mapLabelName(locationId);

            StackPane node = new StackPane();
            node.setManaged(false);
            node.setPrefSize(30, 30);
            node.setMinSize(30, 30);
            node.setMaxSize(30, 30);
            node.resize(30, 30);
            node.setLayoutX(coordinates[0] - 15);
            node.setLayoutY(coordinates[1] - 15);

            Circle outer = new Circle(12, Color.web("#10243a"));
            outer.setStroke(Color.web(locationColor(locationId)));
            outer.setStrokeWidth(2.2);
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT + ";");
            node.getChildren().addAll(outer, iconLabel);
            Tooltip.install(node, new Tooltip(displayName + " (" + locationId + ")"));
            cityMap.getChildren().add(node);

            Label nameLabel = new Label(shortName);
            nameLabel.setManaged(false);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            nameLabel.setWrapText(true);
            nameLabel.setMinWidth(88);
            nameLabel.setPrefWidth(96);
            nameLabel.setMaxWidth(96);
            nameLabel.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 9px; -fx-font-weight: bold; " +
                    "-fx-background-color: #07111cf0; -fx-padding: 2 4 2 4; -fx-background-radius: 3; " +
                    "-fx-border-color: #243b50; -fx-border-radius: 3;");
            // Alternate label placement to reduce collisions; keep bottom-row labels above markers.
            boolean placeAbove = coordinates[1] > height * 0.78 || (locationNumber(locationId) % 3 == 0);
            nameLabel.setLayoutX(coordinates[0] - 48);
            nameLabel.setLayoutY(placeAbove ? coordinates[1] - 35 : coordinates[1] + 14);
            nameLabel.setMouseTransparent(false);
            Tooltip.install(nameLabel, new Tooltip(displayName + " • " + locationId));
            cityMap.getChildren().add(nameLabel);
        }

        // Incidents are drawn from live simulation data, not hard-coded sample routes.
        for (Emergency emergency : simulationEngine.getEmergencies()) {
            double[] point = screenPositions.get(emergency.getLocation());
            if (point == null || emergency.getStatus() == EmergencyStatus.RESOLVED
                    || emergency.getStatus() == EmergencyStatus.CANCELLED) continue;
            Label incidentMarker = new Label(emergencyIcon(emergency.getType()));
            incidentMarker.setStyle("-fx-font-size: 17px; -fx-background-color: #07111c; -fx-padding: 3; -fx-background-radius: 12;");
            incidentMarker.setLayoutX(point[0] + 9);
            incidentMarker.setLayoutY(point[1] - 24);
            Tooltip.install(incidentMarker, new Tooltip(emergency.getType() + " • " +
                    emergency.getSeverity() + " • " + emergency.getStatus()));
            cityMap.getChildren().add(incidentMarker);
        }

        // Map settings control stays inside the map.
        VBox tools = new VBox(2);
        tools.setManaged(false);
        tools.setAlignment(Pos.CENTER);
        tools.setPadding(new Insets(4));
        tools.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        " -fx-background-radius: 4;" +
                        " -fx-border-color: " + BORDER + ";"
        );

        Label settingsTool = new Label("⚙");
        settingsTool.setPadding(new Insets(6, 8, 6, 8));
        settingsTool.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-weight: bold;" +
                        " -fx-font-size: 11px;" +
                        " -fx-cursor: hand;"
        );
        Tooltip.install(settingsTool, new Tooltip("Road and traffic settings"));

        settingsTool.setOnMouseClicked(event ->
                showNavigationSection("⚙ Settings")
        );

        tools.getChildren().add(settingsTool);

        tools.applyCss();
        tools.autosize();

        double toolWidth = Math.max(34, tools.prefWidth(-1));
        double toolHeight = Math.max(34, tools.prefHeight(-1));

        tools.setLayoutX(Math.max(4, width - toolWidth - 8));
        tools.setLayoutY(Math.max(4, height - toolHeight - 8));

        cityMap.getChildren().add(tools);

        // Draw actual emergency vehicles on the city map.
        List<EmergencyVehicle> vehicles = simulationEngine.getVehicles();
        Map<String, SimulationEngine.VehicleJourney> journeys = simulationEngine.getVehicleJourneys();

        for (EmergencyVehicle vehicle : vehicles) {
            String locationId = vehicle.getCurrentLocation();
            double[] coordinates = screenPositions.get(locationId);
            SimulationEngine.VehicleJourney journey = journeys.get(vehicle.getId());
            if (journey != null) {
                double[] from = screenPositions.get(journey.fromLocationId());
                double[] to = screenPositions.get(journey.toLocationId());
                if (from != null && to != null) {
                    coordinates = new double[]{
                            from[0] + (to[0] - from[0]) * journey.progress(),
                            from[1] + (to[1] - from[1]) * journey.progress()
                    };
                }
            }

            // Skip vehicles whose locations are not on the map.
            if (coordinates == null) {
                continue;
            }

            // Draw the vehicle marker with a visible background.
            String vehicleSymbol = switch (vehicle.getType()) {
                case FIRE_ENGINE -> "🚒";
                case AMBULANCE -> "🚑";
                case RESCUE_VEHICLE -> "🚤";
            };
            StackPane vehicleMarker = new StackPane();
            vehicleMarker.setManaged(false);
            vehicleMarker.setMinSize(34, 34);
            vehicleMarker.setPrefSize(34, 34);
            vehicleMarker.setMaxSize(34, 34);

            Circle background = new Circle(15, Color.web("#102a43"));
            background.setStroke(Color.web(BLUE));
            background.setStrokeWidth(2);

            Label vehicleIcon = new Label(vehicleSymbol);
            vehicleIcon.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-padding: 0;" +
                            "-fx-background-color: transparent;"
            );
            vehicleIcon.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            vehicleIcon.setMouseTransparent(true);

            vehicleMarker.getChildren().addAll(background, vehicleIcon);
            vehicleMarker.setLayoutX(coordinates[0] - 17);
            vehicleMarker.setLayoutY(coordinates[1] - 17);

            Tooltip.install(vehicleMarker, new Tooltip(
                    vehicle.getId() + " • " +
                            vehicle.getType() + " • " +
                            simulationEngine.getVehicleActivity(vehicle.getId())
            ));

            cityMap.getChildren().add(vehicleMarker);
        }
    }

    private void drawRoad(double[] first, double[] second, Road road) {
        if (first == null || second == null) return;

        // Dark casing gives roads visual separation from each other and from routes.
        Line casing = new Line(first[0], first[1], second[0], second[1]);
        casing.setStroke(Color.web("#142a3c"));
        casing.setStrokeWidth(7.0);
        casing.setOpacity(0.95);
        casing.setMouseTransparent(true);
        cityMap.getChildren().add(casing);

        Line line = new Line(first[0], first[1], second[0], second[1]);
        String roadColor = road.isClosed() ? "#94a3b8" : switch (road.getTrafficLevel()) {
            case LOW -> "#2dd4bf";
            case MEDIUM -> "#fbbf24";
            case HIGH -> "#fb7185";
        };
        line.setStroke(Color.web(roadColor));
        line.setStrokeWidth(road.isClosed() ? 2.0 : 3.0);
        if (road.isClosed()) line.getStrokeDashArray().addAll(6.0, 5.0);
        line.setOpacity(0.96);
        Tooltip.install(line, new Tooltip(road.getRoadId() + " • " +
                (road.isClosed() ? "CLOSED" : road.getTrafficLevel() + " traffic • " +
                        String.format("%.1f min", road.getEffectiveTravelTimeMinutes()))));
        line.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                TrafficLevel next = switch (road.getTrafficLevel()) {
                    case LOW -> TrafficLevel.MEDIUM;
                    case MEDIUM -> TrafficLevel.HIGH;
                    case HIGH -> TrafficLevel.LOW;
                };
                road.setTrafficLevel(next);
                renderNetworkMapGraphics();
            } else if (event.getButton() == MouseButton.SECONDARY) {
                road.setClosed(!road.isClosed());
                renderNetworkMapGraphics();
            }
        });
        cityMap.getChildren().add(line);
    }

    private String locationName(String id) {
        int n = Integer.parseInt(id.substring(id.indexOf('_') + 1));
        String[] names = {
                "Central Fire Station", "North Homes", "Riverside School", "General Hospital",
                "West Apartments", "North Market", "City Library", "Police Station",
                "Industrial Park", "Central Junction", "Community Clinic", "River Bridge",
                "East School", "Shopping District", "South Homes", "Rescue Depot",
                "Sports Ground", "Water Treatment Plant", "Floodplain Homes", "South Market",
                "Bus Terminal", "Power Substation", "University Campus", "Emergency Shelter"
        };
        return names[Math.max(0, Math.min(names.length - 1, n - 1))];
    }

    private int locationNumber(String id) {
        try {
            return Integer.parseInt(id.substring(id.indexOf('_') + 1));
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    private String mapLabelName(String id) {
        int n = locationNumber(id);
        String[] labels = {
                "Fire Station", "North Homes", "Riverside School", "General Hospital",
                "West Apartments", "North Market", "City Library", "Police Station",
                "Industrial Park", "Central Junction", "Community Clinic", "River Bridge",
                "East School", "Shopping District", "South Homes", "Rescue Depot",
                "Sports Ground", "Water Plant", "Floodplain Homes", "South Market",
                "Bus Terminal", "Power Station", "University", "Emergency Shelter"
        };
        return labels[Math.max(0, Math.min(labels.length - 1, n - 1))];
    }

    private String locationIcon(String id) {
        int n = Integer.parseInt(id.substring(id.indexOf('_') + 1));
        return switch (n) {
            case 1 -> "🚒";
            case 8 -> "🚓";
            case 3, 13 -> "🏫";
            case 4, 11 -> "🏥";
            case 6, 14, 20 -> "🏪";
            case 7 -> "📚";
            case 9 -> "🏭";
            case 10 -> "🚦";
            case 12 -> "🌉";
            case 16, 24 -> "🛟";
            case 17 -> "🏟️";
            case 18 -> "💧";
            case 21 -> "🚌";
            case 22 -> "⚡";
            case 23 -> "🎓";
            default -> "🏘️";
        };
    }

    private String locationColor(String id) {
        int n = Integer.parseInt(id.substring(id.indexOf('_') + 1));
        return switch (n) {
            case 1 -> RED;
            case 3, 8, 13, 23 -> BLUE;
            case 4, 11 -> GREEN;
            case 9, 22 -> AMBER;
            case 16, 24 -> PURPLE;
            default -> "#c084fc";
        };
    }


    // ---------------------------------------------------------
    // SAMPLE DATA
    // ---------------------------------------------------------

    private void refreshDashboard() {
        incidentList.getChildren().clear();
        vehicleList.getChildren().clear();

        // Read actual emergencies from the simulation engine.
        List<Emergency> emergencies = simulationEngine.getEmergencies();
        List<Emergency> activeEmergencies = emergencies.stream()
                .filter(e -> e.getStatus() != EmergencyStatus.RESOLVED
                        && e.getStatus() != EmergencyStatus.CANCELLED)
                .toList();
        incidentCount.setText(String.valueOf(activeEmergencies.size()));
        if (activeEmergencies.isEmpty()) {
            Label emptyIncidents = new Label("No active incidents\nCreate an emergency to start a response.");
            emptyIncidents.setWrapText(true);
            emptyIncidents.setMaxWidth(Double.MAX_VALUE);
            emptyIncidents.setAlignment(Pos.CENTER);
            emptyIncidents.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12px; " +
                    "-fx-padding: 22 12 22 12; -fx-background-color: " + PANEL_ALT + "; " +
                    "-fx-background-radius: 6; -fx-border-color: " + BORDER + "; -fx-border-radius: 6;");
            incidentList.getChildren().add(emptyIncidents);
        }
        for (Emergency emergency : activeEmergencies) {
            EmergencyStatus status = emergency.getStatus();

            String statusText = switch (status) {
                case PENDING -> "Pending";
                case RESPONDING -> "Responding";
                case ON_SCENE -> "On Scene";
                case TRANSPORTING -> "Transporting to hospital";
                case RESOLVED -> "Resolved";
                default -> "Unknown";
            };

            String statusColor = switch (status) {
                case PENDING -> AMBER;
                case RESPONDING -> BLUE;
                case ON_SCENE -> PURPLE;
                case TRANSPORTING -> BLUE;
                case RESOLVED -> GREEN;
                default -> MUTED;
            };

            String severity = emergency.getSeverity().toString();

            String severityColor = switch (severity) {
                case "CRITICAL" -> RED;
                case "HIGH" -> AMBER;
                case "MEDIUM" -> BLUE;
                default -> GREEN;
            };

            incidentList.getChildren().add(
                    createIncidentCard(
                            emergencyIcon(emergency.getType()) + " " + emergency.getType() + " — " + locationName(emergency.getLocation()),
                            "Incident ID: " + emergency.getId().substring(0, 8) + " • " + emergency.getLocation(),
                            severity,
                            severityColor,
                            statusText,
                            statusColor
                    )
            );
        }

        // Read actual vehicles from the simulation engine.
        List<EmergencyVehicle> vehicles = simulationEngine.getVehicles();
        vehicleCount.setText(
                String.valueOf(vehicles.size())
        );
        for (EmergencyVehicle vehicle : vehicles) {
            String status = simulationEngine.getVehicleActivity(vehicle.getId());
            String statusColor = status.equals("Available") ? GREEN
                    : status.equals("On scene") ? PURPLE : BLUE;

            String vehicleType = switch (vehicle.getType()) {
                case FIRE_ENGINE -> "Fire Engine";
                case AMBULANCE -> "Ambulance";
                case RESCUE_VEHICLE -> "Rescue Vehicle";
            };

            vehicleList.getChildren().add(
                    createVehicleCard(
                            vehicle.getId(),
                            vehicleType,
                            locationName(vehicle.getCurrentLocation()) + " (" + vehicle.getCurrentLocation() + ")",
                            status,
                            statusColor
                    )
            );
        }

        // Update the summary counters.
        pendingValue.setText(
                String.valueOf(simulationEngine.getPendingEmergencyCount())
        );

        respondingValue.setText(
                String.valueOf(simulationEngine.getRespondingEmergencyCount())
        );

        onSceneValue.setText(
                String.valueOf(simulationEngine.getOnSceneEmergencyCount())
        );

        resolvedValue.setText(
                String.valueOf(simulationEngine.getResolvedEmergencyCount())
        );

        // Response timer measures the whole active vehicle journey, not wall-clock uptime.
        double elapsedResponseMinutes = simulationEngine.getActiveResponseElapsedMinutes();
        int elapsedResponseSeconds = (int) Math.floor(elapsedResponseMinutes * 60.0);
        timeValue.setText(String.format("%02d:%02d:%02d",
                elapsedResponseSeconds / 3600,
                (elapsedResponseSeconds % 3600) / 60,
                elapsedResponseSeconds % 60));
        double plannedMinutes = simulationEngine.getEstimatedFullResponseDurationMinutes();
        if (plannedMinutes <= 0) {
            durationValue.setText("Awaiting dispatch");
        } else {
            int plannedSeconds = (int) Math.ceil(plannedMinutes * 60.0);
            durationValue.setText(String.format("~%dm %02ds", plannedSeconds / 60, plannedSeconds % 60));
        }

        // Update performance metrics from actual simulation data.
        double averageResponse = simulationEngine.getAverageResponseTimeMinutes();
        avgResponseMetric.setText(Double.isNaN(averageResponse)
                ? "—" : String.format("%.1f min", averageResponse));
        totalIncidentsMetric.setText(String.valueOf(emergencies.size()));

        resolvedMetric.setText(
                String.valueOf(simulationEngine.getResolvedEmergencyCount())
        );

        activeResponsesMetric.setText(
                String.valueOf(
                        vehicles.size() - simulationEngine.getAvailableVehicleCount()
                )
        );
    }

    private String emergencyIcon(CrisisType type) {
        return switch (type) {
            case FIRE -> "🔥";
            case ACCIDENT -> "🚑";
            case FLOOD -> "🌊";
        };
    }

    private void startLiveClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Runnable update = () -> clockValue.setText(LocalTime.now().format(formatter));
        update.run();
        liveClock.getKeyFrames().setAll(new KeyFrame(Duration.seconds(1), event -> update.run()));
        liveClock.setCycleCount(Timeline.INDEFINITE);
        liveClock.play();
        autoSimulation.getKeyFrames().setAll(new KeyFrame(Duration.seconds(1), event -> {
            if (simulationRunning) {
                try {
                    simulationEngine.advanceTime(1.0 / 60.0);
                    simulationClockLabel.setText(String.format("Simulation: %.2f min • real-time playback", simulationEngine.getSimulationTime()));
                    refreshDashboard();
                    renderNetworkMapGraphics();
                } catch (RuntimeException exception) {
                    autoSimulation.stop();
                    simulationRunning = false;
                    showError("Simulation stopped", exception.getMessage());
                }
            }
        }));
        autoSimulation.setCycleCount(Timeline.INDEFINITE);
    }

    private void startSimulation(Button startButton, Button pauseButton) {
        simulationRunning = true;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        autoSimulation.play();
    }

    private void pauseSimulation(Button startButton, Button pauseButton) {
        simulationRunning = false;
        autoSimulation.pause();
        startButton.setDisable(false);
        pauseButton.setDisable(true);
    }

    private void showNavigationSection(String itemName) {
        String content;
        if (itemName.contains("Incidents")) {
            content = simulationEngine.getEmergencies().isEmpty() ? "No incidents have been created yet. Use Create Emergency to add one." :
                    simulationEngine.getEmergencies().stream().map(e -> emergencyIcon(e.getType()) + " " + e.getType() + " at " + locationName(e.getLocation()) + " — " + e.getStatus()).reduce((a,b) -> a + "\n" + b).orElse("No incidents.");
        } else if (itemName.contains("Vehicles")) {
            content = simulationEngine.getVehicles().stream().map(v -> v.getId() + " — " + v.getType() + " — " + simulationEngine.getVehicleActivity(v.getId()) + " at " + locationName(v.getCurrentLocation())).reduce((a,b) -> a + "\n" + b).orElse("No vehicles.");
        } else if (itemName.contains("Analytics")) {
            content = "Locations: " + cityGraph.getLocationIds().size() + "\nRoads: " + cityGraph.getRoads().size() +
                    "\nIncidents: " + simulationEngine.getEmergencies().size() + "\nPending: " + simulationEngine.getPendingEmergencyCount() +
                    "\nResponding: " + simulationEngine.getRespondingEmergencyCount() + "\nOn scene: " + simulationEngine.getOnSceneEmergencyCount() +
                    "\nResolved: " + simulationEngine.getResolvedEmergencyCount();
        } else if (itemName.contains("Settings")) {
            content = "Map controls: left-click a road to cycle LOW → MEDIUM → HIGH traffic. Right-click a road to close or reopen it. Road colors: teal = low, amber = medium, pink = high, dashed gray = closed.";
        } else {
            content = "Dashboard overview: create an emergency, start or pause automatic simulation, advance time manually, and inspect live vehicle and incident status.";
        }
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(itemName.replaceAll("^[^ ]+ ", ""));
        dialog.setHeaderText(itemName);
        dialog.setContentText(content);
        dialog.getDialogPane().setMinWidth(420);
        dialog.showAndWait();
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

    private HBox createTimeDisplayRow(
            String title,
            Label valueLabel,
            String valueColor) {

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 11px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        valueLabel.setStyle(
                "-fx-text-fill: " + valueColor + ";" +
                        " -fx-font-size: 12px;" +
                        " -fx-font-weight: bold;"
        );

        row.getChildren().addAll(titleLabel, spacer, valueLabel);

        return row;
    }

    private String panelStyle() {
        return "-fx-background-color: " + PANEL + ";" +
                " -fx-background-radius: 6;" +
                " -fx-border-color: " + BORDER + ";" +
                " -fx-border-radius: 6;" +
                " -fx-border-width: 1;";
    }

    private String buttonStyle() {
        return "-fx-background-color: " + PANEL_ALT + ";" +
                " -fx-text-fill: " + TEXT + ";" +
                " -fx-border-color: " + BORDER + ";" +
                " -fx-border-radius: 4;" +
                " -fx-background-radius: 4;" +
                " -fx-padding: 6 12 6 12;";
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-size: 13px;" +
                        " -fx-font-weight: bold;"
        );
        label.setWrapText(true);
        return label;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 11px;"
        );
        return label;
    }

    private HBox createMetaCountRow(String title, String count) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 12px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label(count);
        countLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-weight: bold;" +
                        " -fx-font-size: 12px;"
        );

        row.getChildren().addAll(titleLabel, spacer, countLabel);
        return row;
    }

    private HBox createStatusIndicatorRow(
            String key,
            String status,
            String statusColor) {

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(3.5, Color.web(statusColor));

        Label keyLabel = new Label(key);
        keyLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 11px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(status);
        statusLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-size: 11px;"
        );

        row.getChildren().addAll(dot, keyLabel, spacer, statusLabel);
        return row;
    }

    private HBox createSummaryCounter(
            String title,
            Label valueLabel,
            String hexColor) {

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 11px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        valueLabel.setStyle(
                "-fx-text-fill: " + hexColor + ";" +
                        " -fx-font-weight: bold;" +
                        " -fx-font-size: 12px;"
        );

        row.getChildren().addAll(titleLabel, spacer, valueLabel);
        return row;
    }

    private HBox createLegendIcon(
            String text,
            String hexColor,
            boolean isIncident) {

        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(
                4,
                isIncident ? Color.web(hexColor) : Color.TRANSPARENT
        );

        if (!isIncident) {
            dot.setStroke(Color.web(hexColor));
            dot.setStrokeWidth(1.5);
        }

        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 10px;"
        );

        item.getChildren().addAll(dot, label);
        return item;
    }

    private VBox createMetricCard(
            String title,
            Label valueLabel,
            String icon) {

        VBox card = new VBox(4);
        card.setPadding(new Insets(8));
        card.setPrefWidth(110);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: " + PANEL_ALT + ";" +
                        " -fx-background-radius: 4;" +
                        " -fx-border-color: " + BORDER + ";"
        );

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 9px;" +
                        " -fx-font-weight: bold;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        valueLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-size: 14px;" +
                        " -fx-font-weight: bold;"
        );

        HBox dataRow = new HBox(6, iconLabel, valueLabel);
        dataRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titleLabel, dataRow);
        return card;
    }

    private HBox createIncidentCard(
            String id,
            String details,
            String severity,
            String severityColor,
            String status,
            String statusColor) {

        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: " + PANEL_ALT + ";" +
                        " -fx-background-radius: 4;" +
                        " -fx-border-color: " + BORDER + ";"
        );

        VBox leftInfo = new VBox(2);

        Label idLabel = new Label(id);
        idLabel.setStyle(
                "-fx-text-fill: " + TEXT + ";" +
                        " -fx-font-weight: bold;" +
                        " -fx-font-size: 11px;"
        );

        Label severityBadge = new Label("SEVERITY: " + severity);
        severityBadge.setStyle(
                "-fx-text-fill: " + severityColor + ";" +
                        " -fx-font-size: 8px;" +
                        " -fx-font-weight: bold;" +
                        " -fx-padding: 1 4 1 4;"
        );

        HBox titleRow = new HBox(6, idLabel, severityBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label detailsLabel = new Label(details);
        detailsLabel.setWrapText(true);
        detailsLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        " -fx-font-size: 10px;"
        );

        leftInfo.getChildren().addAll(titleRow, detailsLabel);

        Region spacer = new Region();
        HBox.setHgrow(leftInfo, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(status);
        statusBadge.setStyle(
                "-fx-text-fill: " + statusColor + ";" +
                        " -fx-font-size: 9px;" +
                        " -fx-font-weight: bold;" +
                        " -fx-padding: 4 8 4 8;" +
                        " -fx-border-color: " + BORDER + ";" +
                        " -fx-border-radius: 4;"
        );

        card.getChildren().addAll(leftInfo, spacer, statusBadge);
        return card;
    }

    private HBox createVehicleCard(
            String id,
            String type,
            String location,
            String status,
            String statusColor) {

        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: " + PANEL_ALT + ";" +
                        " -fx-background-radius: 6;" +
                        " -fx-border-color: " + BORDER + ";" +
                        " -fx-border-radius: 6;"
        );

        String icon = type.equals("Ambulance") ? "🚑" :
                type.equals("Rescue Vehicle") ? "🚤" : "🚒";
        StackPane iconTile = new StackPane();
        iconTile.setMinSize(38, 38);
        iconTile.setPrefSize(38, 38);
        iconTile.setMaxSize(38, 38);
        iconTile.setStyle("-fx-background-color: #173149; -fx-background-radius: 8;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        iconTile.getChildren().add(iconLabel);

        VBox details = new VBox(3);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label title = new Label(id + "  •  " + type);
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label locationLabel = new Label("📍 " + location);
        locationLabel.setWrapText(true);
        locationLabel.setMaxWidth(Double.MAX_VALUE);
        locationLabel.setTooltip(new Tooltip(location));
        locationLabel.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 10px;");

        Label statusBadge = new Label(status);
        statusBadge.setWrapText(true);
        statusBadge.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        details.getChildren().addAll(title, locationLabel, statusBadge);
        card.getChildren().addAll(iconTile, details);
        return card;
    }

    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle(
                "-fx-background-color: " + PANEL_ALT + ";" +
                        " -fx-text-fill: " + TEXT + ";" +
                        " -fx-border-color: " + BORDER + ";" +
                        " -fx-border-radius: 4;" +
                        " -fx-background-radius: 4;"
        );
    }

    private void setupScrollPane(ScrollPane scrollPane) {
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMinHeight(100);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        " -fx-background-color: transparent;" +
                        " -fx-border-color: transparent;"
        );
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
