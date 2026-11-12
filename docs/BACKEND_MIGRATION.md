# Backend Migration: Firebase → Node.js + PostgreSQL

Referenced from the main [README](../README.md) and [ROADMAP.md](ROADMAP.md) (sprints 2–4).

## Why this is a bounded piece of work

The app's `domain/` layer defines repository interfaces (`AuthRepository`, `TaskRepository`, `NoteRepository`, `SubjectRepository`, `PointsRepository`) that `presentation/` depends on exclusively. `data/` implementations (`AuthRepositoryImpl`, etc.) are the only code that knows about Firebase, and `core/di/RepositoryModule.kt` is the single seam that wires an implementation to each interface. Migrating the backend means: write new `Rest*RepositoryImpl` classes satisfying the same interfaces, then flip the `@Binds` target. Nothing in `domain/` or `presentation/` changes.

## Target stack

| Concern | Choice | Why |
|---|---|---|
| Server framework | NestJS (TypeScript) | Module/DI structure similar to what Hilt already gives the Android side |
| Database | PostgreSQL | Relational — a good fit for Parent/Teacher/Class features (see ROADMAP.md) |
| ORM/migrations | Prisma | Type-safe client, migration history |
| Auth | JWT access token (short-lived) + rotated refresh token (stored hashed), bcrypt/argon2 for passwords | Replaces the Firebase-Auth-has-no-username workaround with a real `username` column |
| File storage | S3 or Cloudflare R2, presigned PUT URLs | Metadata row in Postgres (`task_attachments`), same role Firebase Storage played |
| Push | Keep Firebase Cloud Messaging | FCM is just a delivery mechanism, independent of the datastore/auth choice — no need to migrate it |
| Hosting | Railway / Render / Fly for MVP scale; AWS RDS + ECS later if needed | |

## Schema sketch

Maps directly from the existing DTOs in `data/model/`:

```
users(id, username, password_hash, full_name, role, school_id, class_id, parent_id)
subjects(id, student_id, name, ...)
tasks(id, student_id, type, subject_id, title, description, due_at, status,
      points_value, student_comment, teacher_comment)
task_attachments(id, task_id, url, size_bytes)
points_ledger(id, student_id, task_id, label, points, earned_at)

-- added once Parent/Teacher roles land (ROADMAP.md sprints 5-6):
classes(id, teacher_id, name)
class_students(class_id, student_id)
parent_links(parent_id, student_id)
```

## API contract = the existing domain interfaces, almost verbatim

| Domain interface method | Endpoint |
|---|---|
| `AuthRepository.login` | `POST /auth/login` |
| `AuthRepository.register` | `POST /auth/register` |
| `AuthRepository.currentUser` | `GET /auth/me` |
| `AuthRepository.logout` | `POST /auth/logout` |
| `TaskRepository.getDailyTasks` | `GET /tasks?studentId=` |
| `TaskRepository.getTask` | `GET /tasks/:id` |
| `TaskRepository.ensureTodaysPrayerTasks` | `POST /tasks/ensure-prayers` |
| `TaskRepository.markTaskCompleted` | `PATCH /tasks/:id/complete` |
| `TaskRepository.submitHomework` | `POST /tasks/:id/submit` |
| `NoteRepository` CRUD | `GET/POST/PATCH/DELETE /notes` |
| `SubjectRepository` get/save | `GET/POST /subjects` |
| `PointsRepository.getPointsSummary` | `GET /points/summary?studentId=` |
| `PointsRepository.awardPoints` | `POST /points/award` |

Write the OpenAPI spec from this table before implementation starts.

## Migration steps

1. ✅ **Design & scaffold.** Write the OpenAPI spec above; stand up the NestJS service with Prisma migrations against the schema sketch. (Sprints 2–3, in the [backend repo](https://github.com/mohamedhamdy-Cis/studentcompanion-backend).)
2. **Deploy to staging.** Smoke-test every endpoint directly (Postman/curl) against the real contract before touching Android code. Render Blueprint (`render.yaml`) is written; not yet actually deployed.
3. ✅ **Android REST layer.** Add Retrofit + a `TokenAuthenticator` that attaches the JWT and refreshes on 401. Implement one `Rest*RepositoryImpl` per domain interface, gated behind `BuildConfig.USE_REST_BACKEND` — `domain/` and `presentation/` are untouched by construction. (Sprint 4, merged.)
4. ✅ **Data migration script.** `npm run migrate:firestore` in the backend repo (`src/migration/`) reads every Firestore collection and upserts into Postgres via Prisma — **preserving Firestore document IDs as the Postgres row id** (simpler than remapping to new UUIDs: every foreign key, `task.studentId`/`points_ledger.taskId`/attachment refs, already points at the right row with no remapping table). Idempotent, and a doc that fails to migrate (e.g. an orphaned reference) is skipped with a logged reason rather than aborting the run. **Passwords are not migrated** — Firebase Auth's scrypt hash isn't portable to bcrypt; migrated users get a random temp password and a `migration-password-report.json` to distribute out of band. Verified against the local Firestore emulator + a real Postgres database; not yet run against the real production Firestore project.
5. **Cutover.** Flip `RepositoryModule.kt`'s backend-selection to `Rest*Impl` by setting `BuildConfig.USE_REST_BACKEND = true`. Soak-test with a small user group before the full switch — not done yet; needs a device/emulator (none available in the environment this was built in) and the real production migration run (step 4) first.
6. **Decommission.** Remove Firestore/Auth/Storage SDKs and `firestore.rules` once the cutover is confirmed stable. Keep `google-services.json`/FCM if push stays on Firebase.

## Rollback plan

Keep the Firebase `*Impl` classes compiled in (behind the same flag used for cutover) for one full release cycle after the switch, so a regression can be reverted without a store release delay.
