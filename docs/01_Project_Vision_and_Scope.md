
# VOLTRIC
## EV Charging Station Management System

### 1. Project Overview

VOLTRIC is a centralized EV Charging Station Management
System designed to monitor, manage and remotely control
EV charging stations through the OCPP 1.6J protocol.

### 2. Project Objective

To develop a modular and scalable backend platform
that enables communication between EV charging stations
and a centralized management system.

### 3. Technology Stack

- Java 21
- Spring Boot
- Spring WebSocket
- OCPP 1.6J
- MySQL
- Spring Data JPA
- Hibernate
- Spring Security
- Maven
- JUnit 5
- Postman
- Git and GitHub

### 4. Primary System Actors

1. CMS Administrator
2. EV Charging Station
3. EV Driver

### 5. Core Functional Modules

- Charger Management
- Connector Management
- OCPP Communication
- Authorization Management
- Transaction Management
- Meter Value Management
- Remote Charger Management
- Security and Authentication
- Logging and Monitoring

### 6. V1 Project Scope

The initial version will support OCPP 1.6J using
JSON over WebSocket.

Supported operations will include:

- BootNotification
- Heartbeat
- StatusNotification
- Authorize
- StartTransaction
- StopTransaction
- MeterValues
- RemoteStartTransaction
- RemoteStopTransaction
- Reset
- UnlockConnector

### 7. Out of Scope

- OCPP 2.0.1
- Payment Gateway
- Mobile Application
- EV Roaming
- Advanced Billing
- Smart Charging
- Firmware Management

### 8. Success Criteria

The system should successfully establish charger
connections, process OCPP messages, manage charging
transactions, collect telemetry and execute supported
remote commands.

### 9. Development Approach

The project will follow a modular, backend-first
development approach with incremental implementation,
Git-based version control and simulator-based testing.
