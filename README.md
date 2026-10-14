# Algorithmic Crisis Simulator

An intelligent emergency response and resource optimization system designed to simulate emergencies in a fictional city and support faster, data-driven response decisions.

## Project Overview

The simulator is being developed around the fictional city of **Nexora**. It models emergency incidents, road conditions, emergency vehicles, and resource allocation to explore how algorithms can improve crisis response.

## Planned Capabilities

- Simulate fires, accidents, and floods.
- Represent the city as a graph of connected locations.
- Calculate efficient routes using travel times and traffic conditions.
- Account for road closures when calculating routes.
- Assign compatible emergency vehicles to incidents.
- Analyze response performance using decision metrics.
- Provide a JavaFX-based command interface.

## Technology Stack

- Java 21
- JavaFX
- Maven
- JUnit 5
- Git and GitHub

## Current Progress

- [x] Initial Maven project setup
- [x] Road model with traffic levels and road-closure status
- [x] City graph using an adjacency list
- [x] Route result model
- [x] Initial route optimizer using Dijkstra's algorithm
- [x] Automated tests for graph, road, and routing behavior
- [ ] Emergency event simulation
- [ ] Emergency vehicle allocation
- [ ] Decision analytics
- [ ] Complete JavaFX command interface
- [ ] End-to-end system integration

## Project Status

**In development.** The graph and routing foundations are being implemented and tested. The remaining modules and full system integration are still in progress.

## Running the Project

The project uses Java 21 and Maven. Open it in IntelliJ IDEA, allow Maven to load the project, and run the Maven goal `javafx:run` to launch the JavaFX application.

## Testing

The project uses JUnit 5 for automated tests covering graph behavior, road validation, route calculation, traffic changes, road closures, and unreachable destinations.

## Team Project

This is a collaborative college project. Individual modules are being developed and integrated into a single emergency response simulation.