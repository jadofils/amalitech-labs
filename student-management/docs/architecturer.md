# Architecture — Student Grade Management System

This document explains how the folders/layers relate and call each other.
For the phased build order, see [PLAN.md](PLAN.md).

---

## 1. Layer Dependency Diagram

Arrows show "depends on" / "calls into" — always pointing downward.
Nothing ever calls upward (a repository never calls a service; a model
never calls a controller).

```
                         ┌─────────────┐
                         │   Main.java │   composition root: builds every
                         └──────┬──────┘   object below and wires them together
                                │ constructs & calls
                                ▼
                         ┌─────────────┐
                         │ controller/ │   reads Scanner input, prints output,
                         └──────┬──────┘   catches exceptions from service layer
                                │ calls (through the service INTERFACE)
                                ▼
                         ┌─────────────┐
                         │  service/   │   interfaces only — the business contract
                         └──────┬──────┘   (StudentService, GradeService)
                                │ implemented by
                                ▼
                         ┌─────────────┐
              ┌──────────┤ serviceimpl/│──────────┐   the real business logic:
              │          └──────┬──────┘          │   rules, orchestration
              │ uses            │ uses             │ uses
              ▼                 ▼                  ▼
       ┌────────────┐   ┌─────────────┐    ┌───────────────┐
       │ validation/ │   │ repository/ │    │  exceptions/  │
       └──────┬──────┘   └──────┬──────┘    └───────────────┘
              │ throws          │ implemented by            ▲
              ▼                 ▼                           │
       ┌───────────────┐ ┌──────────────────┐               │
       │  exceptions/  │ │ repository/impl/  │  throws on DB failure
       └───────────────┘ └─────────┬─────────┘               │
                                    │ uses                    │
                                    ▼                          │
                          ┌──────────────┐                    │
                          │   config/    │  reads .env, opens  │
                          │ (JDBC conn)  │  Connection to      │
                          └──────┬───────┘  Postgres           │
                                 ▼                              │
                          ┌──────────────┐                     │
                          │  PostgreSQL  │                     │
                          └──────────────┘                     │
                                                                 │
       ┌───────────────────────────────────────────────────────┘
       │  model/  — Student, Subject, Grade, Gradable
       │  Referenced by EVERY layer above (method params/return types),
       │  but model/ itself depends on nothing else in the app.
       └───────────────────────────────────────────────────────
```

---

## 2. Folder-by-Folder Responsibility

| Folder | Depends on | Never does |
|---|---|---|
| `model/` | nothing (pure domain classes) | no SQL, no `Scanner`, no `System.out` |
| `exceptions/` | nothing | no business logic |
| `validation/` | `model`, `exceptions` | never touches the database |
| `repository/` (interfaces) | `model`, `exceptions` | defines *what* persistence can do, not *how* |
| `repository/impl/` | `model`, `exceptions`, `config` | never contains business rules (no "is this Honors-eligible" logic here) |
| `service/` (interfaces) | `model`, `exceptions` | defines *what* the business layer can do |
| `serviceimpl/` | `model`, `exceptions`, `validation`, `repository` (interfaces only) | never imports `repository.impl` directly, never touches `Scanner`/`System.out` |
| `controller/` | `service` (interfaces only), `model`, `exceptions` | never imports `serviceimpl` or `repository` directly, never contains business rules |
| `config/` | nothing app-specific | just connection/credential plumbing |
| `Main.java` | everything (it's the only class allowed to `new` up every concrete impl) | no business logic, no SQL, no console formatting itself |

**The one rule that matters most:** `serviceimpl` depends on the `repository` *interface*, never on `repository.impl` directly. `controller` depends on the `service` *interface*, never on `serviceimpl` directly. Only `Main.java` is allowed to know about concrete implementations — everywhere else, layers talk to interfaces. This is what makes it possible to swap `GradeRepositoryImpl` for a different storage engine later without touching `serviceimpl` at all.

---

## 3. Worked Example: "Record Grade" End-to-End

Tracing Screenshot 5 from the spec (recording a Mathematics grade for STU001)
through the folders, to make the diagram concrete:

1. **`Main`** already built one `GradeController`, wired to a `GradeService` (backed by `GradeServiceImpl`).
2. User picks menu option 3 → **`controller/GradeController`** reads student ID, subject choice, and grade value via `Scanner`.
3. `GradeController` calls `gradeService.recordGrade(studentId, subjectCode, grade)` — it only knows the `service/GradeService` interface, not the implementation.
4. **`serviceimpl/GradeServiceImpl.recordGrade(...)`** runs:
   a. Calls **`validation/GradeValidator`** to check `0 <= grade <= 100` — throws **`exceptions/InvalidGradeException`** if not, which propagates back up to the controller to print a friendly error.
   b. Calls **`repository/StudentRepository.findById(studentId)`** (interface) — actually runs through **`repository/impl/StudentRepositoryImpl`**, which uses **`config/ConnectionManager`** to reach Postgres. Throws **`exceptions/StudentNotFoundException`** if missing.
   c. Calls **`repository/SubjectRepository.findByCode(subjectCode)`** the same way.
   d. Builds a **`model/Grade`** object, calls **`repository/GradeRepository.save(grade)`**.
   e. Recomputes the student's average via existing grades (fetched through `GradeRepository`), updates `honorsEligible` if the student is a **`model/HonorsStudent`**, and persists that through `StudentRepository`.
5. Control returns to `GradeController`, which prints the `GRADE CONFIRMATION` block from the spec.

Every arrow in that trace matches an arrow in the diagram above — nothing skips a layer.

---

## 4. Why This Shape (for when it feels like overkill)

Nine classes in the original spec becomes ~20 here. That's not accidental
complexity — each split maps to one question:

- **`model` vs `repository`** — "what is a Student" vs "how do I save one." A `Student` object should be usable in a unit test with zero database involved.
- **`repository` interface vs `repository/impl`** — "what can I ask the data layer for" vs "how it's actually fetched." Lets `serviceimpl` be written and reasoned about before `repository/impl` even compiles.
- **`service` vs `serviceimpl`** — same split, one level up: "what can the app do" vs "how it does it."
- **`validation` and `exceptions` as their own folders** — keeps `serviceimpl` readable; business logic shouldn't be interleaved with a wall of `if (grade < 0) throw ...` checks.
- **`controller`** — the only layer allowed to talk to a human (via `Scanner`/`System.out`), so nothing else needs to think about console formatting.
