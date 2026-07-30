# AI-assisted URL Shortener

A Spring Boot 4 / Java 21 prototype that creates short links, redirects safely, and exposes aggregate click analytics. Generated output is reviewed against explicit API, security, and validation criteria before it is retained.

## Run locally

Prerequisites: Java 21 with `JAVA_HOME` set. From `ai_prof_url_shortener`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The service runs at `http://localhost:8080`. H2's development-only console is `/h2-console` (JDBC URL `jdbc:h2:mem:urlshortener`).

Interactive API documentation is available at `http://localhost:8080/swagger-ui.html`; the machine-readable OpenAPI document is `http://localhost:8080/api-docs`.

## API

| Operation | Endpoint | Result |
| --- | --- | --- |
| Create | `POST /api/urls` | Creates a random 8-character short code. |
| Redirect | `GET /{code}` | Returns `302 Found` and increments the click count. |
| Analytics | `GET /api/urls/{code}/analytics` | Returns destination, lifecycle timestamps, and aggregate clicks. |

Create request:

```json
{ "url": "https://example.com/products?id=42", "expiresAt": "2026-12-31T23:59:59Z" }
```

URLs must be absolute `http` or `https` URIs. Expiry must be future-dated. Expired links return `410 Gone`; missing links return `404 Not Found`.

## Architecture

```
Client -> Controller -> Service -> Repository -> H2/JPA
                       |
                       +--> URL/expiry validation, secure code generation, click registration
```

- Request/response records keep JPA entities out of the public API.
- Codes are Base62 values created by `SecureRandom`; bounded retries handle rare collisions.
- Redirect and increment run transactionally. At high volume, use an atomic database increment or an event pipeline.
- H2 is demonstration-only. Production needs a durable database, migrations, backup/recovery, observability, caching, rate limiting, and authenticated analytics.

## Engineering execution record

### Greenfield: core short-link flow

1. Normalize into create, resolve, analytics, and error behaviours.
2. Implement persistence, code allocation, validation, expiry, and HTTP DTOs.
3. Validate create -> redirect -> analytics with MockMvc.

Acceptance criteria: valid links create and redirect correctly, one redirect increments analytics, and unknown codes never redirect.

### Brownfield: expiry enhancement

Impact: nullable `expiresAt` on the entity, creation validation, resolution policy, analytics output, and error-contract tests. Existing links remain non-expiring. A production database requires a migration and compatibility review.

### Ambiguous: analytics

Analytics could mean aggregate clicks, uniques, referrers, geography, or time series. This prototype intentionally provides aggregate clicks only: it is privacy-preserving and directly testable. Richer metrics require product decisions on consent, retention, data classification, bot filtering, and reporting latency.

## Validation, risks, and ownership

- Tests cover application startup, full happy path, invalid URI scheme, and unknown-link handling.
- Input accepts only absolute HTTP(S) destinations to prevent script-scheme redirects. Production policy should also address abuse and SSRF risk.
- Redirect policy, tracking collection, authentication, retention, and database migrations need human security/product sign-off.
- AI accelerated decomposition, implementation drafts, tests, and review preparation. The engineer owns acceptance, correctness, threat modelling, and production readiness.

Limitations: no custom aliases, deletion, authentication, rate limiting, distributed uniqueness guarantee, detailed analytics, migrations, or production observability. In-memory H2 data is lost on restart.
