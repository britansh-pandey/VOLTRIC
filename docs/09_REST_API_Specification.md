# VOLTRIC – REST API Specification

**Project:** VOLTRIC – EV Charging Station Management System  
**Document:** REST API Specification  
**Version:** 1.0  
**API Version:** v1  
**Base URL:** `/api/v1`  
**Backend:** Java 21 + Spring Boot  
**Authentication:** Spring Security + JWT  
**Database:** MySQL  
**Protocol:** OCPP 1.6J

---

## 1. Introduction

This document defines the REST APIs required for VOLTRIC V1.

The APIs provide a communication interface between the CMS frontend and the Spring Boot backend.

The API layer will support:

- CMS authentication and user management.
- Charger and connector management.
- RFID authorization management.
- Charging transaction monitoring.
- Meter value and telemetry retrieval.
- Remote charger commands.
- Charger configuration management.
- Monitoring and dashboard information.

Charger-to-CMS communication will continue to use OCPP 1.6J over WebSocket. REST endpoints will not replace the OCPP communication channel.

## 2. API Standards

### 2.1 Base URL

Development:

`http://localhost:8080/api/v1`

Production:

`https://your-domain.com/api/v1`

The production domain will be finalized during deployment.

### 2.2 HTTP Methods

| Method | Purpose |
|---|---|
| GET | Retrieve resources |
| POST | Create a resource or initiate an operation |
| PUT | Replace or update a resource |
| PATCH | Partially update a resource |
| DELETE | Delete a resource where deletion is permitted |

### 2.3 HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 202 | Accepted for asynchronous processing |
| 204 | Successful request with no response body |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Resource Not Found |
| 409 | Conflict |
| 422 | Validation or business-rule failure |
| 500 | Internal Server Error |
| 503 | Required service temporarily unavailable |

### 2.4 Authentication

CMS APIs will use JWT-based authentication.

After successful login, the backend returns an access token.

Subsequent protected requests must include:

`Authorization: Bearer <access_token>`

The exact token expiry, refresh-token strategy and logout invalidation mechanism will be finalized during security implementation.

### 2.5 Standard Response Format

Successful response:

```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": {},
  "timestamp": "2026-11-11T06:00:00Z"
}
```

Error response:

```json
{
  "success": false,
  "message": "Invalid request",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "email",
      "message": "Email format is invalid"
    }
  ],
  "timestamp": "2026-11-11T06:00:00Z"
}
```

The backend should use a consistent response structure across controllers. The HTTP status code must accurately represent the outcome.

## 3. Authentication Module APIs

Base path: `/auth`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/login` | Authenticate CMS user | Public |
| POST | `/auth/refresh` | Refresh access token, if refresh tokens are enabled | Refresh-token policy |
| POST | `/auth/logout` | Invalidate refresh token/session, if implemented | Authenticated |
| GET | `/auth/me` | Retrieve current authenticated user | Authenticated |

### 3.1 Login

**Endpoint:** `POST /auth/login`

Request:

```json
{
  "email": "admin@voltric.com",
  "password": "your-password"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<jwt-access-token>",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "fullName": "CMS Administrator",
      "email": "admin@voltric.com",
      "roles": [
        "SUPER_ADMIN"
      ]
    }
  }
}
```

The token and expiry shown are illustrative. Actual values will be configured by the application.

### 3.2 Current User

**Endpoint:** `GET /auth/me`

Returns the current authenticated user's profile and assigned roles.

## 4. User Management Module APIs

Base path: `/users`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/users` | Create CMS user | Super Admin |
| GET | `/users` | Retrieve users with pagination | Super Admin |
| GET | `/users/{userId}` | Retrieve user details | Super Admin / Authorized user |
| PUT | `/users/{userId}` | Update user details | Super Admin |
| PATCH | `/users/{userId}/status` | Activate or deactivate user | Super Admin |
| PUT | `/users/{userId}/roles` | Assign roles | Super Admin |
| GET | `/users/{userId}/chargers` | Retrieve assigned chargers | Super Admin / Authorized user |
| PUT | `/users/{userId}/chargers` | Replace charger assignments | Super Admin |

### 4.1 Create User

**Endpoint:** `POST /users`

Request:

```json
{
  "fullName": "Charging Operator",
  "email": "operator@voltric.com",
  "password": "temporary-password",
  "roles": [
    "CHARGING_STATION_OPERATOR"
  ]
}
```

The backend must hash the password before persistence.

The request must not accept arbitrary roles from unauthorized users.

### 4.2 Assign Chargers

**Endpoint:** `PUT /users/{userId}/chargers`

Request:

```json
{
  "chargerIds": [101, 102, 103]
}
```

Only authorized administrators can change operator assignments.

## 5. Charger Management Module APIs

Base path: `/chargers`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/chargers` | Register a charger | Super Admin |
| GET | `/chargers` | Retrieve charger list | Authorized users |
| GET | `/chargers/{chargerId}` | Retrieve charger details | Authorized users |
| PUT | `/chargers/{chargerId}` | Update charger metadata | Super Admin |
| PATCH | `/chargers/{chargerId}/registration-status` | Approve, reject or disable charger | Super Admin |
| GET | `/chargers/{chargerId}/connectors` | Retrieve charger connectors | Authorized users |
| GET | `/chargers/{chargerId}/status` | Retrieve latest charger status | Authorized users |
| GET | `/chargers/{chargerId}/transactions` | Retrieve charger transaction history | Authorized users |
| GET | `/chargers/{chargerId}/connection` | Retrieve communication status | Authorized users |

### 5.1 Register Charger

**Endpoint:** `POST /chargers`

Request:

```json
{
  "chargePointId": "VOLTRIC_CP_001",
  "chargerName": "Station 001",
  "vendor": "Example Manufacturer",
  "model": "Model X",
  "serialNumber": "SN001"
}
```

Response:

```json
{
  "success": true,
  "message": "Charger registered successfully",
  "data": {
    "id": 101,
    "chargePointId": "VOLTRIC_CP_001",
    "registrationStatus": "PENDING"
  }
}
```

Registration status is subject to the configured registration policy.

### 5.2 Charger List

**Endpoint:** `GET /chargers`

Supported query parameters:

- `page`
- `size`
- `sortBy`
- `sortDirection`
- `connectionStatus`
- `registrationStatus`
- `search`

Example:

`GET /api/v1/chargers?page=0&size=10&connectionStatus=CONNECTED`

The backend must apply the requesting user's charger access restrictions.

## 6. Connector Management Module APIs

Base path: `/connectors`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/connectors` | Retrieve connectors | Authorized users |
| GET | `/connectors/{connectorId}` | Retrieve connector details | Authorized users |
| GET | `/connectors/{connectorId}/status` | Retrieve connector status | Authorized users |
| GET | `/connectors/{connectorId}/transactions` | Retrieve connector transaction history | Authorized users |

Connector records are primarily created or updated based on charger information and OCPP status events.

A CMS user must not directly modify an operational connector status through a generic CRUD endpoint. Status changes should reflect actual OCPP events or explicitly authorized CMS operations.

## 7. Authorization Module APIs

Base path: `/id-tags`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/id-tags` | Register an RFID idTag | Super Admin |
| GET | `/id-tags` | Retrieve idTags | Authorized users |
| GET | `/id-tags/{idTagId}` | Retrieve idTag details | Authorized users |
| PUT | `/id-tags/{idTagId}` | Update idTag details | Super Admin |
| PATCH | `/id-tags/{idTagId}/status` | Block, activate or disable idTag | Super Admin |
| GET | `/id-tags/{idTagId}/transactions` | Retrieve idTag transaction history | Authorized users |

### 7.1 Register idTag

**Endpoint:** `POST /id-tags`

Request:

```json
{
  "idTag": "RFID123456",
  "status": "ACTIVE",
  "expiryDate": "2027-12-31T23:59:59Z"
}
```

The idTag must comply with the OCPP 1.6 maximum length of 20 characters.

### 7.2 Authorization Flow

When a charger sends an OCPP Authorize request:

1. The OCPP layer identifies the charger.
2. The authorization handler delegates to the authorization module.
3. The module validates the idTag.
4. The module checks status and expiry.
5. The module returns the appropriate OCPP authorization result.

This operation is not performed by calling the CMS REST API.

## 8. Transaction Management Module APIs

Base path: `/transactions`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/transactions` | Retrieve all transactions | Authorized users |
| GET | `/transactions/{transactionId}` | Retrieve transaction details | Authorized users |
| GET | `/transactions/active` | Retrieve active transactions | Authorized users |
| GET | `/transactions/charger/{chargerId}` | Retrieve charger transactions | Authorized users |
| GET | `/transactions/connector/{connectorId}` | Retrieve connector transactions | Authorized users |
| GET | `/transactions/{transactionId}/meter-values` | Retrieve associated meter values | Authorized users |

Supported filters:

- `status`
- `chargerId`
- `connectorId`
- `idTag`
- `from`
- `to`
- `page`
- `size`

### 8.1 Transaction Details Response

**Endpoint:** `GET /transactions/{transactionId}`

Example:

```json
{
  "success": true,
  "message": "Transaction retrieved successfully",
  "data": {
    "id": 5001,
    "ocppTransactionId": 10001,
    "chargerId": 101,
    "connectorId": 201,
    "idTag": "RFID123456",
    "status": "ACTIVE",
    "meterStart": 12000.000,
    "meterStop": null,
    "energyConsumed": null,
    "startedAt": "2026-11-11T05:30:00Z",
    "stoppedAt": null
  }
}
```

The database's internal transaction record ID and the OCPP transaction ID must remain distinct in the API.

### 8.2 Important Rule

CMS users can retrieve transaction information through REST APIs.

Transaction creation and completion are driven by OCPP StartTransaction and StopTransaction messages, not by ordinary CMS transaction CRUD operations.

## 9. Meter Value Module APIs

Base path: `/meter-values`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/meter-values` | Retrieve meter readings | Authorized users |
| GET | `/meter-values/{meterValueId}` | Retrieve a meter sample | Authorized users |
| GET | `/meter-values/charger/{chargerId}` | Retrieve charger telemetry | Authorized users |
| GET | `/meter-values/connector/{connectorId}` | Retrieve connector telemetry | Authorized users |
| GET | `/meter-values/transaction/{transactionId}` | Retrieve transaction telemetry | Authorized users |

Supported filters:

- `measurand`
- `unit`
- `from`
- `to`
- `page`
- `size`

### 9.1 Meter Value Response

Example:

```json
{
  "success": true,
  "message": "Meter values retrieved successfully",
  "data": [
    {
      "chargerId": 101,
      "connectorId": 1,
      "transactionId": 10001,
      "sampledAt": "2026-11-11T05:45:00Z",
      "measurand": "Energy.Active.Import.Register",
      "value": "15000",
      "unit": "Wh"
    },
    {
      "chargerId": 101,
      "connectorId": 1,
      "transactionId": 10001,
      "sampledAt": "2026-11-11T05:45:00Z",
      "measurand": "Power.Active.Import",
      "value": "3500",
      "unit": "W"
    }
  ]
}
```

The REST API returns stored telemetry. The incoming MeterValues message is received through OCPP WebSocket communication.

## 10. Remote Command Module APIs

Base path: `/chargers/{chargerId}/commands`

These endpoints initiate asynchronous commands to charging stations.

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/chargers/{chargerId}/commands/remote-start` | Request RemoteStartTransaction | Authorized operator / Super Admin |
| POST | `/chargers/{chargerId}/commands/remote-stop` | Request RemoteStopTransaction | Authorized operator / Super Admin |
| POST | `/chargers/{chargerId}/commands/reset` | Request Reset | Super Admin |
| POST | `/chargers/{chargerId}/commands/unlock` | Request UnlockConnector | Authorized operator / Super Admin |
| GET | `/commands/{commandId}` | Retrieve command status | Authorized users |
| GET | `/chargers/{chargerId}/commands` | Retrieve charger command history | Authorized users |

### 10.1 Remote Start

**Endpoint:** `POST /chargers/{chargerId}/commands/remote-start`

Request:

```json
{
  "connectorId": 1,
  "idTag": "RFID123456"
}
```

Response:

```json
{
  "success": true,
  "message": "Remote start command submitted",
  "data": {
    "commandId": 7001,
    "action": "RemoteStartTransaction",
    "status": "PENDING"
  }
}
```

The API should generally return HTTP 202 Accepted when the command has been accepted for asynchronous processing.

### 10.2 Remote Stop

**Endpoint:** `POST /chargers/{chargerId}/commands/remote-stop`

Request:

```json
{
  "transactionId": 10001
}
```

The transaction ID is the OCPP transaction ID, not the internal database primary key.

### 10.3 Reset

**Endpoint:** `POST /chargers/{chargerId}/commands/reset`

Request:

```json
{
  "type": "Soft"
}
```

Supported OCPP 1.6 reset types are Soft and Hard.

### 10.4 Unlock Connector

**Endpoint:** `POST /chargers/{chargerId}/commands/unlock`

Request:

```json
{
  "connectorId": 1
}
```

### 10.5 Command Status

**Endpoint:** `GET /commands/{commandId}`

Example response:

```json
{
  "success": true,
  "message": "Command status retrieved",
  "data": {
    "commandId": 7001,
    "action": "RemoteStartTransaction",
    "status": "ACCEPTED",
    "requestedAt": "2026-11-11T05:50:00Z",
    "respondedAt": "2026-11-11T05:50:02Z"
  }
}
```

**Critical distinction:**

- PENDING: command created but not yet sent.
- SENT: command sent to charger.
- ACCEPTED: charger accepted the OCPP command.
- REJECTED: charger rejected the OCPP command.
- FAILED: command could not be processed or delivered.
- TIMEOUT: expected protocol response was not received within the configured time.

For RemoteStartTransaction, the actual charging session must still be confirmed through StartTransaction.

For RemoteStopTransaction, completion must still be confirmed through StopTransaction.

## 11. Configuration Management Module APIs

Base path: `/chargers/{chargerId}/configuration`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/chargers/{chargerId}/configuration` | Retrieve stored configuration | Authorized users |
| POST | `/chargers/{chargerId}/configuration/read` | Request GetConfiguration | Super Admin |
| PUT | `/chargers/{chargerId}/configuration/{key}` | Request ChangeConfiguration | Super Admin |
| GET | `/chargers/{chargerId}/configuration/history` | Retrieve configuration history | Authorized users |

### 11.1 Change Configuration

**Endpoint:** `PUT /chargers/{chargerId}/configuration/{key}`

Request:

```json
{
  "value": "60"
}
```

The backend sends an OCPP ChangeConfiguration request and records the result.

The stored configuration must not be treated as successfully updated merely because the CMS REST request succeeded. It should reflect the confirmed charger response and the application's configuration synchronization policy.

## 12. Monitoring Module APIs

Base path: `/monitoring`

| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/monitoring/dashboard` | Retrieve CMS dashboard summary | Authorized users |
| GET | `/monitoring/chargers/online` | Retrieve connected chargers | Authorized users |
| GET | `/monitoring/chargers/offline` | Retrieve disconnected chargers | Authorized users |
| GET | `/monitoring/transactions/active` | Retrieve active sessions | Authorized users |
| GET | `/monitoring/chargers/faulted` | Retrieve chargers with fault information | Authorized users |
| GET | `/monitoring/charger-status-history/{chargerId}` | Retrieve charger status history | Authorized users |
| GET | `/monitoring/ocpp-logs` | Retrieve OCPP message logs | Super Admin / Authorized support role |

### 12.1 Dashboard Response

**Endpoint:** `GET /monitoring/dashboard`

Example:

```json
{
  "success": true,
  "message": "Dashboard data retrieved",
  "data": {
    "totalChargers": 100,
    "connectedChargers": 85,
    "disconnectedChargers": 15,
    "activeTransactions": 32,
    "faultedConnectors": 3
  }
}
```

These values are illustrative. Dashboard data must be calculated from actual database records and current communication state.

## 13. Pagination and Filtering

List APIs should support pagination.

Example:

`GET /api/v1/transactions?page=0&size=20&sortBy=startedAt&sortDirection=DESC`

Example response:

```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  }
}
```

Recommended defaults:

- Default page: 0.
- Default size: 20.
- Maximum page size: 100.
- Default sort direction: DESC for historical records.

The backend must validate filter and sorting fields rather than accepting arbitrary database column names.

## 14. Validation Rules

| Field | Validation |
|---|---|
| Email | Valid email format and unique |
| Password | Enforce configured password policy |
| Charge point ID | Required and unique |
| Connector ID | Positive integer for physical connector operations |
| idTag | Maximum 20 characters |
| OCPP transaction ID | Protocol-compatible integer |
| Charger ID | Must refer to an existing charger |
| Command type | Must be supported by V1 |
| Date range | Start date must not be after end date |
| Page size | Must not exceed configured maximum |

The backend must validate user permissions and business constraints independently of frontend validation.

## 15. Authorization Matrix

This is the initial API access policy. It must be implemented using server-side authorization checks.

| API area | Super Admin | Charging Station Operator | System Administrator |
|---|---|---|---|
| Authentication | Yes | Yes | Yes, if CMS account exists |
| User management | Full access | No | No automatic access |
| Charger registration | Yes | No | No |
| Charger monitoring | All chargers | Assigned chargers | No automatic access |
| Connector monitoring | All chargers | Assigned chargers | No automatic access |
| idTag management | Full access | Read-only if granted | No automatic access |
| Transaction monitoring | All chargers | Assigned chargers | No automatic access |
| Meter value monitoring | All chargers | Assigned chargers | No automatic access |
| Remote commands | Yes | Authorized assigned chargers | No automatic access |
| Configuration changes | Yes | No by default | No automatic access |
| Audit and OCPP logs | Authorized access | Restricted | Infrastructure logs through operational tooling |

All access restrictions must be enforced in the backend, not only in the frontend.

## 16. OCPP WebSocket Endpoint

Charger communication is separate from the REST API base path.

Proposed WebSocket endpoint:

`/ocpp/{chargePointId}`

Example:

`ws://localhost:8080/ocpp/VOLTRIC_CP_001`

For deployment, use secure WebSocket transport:

`wss://your-domain.com/ocpp/VOLTRIC_CP_001`

The WebSocket endpoint handles OCPP 1.6J messages including:

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
- GetConfiguration
- ChangeConfiguration

The final WebSocket registration, authentication and subprotocol configuration will be established during OCPP implementation.

## 17. API Error Handling

The application should use a centralized exception-handling mechanism.

Suggested error codes:

| Error code | Meaning |
|---|---|
| VALIDATION_ERROR | Invalid request data |
| AUTHENTICATION_FAILED | Invalid credentials or token |
| ACCESS_DENIED | User lacks permission |
| RESOURCE_NOT_FOUND | Requested resource does not exist |
| DUPLICATE_RESOURCE | Duplicate charger, email or idTag |
| CHARGER_NOT_CONNECTED | Charger has no active session |
| INVALID_CONNECTOR | Connector does not exist or is invalid |
| TRANSACTION_NOT_ACTIVE | Transaction cannot be operated on |
| COMMAND_TIMEOUT | Charger response timeout |
| COMMAND_REJECTED | Charger rejected command |
| DATABASE_ERROR | Persistence operation failed |
| INTERNAL_SERVER_ERROR | Unexpected application failure |

Sensitive internal exception details, SQL statements, passwords and tokens must never be exposed in API responses.

## 18. API Security Requirements

1. Require authentication for protected CMS endpoints.
2. Apply role-based access control.
3. Enforce operator-to-charger assignment restrictions.
4. Validate all incoming request bodies.
5. Hash passwords using a secure password encoder.
6. Use HTTPS in deployed environments.
7. Apply request rate limits to sensitive endpoints where appropriate.
8. Prevent unauthorized remote command execution.
9. Record sensitive CMS operations in audit logs.
10. Avoid exposing secrets in API responses and logs.

## 19. V1 API Scope

Included:

- Authentication and user management.
- Charger and connector management.
- idTag management.
- Transaction and meter value retrieval.
- Remote charger commands.
- Charger configuration operations.
- Dashboard and monitoring.
- OCPP WebSocket endpoint.

Excluded:

- Payment APIs.
- Billing APIs.
- EV driver mobile APIs.
- Roaming APIs.
- Multi-tenant organization APIs.
- Smart charging APIs.
- Firmware management APIs.
- OCPP 2.0.1-specific endpoints.

## 20. Step 9 Acceptance Criteria

Step 9 is complete when:

1. All V1 REST API groups are documented.
2. HTTP methods and endpoint paths are defined.
3. Request and response structures are provided for core operations.
4. Authentication and authorization requirements are defined.
5. Error response conventions are documented.
6. Pagination and filtering standards are established.
7. Remote command asynchronous behavior is documented.
8. REST and OCPP responsibilities are clearly separated.
9. API access restrictions are documented.
10. The document is reviewed and merged into the main branch.

---

**End of Document – VOLTRIC REST API Specification**