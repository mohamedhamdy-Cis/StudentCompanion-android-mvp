# Student Companion — Android MVP

Clean Architecture + MVVM, Kotlin, Jetpack Compose, Hilt, Firebase (MVP backend).

## Tech stack (latest stable as of this scaffold)

| Layer | Tech |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.11.00), Material 3 |
| DI | Hilt 2.52 |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation-Compose 2.8.4 |
| Backend (MVP) | Firebase Auth, Firestore, Storage, Cloud Messaging |
| Image loading | Coil |
| Build | AGP 8.6.1, Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`) |

## Architecture: Clean Architecture + MVVM

```
presentation/   →  Compose screens + ViewModels (MVVM). Talks only to domain use cases.
domain/         →  Pure Kotlin: models, repository INTERFACES, use cases. No Android/Firebase imports.
data/           →  Firebase data sources, DTOs, mappers, repository IMPLEMENTATIONS.
core/           →  DI modules, navigation graph, design system (theme/colors), shared utils.
```

Dependency rule: `presentation → domain ← data`. The `domain` layer never imports
from `data` or `presentation` — it defines the contracts (`AuthRepository`,
`TaskRepository`, etc.) that `data` implements and `presentation` consumes
through use cases. This is what makes the backend swap (see below) a
data-layer-only change.

### Package layout (Google's recommended structure, package-by-feature within each layer)

```
com.studentcompanion.app/
├── core/
│   ├── di/                    Hilt modules (FirebaseModule, RepositoryModule)
│   ├── navigation/            Screen routes + NavGraph
│   ├── designsystem/          Color.kt, Theme.kt, Type.kt (lavender M3 palette)
│   └── util/                  Result wrapper, Constants, FCM service
├── domain/
│   ├── model/                 User, Task, Note, Subject, PointsEntry
│   ├── repository/            Interfaces only (the "ports")
│   └── usecase/                auth/ task/ note/ points/
├── data/
│   ├── remote/firebase/       FirebaseAuthDataSource, FirestoreTaskDataSource, etc.
│   ├── repository/            *Impl classes satisfying domain interfaces
│   ├── model/                 Firestore DTOs
│   └── mapper/                DTO ↔ domain model mappers
└── presentation/
    ├── auth/login/  auth/register/
    ├── onboarding/
    ├── home/
    ├── notes/
    ├── homework/
    └── points/
```

## Firebase → REST migration path

This is the reason the domain layer exists. When you're ready to move off
Firebase:

1. Implement new classes (e.g. `RestAuthRepositoryImpl`) satisfying the same
   `domain/repository` interfaces, using Retrofit/Ktor instead of Firebase SDKs.
2. Change the `@Binds` target in `core/di/RepositoryModule.kt` from the
   `*Impl` (Firebase) class to the new REST-backed class.
3. Nothing in `domain/` or `presentation/` changes — ViewModels and use cases
   only ever depended on the interface.

`RepositoryModule.kt` has a comment marking this exact seam.

## What's implemented in this MVP scaffold

- **Auth**: Login + Register screens, **username/password** (Firebase Auth has
  no native username auth, so usernames are mapped to a synthetic email
  internally in `FirebaseAuthDataSource` — invisible to the rest of the app).
- **Session**: screens load data for the signed-in user (`ObserveCurrentUserUseCase`).
  Cold start skips login if a Firebase session already exists.
- **Onboarding**: default subject list is saved to Firestore for the signed-in student.
- **Home**: unified prayer + homework timeline. Opening Home seeds today's five
  prayer tasks (Fajr–Isha, Asia/Riyadh times) if they don't exist yet.
- **Task detail**: prayer can be marked complete with an optional comment;
  homework opens the submission screen. Completing / submitting writes a
  points-ledger entry.
- **Notes**: list, add, detail with **edit** and **delete**.
- **Homework**: list + **submission screen** with up to 5 photos, 5 MB each.
- **Points**: total + streak hero card, recent points ledger.

## After you add Authentication in Firebase

Do these in order — Email/Password is required even though the app UI shows username:

1. **Authentication → Sign-in method** → enable **Email/Password**.
2. Confirm the Android app in **Project settings** uses package `com.studentcompanion.app`
   and that `app/google-services.json` is the file you downloaded for that app.
3. **Firestore Database** → create the database (start in test mode for local
   development, then paste `firestore.rules` from this repo before any school data).
4. **Storage** → get started (needed for homework photos).
5. **Cloud Messaging** is optional for this student loop; parent/teacher push comes later.
6. Run the app, **Register** a student, pick subjects, then open Home. You should
   see today's prayers. Tap one → **Mark as prayed** → check Points.

Username `ahmad` is stored in Auth as `ahmad@studentcompanion.local`. Never create
users by hand in the console with a real email unless it matches that mapping.

## What's still stubbed

- Push notification content/display in `StudentCompanionMessagingService`.
- Parent and Teacher role screens (this scaffold is Student-only).
- Live prayer-time fetch (Aladhan API) — Home currently seeds fixed Riyadh-ish times.
- Real consecutive-day streak calculation (`PointsRepositoryImpl.calculateStreak`).

## Setup

1. **Firebase project**: create one at console.firebase.google.com, register
   an Android app with package name `com.studentcompanion.app`, download
   `google-services.json`, and place it at `app/google-services.json`
   (see `app/google-services.json.SAMPLE` for the expected shape — a real
   file is required for the app to build and run).
2. Enable **Authentication (Email/Password provider)**, **Firestore**,
   **Storage**, and **Cloud Messaging** in the Firebase console.
3. Open the project root in Android Studio (Ladybug or newer) and let Gradle
   sync — the version catalog (`gradle/libs.versions.toml`) pins everything.
4. Firestore security rules and indexes aren't included in this scaffold —
   add rules restricting each collection to its owning student /
   linked parent / linked teacher before any real pilot data goes in.

## Points reference (`core/util/Constants.kt`)

| Action | Points |
|---|---|
| Prayer completed on time | 10 |
| Prayer completed late | 5 |
| Homework submitted | 15 |
| Note added | 5 |
| 5-day streak bonus | 20 |

Change these in one place; every use case reads from `Constants`.
