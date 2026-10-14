# Algorithmic Crisis Simulator — Project Contract

**Project Name:** Algorithmic Crisis Simulator
**City:** Nexora
**Language:** Java 21
**GUI:** JavaFX
**Build Tool:** Maven
**Testing:** JUnit 5
**Team Size:** 5 members

**Status:** Draft — requires team review and approval.

## 1. Project Objective

Build an emergency response simulation system for the fictional city of Nexora. The system will simulate emergencies, calculate suitable routes, assign available emergency vehicles, and display response information and analytics through a JavaFX interface.

The goal is to build one integrated, working application rather than five disconnected modules.

## 2. City and Simulation Data

The initial scenario will contain:

* 24 locations, identified as `LOC_01` through `LOC_24`.
* 38 roads, identified as `RD_01` through `RD_38`.
* 6 emergency vehicles.
* Two-way roads unless a road is explicitly defined otherwise.

Traffic levels and their travel-time multipliers:

| Traffic Level | Multiplier |
| ------------- | ---------: |
| LOW           |        1.0 |
| MEDIUM        |        1.5 |
| HIGH          |        2.0 |

A road's effective travel time is:

`Effective Travel Time = Base Travel Time × Traffic Multiplier`

Closed roads must be excluded from new route calculations.

## 3. Emergency Types

| Incident Type | Required Vehicle Type |
| ------------- | --------------------- |
| FIRE          | FIRE_ENGINE           |
| ACCIDENT      | AMBULANCE             |
| FLOOD         | RESCUE_VEHICLE        |

The initial implementation will use these compatibility rules.

## 4. Incident Priority

Incident severity scores:

| Severity | Score |
| -------- | ----: |
| CRITICAL |   100 |
| HIGH     |    70 |
| MEDIUM   |    40 |
| LOW      |    10 |

Priority formula:

`Priority = Severity Score + min(2 × Waiting Time, 30)`

Waiting time is measured in simulated minutes.

Incidents are considered in this order:

1. Higher priority first.
2. Older incident first when priorities are equal.
3. Incident ID in ascending order if the previous criteria are equal.

## 5. Incident and Vehicle Statuses

Incident statuses:

* `PENDING`
* `ASSIGNED`
* `RESPONDING`
* `ON_SCENE`
* `RESOLVED`
* `CANCELLED`

Vehicle statuses:

* `AVAILABLE`
* `RESPONDING`
* `ON_SCENE`

A vehicle must not be assigned to multiple active incidents simultaneously.

## 6. Shared Data Models

The team will use common model classes rather than creating separate versions inside individual modules.

### Location

Represents a city location.

Expected information:

* Location ID
* Display name

### Road

Represents a connection between two locations.

Expected information:

* Road ID
* Endpoint location IDs
* Positive base travel time in simulated minutes
* Traffic level
* Whether the road is closed

### Incident

Represents an emergency event.

Expected information:

* Incident ID
* Incident type
* Severity
* Creation time
* Current status
* Incident location
* Assigned vehicle ID, if any

### EmergencyVehicle

Represents an emergency response vehicle.

Expected information:

* Vehicle ID
* Vehicle type
* Current status
* Current location

### Route

Represents a calculated route.

Expected information:

* Ordered location IDs
* Ordered road IDs
* Total estimated travel time

Exact Java field types and constructors must be agreed upon before the relevant classes are implemented.

## 7. Module Responsibilities

### Member 1 — Dynamic Route Optimizer

* Represent the city as a graph.
* Store roads using an adjacency-list structure.
* Implement Dijkstra's shortest-path algorithm.
* Account for traffic and closed roads.
* Test route calculations and unreachable destinations.

### Member 2 — Emergency Event Simulator

* Create incidents manually and/or automatically.
* Track incident creation time and lifecycle.
* Update incident status as the simulation progresses.
* Provide incidents to the allocation module.

### Member 3 — Resource Allocation Optimizer

* Calculate incident priority.
* Select compatible available vehicles.
* Choose the vehicle with the lowest valid estimated arrival time.
* Use vehicle ID to break equal-ETA ties.
* Reserve a vehicle immediately after assigning it.
* Leave an incident pending if no suitable vehicle or valid route exists, and provide an understandable reason.

This allocation approach is a greedy heuristic; it is not guaranteed to find a globally optimal allocation.

### Member 4 — Decision Analytics Engine

* Calculate response-time metrics.
* Track vehicle utilization.
* Compare agreed response strategies.
* Provide understandable results to the interface.

### Member 5 — Crisis Command Interface

* Build the JavaFX dashboard.
* Display city locations, roads, incidents, vehicles, and statuses.
* Provide controls for starting the simulation and managing incidents or road conditions.
* Display response information and analytics.

The interface must call the core system rather than independently implementing routing or allocation rules.

## 8. Proposed Package Structure

Shared package structure:

* `acs.model` — shared model classes
* `acs.model.enums` — shared enumerations
* `acs.graph` — graph representation
* `acs.routing` — route calculation
* `acs.allocation` — resource allocation
* `acs.simulation` — simulation and incident lifecycle
* `acs.analytics` — response metrics
* `acs.ui` — JavaFX interface

All members must coordinate before changing shared classes, package names, or public method signatures.

## 9. Proposed Routing Interface

The routing module proposes the following API:

* `CityGraph.addLocation(Location location)`
* `CityGraph.addRoad(Road road)`
* `CityGraph.getLocation(String locationId)`
* `CityGraph.getOutgoingRoads(String locationId)`
* `RouteOptimizer.findRoute(CityGraph graph, String startLocationId, String destinationLocationId)`

`findRoute` is proposed to return `Optional<Route>`.

Expected behaviour:

* Return a route with the lowest total traffic-adjusted travel time when one exists.
* Return `Optional.empty()` when no route exists.
* A route from a location to itself is valid and has zero travel time.
* Invalid location IDs must be handled consistently and documented.
* A two-way road should be available for traversal in both directions.

These method signatures and behaviours are proposals and must be reviewed before implementation.

## 10. Technical Rules

* Use Java 21.
* Use JavaFX for the graphical interface.
* Use Maven for dependencies and builds.
* Use JUnit 5 for automated tests.
* Keep routing, allocation, simulation, and analytics logic independent of JavaFX.
* Begin with in-memory data; do not add a database unless the core system is working and the team agrees.
* Avoid duplicate shared classes.
* Do not change a shared interface without communicating the change to the team.
* Test modules individually and test the integrated application.
* Document known limitations honestly.

## 11. Decisions Requiring Team Agreement

Before implementation, the team must confirm:

1. The exact Java field types, constructors, and access methods for shared models.
2. How simulated time will be represented and advanced.
3. How response time and vehicle utilization will be calculated.
4. How road closures and traffic changes will affect an ongoing response.
5. The exact public interfaces connecting the five modules.
6. Which initial city map, road connections, and vehicle starting locations will be used.

## 12. Integration and Acceptance Criteria

The minimum integrated demonstration should show:

1. A city map or graph with locations and roads.
2. Different traffic levels and a road closure.
3. Creation of an emergency incident.
4. Calculation of a valid route, or an understandable indication that no route exists.
5. Assignment of a compatible available vehicle.
6. Updates to incident and vehicle statuses.
7. Display of basic response-time or utilization analytics.
8. Automated tests for important routing and allocation cases.

The team should prioritize a working end-to-end scenario before optional features or visual polish.

---

**End of proposed project contract.**
