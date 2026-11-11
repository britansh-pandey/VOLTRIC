# VOLTRIC – Software Requirements Specification (SRS)

**Project:** VOLTRIC – EV Charging Station Management System  
**Document:** Software Requirements Specification  
**Version:** 1.0  
**Status:** V1 Baseline – Pending Review and Approval  
**Architecture:** Feature-Based Modular Monolith  
**Backend:** Java 21 + Spring Boot  
**Database:** MySQL 8.x  
**Protocol:** OCPP 1.6J  
**Organization Model:** Single Organization

---

## 1. Purpose

This document establishes the approved software requirements and technical boundaries for VOLTRIC V1.

It consolidates the project vision, stakeholder requirements, functional requirements, non-functional requirements, OCPP scope, system workflows, high-level architecture, database design and REST API specification.

It will serve as the reference for implementation, testing and future requirement changes.

## 2. Product Overview

VOLTRIC is a centralized EV Charging Station Management System designed to manage and monitor electric vehicle charging stations through OCPP 1.6J.

The platform provides:

- Charger registration and monitoring.
- Connector status management.
- RFID/idTag authorization.
- Charging transaction management.
- Meter value and telemetry collection.
- Remote charger management.
- Charger configuration management.
- CMS user and role management.
- Communication and audit logging.

The system will initially support one organization managing multiple charging stations.

## 3. Stakeholders and Roles

### 3.1 Super Administrator

- Register and manage chargers.
- Manage CMS users and roles.
- Manage RFID/idTag records.
- Monitor all chargers and transactions.
- Execute authorized remote commands.
- Manage charger configurations.
- Access administrative logs.

### 3.2 Charging Station Operator

- Monitor assigned charging stations.
- View connector and transaction information.
- View telemetry for assigned chargers.
- Execute authorized remote start and remote stop operations on assigned chargers.

### 3.3 System Administrator

- Maintain backend infrastructure.
- Monitor application health.
- Manage deployment, backups and infrastructure failures.
- Does not automatically receive CMS business permissions.

### 3.4 EV Driver

- Presents an RFID/idTag to the charging station.
- Requests authorization through the charger.
- Starts and stops charging through charger interaction.

No EV driver mobile application is included in V1.

### 3.5 EV Charging Station

- Establishes an OCPP WebSocket connection.
- Sends charger status and operational events.
- Sends authorization and transaction requests.
- Sends meter readings.
- Receives supported CMS-initiated commands.

## 4. System Scope

### 4.1 Included in V1

1. CMS authentication and authorization.
2. CMS user and role management.
3. Charger registration and monitoring.
4. Connector management.
5. RFID/idTag authorization.
6. Charging transaction lifecycle.
7. Meter value and telemetry processing.
8. Remote charger commands.
9. Charger configuration operations.
10. OCPP communication logging.
11. Audit logging.
12. Dashboard and monitoring APIs.

### 4.2 Excluded from V1

- OCPP 2.0.1 and OCPP 2.1.
- Multi-tenant organization management.
- EV driver mobile application.
- Payment gateway integration.
- EV roaming.
- Advanced billing and invoicing.
- Smart charging.
- Firmware update management.
- Distributed WebSocket session management.
- Microservices deployment.

## 5. Functional Requirements

### FR-01: Authentication and User Management

- The system shall authenticate CMS users.
- The system shall support role-based access control.
- The system shall support Super Administrator, Charging Station Operator and System Administrator roles.
- The system shall allow authorized administrators to create and manage CMS users.
- The system shall support operator-to-charger assignments.
- The system shall restrict users to their authorized resources.

### FR-02: Charger Management

- The system shall allow Super Administrators to pre-register chargers.
- Every charger shall have a unique chargePointId.
- The system shall maintain charger manufacturer, model and identity information.
- Unknown chargers shall not be automatically registered.
- The system shall reject unauthorized charger connections.
- The system shall maintain charger communication status separately from operational status.

### FR-03: OCPP Communication

- The system shall support OCPP 1.6J JSON over WebSocket.
- The system shall accept charger WebSocket connections.
- The system shall parse and route OCPP messages.
- The system shall support CALL, CALLRESULT and CALLERROR frames.
- The system shall correlate outgoing requests and incoming responses using message IDs.
- The system shall handle malformed messages and communication failures.

### FR-04: Connector Management

- A charger shall support multiple physical connectors.
- Each connector shall be associated with one charger.
- Connector IDs shall be unique within a charger.
- Connector ID 0 shall be treated as a charge-point-level OCPP identifier, not a physical connector.
- The system shall maintain the latest known operational status of connectors.
- The system shall maintain connector status history.

### FR-05: Authorization

- The system shall support RFID/idTag-based authorization.
- The system shall validate idTags received through OCPP.
- The system shall check idTag status and expiry.
- The system shall return appropriate OCPP authorization results.
- The system shall allow authorized administrators to manage idTag records.

### FR-06: Transaction Management

- The system shall process StartTransaction requests.
- The system shall generate CMS-managed OCPP transaction IDs.
- The system shall process StopTransaction requests.
- The system shall maintain transaction history.
- The system shall prevent two active transactions from claiming the same physical connector.
- The system shall calculate energy consumption from validated meter readings.
- The system shall support active transaction recovery after application restart.
- A charger disconnection shall not automatically complete an active transaction.

### FR-07: Meter Value Management

- The system shall process OCPP MeterValues messages.
- The system shall support multiple meter-value entries and sampled values.
- The system shall preserve original sampled values and units.
- The system shall associate readings with charger, connector and transaction where applicable.
- The system shall validate timestamps and measurement values.
- The system shall expose stored telemetry through REST APIs.

### FR-08: Remote Charger Management

The system shall support:

- RemoteStartTransaction.
- RemoteStopTransaction.
- Reset.
- UnlockConnector.

The system shall track command status, message correlation, response and timeout.

The system shall distinguish command acceptance from actual operation completion.

### FR-09: Monitoring and Logging

- The system shall provide charger monitoring APIs.
- The system shall provide active transaction monitoring.
- The system shall maintain charger status history.
- The system shall maintain OCPP communication logs.
- The system shall maintain CMS audit logs.
- The system shall provide dashboard summary information.

### FR-10: Configuration Management

- The system shall support GetConfiguration.
- The system shall support ChangeConfiguration.
- The system shall maintain the latest known charger configuration.
- The system shall preserve configuration change history.
- Configuration changes shall require Super Administrator authorization.

## 6. OCPP 1.6J Requirements

### Charger-Initiated Actions

| OCPP Action | Purpose |
|---|---|
| BootNotification | Charger registration and boot information |
| Heartbeat | Charger communication monitoring |
| StatusNotification | Connector and charger operational status |
| Authorize | RFID/idTag authorization |
| StartTransaction | Start charging session |
| MeterValues | Telemetry transmission |
| StopTransaction | Complete charging session |

### CMS-Initiated Actions

| OCPP Action | Purpose |
|---|---|
| RemoteStartTransaction | Request remote charging start |
| RemoteStopTransaction | Request remote charging stop |
| Reset | Request charger reset |
| UnlockConnector | Request connector unlocking |
| GetConfiguration | Retrieve charger configuration |
| ChangeConfiguration | Change supported charger configuration |

The system shall support OCPP 1.6J message frame types CALL, CALLRESULT and CALLERROR.

## 7. Non-Functional Requirements

### Performance

- The initial engineering target shall be 100 simultaneously connected chargers.
- REST API response time target: p95 within 2 seconds under the defined test profile.
- Internal OCPP message processing target: p95 within 1 second under the defined test profile, excluding external charger response time.

### Reliability

- The system shall handle charger disconnections.
- The system shall recover active transaction state after application restart.
- The system shall handle duplicate and invalid OCPP messages safely.
- The system shall track remote command timeouts.

### Security

- CMS APIs shall require authentication except explicitly public authentication endpoints.
- The system shall implement role-based authorization.
- Passwords shall be securely hashed.
- Charger connections shall be validated.
- Production communication shall use HTTPS/WSS.
- Sensitive actions shall be audited.

### Data Integrity

- Primary and foreign key constraints shall protect relational integrity.
- Transaction IDs shall be unique and protocol-compatible.
- Duplicate active connector sessions shall be prevented.
- Timestamps shall be stored in UTC.
- Historical transaction and audit records shall be retained according to an approved retention policy.

### Maintainability

- Business capabilities shall be organized into feature-based modules.
- OCPP communication shall be separated from business logic.
- Database changes shall use Flyway migrations.
- The application shall have unit and integration tests.

The performance targets are engineering test objectives, not current guarantees or production SLAs.

## 8. Approved Architecture

### 8.1 Architectural Style

Feature-Based Modular Monolith.

### 8.2 Backend Technology

- Java 21.
- Spring Boot.
- Spring Web.
- Spring WebSocket.
- Spring Security.
- Spring Data JPA.
- Hibernate.
- Maven.
- MySQL 8.x.
- Flyway.
- JUnit 5 and Mockito.

### 8.3 Official Package Structure

```text
com/voltric/cms/
│
├── VoltricApplication.java
│
├── common/
│   ├── config/
│   ├── exception/
│   ├── response/
│   └── util/
│
├── modules/
│   ├── authentication/
│   ├── user/
│   ├── charger/
│   ├── connector/
│   ├── authorization/
│   ├── transaction/
│   ├── metervalue/
│   ├── remotecommand/
│   └── configuration/
│
└── ocpp/
    ├── websocket/
    ├── protocol/
    ├── message/
    ├── handler/
    ├── router/
    └── session/
```

Each business module may contain its own controller, service, repository, entity, DTO, mapper and enum packages as required.

## 9. Database Baseline

The logical database design shall use MySQL 8.x with InnoDB.

The baseline consists of the following 18 tables:

1. users
2. roles
3. user_roles
4. refresh_tokens
5. user_charger_assignments
6. chargers
7. connectors
8. charger_status_logs
9. id_tags
10. transactions
11. active_connector_sessions
12. meter_value_batches
13. meter_samples
14. remote_commands
15. charger_configurations
16. configuration_change_logs
17. ocpp_message_logs
18. audit_logs

The implementation shall preserve the following distinctions:

- Internal database IDs versus OCPP transaction IDs.
- Charger communication status versus connector operational status.
- Transaction lifecycle status versus command status.
- Meter-value batches versus individual meter samples.

The detailed table definitions, relationships, constraints and indexes are specified in the database design document.

## 10. REST API Baseline

REST APIs shall use the base path:

`/api/v1`

The major API groups are:

- `/auth`
- `/users`
- `/chargers`
- `/connectors`
- `/id-tags`
- `/transactions`
- `/meter-values`
- `/chargers/{chargerId}/commands`
- `/commands`
- `/chargers/{chargerId}/configuration`
- `/monitoring`

Protected CMS endpoints shall use JWT-based authentication and backend-enforced authorization.

Refresh tokens shall be revocable and stored securely in hashed form. Logout shall revoke the relevant refresh token. Existing access tokens shall expire naturally unless a separate access-token revocation mechanism is later approved.

Remote commands shall use asynchronous processing. An API response confirming command submission or charger acceptance shall not be interpreted as proof that charging has started or stopped.

## 11. Charger and Transaction State Rules

The system shall maintain separate state categories.

### Charger Communication State

- CONNECTED
- DISCONNECTED
- RECONNECTING

### Connector Operational State

- AVAILABLE
- PREPARING
- CHARGING
- SUSPENDED_EV
- SUSPENDED_EVSE
- FINISHING
- RESERVED
- UNAVAILABLE
- FAULTED

### Transaction State

- ACTIVE
- COMPLETED
- RECOVERY_PENDING

### Remote Command State

- PENDING
- SENT
- ACCEPTED
- REJECTED
- FAILED
- TIMEOUT

The application shall not treat these categories as interchangeable.

## 12. Security and Access Baseline

- Super Administrators have administrative access across the organization.
- Charging Station Operators are restricted to assigned chargers.
- Operators can request remote start and stop operations on assigned chargers.
- Super Administrators can execute all supported V1 remote commands.
- Configuration changes are restricted to Super Administrators.
- System Administrators do not automatically receive CMS business permissions.
- Authorization must be enforced at the backend API and service layers.

## 13. Initial Capacity and Deployment Baseline

The initial performance validation target is 100 simultaneously connected chargers.

V1 will initially use:

- One Spring Boot application instance.
- In-memory WebSocket session management.
- MySQL-backed business persistence.
- Configurable command and connection timeouts.

Horizontal scaling and distributed session management are excluded from V1.

## 14. Testing Requirements

The system shall be tested through:

- Unit tests.
- Service and repository integration tests.
- REST API tests.
- OCPP protocol tests.
- WebSocket connection tests.
- Simulator-based charging tests.
- Transaction lifecycle tests.
- Remote command tests.
- Application restart and recovery tests.
- Load testing against the 100-connection target.

## 15. Phase 1 Deliverable Traceability

| Step | Deliverable | Baseline reference |
|---|---|---|
| 1 | Project Vision and Scope | Product objectives and V1 boundaries |
| 2 | Stakeholder Analysis | Stakeholders, roles and permissions |
| 3 | Functional Requirements | Functional requirements FR-01 to FR-10 |
| 4 | Non-Functional Requirements | Performance, reliability, security and maintainability |
| 5 | OCPP Scope and Message Matrix | Supported OCPP 1.6J actions |
| 6 | System Workflow Design | Charger, authorization, transaction and recovery workflows |
| 7 | High-Level System Architecture | Modular monolith and package structure |
| 8 | Database Design and ERD | Logical database model |
| 9 | REST API Specification | CMS API contract |
| 10 | SRS Review and Freeze | Consolidated V1 baseline |

## 16. Change Management

After approval, this SRS becomes the V1 requirements baseline.

Any future change to requirements must:

1. Identify the affected requirement.
2. Explain why the change is necessary.
3. Identify affected modules, APIs, database tables and tests.
4. Assess scope, compatibility and implementation impact.
5. Be documented and reviewed.
6. Be merged through the project's GitHub pull-request workflow.

Changes that introduce excluded features require explicit scope approval.

## 17. Phase 1 Exit Criteria

Phase 1 can be marked complete when:

- All ten design deliverables exist.
- Cross-document inconsistencies have been reviewed.
- Open V1 assumptions have been resolved or explicitly deferred.
- The SRS baseline has been reviewed.
- Required documentation branches have been merged into `main`.
- The V1 scope is approved.

**SRS Status:** Pending final review and approval.

---

**End of Document – VOLTRIC Software Requirements Specification V1.0**