# SmartCity360

SmartCity360 is a smart city management application built for an Object-Oriented Design Patterns assignment. It combines energy monitoring, zone power control, resident complaints, and configurable city policies in a web portal.

## Features

- Separate resident and staff sign-in, with account type checked during authentication.
- Interactive energy charts with city and zone filters, tooltips, selectable data points, and keyboard navigation.
- Authenticated staff access to individual building meter readings.
- Resident complaint submission and tracking, with severity assignment and routing by staff.
- City manager power controls for cities, districts, streets, and individual buildings.
- City policy creation, editing, deletion, and evaluation against sample noise and time values.
- Responsive navigation, sidebar profile and sign-out, search, filters, confirmation dialogs, and form updates without full page reloads.

## Technology

Java 17, Spring Boot 4.1.2, Spring MVC, Spring Security, Spring Data JPA, Hibernate, MySQL, and Thymeleaf. The frontend uses CSS, JavaScript, and SVG charts. Maven Wrapper is included; there is no separate frontend build step.

## Design patterns

| Part | Pattern                 | Implementation                                                                                                                                                                                       |
| ---- | ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A    | Decorator               | `EnergyMeterStream` is wrapped with authentication, optional GZIP compression, and optional AES-GCM encryption in the `BUILDING` branch of `PowerControlServiceImpl.getUsage(...)`.                  |
| B    | Composite               | `City`, `District`, `Street`, and `Building` implement `Zone`. Parent zones aggregate usage and cascade power commands to their children. Streets also control their streetlights.                   |
| C    | Chain of Responsibility | `LocalStaff`, `Supervisor`, and `CityManager` handle LOW, MEDIUM, and HIGH severity complaints respectively, forwarding complaints along the staff hierarchy.                                        |
| D    | Flyweight               | `StreetlightFactory` reuses `SharedStreetlight` instances for matching wattage, color, color temperature, and light distribution. Individual streetlights retain their own location and power state. |
| E    | Interpreter             | `PolicyParser` builds a `PolicyExpression` tree from noise and parking rules. Terminal expressions and `AND`, `OR`, and `NOT` expressions evaluate a `PolicyContext`.                                |

### Building meter decorators

The credentialed service overload returns a byte array:

```java
byte[] reading = powerControlService.getUsage(
        buildingId, ZoneType.BUILDING, staffIdentity, token,
        true,  // compress
        true   // encrypt
);
```

Authentication runs before the building lookup or encryption key access. Staff can authenticate with a valid staff access token, or through their matching authenticated portal session when the token argument is `null`. The overload without credentials rejects building readings.

`BuildingEnergyMeter` produces UTF-8 JSON containing `buildingId`, `powerOn`, `powerUsage`, and `readAt`. With both options enabled, the JSON is compressed with GZIP and then encrypted using AES-GCM. Encrypted output contains a 12-byte nonce followed by ciphertext and a 16-byte authentication tag; decoding requires decryption before decompression.

The web portal currently requests authenticated, uncompressed JSON readings. Compression and encryption are available through the service overload.

## Portal permissions

| Capability                                          | Resident | Local staff | Supervisor | City manager |
| --------------------------------------------------- | -------- | ----------- | ---------- | ------------ |
| View city, district, and street usage charts        | Yes      | Yes         | Yes        | Yes          |
| Read building meters and view building power status | No       | Yes         | Yes        | Yes          |
| Submit and track own complaints                     | Yes      | No          | No         | No           |
| View all complaints and assign severity             | No       | Yes         | Yes        | Yes          |
| View city policies                                  | Yes      | Yes         | Yes        | Yes          |
| Manage and preview policies                         | No       | Yes         | Yes        | Yes          |
| Switch zone power on or off                         | No       | No          | No         | Yes          |

Residents submit a description without selecting severity. Staff assign severity and the complaint is routed to the appropriate handler. Assessment details and the handling path are recorded.

Passwords are stored as BCrypt hashes and staff access tokens are stored as hashes. State-changing web forms use CSRF protection. Sign-out uses a POST request, and sessions expire after 30 minutes of inactivity.

## Run locally

### 1. Prerequisites

- JDK 17 with `JAVA_HOME` configured.
- A running MySQL server, available on `localhost:3306` by default.
- A database user with permission to create the application database and tables, or an existing database with appropriate permissions.

Run the following commands from the repository root.

### 2. Configure the application

Create a `.env` file in the repository root. Spring imports it as a Java properties file:

```properties
DATABASE_USERNAME=your_mysql_username
DATABASE_PASSWORD=your_mysql_password
ENERGY_METER_ENCRYPTION_KEY=paste_generated_base64_key_here
```

Generate an AES key in PowerShell, then copy the output into `ENERGY_METER_ENCRYPTION_KEY`:

```powershell
$meterKey = New-Object byte[] 32
$keyGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$keyGenerator.GetBytes($meterKey)
$keyGenerator.Dispose()
[Convert]::ToBase64String($meterKey)
```

The key must be Base64 encoding of 16, 24, or 32 bytes. Environment variables can also supply these configuration values. `.env` and `.seed/` are excluded from Git.

The default database URL is:

```text
jdbc:mysql://localhost:3306/smartcity360?createDatabaseIfNotExist=true
```

Override it with `SPRING_DATASOURCE_URL` if needed. Other defaults are in [application.yaml](src/main/resources/application.yaml). Hibernate uses `ddl-auto=update`, preserving existing data between restarts.

### 3. Start the server

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```sh
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

### 4. Sign in with seeded accounts

Startup initializers populate demo zones, streetlights, staff, residents, complaints, and policies when their corresponding data is absent. Portal accounts are created for residents and staff who do not already have one.

Generated local credentials are written to:

| File                                   | Contents                                                                                       |
| -------------------------------------- | ---------------------------------------------------------------------------------------------- |
| `.seed/portal-accounts.properties`     | Portal usernames and generated passwords, in `username=password` format.                       |
| `.seed/staff-access-tokens.properties` | Staff service tokens, keyed by `STAFF_TYPE.staffId`. These are separate from portal passwords. |

Select **Resident** for usernames such as `resident.1`. Select **Staff** for `local_staff.1`, `supervisor.1`, or `city_manager.1`. Use the password from the generated file; account IDs depend on the database contents.

Existing accounts and passwords are retained on restart. Deleting a credential file does not reset accounts or recover their passwords. Keep the generated files private.

## Policy language

Rules use non-negative noise thresholds and 24-hour `HH:mm` times:

```text
NOISE <= 55
PARKING BETWEEN 08:00 AND 18:00
NOISE <= 45 AND PARKING BETWEEN 22:00 AND 06:00
NOT (NOISE > 70) OR PARKING BETWEEN 09:00 AND 17:00
```

- Noise comparisons: `<`, `<=`, `==`, `!=`, `>=`, and `>`.
- Logical precedence: `NOT`, then `AND`, then `OR`. Parentheses override precedence; keywords are case-insensitive.
- Parking intervals include the start and exclude the end. Overnight intervals wrap past midnight; equal start and end times are invalid.
- Invalid rules are rejected before saving. Preview evaluates a rule without saving it.

## Project structure

```text
src/main/java/lk/urbantechsolutions/smartcity360/
  config/       Application security and demo data initializers
  controller/   Portal pages, chart data, meter readings, and power actions
  model/        Entities and design pattern implementations
  repository/   Spring Data JPA repositories
  security/     Portal authentication and access checks
  service/      Business services and their implementations
src/main/resources/
  static/       CSS, JavaScript, and images
  templates/    Thymeleaf pages and shared fragments
  application.yaml
src/test/java/  Unit, web, and database integration tests
```

## Tests and packaging

The test suite includes decorator behavior, staff authentication, complaint routing, policy parsing, portal permissions, seeders, and persisted zone power changes. Integration tests require MySQL and the configuration values above.

Use a separate database and credential output directory for a test run. This PowerShell command creates a unique database name:

```powershell
$testDatabase = 'smartcity360_test_' + [guid]::NewGuid().ToString('N')
.\mvnw.cmd test "-Dspring.datasource.url=jdbc:mysql://localhost:3306/${testDatabase}?createDatabaseIfNotExist=true" '-Dseed.staff-token-file=target/test-seed/staff-access-tokens.properties' '-Dseed.portal-account-file=target/test-seed/portal-accounts.properties'
```

The test database remains available for inspection after the run. To build an executable JAR while using the same test configuration:

```powershell
.\mvnw.cmd package "-Dspring.datasource.url=jdbc:mysql://localhost:3306/${testDatabase}?createDatabaseIfNotExist=true" '-Dseed.staff-token-file=target/test-seed/staff-access-tokens.properties' '-Dseed.portal-account-file=target/test-seed/portal-accounts.properties'
java -jar target/SmartCity360-1.0.jar
```

## Current scope

Meter readings use stored building usage values; no physical meter hardware or continuous telemetry feed is connected. Switching power changes the stored device state and does not reset recorded usage. Policy evaluation uses supplied facts rather than automatic enforcement against sensors.

Residents currently have no building association. City managers can control any city because the domain model does not assign managers to individual cities.
