# SgrPickup – Backend

Spring Boot backend for **SgrPickup**, my bachelor's thesis project (Politehnica University of Timișoara, 2025).

SgrPickup is an on-demand pickup service for Romania's deposit-return system (SGR). Users request a pickup for a sack of returnable bottles and cans. A driver accepts the request and collects it, and both get paid from the deposit value.

## The system

| Repository | What it is | Stack |
|---|---|---|
| **LicentaBackend** (this repo) | REST API, business logic, payments | Kotlin, Spring Boot, PostgreSQL |
| [LicentaClientAppFrontend](https://github.com/MarcAndreiFurtos/LicentaClientAppFrontend) | Android app for users requesting pickups | Kotlin, Jetpack Compose |
| [LicentaDriverFrontend](https://github.com/MarcAndreiFurtos/LicentaDriverFrontend) | Web app for drivers | Next.js, TypeScript, Tailwind |
| [LicentaWebView](https://github.com/MarcAndreiFurtos/LicentaWebView) | Mobile wrapper for the driver web app | React Native |

## How a pickup works

1. The user enters a pickup address and sack size in the Android app. The backend validates the address with the Google Geocoding API and estimates the sack's deposit value from its volume.
2. Drivers see pending pickups and accept one. The pickup moves to `IN_PROGRESS` and the driver's card is charged the estimated value through Stripe.
3. The driver's location is updated while en route. The backend calculates distance and ETA with the Google Distance Matrix API, with retries.
4. When the pickup is completed and paid, payouts go to the user's and driver's Stripe Connect accounts.
5. Users get email notifications at key steps.

Pickup lifecycle: `PENDING → IN_PROGRESS → COMPLETED` (or `CANCELLED`).

## Features

- REST API for users, pickups, payment cards and Stripe accounts
- Card tokenization, so raw card data is never stored
- Stripe charges and Connect payouts
- Google Maps geocoding, distance and ETA
- Email notifications with Spring Mail
- Pickup history for users and drivers
- OpenAPI docs with Swagger UI at `/swagger-ui`
- HTTPS via an SSL keystore

## Tech stack

Kotlin · Spring Boot 3 (Web, Data JPA, Security, OAuth2 Client, Mail) · Hibernate · PostgreSQL · Stripe API · Google Maps APIs · Maven

## Project structure

```
src/main/kotlin/org/licenta3/licentabackend3/
├── Controller/   REST endpoints (users, pickups, cards, Stripe)
├── Service/      business logic: pickups, payments, tokenization, email
├── Entities/     JPA entities (User, SgrPickup, TokenizedCard)
├── Repository/   Spring Data repositories
├── DTO/          request/response objects
├── Mappers/      entity ↔ DTO mapping
└── Config/       security and app configuration
```

## Main endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/sgrPickup` | Create a pickup request |
| `GET` | `/api/sgrPickup/pending` | List all pending pickups (drivers) |
| `PUT` | `/api/sgrPickup/{id}/progress` | Driver accepts a pickup |
| `PUT` | `/api/sgrPickup/{id}/dLocation` | Update the driver's location |
| `GET` | `/api/sgrPickup/{id}/eta` | Get distance and ETA to the pickup |
| `PUT` | `/api/sgrPickup/{id}/complete` | Mark a pickup as completed |
| `PUT` | `/api/sgrPickup/{id}/cancel` | Cancel a pickup |
| `GET` | `/api/sgrPickup/{userId}/history` | A user's completed pickups |
| `POST` | `/api/cards/tokenize` | Tokenize and save a card |
| `POST` | `/api/users` | Register a user |

The full list is in Swagger UI.

## Running locally

Requirements: JDK 17+, PostgreSQL, a Stripe test account and a Google Maps API key.

1. Create a PostgreSQL database.
2. Configure the database, mail, Stripe, Google Maps and SSL settings in `src/main/resources/application.yml`. Keep real keys out of version control by using environment variables or a local, git-ignored config file.
3. Run:

```bash
./mvnw spring-boot:run
```

The API starts on `https://localhost:8443`.
