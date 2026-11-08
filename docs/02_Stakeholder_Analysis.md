# VOLTRIC — Stakeholder Analysis

## 1. Purpose

This document identifies the human and technical stakeholders interacting with VOLTRIC, an EV Charging Station Management System.

It defines their responsibilities, interactions, access requirements and operational expectations.

## 2. Organizational Model

VOLTRIC V1 will follow a **Single-Organization Architecture**.

A single organization will own and manage all charging stations registered within the CMS.

The system will support:
- Multiple charging stations.
- Multiple connectors per charging station.
- Multiple administrators and operators.
- Role-based access control.
- Centralized charger monitoring and management.

Multi-tenant functionality is excluded from V1.

## 3. Stakeholder Identification

### 3.1 Super Administrator

Responsibilities:
- Register and manage charging stations.
- Manage CMS administrator and operator accounts.
- Configure charger-related settings.
- Manage idTag authorization records.
- Monitor all charging operations.
- Execute authorized remote commands.
- Access operational reports and logs.

### 3.2 Charging Station Operator

Responsibilities:
- Monitor assigned charging stations.
- View charger and connector availability.
- Monitor active and completed transactions.
- Execute authorized remote commands.
- Investigate operational issues.
- Monitor charging and energy information.

### 3.3 System Administrator

Responsibilities:
- Maintain backend infrastructure.
- Monitor application health.
- Maintain database backups.
- Configure deployment environments.
- Investigate application and infrastructure failures.
- Maintain system availability.

System administrators will not automatically receive business-level CMS permissions.

### 3.4 EV Driver

Responsibilities:
- Present supported RFID/idTag credentials.
- Request charging authorization.
- Initiate charging through the charging station.
- Stop charging through supported charger operations.

A dedicated EV driver mobile application is outside V1 scope.

### 3.5 EV Charging Station

Responsibilities:
- Establish a WebSocket connection with VOLTRIC.
- Send BootNotification.
- Send Heartbeat messages.
- Send StatusNotification messages.
- Send Authorize requests.
- Send StartTransaction and StopTransaction messages.
- Send MeterValues.
- Receive and respond to supported remote commands.

Communication protocol: OCPP 1.6J.

## 4. Stakeholder Access Matrix

| Operation | Super Admin | Operator | System Admin |
|---|---|---|---|
| Register chargers | Yes | No | No |
| Update charger details | Yes | Limited | No |
| View all chargers | Yes | Assigned | No |
| Monitor charger status | Yes | Assigned | No |
| View transactions | Yes | Assigned | No |
| Execute remote commands | Yes | Authorized | No |
| Manage idTags | Yes | Limited | No |
| Manage CMS users | Yes | No | No |
| Monitor infrastructure | No | No | Yes |
| Manage deployments | No | No | Yes |

## 5. Stakeholder Interaction

1. Administrators manage charging infrastructure through the CMS.
2. Operators monitor and manage their assigned charging stations.
3. EV drivers interact with chargers using supported authorization methods.
4. Charging stations communicate with the CMS through OCPP 1.6J over WebSocket.
5. System administrators maintain the infrastructure on which VOLTRIC operates.

## 6. Stakeholder Requirements

- STK-01: The system shall support a single organization.
- STK-02: The system shall support multiple CMS administrators and operators.
- STK-03: The system shall enforce role-based permissions.
- STK-04: Administrators shall manage charging stations.
- STK-05: Operators shall monitor assigned charging stations.
- STK-06: Authorized users shall execute supported remote commands.
- STK-07: EV drivers shall be authorized using supported idTag credentials.
- STK-08: Charging stations shall communicate using OCPP 1.6J over WebSocket.
- STK-09: System administrators shall maintain application infrastructure.
- STK-10: The CMS shall maintain relevant operational and audit records.

## 7. V1 Assumptions

- Only one organization operates the CMS.
- Multiple chargers can connect simultaneously.
- Charger registration is controlled by the Super Administrator.
- RFID/idTag is the primary authorization mechanism.
- Operators have permission-based access.
- The backend uses OCPP 1.6J.
- No dedicated mobile application is required.

## 8. Out of Scope

- Multi-tenant organization management.
- EV driver mobile application.
- Payment gateway integration.
- EV roaming.
- Advanced billing.

## 9. Completion Criteria

Stakeholder analysis is complete when:
- All primary stakeholders are identified.
- Human and technical actors are distinguished.
- Responsibilities are documented.
- Access requirements are defined.
- Organizational architecture is finalized.
- Stakeholder requirements are assigned unique IDs.

**Status: Finalized for VOLTRIC V1 — Subject to Phase 1 SRS review.**