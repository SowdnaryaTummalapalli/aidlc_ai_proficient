# AI-Assisted URL Shortener

A production-minded Spring Boot prototype that shortens HTTP(S) URLs, redirects users, and records click analytics in H2. The engineer owns requirements, design choices, code review, and quality gates; AI is used as an implementation accelerator.

## Run it

Prerequisites: Java 21 and no separately installed database.

```powershell
cd aidlc/url_shortener
./mvnw.cmd spring-boot:run
```

The service starts at `http://localhost:8080`; H2's development console is at `/h2-console` (JDBC URL: `jdbc:h2:mem:urlshortener`).

```powershell
# Create a link
curl.exe -i -X POST http://localhost:8080/api/v1/urls -H "Content-Type: application/json" -d "{\"destinationUrl\":\"https://example.com/docs\",\"customCode\":\"guide2026\"}"

# Follow a link (a click is recorded)
curl.exe -i http://localhost:8080/guide2026

# Obtain analytics
curl.exe http://localhost:8080/api/v1/urls/guide2026/analytics
```

Run checks with `./mvnw.cmd test`.

## API contract

| Endpoint | Outcome |
| --- | --- |
| `POST /api/v1/urls` | Creates a short link. Body: `destinationUrl` (required), optional `customCode` (4–32 URL-safe characters), optional future `expiresAt` (ISO-8601 instant). Returns `201` and its URL. |
| `GET /{shortCode}` | Records a click and returns a `302 Location` redirect. Expired or unknown links return `404`. |
| `GET /api/v1/urls/{shortCode}/analytics` | Returns URL metadata, total clicks, and click counts by UTC day for the last 30 days. |

Malformed requests return `400`; an already-used custom code returns `409`.

## Architecture and control flow

```text
Client -> REST controllers -> UrlService -> JPA repositories -> H2
                         |       |              |
                         |       +-> secure code generator
                         +-> validation / consistent HTTP errors

GET /{code} -> locate active link -> persist ClickEvent -> 302 destination
GET analytics -> link metadata + click-event range -> UTC daily aggregation
```

The web layer only translates HTTP. `UrlService` expresses the use cases and can be tested independently. Repositories isolate persistence. `ShortCodeGenerator` is an interface, so randomness is replaceable in tests. A `Clock` is injected rather than read statically, making expiry behavior deterministic. This applies single responsibility, dependency inversion, and testable composition without unnecessary abstractions.

## Engineering analysis and execution record

### Normalized requirement and assumptions

- A shortened link must be durable for the lifetime of the running prototype, redirect safely, and expose basic analytics.
- “Analytics” is interpreted as total clicks and per-day traffic. User-agent, IP, geolocation, and unique-visitor tracking are explicitly out of scope because they introduce privacy, consent, and retention requirements.
- Only absolute `http`/`https` destinations are accepted; this blocks `javascript:`, relative, and malformed redirect targets.
- Expired links behave as not found, avoiding disclosure of historical destinations. Times are evaluated in UTC.
- H2 is selected as required. A real deployment replaces `create-drop` with Flyway migrations and a durable database.

### Decomposition, dependencies, and AI traceability

| Work item | Intent / acceptance criteria | Execution and ownership |
| --- | --- | --- |
| API and model | Specify creation, redirect, expiry and analytics contracts before code. | Engineer-defined; AI-assisted initial DTO/entity scaffolding was reviewed and edited. |
| Persistence | Enforce unique short codes and retain immutable click events. | Engineer selected JPA/H2 constraints and collision defense; AI output accepted after review. |
| Use cases | Keep HTTP and persistence concerns out of business logic. | Engineer chose ports (`UrlService`, `ShortCodeGenerator`) and injected `Clock`; implementation reviewed. |
| Reliability/security | Secure IDs, bounded retries, URL validation, no redirect caching. | Engineer-directed; generated suggestions that lacked scheme validation were rejected. |
| Validation | Prove happy path and failure behavior end-to-end. | AI-assisted test drafting, manually reviewed against acceptance criteria. |
| Review artefacts | Make decisions, limitations, and operational path inspectable. | Engineer-authored and approved in this document. |

No secrets, customer data, or production code were provided to AI. Every generated or edited artifact remains subject to engineer sign-off; database migrations, security-policy changes, and deployment configuration require explicit human approval.

### Three assignment scenarios

1. **Greenfield – initial shortener:** Decompose into API contract → data schema → creation/redirect paths → analytics → tests. Validate creation produces `201`, redirect produces `302`, and a click appears in analytics.
2. **Brownfield – expiry enhancement:** Impacted flow is create DTO/controller → `ShortUrl.expiresAt` → service validation/resolution → API behavior/tests. The implementation rejects past expiry, stores future expiry, and resolves an expired code as `404`. A database migration and backward-compatibility review are mandatory when persistence is durable.
3. **Ambiguous – “analytics”:** Clarified internally as aggregate totals and 30 UTC daily buckets, not personal tracking. Decompose event capture → indexed retrieval → aggregation → response contract. Validate one redirect increases `totalClicks`; load-test aggregation before enabling high-cardinality reports.

## Quality gates, risks, and production path

Automated integration tests cover the end-to-end create/redirect/analytics path, unsafe schemes, and duplicate aliases. Before release, run unit/integration tests, static analysis (for example SpotBugs/Checkstyle), dependency/CVE scanning, API contract tests, and a redirect/analytics load test.

Key trade-offs: H2 and event-per-click storage are deliberately simple but not horizontally scalable; analytics currently reads up to 30 days of events and should move to scheduled aggregates or an analytics store under load. The in-memory database loses data on restart. Public shortening endpoints also need rate limiting, abuse/phishing controls, authentication/authorization for management APIs, observability, privacy retention rules, durable migrations, and a configurable public base URL behind a trusted proxy. Destination validation does not dereference remote URLs; adding reputation or SSRF checks must be designed carefully to avoid turning the service into a network scanner.
