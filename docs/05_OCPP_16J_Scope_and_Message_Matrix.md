# VOLTRIC – OCPP 1.6J Scope and Message Matrix

**Document:** 05 – OCPP 1.6J Scope and Message Matrix  
**Project:** VOLTRIC – EV Charging Station Management System  
**Version:** 1.0  
**Protocol:** OCPP 1.6J (JSON over WebSocket)  
**Architecture:** Single-organization CMS  
**Initial Capacity Target:** 100 simultaneously connected chargers  
**Status:** Draft for review

---

## 1. Purpose

This document defines the OCPP 1.6J communication scope for VOLTRIC V1.

It identifies:
- OCPP actions supported in V1.
- Message direction and initiating party.
- Business purpose and triggering event.
- Important request and response fields.
- Expected CMS processing responsibilities.
- Backend components responsible for handling each action.
- OCPP actions excluded from the initial implementation.

This document will serve as the implementation reference for the OCPP communication layer.

## 2. Protocol Overview

VOLTRIC V1 will use OCPP 1.6J, which communicates through JSON messages over WebSocket.

### 2.1 Communication participants

| Participant | OCPP role | Responsibility |
|---|---|---|
| EV Charging Station | Charge Point (CP) | Sends charger events and receives remote commands |
| VOLTRIC Backend | Central System (CS/CSMS) | Receives charger messages, manages charging operations, and sends remote commands |

### 2.2 WebSocket connection

A charger establishes a WebSocket connection with VOLTRIC.

The connection shall:
- Identify the charger using its registered Charge Point identity.
- Use the OCPP 1.6J WebSocket subprotocol `ocpp1.6`.
- Associate the connection with the corresponding charger record.
- Maintain a communication session for subsequent OCPP messages.
- Reject or appropriately handle unauthorized or unknown charger identities according to the registration policy.

The exact URL path and deployment context path will be defined in the technical design and configuration.

### 2.3 OCPP message frame structure

OCPP 1.6J uses three primary JSON message types:

| Message Type ID | Name | Purpose |
|---|---|---|
| 2 | CALL | Initiates an OCPP request |
| 3 | CALLRESULT | Returns a successful response to a CALL |
| 4 | CALLERROR | Returns an OCPP error response |

Illustrative request:

```json
[
  2,
  "unique-message-id",
  "Heartbeat",
  {}
]
```

Illustrative successful response:

```json
[
  3,
  "unique-message-id",
  {
    "currentTime": "2026-11-10T08:00:00Z"
  }
]
```

The message ID links a response to its original request. VOLTRIC must preserve and validate this correlation.

## 3. V1 OCPP Scope

VOLTRIC V1 will implement the following 13 OCPP actions.

### 3.1 Charger-initiated actions

| No. | OCPP action | Trigger | Main purpose | V1 priority |
|---|---|---|---|---|
| 1 | BootNotification | Charger startup or reboot | Register charger startup and identify charger hardware/software | Critical |
| 2 | Heartbeat | Configured periodic interval | Maintain liveness and synchronize time | Critical |
| 3 | StatusNotification | Connector or charger status changes | Report operational status | Critical |
| 4 | Authorize | Driver presents an idTag/RFID | Validate charging authorization | Critical |
| 5 | StartTransaction | Charging session starts | Create a charging transaction | Critical |
| 6 | MeterValues | Meter sampling event or configured interval | Report energy and electrical telemetry | Critical |
| 7 | StopTransaction | Charging session ends | Complete transaction and record final meter reading | Critical |

### 3.2 CMS-initiated actions

| No. | OCPP action | Trigger | Main purpose | V1 priority |
|---|---|---|---|---|
| 8 | RemoteStartTransaction | Authorized operator requests charging remotely | Ask charger to start a charging session | Critical |
| 9 | RemoteStopTransaction | Authorized operator requests transaction termination | Ask charger to stop an active transaction | Critical |
| 10 | Reset | Authorized operator requests charger restart | Request a soft or hard reset | High |
| 11 | UnlockConnector | Authorized operator requests connector unlock | Request connector unlocking | High |
| 12 | GetConfiguration | Operator or administrator requests charger configuration | Read supported configuration values | High |
| 13 | ChangeConfiguration | Administrator requests a configuration change | Update a supported charger configuration key | High |

**Scope rule:** An action is considered implemented only after its message handling, protocol response, business processing, persistence where applicable, and test cases are complete.

## 4. Detailed Charger-Initiated Message Matrix

### 4.1 BootNotification

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Charger startup or reboot |
| Request fields | chargePointVendor, chargePointModel; optional serial and firmware details |
| Response fields | currentTime, interval, status |
| Important statuses | Accepted, Pending, Rejected |
| CMS responsibility | Identify charger, validate registration, update charger metadata and boot status, return current time and heartbeat interval as appropriate |
| Backend component | BootNotificationHandler, ChargerService |
| Persistence | Charger details, last boot time, registration/connection status |

**Important behavior:** The CMS must distinguish an accepted charger from a pending or rejected charger. A rejected charger must not be treated as fully operational.

### 4.2 Heartbeat

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Configured periodic heartbeat interval |
| Request fields | Empty payload |
| Response fields | currentTime |
| CMS responsibility | Record last heartbeat and return server time |
| Backend component | HeartbeatHandler, ChargerConnectionService |
| Persistence | Last heartbeat timestamp and relevant communication event |

**Important behavior:** Heartbeat monitoring helps identify stale or lost charger communication. A heartbeat response alone must not overwrite an active charging or fault status.

### 4.3 StatusNotification

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Charger or connector status change |
| Request fields | connectorId, status, errorCode, timestamp; optional vendor information |
| Response | Empty successful response payload |
| CMS responsibility | Update the appropriate charger/connector status and record the event |
| Backend component | StatusNotificationHandler, ConnectorService, ChargerStatusService |
| Persistence | Connector status, charger status where applicable, status history |

**Important behavior:** Connector ID `0` represents the charge point as a whole. It must not be treated as a physical connector.

The implementation must preserve the distinction between charger-level and connector-level status.

### 4.4 Authorize

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Driver presents RFID/idTag |
| Request fields | idTag |
| Response fields | idTagInfo with authorization status and optional validity information |
| Important statuses | Accepted, Blocked, Expired, Invalid, ConcurrentTx |
| CMS responsibility | Validate idTag and return the correct authorization decision |
| Backend component | AuthorizeHandler, AuthorizationService, IdTagRepository |
| Persistence | Authorization/audit event where required |

**Important behavior:** An accepted authorization does not itself create a transaction. Transaction creation is handled by StartTransaction.

### 4.5 StartTransaction

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Charger reports that a charging session has started |
| Request fields | connectorId, idTag, meterStart, timestamp; optional reservationId |
| Response fields | transactionId, idTagInfo |
| CMS responsibility | Validate charger, connector, and authorization; create transaction; return a CMS transaction ID |
| Backend component | StartTransactionHandler, TransactionService, TransactionRepository |
| Persistence | Transaction record and initial meter reading |

**Important behavior:**
- Transaction IDs must be generated and managed consistently by the CMS.
- Duplicate or conflicting starts must not create unintended duplicate active transactions.
- The initial meter reading must be stored with its original unit and timestamp.
- A transaction must be associated with the correct charger and connector.

### 4.6 MeterValues

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Periodic sampling, transaction-related sampling, or another configured meter event |
| Request fields | connectorId, optional transactionId, meterValue array |
| Response | Empty successful response payload |
| CMS responsibility | Parse sampled values, validate timestamps and units, normalize supported measurements, and persist telemetry |
| Backend component | MeterValuesHandler, MeterValueService, MeterValueRepository |
| Persistence | Meter values and associated transaction/charger references |

Supported measurement examples for V1:
- Energy.Active.Import.Register
- Power.Active.Import
- Current.Import
- Voltage
- Temperature
- SoC, when supplied by the charger

**Important behavior:**
- A meter message can contain multiple sampled values.
- Each sampled value can have its own measurand, unit, context, and phase.
- VOLTRIC must not assume every charger reports every measurement.
- Raw measurement values and timestamps should be preserved where practical.
- Energy unit conversions must be explicit. For example, Wh to kWh requires division by 1,000.

### 4.7 StopTransaction

| Attribute | Definition |
|---|---|
| Direction | CP → CS |
| Trigger | Charging session termination |
| Request fields | transactionId, meterStop, timestamp; optional idTag, reason, transactionData |
| Response fields | Optional idTagInfo |
| CMS responsibility | Locate active transaction, record final meter reading, calculate energy consumed, and complete transaction |
| Backend component | StopTransactionHandler, TransactionService |
| Persistence | Final transaction state, stop time, final meter reading, energy consumed |

**Important behavior:**
- The transaction must be identified using the CMS transaction ID.
- A completed transaction must not be completed again through duplicate processing.
- Energy consumption should be calculated from valid start and stop readings.
- The stop reason should be retained when supplied.

## 5. Detailed CMS-Initiated Message Matrix

### 5.1 RemoteStartTransaction

| Attribute | Definition |
|---|---|
| Direction | CS → CP |
| Trigger | Authorized operator requests remote charging start |
| Request fields | idTag; optional connectorId |
| Response fields | status |
| Important statuses | Accepted, Rejected |
| CMS responsibility | Validate user permissions, charger connection, idTag, and request; send OCPP command and track response |
| Backend component | RemoteCommandService, RemoteStartTransactionHandler |
| Persistence | Remote command record and result |

**Critical distinction:** `Accepted` means the charger accepted the remote-start request. It does not prove that charging has started.

VOLTRIC must wait for the subsequent charger-originated transaction messages, particularly StartTransaction, to confirm the actual session.

### 5.2 RemoteStopTransaction

| Attribute | Definition |
|---|---|
| Direction | CS → CP |
| Trigger | Authorized operator requests stop for an active transaction |
| Request fields | transactionId |
| Response fields | status |
| Important statuses | Accepted, Rejected |
| CMS responsibility | Validate the active transaction and charger session, send command, and track response |
| Backend component | RemoteCommandService, RemoteStopTransactionHandler |
| Persistence | Remote command record and result |

**Critical distinction:** An accepted command is not equivalent to a completed transaction. VOLTRIC must wait for StopTransaction to confirm and persist the final transaction state.

### 5.3 Reset

| Attribute | Definition |
|---|---|
| Direction | CS → CP |
| Trigger | Authorized operator requests charger restart |
| Request fields | type |
| Supported types | Soft, Hard |
| Response fields | status |
| Important statuses | Accepted, Rejected |
| CMS responsibility | Validate permission and charger availability, dispatch reset request, record outcome |
| Backend component | RemoteCommandService, ResetHandler |
| Persistence | Command history and charger communication events |

The CMS must monitor subsequent charger reconnection and boot notification. A successful command response does not guarantee that the charger has completed its restart.

### 5.4 UnlockConnector

| Attribute | Definition |
|---|---|
| Direction | CS → CP |
| Trigger | Authorized operator requests connector unlock |
| Request fields | connectorId |
| Response fields | status |
| Important statuses | Unlocked, UnlockFailed, NotSupported |
| CMS responsibility | Validate connector and permissions, dispatch unlock command, record result |
| Backend component | RemoteCommandService, UnlockConnectorHandler |
| Persistence | Command history and result |

### 5.5 GetConfiguration

| Attribute | Definition |
|---|---|
| Direction | CS → CP |
| Trigger | Administrator requests configuration information |
| Request fields | Optional key list |
| Response fields | configurationKey array, optional unknownKey array |
| CMS responsibility | Dispatch request and store or return the charger response |
| Backend component | ChargerConfigurationService, GetConfigurationHandler |
| Persistence | Retrieved configuration snapshot or command history |

### 5.6 ChangeConfiguration

| Attribute | Definition |
|---|---|
| Direction | CS → CP |
| Trigger | Authorized administrator requests configuration change |
| Request fields | key, value |
| Response fields | status |
| Important statuses | Accepted, Rejected, RebootRequired, NotSupported |
| CMS responsibility | Validate permission and supported key, dispatch request, track result |
| Backend component | ChargerConfigurationService, ChangeConfigurationHandler |
| Persistence | Requested change, result, and audit record |

**Important behavior:** The CMS must not assume a configuration change succeeded merely because the command was sent. It must process the charger's actual OCPP response.

## 6. Common OCPP Processing Requirements

Every OCPP message handler must follow a consistent processing pattern.

1. Receive the WebSocket frame.
2. Identify the message type.
3. Parse the JSON frame.
4. Extract message ID, action, and payload.
5. Resolve the associated charger session.
6. Validate message structure and required fields.
7. Dispatch the action to its handler.
8. Execute relevant business logic.
9. Persist required data.
10. Construct the appropriate CALLRESULT or CALLERROR response.
11. Send the response using the original message ID.
12. Record processing outcome and diagnostic information.

### 6.1 Error handling

VOLTRIC shall distinguish:
- Invalid JSON or malformed OCPP frame.
- Unsupported OCPP action.
- Invalid or missing payload fields.
- Unknown charger identity.
- Business validation failure.
- Database or internal processing failure.
- WebSocket disconnection or timeout.

Protocol-level errors must be handled using the appropriate OCPP error response rather than returning arbitrary JSON structures.

### 6.2 Message correlation

- Every outgoing OCPP CALL must use a unique message ID within the relevant communication context.
- Responses must be matched to their originating request.
- A response with an unknown or expired message ID must not be applied to an unrelated command.
- Pending commands must have a configurable timeout.
- Late responses must be handled safely.

## 7. OCPP Transaction Workflow

### 7.1 Normal charging session

```text
EV Driver
   |
   | Presents RFID / idTag
   v
Charging Station
   |
   | Authorize.req
   v
VOLTRIC CMS
   |
   | Authorize.conf (Accepted)
   v
Charging Station
   |
   | StartTransaction.req
   v
VOLTRIC CMS
   |
   | StartTransaction.conf (transactionId)
   v
Charging Session Active
   |
   | MeterValues.req (periodic)
   v
VOLTRIC CMS
   |
   | MeterValues.conf
   v
Charging Session Continues
   |
   | StopTransaction.req
   v
VOLTRIC CMS
   |
   | StopTransaction.conf
   v
Transaction Completed
```

### 7.2 Remote start workflow

```text
CMS Operator
   |
   | Remote start request
   v
VOLTRIC CMS
   |
   | Validate permissions and request
   | Send RemoteStartTransaction.req
   v
Charging Station
   |
   | RemoteStartTransaction.conf
   v
VOLTRIC CMS
   |
   | Track Accepted / Rejected
   v
Charging Station
   |
   | StartTransaction.req (if charging starts)
   v
VOLTRIC CMS
   |
   | Create transaction
   v
Charging Session Active
```

The remote command response and actual transaction start are separate events.

## 8. OCPP Actions Excluded from V1

The following actions are not part of the initial V1 implementation scope.

| Feature profile / area | Excluded actions or capabilities |
|---|---|
| Core profile | ChangeAvailability, ClearCache, DataTransfer |
| Firmware Management | GetDiagnostics, DiagnosticsStatusNotification, FirmwareStatusNotification, UpdateFirmware |
| Local Auth List Management | SendLocalList, GetLocalListVersion |
| Reservation | ReserveNow, CancelReservation |
| Smart Charging | SetChargingProfile, ClearChargingProfile, GetCompositeSchedule |
| Remote Trigger | TriggerMessage |

These exclusions are deliberate scope decisions, not claims that the actions are unsupported by OCPP 1.6.

Future implementation requires a separate requirement and design review.

## 9. Backend Component Mapping

| OCPP responsibility | Proposed backend component |
|---|---|
| WebSocket session management | OcppWebSocketHandler / ChargerSessionManager |
| Message parsing and routing | OcppMessageParser / OcppMessageRouter |
| BootNotification | BootNotificationHandler |
| Heartbeat | HeartbeatHandler |
| StatusNotification | StatusNotificationHandler |
| Authorization | AuthorizeHandler / AuthorizationService |
| Transaction start | StartTransactionHandler / TransactionService |
| Meter telemetry | MeterValuesHandler / MeterValueService |
| Transaction stop | StopTransactionHandler / TransactionService |
| Remote commands | RemoteCommandService |
| Charger reset | ResetHandler |
| Connector unlock | UnlockConnectorHandler |
| Configuration retrieval | GetConfigurationHandler |
| Configuration update | ChangeConfigurationHandler |
| OCPP request correlation | OcppPendingRequestManager |
| OCPP logs and diagnostics | OcppMessageLogService |

These are proposed logical components. Their final Java package and class names will be confirmed during Phase 2 architecture and implementation.

## 10. Initial OCPP Test Matrix

| Test ID | Scenario | Expected result |
|---|---|---|
| OCPP-TC-01 | Charger connects using OCPP 1.6J WebSocket | Session established for registered charger |
| OCPP-TC-02 | Charger sends BootNotification | Correct boot response and charger state |
| OCPP-TC-03 | Charger sends Heartbeat | Current server time returned |
| OCPP-TC-04 | Charger sends StatusNotification | Correct charger/connector status updated |
| OCPP-TC-05 | Charger sends Authorize for valid idTag | Accepted authorization |
| OCPP-TC-06 | Charger sends Authorize for invalid idTag | Rejected authorization |
| OCPP-TC-07 | Charger sends StartTransaction | Transaction created and transaction ID returned |
| OCPP-TC-08 | Charger sends MeterValues | Valid telemetry persisted |
| OCPP-TC-09 | Charger sends StopTransaction | Transaction completed and energy calculated |
| OCPP-TC-10 | Operator sends RemoteStartTransaction | Command result tracked; actual start confirmed separately |
| OCPP-TC-11 | Operator sends RemoteStopTransaction | Command result tracked; transaction completion confirmed separately |
| OCPP-TC-12 | Operator sends Reset | Valid response tracked and reconnection monitored |
| OCPP-TC-13 | Operator sends UnlockConnector | Unlock response processed |
| OCPP-TC-14 | Administrator sends GetConfiguration | Charger configuration response processed |
| OCPP-TC-15 | Administrator sends ChangeConfiguration | Actual charger result processed |
| OCPP-TC-16 | Malformed OCPP message received | Appropriate error handling without application crash |
| OCPP-TC-17 | Unknown charger connects | Registration policy enforced |
| OCPP-TC-18 | Duplicate message or transaction event received | No unintended duplicate business operation |
| OCPP-TC-19 | Charger disconnects during pending command | Command timeout or failure handled |
| OCPP-TC-20 | 100 chargers connect simultaneously | Initial connection capacity target verified |

## 11. Dependencies on Other Phase 1 Documents

| Related document | Dependency |
|---|---|
| 01 – Project Vision and Scope | Defines V1 boundaries |
| 02 – Stakeholder Analysis | Defines CMS roles and permissions |
| 03 – Functional Requirements | Defines required charger and CMS operations |
| 04 – Non-Functional Requirements | Defines reliability, security, and capacity expectations |
| 05 – OCPP Scope and Message Matrix | Defines protocol implementation scope |

## 12. Acceptance Criteria

This document is ready for Phase 1 review when:

1. All 13 selected OCPP actions are documented.
2. Message directions and responsibilities are clear.
3. Request and response behavior is mapped.
4. Remote command acceptance is distinguished from actual charging outcomes.
5. Error handling and message correlation requirements are defined.
6. Excluded OCPP actions are explicitly documented.
7. Every selected action has an associated test scenario.
8. The document is reviewed and merged into the `main` branch.

**Phase 1 deliverable:** `docs/05_OCPP_16J_Scope_and_Message_Matrix.md`