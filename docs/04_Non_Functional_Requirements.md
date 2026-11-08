# VOLTRIC – Non-Functional Requirements (NFR)

**Document:** 04 – Non-Functional Requirements  
**Project:** VOLTRIC – EV Charging Station Management System  
**Version:** 1.0 (Initial V1 Baseline)  
**Architecture:** Single-organization  
**Protocol:** OCPP 1.6J over WebSocket  
**Initial Capacity Target:** 100 simultaneously connected chargers  
**Status:** Draft for review

---

## 1. Purpose

This document defines the quality attributes and operational constraints that VOLTRIC must satisfy in V1.

Functional requirements describe what the system does. Non-functional requirements define how reliably, securely, efficiently, and maintainably it must perform those functions.

These requirements will guide architecture decisions, implementation, and testing.

## 2. Scope and Assumptions

- VOLTRIC V1 supports a single organization.
- The system uses Java 21, Spring Boot, Maven, and MySQL.
- Charger communication uses OCPP 1.6J over JSON WebSocket connections.
- The initial target is 100 simultaneously connected chargers.
- Testing will initially use simulators and a controlled development environment.
- Production availability and performance commitments are outside the scope of this academic V1 baseline.

## 3. Performance Requirements

| ID | Requirement |
|---|---|
| NFR-PERF-001 | The system shall support 100 simultaneously connected charger WebSocket sessions under the defined V1 test profile. |
| NFR-PERF-002 | Under the defined 100-charger test profile, normal CMS REST API requests should achieve a 95th-percentile response time of 2 seconds or less, excluding external network delays. |
| NFR-PERF-003 | Under the same test profile, valid OCPP messages should be parsed, validated, and dispatched to their relevant handlers within a 95th-percentile processing time of 1 second, excluding charger response time and external network delays. |
| NFR-PERF-004 | Database queries used in routine charger, connector, transaction, and telemetry operations should be indexed and monitored to identify slow queries. |
| NFR-PERF-005 | The system shall avoid unnecessary synchronous processing in the OCPP communication path that could delay message handling. |

**Measurement note:** Response-time targets are initial test targets. They must be measured on a documented machine configuration and representative database dataset.

## 4. Scalability Requirements

| ID | Requirement |
|---|---|
| NFR-SCALE-001 | The application shall support at least 100 simultaneously connected chargers in the initial V1 test environment. |
| NFR-SCALE-002 | The application shall support multiple connectors per charger, subject to configured charger capabilities. |
| NFR-SCALE-003 | The system shall handle concurrent OCPP messages from different chargers without mixing charger sessions or transaction data. |
| NFR-SCALE-004 | The application architecture shall separate communication handling, business services, and persistence to support future scaling. |
| NFR-SCALE-005 | Database access and connection-pool settings shall be configurable to support the defined workload. |

V1 does not promise horizontal scaling across multiple CMS instances. That requires a separate session-management and message-routing design.

## 5. Availability and Reliability Requirements

| ID | Requirement |
|---|---|
| NFR-REL-001 | The system shall detect charger communication loss through WebSocket disconnect events and configured heartbeat monitoring. |
| NFR-REL-002 | A disconnected charger shall not be incorrectly represented as actively connected. |
| NFR-REL-003 | The system shall allow a charger to reconnect and re-establish its OCPP communication session. |
| NFR-REL-004 | Temporary communication failures shall not silently corrupt or duplicate transaction records. |
| NFR-REL-005 | A failure affecting one charger session should not unnecessarily terminate other active charger sessions. |
| NFR-REL-006 | The system shall log unexpected application errors and communication failures with sufficient diagnostic context. |

## 6. Security Requirements

| ID | Requirement |
|---|---|
| NFR-SEC-001 | CMS user passwords shall be stored using a secure, adaptive password-hashing algorithm. Plain-text password storage is prohibited. |
| NFR-SEC-002 | Access to protected REST APIs shall require authentication. |
| NFR-SEC-003 | Role-based authorization shall restrict operations according to the user's assigned role and permissions. |
| NFR-SEC-004 | Charger connections shall be associated with registered charger identities. |
| NFR-SEC-005 | The application shall validate incoming OCPP messages before executing business operations. |
| NFR-SEC-006 | Production deployments shall use TLS-protected communication for REST and WebSocket endpoints. |
| NFR-SEC-007 | Database credentials, tokens, and other secrets shall not be committed to Git or exposed in application logs. |
| NFR-SEC-008 | Sensitive information shall be masked or excluded from application and audit logs where appropriate. |
| NFR-SEC-009 | Remote charger commands shall be restricted to authorized CMS users and validated before dispatch. |

## 7. Data Integrity and Consistency Requirements

| ID | Requirement |
|---|---|
| NFR-DATA-001 | Charger, connector, transaction, and meter-value records shall maintain valid database relationships. |
| NFR-DATA-002 | Database constraints shall prevent invalid references and duplicate records where uniqueness is required. |
| NFR-DATA-003 | Transaction start and stop operations shall preserve consistent transaction state and associated energy readings. |
| NFR-DATA-004 | Repeated or duplicate OCPP messages shall not create unintended duplicate transaction operations. |
| NFR-DATA-005 | Database operations involving multiple related changes shall use appropriate transaction boundaries. |
| NFR-DATA-006 | Meter readings shall retain their original measurement timestamps and units, with consistent normalization for stored values. |
| NFR-DATA-007 | Transaction energy consumption shall be calculated using valid meter readings and documented unit conversions. |

## 8. Fault Tolerance and Recovery Requirements

| ID | Requirement |
|---|---|
| NFR-REC-001 | The system shall handle unexpected charger disconnections without crashing the CMS application. |
| NFR-REC-002 | A charger shall be able to reconnect after a temporary network interruption. |
| NFR-REC-003 | The system shall maintain sufficient persisted transaction data to recover active transaction state after an application restart. |
| NFR-REC-004 | Recovery logic shall avoid creating duplicate active transactions for an already-existing charging session. |
| NFR-REC-005 | OCPP remote commands shall have configurable response timeouts and a recorded outcome such as accepted, rejected, timed out, or failed. |
| NFR-REC-006 | An unavailable charger shall not be treated as having successfully executed a remote command. |
| NFR-REC-007 | Application startup shall validate required configuration and database connectivity and report failures clearly. |

## 9. Logging and Monitoring Requirements

| ID | Requirement |
|---|---|
| NFR-MON-001 | The application shall maintain structured logs for important application and OCPP events. |
| NFR-MON-002 | Logs shall include relevant identifiers such as charger ID, connector ID, transaction ID, and OCPP message/action where available. |
| NFR-MON-003 | OCPP request-response processing shall be traceable using message or correlation identifiers. |
| NFR-MON-004 | Important user operations, including remote commands and configuration changes, shall be auditable. |
| NFR-MON-005 | The system shall record communication errors, validation failures, and unexpected application exceptions. |
| NFR-MON-006 | Logging shall avoid exposing passwords, authentication secrets, and other sensitive data. |

## 10. Maintainability and Extensibility Requirements

| ID | Requirement |
|---|---|
| NFR-MAINT-001 | The application shall use a modular architecture separating controllers, services, repositories, entities, DTOs, and OCPP communication components. |
| NFR-MAINT-002 | OCPP message handling shall be separated from business logic. |
| NFR-MAINT-003 | Business rules shall not be duplicated across controllers and OCPP message handlers. |
| NFR-MAINT-004 | Important business operations shall have automated unit or integration tests. |
| NFR-MAINT-005 | Database schema changes shall be version-controlled through a migration mechanism. |
| NFR-MAINT-006 | Configuration values shall be externalized instead of being unnecessarily hard-coded. |
| NFR-MAINT-007 | Code shall follow consistent naming, package organization, and exception-handling conventions. |

## 11. Compatibility and Interoperability Requirements

| ID | Requirement |
|---|---|
| NFR-COMP-001 | V1 charger communication shall follow the OCPP 1.6J JSON-over-WebSocket protocol. |
| NFR-COMP-002 | The system shall validate incoming messages against the relevant OCPP 1.6J message structures and supported operations. |
| NFR-COMP-003 | OCPP message handlers shall process valid protocol messages and return the appropriate protocol response or error. |
| NFR-COMP-004 | The system shall be tested with an OCPP 1.6J-compatible simulator. |
| NFR-COMP-005 | Unsupported OCPP actions or malformed messages shall be handled without crashing the application. |

## 12. Backup and Data Retention Requirements

| ID | Requirement |
|---|---|
| NFR-BACK-001 | The database shall support backup and restoration using documented procedures. |
| NFR-BACK-002 | Backup procedures shall include the records needed to restore charger, connector, transaction, and configuration data. |
| NFR-BACK-003 | Database restoration shall be tested in a controlled environment. |
| NFR-BACK-004 | Retention policies for OCPP logs, telemetry, and audit records shall be configurable and documented before production deployment. |

## 13. Initial Verification Strategy

| Area | Verification approach | Initial success condition |
|---|---|---|
| Concurrent connections | Connect 100 simulated chargers | All 100 sessions remain established during the defined test |
| REST performance | Run representative API load tests | 95th-percentile response time is at most 2 seconds under the test profile |
| OCPP processing | Send representative OCPP messages | 95th-percentile internal processing time is at most 1 second |
| Reliability | Disconnect and reconnect chargers | CMS remains operational and reconnects are handled correctly |
| Transaction integrity | Run start, meter-value, and stop flows | No unintended duplicate transactions or invalid energy calculations |
| Recovery | Restart CMS during an active transaction | Persisted transaction state is recovered according to the recovery design |
| Security | Test authentication and role restrictions | Unauthorized access and remote operations are rejected |
| Backup | Restore a database backup | Required records are restored and verified |

## 14. Out of Scope for V1

The following are not guaranteed by this initial NFR baseline:

- A contractual production SLA or 99.9% availability guarantee.
- Multi-instance horizontal scaling and distributed WebSocket session management.
- Support for more than 100 simultaneous chargers.
- OCPP 2.0.1 or later protocol support.
- High-availability database clustering or automated disaster recovery.
- Formal third-party security certification or penetration-test certification.

These can be evaluated as future engineering milestones.

## 15. Acceptance and Review

This document is the initial V1 NFR baseline for VOLTRIC.

Before implementation begins, the requirements should be reviewed for consistency with the project scope, functional requirements, architecture, and available test environment.

Any changes to capacity, performance targets, or security expectations shall be documented and version-controlled through the project's Git workflow.

**Phase 1 deliverable:** `docs/04_Non_Functional_Requirements.md`