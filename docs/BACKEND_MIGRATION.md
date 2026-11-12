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

1. **Design & scaffold.** Write the OpenAPI spec above; stand up the NestJS service with Prisma migrations against the schema sketch.
2. **Deploy to staging.** Smoke-test every endpoint directly (Postman/curl) against the real contract before touching Android code.
3. **Android REST layer.** Add Retrofit + an `AuthInterceptor` that attaches the JWT and refreshes on 401. Implement one `Rest*RepositoryImpl` per domain interface — `domain/` and `presentation/` are untouched by construction.
4. **Data migration script.** A one-time Node/TS script reads every Firestore collection and inserts into Postgres via Prisma, remapping Firestore document IDs to UUIDs and re-pointing foreign keys (`task.studentId`, `points_ledger.taskId`, attachment references).
5. **Cutover.** Flip `RepositoryModule.kt`'s `@Binds` targets from `Firebase*Impl` to `Rest*Impl`, ideally behind a build flavor or remote flag so it can be reverted without a new release. Soak-test with a small user group before the full switch.
6. **Decommission.** Remove Firestore/Auth/Storage SDKs and `firestore.rules` once the cutover is confirmed stable. Keep `google-services.json`/FCM if push stays on Firebase.

## Rollback plan

Keep the Firebase `*Impl` classes compiled in (behind the same flag used for cutover) for one full release cycle after the switch, so a regression can be reverted without a store release delay.
