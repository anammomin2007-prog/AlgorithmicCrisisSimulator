package acs.config;

import acs.graph.CityGraph;
import acs.model.Location;
import acs.model.Road;
import acs.model.enums.TrafficLevel;

public class NexoraCity {

    public static CityGraph createCity() {
        CityGraph graph = new CityGraph();

        // Create 24 locations
        for (int i = 1; i <= 24; i++) {
            String locationId = String.format("LOC_%02d", i);
            String locationName = "Nexora Location " + i;

            graph.addLocation(
                    new Location(locationId, locationName)
            );
        }

        // Create 38 roads connecting the locations
        int[][] roadConnections = {
                {1, 2}, {1, 3}, {19, 20}, {20, 21},
                {2, 5}, {2, 6}, {21, 22}, {3, 6},
                {3, 7}, {4, 7}, {4, 8}, {1, 4},
                {5, 9}, {5, 10}, {6, 7}, {6, 10},
                {6, 11}, {22, 23}, {7, 11}, {7, 12},
                {8, 12}, {8, 13}, {9, 10}, {9, 14},
                {10, 11}, {10, 14}, {10, 15}, {23, 24},
                {11, 15}, {11, 16}, {12, 13}, {12, 16},
                {13, 17}, {14, 15}, {14, 18}, {7, 8},
                {16, 17}, {18, 19}
        };

        for (int i = 0; i < roadConnections.length; i++) {
            int from = roadConnections[i][0];
            int to = roadConnections[i][1];

            String roadId = String.format("RD_%02d", i + 1);
            String fromId = String.format("LOC_%02d", from);
            String toId = String.format("LOC_%02d", to);

            double baseTravelTime = 2 + (i % 5);

            graph.addRoad(
                    new Road(
                            roadId,
                            fromId,
                            toId,
                            baseTravelTime,
                            TrafficLevel.LOW
                    )
            );
        }

        return graph;
    }
}