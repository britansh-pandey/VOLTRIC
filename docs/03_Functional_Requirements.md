# VOLTRIC
# Functional Requirements Specification (FRS)

**Document ID:** VOLTRIC-FRS-001  
**Version:** 1.0  
**Project:** EV Charging Station Management System  
**Protocol:** OCPP 1.6J  
**Status:** Draft for Review

---

## 1. Introduction

This document defines the functional requirements of VOLTRIC, a centralized EV Charging Station Management System.

It describes the operations, functionalities and expected behavior of the system.

## 2. System Scope

VOLTRIC V1 will support a single organization managing multiple EV charging stations through a centralized backend.

The system will provide:

- User management.
- Charger management.
- OCPP communication.
- Connector management.
- Authorization.
- Transaction management.
- Meter value processing.
- Remote charger management.
- Monitoring and logging.
- Configuration management.

## 3. User Management Requirements

- FR-USR-001: The system shall allow the Super Administrator to create CMS user accounts.
- FR-USR-002: The system shall authenticate users using valid credentials.
- FR-USR-003: The system shall assign roles to CMS users.
- FR-USR-004: The system shall restrict operations according to user permissions.
- FR-USR-005: The system shall allow administrators to deactivate user accounts.

## 4. Charger Management Requirements

- FR-CHG-001: The system shall allow administrators to register charging stations.
- FR-CHG-002: Every charger shall have a unique Charge Point ID.
- FR-CHG-003: The system shall allow administrators to update charger information.
- FR-CHG-004: The system shall maintain charger registration information.
- FR-CHG-005: The system shall display the current connectivity status of chargers.
- FR-CHG-006: The system shall maintain the last successful communication timestamp.
- FR-CHG-007: The system shall support multiple charging stations.

## 5. OCPP Communication Requirements

- FR-OCPP-001: The system shall establish WebSocket connections with registered charging stations.
- FR-OCPP-002: The system shall support OCPP 1.6J JSON message framing.
- FR-OCPP-003: The system shall identify chargers using their Charge Point ID.
- FR-OCPP-004: The system shall parse incoming OCPP messages.
- FR-OCPP-005: The system shall route messages to their respective handlers.
- FR-OCPP-006: The system shall generate valid OCPP responses.
- FR-OCPP-007: The system shall correlate incoming responses with outgoing commands using unique message IDs.
- FR-OCPP-008: The system shall handle WebSocket disconnections.
- FR-OCPP-009: The system shall maintain active charger communication sessions.
- FR-OCPP-010: The system shall handle invalid or unsupported OCPP messages appropriately.

## 6. Connector Management Requirements

- FR-CON-001: The system shall maintain connector information for each charger.
- FR-CON-002: The system shall maintain the operational status of each connector.
- FR-CON-003: The system shall process StatusNotification messages.
- FR-CON-004: The system shall associate connectors with their respective chargers.
- FR-CON-005: The system shall display connector availability.

## 7. Authorization Requirements

- FR-AUTH-001: The system shall maintain authorized idTag records.
- FR-AUTH-002: The system shall process incoming Authorize requests.
- FR-AUTH-003: The system shall validate received idTag credentials.
- FR-AUTH-004: The system shall return appropriate authorization status.
- FR-AUTH-005: The system shall reject invalid or blocked credentials.

## 8. Transaction Management Requirements

- FR-TXN-001: The system shall process StartTransaction messages.
- FR-TXN-002: The system shall validate charger, connector and authorization information before accepting a transaction.
- FR-TXN-003: The system shall generate a unique transaction identifier.
- FR-TXN-004: The system shall store transaction start information.
- FR-TXN-005: The system shall maintain active transaction status.
- FR-TXN-006: The system shall process StopTransaction messages.
- FR-TXN-007: The system shall calculate energy consumed using valid meter readings.
- FR-TXN-008: The system shall store transaction completion information.
- FR-TXN-009: The system shall prevent duplicate active transactions on the same connector.
- FR-TXN-010: The system shall maintain transaction history.

## 9. Meter Value Requirements

- FR-MTR-001: The system shall process MeterValues messages.
- FR-MTR-002: The system shall extract sampled values from incoming messages.
- FR-MTR-003: The system shall support energy measurements.
- FR-MTR-004: The system shall support power measurements.
- FR-MTR-005: The system shall support voltage and current measurements.
- FR-MTR-006: The system shall associate telemetry with the correct charger and connector.
- FR-MTR-007: The system shall associate transaction-related telemetry with the relevant transaction.
- FR-MTR-008: The system shall maintain timestamped telemetry history.
- FR-MTR-009: The system shall expose historical meter data through management APIs.

## 10. Remote Management Requirements

- FR-RMT-001: The system shall support RemoteStartTransaction.
- FR-RMT-002: The system shall support RemoteStopTransaction.
- FR-RMT-003: The system shall support Reset.
- FR-RMT-004: The system shall support UnlockConnector.
- FR-RMT-005: The system shall send remote commands through active WebSocket sessions.
- FR-RMT-006: The system shall process CALLRESULT responses.
- FR-RMT-007: The system shall process CALLERROR responses.
- FR-RMT-008: The system shall maintain remote command execution records.

## 11. Monitoring and Logging Requirements

- FR-MON-001: The system shall maintain charger communication status.
- FR-MON-002: The system shall record charger connectivity events.
- FR-MON-003: The system shall maintain OCPP communication logs.
- FR-MON-004: The system shall record transaction events.
- FR-MON-005: The system shall record remote command results.
- FR-MON-006: The system shall expose operational information to authorized users.

## 12. Configuration Management Requirements

- FR-CFG-001: The system shall support GetConfiguration requests.
- FR-CFG-002: The system shall support ChangeConfiguration requests.
- FR-CFG-003: The system shall store charger configuration information where applicable.
- FR-CFG-004: The system shall handle unsupported configuration keys appropriately.
- FR-CFG-005: The system shall record configuration command results.

## 13. Requirement Acceptance

Every functional requirement must be:

1. Implementable.
2. Testable.
3. Traceable to a software component.
4. Associated with one or more test cases.
5. Verified before release.

## 14. V1 Scope Restrictions

The following are excluded:

- OCPP 2.0.1 and 2.1.
- Payment gateway.
- Mobile application.
- EV roaming.
- Advanced billing.
- Smart charging.
- Firmware management.

## 15. Completion Criteria

The FRS is considered complete when:

- All functional modules are identified.
- Each module has uniquely identified requirements.
- Requirement behavior is sufficiently defined.
- Acceptance criteria are established.
- Requirements are reviewed against stakeholder expectations.
- Requirements are approved and version-controlled.

**Document Status:** Draft for Review.
