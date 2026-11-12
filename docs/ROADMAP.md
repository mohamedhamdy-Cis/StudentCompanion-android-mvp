# Feature Roadmap

Proposed sprint plan (2 weeks each). See [ARCHITECTURE_REVIEW.md](ARCHITECTURE_REVIEW.md) for the assessment this is based on, and [BACKEND_MIGRATION.md](BACKEND_MIGRATION.md) for the detail behind sprints 2–4.

## Sequencing rationale

Parent/Teacher/Class features are inherently relational (a teacher owns many classes, a class has many students, a parent links to many students) — exactly what Firestore's document model fights and PostgreSQL is built for. Building those features on Firestore now means re-modeling them again during the migration. This plan stabilizes the current MVP, migrates the backend, then builds the relational features natively on Postgres. (Alternative: build Parent/Teacher on Firestore first for faster visible progress, and accept the rework later — a valid trade-off if that's the priority.)

## Sprints

### Sprint 1 — Stabilize the MVP
- Unit tests for use cases and mappers (the pure-Kotlin domain layer makes this cheap)
- Fix `calculateStreak` with real timezone-safe day-boundary logic
- Wire the Aladhan API for live prayer times, replacing the fixed Riyadh-ish seed times
- Client-side image compression before homework photo upload
- Basic CI: GitHub Actions running lint + unit tests + `assembleDebug`

### Sprint 2 — Backend foundation
- Stand up NestJS + PostgreSQL (Prisma) skeleton
- Design an OpenAPI contract mirroring the existing domain repository interfaces 1:1
- JWT auth (access + refresh), real username/password storage — removes the synthetic-email workaround
- Deploy to a staging environment (Railway/Render/Fly)

### Sprint 3 — Backend migration: data
- Users, Subjects, Tasks, Points endpoints in Node
- Prisma schema for all current Firestore collections
- One-time Firestore → Postgres export/import script

### Sprint 4 — Backend migration: files & cutover
- Homework attachments move to S3/R2 via presigned uploads
- Android: `Rest*RepositoryImpl` implementations (Retrofit/OkHttp, token storage via DataStore)
- Swap `@Binds` targets in `RepositoryModule`; soak-test with Firebase impls kept available behind a flag, then cut over and drop Firestore/Auth/Storage SDKs (FCM can stay — it's push delivery only, independent of this)

### Sprint 5 — Parent role
- Parent account type, parent↔student linking flow
- Read-only view of a linked student's tasks/points (a straightforward join on Postgres vs. a Firestore denormalization problem)

### Sprint 6 — Teacher role & classes
- Class creation, roster management
- Assignment creation (homework authored by a teacher instead of self-seeded)
- Grading flow using the existing `TaskStatus.APPROVED` / `NEEDS_REVISION` / `teacherComment` fields

### Sprint 7 — Notifications & engagement
- FCM payload handling (currently stubbed in `StudentCompanionMessagingService`)
- Reminder notifications for pending prayers/homework
- In-app notification center

### Sprint 8 — Polish & scale
- Offline-first cache (Room) synced against the new API
- Pagination throughout (points ledger, task lists)
- Leaderboard / class ranking
- Arabic localization + RTL support
- Settings/profile screen, dark mode

## Backlog (beyond sprint 8)
- Biometric login
- Home-screen widget for next prayer/task
- Calendar/agenda view
- Multi-child parent dashboard
- School admin console (likely a separate web app once the Node API exists)
