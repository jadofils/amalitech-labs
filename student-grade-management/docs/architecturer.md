# Architecture — Student Grade Management System

This document explains how the folders/layers relate and call each other.
For the phased build order, see [PLAN.md](PLAN.md).

---

## 1. Layer Dependency Diagram

Arrows show "depends on" / "calls into" — always pointing downward.
Nothing ever calls upward (a repository never calls a service; a model
never calls a manager).

```
                         ┌─────────────┐
                         │   Main.java │   composition root: builds every
                         └──────┬──────┘   object below and wires them together
                                │ constructs & calls
                                ▼
                         ┌─────────────┐
                         │  manager/   │   facades: hydrates grades, computes
                         └──────┬──────┘   averages, formats output for Main
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
              │ throws          │ implemented by
              ▼                 ▼
       ┌───────────────┐ ┌──────────────────┐
        │  exceptions/  │ │ repository/impl/  │  array-backed in-memory
       └───────────────┘ └──────────────────┘  storage — Student[50], Grade[200], Subject[50] arrays — no DB, no config

       ┌───────────────────────────────────────────────────────
       │  model/  — Student, Subject, Grade, Gradable
       │  Referenced by EVERY layer above (method params/return types),
       │  but model/ itself depends on nothing else in the app.
       └───────────────────────────────────────────────────────
```

---

## 2. Folder-by-Folder Responsibility

| Folder | Depends on | Never does |
|---|---|---|
| `model/` | nothing (pure domain classes) | no `Scanner`, no `System.out` |
| `exceptions/` | nothing | no business logic |
| `validation/` | `model`, `exceptions` | never calls a repository |
| `repository/` (interfaces) | `model`, `exceptions` | defines *what* persistence can do, not *how* |
| `repository/impl/` | `model`, `exceptions` | never contains business rules (no "is this Honors-eligible" logic here) |
| `service/` (interfaces) | `model`, `exceptions` | defines *what* the business layer can do |
| `serviceimpl/` | `model`, `exceptions`, `validation`, `repository` (interfaces only) | never imports `repository.impl` directly, never touches `Scanner`/`System.out` |
| `manager/` | `service` (interfaces only), `repository` (interfaces), `model`, `exceptions` | never contains business rules itself — delegates to services, hydrates data |
| `Main.java` | everything (it's the only class allowed to `new` up every concrete impl) | no business logic, no data-access logic, no console formatting itself |

**The one rule that matters most:** `serviceimpl` depends on the `repository` *interface*, never on `repository.impl` directly. `manager` depends on the `service` *interface*, never on `serviceimpl` directly. Only `Main.java` is allowed to know about concrete implementations — everywhere else, layers talk to interfaces. This is what makes it possible to swap `StudentRepositoryImpl` for a different storage engine later without touching `serviceimpl` at all.

---

## 3. Worked Example: "Record Grade" End-to-End

Tracing the recording of a Mathematics grade for STU001 through the
folders, to make the diagram concrete:

1. **`Main`** already built one `GradeManager`, wired to a `GradeService` (backed by `GradeServiceImpl`).
2. User picks menu option 6 → **`Main`** reads student ID, subject type, subject choice, and grade value via `Scanner`.
3. `Main` calls `gradeManager.addGrade(grade)` — it only knows the `manager/GradeManager` facade.
4. **`manager/GradeManager.addGrade(...)`** delegates to `gradeService.recordGrade(...)`.
5. **`serviceimpl/GradeServiceImpl.recordGrade(...)`** runs:
   a. Calls **`validation/GradeValidator`** to check `0 <= grade <= 100` — throws **`exceptions/GradeException`** if not.
   b. Calls **`repository/StudentRepository.findStudentById(studentId)`** (interface) — runs through **`repository/impl/StudentRepositoryImpl`** (in-memory `HashMap`). Throws **`exceptions/StudentNotFoundException`** if missing.
   c. Calls **`repository/SubjectRepository.findByCode(subjectCode)`** the same way.
   d. Builds a **`model/Grade`** object, calls **`repository/GradeRepository.addGrade(grade)`**.
   e. Recomputes the student's average via existing grades (fetched through `GradeRepository`), updates `honorsEligible` if the student is a **`model/HonorsStudent`**, and updates through `StudentRepository`.
6. Control returns to `Main`, which prints the `GRADE CONFIRMATION` block.

Every arrow in that trace matches an arrow in the diagram above — nothing skips a layer.

---

## 4. Why This Shape (for when it feels like overkill)

Nine classes in the original spec becomes ~20 here. That's not accidental
complexity — each split maps to one question:

- **`model` vs `repository`** — "what is a Student" vs "how do I save one." A `Student` object should be usable in a unit test with zero storage involved.
- **`repository` interface vs `repository/impl`** — "what can I ask the data layer for" vs "how it's actually fetched." Lets `serviceimpl` be written and reasoned about before `repository/impl` even compiles.
- **`service` vs `serviceimpl`** — same split, one level up: "what can the app do" vs "how it does it."
- **`validation` and `exceptions` as their own folders** — keeps `serviceimpl` readable; business logic shouldn't be interleaved with a wall of `if (grade < 0) throw ...` checks.
- **`manager`** — the layer that Main actually depends on, providing a higher-level API (hydrate grades, compute averages, format reports) without Main needing to orchestrate multiple services itself.
- **No `controller` package** — Main handles menu I/O directly, since the app is small enough that a separate controller layer adds indirection without benefit.
