# Architecture Review

A point-in-time assessment of the Clean Architecture + MVVM scaffold, as a baseline for the [roadmap](ROADMAP.md) and [backend migration plan](BACKEND_MIGRATION.md).

## Strengths

- **Clean separation is real, not cosmetic.** `domain/` has zero Android or Firebase imports — models, repository interfaces, and use cases are pure Kotlin. `data/` implements those interfaces against Firebase; `presentation/` only calls use cases. This is what makes the backend swap a data-layer-only change instead of a rewrite.
- **The migration seam is explicit.** `core/di/RepositoryModule.kt` binds each domain repository interface to its current (Firebase) implementation via `@Binds`, with a comment marking exactly where to swap in REST-backed implementations later.
- **Domain models already anticipate where the product is going.** `User.role/schoolId/classId/parentId`, `TaskStatus.APPROVED/NEEDS_REVISION`, `Task.teacherComment` are all modeled even though only the Student role and flow are wired up today.
- **Reactive by default.** `AuthRepositoryImpl.currentUser()` wraps Firebase's `AuthStateListener` in a `callbackFlow` so the UI stays in sync with session changes automatically; task/points streams are `Flow`-based throughout.
- **Firestore rules enforce per-student ownership** as a baseline (`ownedByMe()` / `creatingForMe()` in `firestore.rules`).

## Gaps and risks

1. **No tests exist.** JUnit/Espresso/Compose-test dependencies are declared but there are no test files. The pure-Kotlin domain layer (use cases, mappers) is trivially unit-testable and currently untested.
2. **Auth is a workaround.** Firebase Auth has no native username login, so usernames are mapped to a synthetic email (`username@studentcompanion.local`) inside `FirebaseAuthDataSource`. It works, but it's exactly the kind of thing a real backend (own `users` table, own password hashing) handles more directly.
3. **Streak calculation is a placeholder.** `PointsRepositoryImpl.calculateStreak` string-slices ISO timestamps to get a date and counts distinct days, capped at 30 — no real timezone-safe day-boundary logic. Already flagged as stubbed in the project README.
4. **No pagination.** The points ledger is read in full and truncated to 20 entries client-side in memory. Fine at MVP scale, won't hold up under real usage.
5. **Single Gradle module.** Reasonable at this size; will slow iteration once Parent/Teacher features land. Worth splitting into `:core` / `:domain` / `:data` / `:feature-*` modules — ideally timed around the backend migration, since that work already touches `data/`.
6. **No CI, crash reporting, or analytics wired in.**
7. **Homework photos are uploaded unresized** (up to 5 × 5 MB, validated client-side only) — a storage/bandwidth cost once a backend has to store them.
8. **Push notification content is stubbed** (`StudentCompanionMessagingService` has no payload handling yet).

## Net assessment

This is a well-engineered MVP scaffold specifically because the Firebase dependency was treated as a seam from day one rather than baked into the domain or presentation layers. That one decision is what makes the [backend migration](BACKEND_MIGRATION.md) a bounded, de-risked piece of work rather than a rewrite.
