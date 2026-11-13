# Student Companion — Android

Clean Architecture + MVVM, Kotlin, Jetpack Compose, Hilt, backed by the
[Student Companion NestJS backend](https://github.com/mohamedhamdy-Cis/studentcompanion-backend).

## Tech stack

| Layer | Tech |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.11.00), Material 3 |
| DI | Hilt 2.52 |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation-Compose 2.8.4 |
| Networking | Retrofit + OkHttp + Gson, JWT access/refresh tokens in DataStore |
| Backend | [studentcompanion-backend](https://github.com/mohamedhamdy-Cis/studentcompanion-backend) (NestJS + PostgreSQL) |
| Image loading | Coil |
| Build | AGP 8.6.1, Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`) |

## Architecture: Clean Architecture + MVVM

```
presentation/   →  Compose screens + ViewModels (MVVM). Talks only to domain use cases.
domain/         →  Pure Kotlin: models, repository INTERFACES, use cases.
data/           →  Retrofit API, DTOs, mappers, repository IMPLEMENTATIONS.
core/           →  DI modules, navigation graph, design system (theme/colors), shared utils.
```

Dependency rule: `presentation → domain ← data`. The `domain` layer never imports
from `data` or `presentation` — it defines the contracts (`AuthRepository`,
`TaskRepository`, etc.) that `data` implements and `presentation` consumes
through use cases. Swapping the backend means changing only
`core/di/RepositoryModule.kt` and the classes it binds.

### Package layout

```
com.studentcompanion.app/
├── core/
│   ├── di/                    Hilt modules (RepositoryModule, RestNetworkModule, AppModule)
│   ├── navigation/            Screen routes + NavGraph
│   ├── designsystem/          Color.kt, Theme.kt, Type.kt (lavender M3 palette)
│   └── util/                  Result wrapper, Constants, ImageCompressor
├── domain/
│   ├── model/                 User, Task, Note, Subject, PointsEntry, LinkCode
│   ├── repository/            Interfaces only (the "ports")
│   └── usecase/               auth/ task/ note/ points/ subject/ parent/
├── data/
│   ├── remote/rest/           StudentCompanionApi, DTOs, mappers, token storage + refresh, S3 uploads
│   └── repository/rest/       Rest*RepositoryImpl classes satisfying domain interfaces
└── presentation/
    ├── auth/login/  auth/register/
    ├── onboarding/
    ├── home/  task/  notes/  homework/  points/
    └── parent/                share code, parent home, redeem code, child detail
```

## What's implemented

- **Auth**: username/password register and login, as a Student or a Parent.
  Tokens are refreshed automatically on a 401.
- **Session**: cold start skips login while a valid session exists, and routes
  a parent and a student to their own home screens.
- **Onboarding**: a student picks their subjects.
- **Home**: unified prayer + homework timeline. The backend seeds today's five
  prayer tasks with live Riyadh prayer times.
- **Task detail**: mark a prayer complete with an optional comment; homework
  opens the submission screen.
- **Notes**: list, add, edit, delete.
- **Homework**: submission with up to 5 photos, compressed and uploaded
  straight to object storage via presigned URLs.
- **Points**: total, day streak, recent ledger.
- **Parent role**: a student shares a 6-character code; a parent redeems it
  and sees that student's tasks and points (read-only).

## Running locally

1. Start the backend — see its
   [README](https://github.com/mohamedhamdy-Cis/studentcompanion-backend#local-development).
   It listens on `http://localhost:3000`.
2. Open this project in Android Studio (Ladybug or newer) and let Gradle sync.
3. Run on an **emulator**. Debug builds point at `http://10.0.2.2:3000/` (the
   emulator's address for your machine) and are allowed to use plain HTTP for
   that host only (`app/src/debug/res/xml/network_security_config.xml`).

A physical device or a release build needs `BACKEND_BASE_URL` in
`app/build.gradle.kts` pointed at a reachable HTTPS backend instead.

## What's not built yet

- Teacher role and classes (Sprint 6).
- Push notifications (Sprint 7) — the Firebase Cloud Messaging stub was removed
  along with the rest of Firebase and will need a fresh implementation.
- Live multi-device sync: lists refresh after changes made in this app, but not
  after changes made on another device.

## Points reference

Scoring is decided by the backend. The app keeps a few values in
`core/util/Constants.kt` for display and for the homework-submission award.

| Action | Points |
|---|---|
| Prayer completed on time | 10 |
| Prayer completed late | 5 |
| Homework submitted | 15 |
