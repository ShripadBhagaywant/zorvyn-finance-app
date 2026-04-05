# Zorvyn Finance Backend 💸💰

A RESTful backend for a personal finance dashboard system built with **Spring Boot 3**, **Spring Security**, **JWT authentication**, and **MySQL**. Users can manage their own financial records and view summary analytics through a role-protected API.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Role-Based Access Control](#role-based-access-control)
- [Authentication Flow](#authentication-flow)
- [Request Tracing (Logging Filter)](#request-tracing-logging-filter)
- [Security Features](#security-features)
- [Assumptions & Design Decisions](#assumptions--design-decisions)
- [Known Limitations & Tradeoffs](#known-limitations--tradeoffs)

---

## Tech Stack 👇

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Java 17                             |
| Framework      | Spring Boot 3                       |
| Security       | Spring Security + JWT (JJWT)        |
| Persistence    | Spring Data JPA + Hibernate         |
| Database       | MySQL 8                             |
| Mapping        | ModelMapper                         |
| Validation     | Jakarta Bean Validation             |
| Build          | Maven                               |

---

## Architecture Overview 📚

```
com.zorvyn.finance.app
├── config/          # AppConfig (ModelMapper, PasswordEncoder)
├── controller/      # REST controllers (Auth, User, Financial, Dashboard)
├── dtos/
│   ├── request/     # Validated inbound payloads
│   └── response/    # Outbound response shapes
├── entity/          # JPA entities (User, FinancialRecord, BlackListedToken)
│   └── enums/       # Role, Status, Category, TransactionType
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── filters/         # LoggingFilter (correlation ID)
├── projection/      # JPA interface projections (FinancialRecordProjection)
├── repository/      # Spring Data repositories
├── security/        # JwtService, JwtAuthFilter, CookieService, SecurityConfig
├── service/         # Service interfaces + implementations
├── specification/   # JPA Specifications for dynamic filtering
└── utils/           # IdentifierUtils (UUID parsing)
```

The application follows a strict layered architecture: **Controller → Service → Repository**. Business logic lives in the service layer. Controllers handle HTTP concerns only. Specifications isolate query-building logic from service code.

---

## Getting Started

### Prerequisites 🫵

- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### 1. Clone and configure

```bash
git clone https://github.com/shripadBhagaywant/zorvyn-finance-app.git
cd finance.app
```

### 2. Create the database

```sql
CREATE DATABASE finance_app;
```

### 3. Set environment variables

```bash
export JWT_SECRET=your_base64_encoded_secret_here
# Generate a strong one: openssl rand -base64 64
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

> Hibernate is configured with `ddl-auto: update` — tables are created automatically on first run.

---

## Environment Variables

| Variable        | Required | Description                                                       |
|-----------------|----------|-------------------------------------------------------------------|
| `JWT_SECRET`    | Yes      | Base64-encoded HMAC-SHA256 secret key. Min 256 bits recommended. The app will **fail to start** if this is not set. |
| `COOKIE_SECURE` | No       | Set to `true` in production (HTTPS). Defaults to `false` for local dev. |

---

## API Reference

### Auth — `/api/v1/auth`

| Method | Path      | Auth     | Description                        |
|--------|-----------|----------|------------------------------------|
| POST   | `/login`  | Public   | Login with email + password. Sets HttpOnly cookie and returns token. |
| POST   | `/logout` | Required | Blacklists the current JWT and clears the cookie. |

### Users — `/api/v1/users`

| Method | Path              | Role Required         | Description                  |
|--------|-------------------|-----------------------|------------------------------|
| POST   | `/register`       | Public                | Register a new user. Default role: VIEWER. |
| GET    | `/`               | ADMIN or ANALYST      | List all users with filters (email, role, status). Paginated. |
| GET    | `/{id}`           | ADMIN or own user     | Get user by ID.              |
| PATCH  | `/{id}/role`      | ADMIN                 | Change a user's role.        |
| PATCH  | `/{id}/status`    | ADMIN                 | Activate or deactivate a user. |
| DELETE | `/{id}`           | ADMIN                 | Soft-delete a user.          |

### Financial Records — `/api/v1/financial-records`

| Method | Path    | Role Required         | Description                                    |
|--------|---------|-----------------------|------------------------------------------------|
| POST   | `/`     | Any authenticated     | Create a record. Automatically linked to the current user. |
| GET    | `/`     | Any authenticated     | List own records. Supports filtering by type, category, date range. Paginated. |
| GET    | `/{id}` | Any authenticated     | Get a single record. Ownership enforced.       |
| PUT    | `/{id}` | ADMIN or ANALYST      | Update a record. Ownership enforced (ADMIN bypasses). |
| DELETE | `/{id}` | ADMIN                 | Soft-delete a record. ADMIN can delete any record. |

**Filter parameters for `GET /`:**

| Param      | Type              | Example                    |
|------------|-------------------|----------------------------|
| `type`     | `INCOME`/`EXPENSE`| `?type=EXPENSE`            |
| `category` | See enums below   | `?category=FOOD`           |
| `start`    | ISO datetime      | `?start=2024-01-01T00:00:00` |
| `end`      | ISO datetime      | `?end=2024-12-31T23:59:59` |
| `page`     | int (0-based)     | `?page=0&size=20`          |

**Supported categories:** `SALARY`, `RENT`, `FOOD`, `LEISURE`, `INVESTMENTS`, `UTILITIES`, `OTHER`

### Dashboard — `/api/v1/dashboard`

| Method | Path       | Auth     | Description                                              |
|--------|------------|----------|----------------------------------------------------------|
| GET    | `/summary` | Required | Returns totals, category breakdown, recent activity, and weekly trends for the current user. |

**Dashboard response shape:**

```json
{
  "totalIncome": 5000.00,
  "totalExpense": 2300.00,
  "netBalance": 2700.00,
  "categoryBreakdown": [
    { "category": "FOOD", "total": 800.00 }
  ],
  "recentActivity": [...],
  "weeklyTrends": [
    { "label": "Mon", "amount": 200.00 },
    { "label": "Tue", "amount": -150.00 }
  ]
}
```

---

## Role-Based Access Control

Three roles are supported, assigned at registration (default: `VIEWER`) and changeable by an `ADMIN`.

| Capability                         | VIEWER | ANALYST | ADMIN |
|------------------------------------|--------|---------|-------|
| Register / login                   | ✅     | ✅      | ✅    |
| View own dashboard summary         | ✅     | ✅      | ✅    |
| Create own financial records       | ✅     | ✅      | ✅    |
| Read own financial records         | ✅     | ✅      | ✅    |
| Update own financial records       | ❌     | ✅      | ✅    |
| Delete financial records           | ❌     | ❌      | ✅    |
| View all users                     | ❌     | ✅      | ✅    |
| View any user by ID                | ❌     | ❌      | ✅    |
| Change user roles / status         | ❌     | ❌      | ✅    |
| Delete users                       | ❌     | ❌      | ✅    |
| Admin can manage any user's record | —      | —       | ✅    |

Access control is enforced at two levels:

1. **`@PreAuthorize` on controller methods** — rejects requests before they reach the service layer.
2. **Ownership check in `FinancialServiceImpl`** — verifies the current user owns the record. `ADMIN` role bypasses this check.

---

## Authentication Flow ➡️

The API uses **JWT stored in an HttpOnly cookie** (`zorvyn_at`).

```
POST /api/v1/auth/login
  → Validates credentials
  → Checks account status (active, not locked)
  → Generates JWT with JTI claim
  → Sets cookie: zorvyn_at=<token>; HttpOnly; SameSite=Lax
  → Returns token in body (for non-browser clients)

All subsequent requests
  → JwtAuthFilter extracts token from cookie
  → Checks JTI against blacklist table
  → Validates signature and expiry
  → Sets SecurityContext

POST /api/v1/auth/logout
  → Saves JTI + expiry to blacklisted_tokens table
  → Clears cookie
```

**Token configuration:**

| Setting         | Value               |
|-----------------|---------------------|
| Algorithm       | HMAC-SHA256         |
| Expiry          | 12 hours            |
| Cookie name     | `zorvyn_at`         |
| HttpOnly        | true                |
| SameSite        | Lax                 |

**Token cleanup:** A scheduled job runs every 12 hours (`0 0 */12 * * *`) to delete expired entries from the `blacklisted_tokens` table, preventing unbounded growth.

---

## Request Tracing (Logging Filter)

Every request passes through `LoggingFilter`, a standard servlet `Filter` registered as a Spring `@Component`.

**What it does:**

1. Checks the incoming `X-Correlation-Id` header. If present and a valid UUID format, it reuses it (useful when requests come through a gateway or load balancer that already set the ID). If absent or invalid, it generates a new UUID.
2. Puts the correlation ID into **SLF4J MDC** under the key `correlationId`. This makes it automatically available in every log line for that request thread without passing it around manually.
3. Sets `X-Correlation-Id` on the response so the client can reference the same ID when reporting issues.
4. Clears the MDC in a `finally` block after the request completes to prevent memory leaks in thread-pooled environments.

**Where it appears in logs** (configured in `logback.xml`):

```
2024-11-15 14:32:01 [http-nio-8080-exec-3] [a3f1c2d4-...] INFO  FinancialServiceImpl - Creating record for user abc123
```

The `[a3f1c2d4-...]` segment is the correlation ID. All log lines within a single request share the same ID, making it easy to trace a request end-to-end across service calls.

**How to use it as a client:**

Send `X-Correlation-Id: <your-uuid>` in any request to have that ID used across all logs for that request. This is especially useful during debugging or when integrating with a frontend that tracks its own request IDs.

---

## Security Features

| Feature                    | Implementation                                                                 |
|----------------------------|--------------------------------------------------------------------------------|
| Password hashing           | BCrypt with strength 12                                                        |
| JWT blacklisting on logout | JTI stored in `blacklisted_tokens`, checked on every authenticated request     |
| Account lockout            | Locks after 5 failed login attempts. Auto-unlocks after lock duration expires. |
| Soft deletes               | `@SQLDelete` + `@SQLRestriction` — deleted records never appear in queries     |
| Input validation           | Jakarta Bean Validation on all request DTOs                                    |
| Stateless sessions         | `SessionCreationPolicy.STATELESS` — no server-side session state               |
| CSRF disabled              | Safe because authentication is cookie+JWT, not session-based form auth         |
| Correlation ID tracing     | `LoggingFilter` — all logs per request share a traceable ID                    |
| Store Cookie in HttpOnly   | `Store Cookie in Http Only` to avoid for JavaScript Access                     |
| Adding Projection          | `TO avoid N+1 Query` Used projection to avoid the N+1 query problem.           |
| Adding  Swagger for API    | `API Documention` Swagger helps for API Documentation .                        |
---

## Assumptions & Design Decisions

**1. Users own their own financial records.**
Every `FinancialRecord` is linked to the user who created it via `createdBy`. All read and write operations are scoped to the current user. An `ADMIN` can access and manage any user's records. This models a personal finance tracker where each user manages their own data.

**2. VIEWER role can create records.**
Although the assignment describes VIEWER as read-only for dashboard data, the codebase is built around personal record ownership — it would be illogical for a VIEWER to be unable to enter their own transactions. VIEWERs can create and read their own records but cannot update or delete them.

**3. Soft deletes everywhere.**
Both `User` and `FinancialRecord` entities use soft deletes (`is_deleted` flag) via Hibernate's `@SQLDelete` and `@SQLRestriction`. Hard deletes never happen through normal API usage. This preserves data integrity and audit history.

**4. Re-registration of deleted emails is blocked.**
If an email was previously registered and soft-deleted, re-registration returns a specific error message directing the user to contact support rather than silently creating a new account.

**5. Dashboard is scoped to the current user.**
The dashboard summary (totals, category breakdown, recent activity, weekly trends) always reflects only the authenticated user's records. There is no admin-wide aggregate view.

**6. Weekly trends show the current Mon–Sun week.**
Each day's trend value is the net amount (income minus expense) for that day. Days with no transactions show `0.00`.

---

## Known Limitations & Tradeoffs

- **No refresh token.** The 12-hour JWT is the only token. After expiry the user must log in again. A refresh token mechanism would improve UX for long sessions.
- **Account lock duration is fixed in the entity.** Making the lock duration configurable via `application.yml` would be an improvement.
- **No rate limiting.** The login endpoint has account-level lockout but no IP-level rate limiting. A production system should add this.
- **`secure: false` in default config.** The auth cookie is not marked secure in local development. This must be set to `true` (via `COOKIE_SECURE=true` env var) in any HTTPS environment.
- **ModelMapper with records.** Java records have no setters; ModelMapper uses reflection with `PRIVATE` field access. This works but is fragile. A future improvement would use explicit constructors or a mapping library that natively supports records.
- **No `Rate limiting`** Api Calls. and not add unit test and integration test-cases. 
- **`ADD OAUTH2 CLient`** Setup Auth2 client and secret and `YML File` then created a Success and Error Handlers for that.
- **`Setup CORS CONFIG`** For Frontend and add success and error urls in `YML files` must be `REACT APP`.  