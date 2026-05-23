# CodEdu Desktop

A gamified Java learning platform built as a CS102 semester project at Bilkent University. Users work through Java topics, earn points and rewards as they progress, and compete with others through a ranking system — all from a desktop application backed by a live server.

---

## Team

- Elvin İsmayil
- Ahmet Erhan Avcı
- Erkam Özdemir
- Yusif Ahmadzada
- Musa Alper Yılmaz

---

## Architecture

The JavaFX desktop client communicates with a Spring Boot backend over REST and WebSocket. The backend handles all business logic, persistence, and ranking calculations. The client never touches the database directly.

```
JavaFX Desktop Client
        │
        ├── REST (HTTP)   ──► Spring Boot Backend ──► PostgreSQL (Neon)
        └── WebSocket     ──► Spring Boot Backend
```

---

## Tech Stack

**Client**
- JavaFX — scenes, controllers, event handling, and UI components
- Maven — build and dependency management

**Backend**
- Spring Boot — REST API and application layer
- Spring WebSocket (STOMP) — real-time updates (rankings, notifications)
- Spring Data JPA — ORM and database access
- PostgreSQL (Neon) — cloud-hosted relational database

---

## Project Structure

The backend is organized into clearly separated layers:

**Controllers** receive HTTP and WebSocket requests and delegate immediately to the service layer. No logic lives here.

**Services** contain all business logic — reward calculation, rank updates, progress tracking, and input validation. This is also where entities are mapped to DTOs before anything leaves the backend.

**DTOs (Data Transfer Objects)** define exactly what the client sends and receives. The internal domain model stays internal — entities never cross the API boundary.

**Repositories** extend Spring Data JPA interfaces to handle all database reads and writes. No raw SQL.

**Seeders** populate the database with initial content — Java topics, questions, reward tiers — so the app has data to work with on first run.

---

## Core Features

**Learning** — structured Java topics that users work through step by step.

**Rewards & XP** — completing lessons and challenges grants points and unlocks rewards, giving users a reason to keep going.

**Ranked mode** — users compete against each other, with standings calculated and updated on the backend and pushed to connected clients in real time via WebSocket.

---

## What We Used in Practice

- Full client-server separation: JavaFX app consuming a Spring Boot REST API
- Real-time ranked updates via WebSocket / STOMP without polling
- DTO pattern keeping the API contract independent from the data model
- 3-tier backend: Controllers → Services → Repositories
- Database seeding for reproducible initial state
- Maven managing dependencies across client and server
- JPA entity mapping and PostgreSQL schema design
- Multi-screen JavaFX application with scene switching and state management

---

*Bilkent University — CS102 Spring Project*
