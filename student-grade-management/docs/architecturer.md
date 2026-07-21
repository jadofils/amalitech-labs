# Architecture — Student Grade Management System

This document explains how the folders/layers relate and call each other,
as the code actually stands on `develop` today. For the original,
pre-implementation plan (PostgreSQL, `controller/`, Maven from day one),
see [Plan.md](Plan.md); for the full history of how the two diverged, see
[../CHANGELOG.md](../CHANGELOG.md).

---

## 1. Layer Dependency Diagram

Arrows show "depends on" / "calls into" — always pointing downward.
Nothing ever calls upward (a repository never calls a service; a model
never calls a manager).

```
                         ┌─────────────┐
                         │  app.Main   │   composition root only: builds every
                         └──────┬──────┘   object below and wires them together
                                │ constructs, then hands off to
                                ▼
                         ┌─────────────┐
                         │app.ConsoleApp│  the menu loop: print, read, dispatch,
                         └──────┬──────┘  translate exceptions - takes its Scanner
                                │ calls    via constructor, so a test can swap in
                                │          scripted input instead of System.in
                                ▼
                         ┌─────────────┐
                         │  console/   │   one MenuAction implementation per
                         └──────┬──────┘   menu feature — the only *Action I/O
                                │ calls (through the manager/calculator/... APIs)
                                ▼
                 ┌──────────────┴───────────────┐
                 ▼                               ▼
          ┌─────────────┐                ┌───────────────┐
          │  manager/   │                │  calculators/  │  GPACalculator,
          └──────┬──────┘                │  export/       │  ReportGenerator,
                 │ calls (service         │  imports/      │  BulkImportService -
                 │ INTERFACE)             └───────┬────────┘  each owns one interface
                 ▼                                │            colocated with its
          ┌─────────────┐                         │            sole implementer
          │  service/   │  interface + Impl                    (Calculable, Exportable)
          └──────┬──────┘  colocated in the
                 │ implemented by         same package
                 ▼
          ┌─────────────┐
          │  ...Impl    │   the real business logic: rules, orchestration
          └──────┬──────┘
                  │ uses
        ┌─────────┼───────────┐
        ▼         ▼           ▼
 ┌────────────┐ ┌───────────┐ ┌───────────────┐
 │ utils/     │ │repository/│ │  exceptions/  │
 │validators/ │ └─────┬─────┘ └───────────────┘
 └────────────┘       │ implemented by
                       ▼
                ┌──────────────────┐
                │ ...Impl          │  array-backed in-memory storage —
                └──────────────────┘  Student[50], Grade[200], Subject[50] — no DB, no config

       ┌───────────────────────────────────────────────────────
       │  model/  — Student, Subject, Grade, Gradable, enums/
       │  Referenced by EVERY layer above (method params/return types),
       │  but model/ itself depends on nothing else in the app.
       └───────────────────────────────────────────────────────

       ┌───────────────────────────────────────────────────────
       │  dto/ + mapper/  — read-only projections for two display paths
       │  only (Search Students results, exported detailed report).
       │  Everywhere else in the app still passes real model/ objects.
       └───────────────────────────────────────────────────────
```

---

## 2. Folder-by-Folder Responsibility

| Folder | Depends on | Never does |
|---|---|---|
| `model/` (incl. `model/enums/`) | nothing (pure domain classes + enums) | no `Scanner`, no `System.out` |
| `exceptions/` | nothing (all extend the common `ApplicationException`) | no business logic |
| `utils/`, `utils/validators/` | `model`, `exceptions` | never calls a repository; pure functions only |
| `repository/{student,grade,subject}/` | `model`, `exceptions` | interface defines *what* persistence can do; `Impl` (same package) defines *how* — array-backed, never a `HashMap` |
| `service/` | `model`, `exceptions`, `utils.validators`, `repository` (interfaces only) | interface + `Impl` colocated in the same package; `Impl` never imports a repository's `Impl` class directly, only its interface |
| `manager/` | `service` (interfaces only), `repository` (interfaces), `model` | never contains business rules itself — delegates to services, hydrates data for display |
| `calculators/`, `export/`, `imports/` | `manager`, `model`, `utils` | each owns one small interface (`Calculable`, `Exportable`) colocated with its one implementer in the same package — no separate `interfaces/` package exists |
| `dto/`, `mapper/` | `model` | never used by Add Student / Record Grade — those keep working with real domain objects |
| `console/` | `manager`, `calculators`, `export`, `imports`, `dto`, `mapper`, `utils`, `model.enums.Role` | the layer that touches `Scanner`/`System.out` for each individual feature; each `MenuAction` owns exactly one menu feature |
| `app.ConsoleApp` | `console` (`MenuAction`), `model.enums.Role` | the menu loop itself (print, read, dispatch, authorize, translate exceptions) — takes its `Scanner` via constructor rather than binding to `System.in`, which is what makes it unit-testable (`tests/app/ConsoleAppTest.java`) |
| `app.Main` | everything (it's the only class allowed to `new` up every concrete impl) | no business logic, no data-access logic, no console formatting of its own — it only builds the `console/*Action` instances and one `ConsoleApp`, then calls `run()` |

**The one rule that matters most:** every `...Impl` class depends on the
*interface* one layer down, never on another layer's `Impl` directly.
`service.StudentServiceImpl` depends on `repository.StudentRepository`
(the interface), never on `StudentRepositoryImpl`. Only `Main.java` is
allowed to know about concrete implementations — everywhere else, layers
talk to interfaces. This is what makes it possible to swap
`StudentRepositoryImpl` for a different storage engine later without
touching `StudentServiceImpl` at all.

**Why interfaces sit next to their implementer instead of in one shared
`interfaces/` folder:** `StudentService`/`StudentServiceImpl`,
`StudentRepository`/`StudentRepositoryImpl`, `Calculable`/`GPACalculator`,
`Exportable`/`ReportGenerator`, and `Searchable`/`StudentSearcher` are all
colocated this way — a single, consistent convention across the whole
codebase (this used to be split two ways; see CHANGELOG.md's "Professional
structure & SOLID refactor" section for why it was unified). The `console`
package's own `MenuAction` interface follows the same pattern.

---

## 3. Worked Example: "Record Grade" End-to-End

Tracing the recording of a Mathematics grade for STU001 through the
folders, to make the diagram concrete:

1. **`app.Main`** already built one `GradeManager`, wired to a `GradeService`
   (backed by `GradeServiceImpl`), and one `RecordGradeAction` holding
   references to both, then handed the full action list to an
   **`app.ConsoleApp`** and called `run()`.
2. User picks menu option 3 → **`console/RecordGradeAction.execute()`**
   reads student ID, subject type, subject choice, and grade value via
   `Scanner`, sanitizing free-text input through `utils.InputSanitizer`.
3. `RecordGradeAction` calls `gradeManager.addGrade(grade)` — it only
   knows the `manager/GradeManager` facade, never a service or repository
   directly.
4. **`manager/GradeManager.addGrade(...)`** delegates to
   `gradeService.recordGrade(...)`.
5. **`service/GradeServiceImpl.recordGrade(...)`** runs:
   a. Calls **`repository/StudentRepository.findStudentById(studentId)`**
      (interface) — runs through **`repository/student/StudentRepositoryImpl`**
      (a `Student[50]` array, not a `HashMap`). Throws
      **`exceptions/StudentNotFoundException`** if missing.
   b. Calls **`repository/SubjectRepository.findSubjectByCode(subjectCode)`**
      the same way.
   c. Builds a **`model/grade/Grade`** object — its constructor itself
      validates `0 <= grade <= 100` via the `Gradable` contract, throwing
      **`exceptions/InvalidGradeException`** if not.
   d. Calls **`repository/GradeRepository.addGrade(grade)`**
      (interface) → **`repository/grade/GradeRepositoryImpl`** (a
      `Grade[200]` array).
6. Control returns to `RecordGradeAction`, which prints the
   `GRADE CONFIRMATION` block; `ConsoleApp`'s loop reads the next menu choice.

Every arrow in that trace matches an arrow in the diagram above — nothing
skips a layer.

---

## 4. Why This Shape (for when it feels like overkill)

- **`model` vs `repository`** — "what is a Student" vs "how do I save
  one." A `Student` object should be usable in a unit test with zero
  storage involved.
- **`repository` interface vs `Impl` (same package)** — "what can I ask
  the data layer for" vs "how it's actually fetched." Lets
  `service.*Impl` be written and reasoned about before a given `Impl`
  even compiles.
- **`service` interface vs `Impl` (same package)** — same split, one
  level up: "what can the app do" vs "how it does it."
- **`utils.validators` and `exceptions` as their own packages** — keeps
  `service.*Impl` readable; business-rule validation shouldn't be
  interleaved with a wall of `if (grade < 0) throw ...` checks, and every
  custom exception shares one root (`ApplicationException`) so `Main`
  never needs a generic `catch (Exception e)`.
- **`manager`** — the layer most of `console/` actually depends on,
  providing a higher-level API (hydrate grades, compute averages, format
  reports) without each `MenuAction` needing to orchestrate multiple
  services itself.
- **`console/` (not a `controller` package)** — one class per menu
  feature, all implementing `MenuAction`. This replaced a single
  760-line `Main` that mixed console I/O, orchestration, and
  authorization for every feature in one file — see CHANGELOG.md's SRP
  fix for the full reasoning. The app is still small enough that this is
  the only "controller-shaped" layer it needs; there's no separate
  `service`-facing controller on top of it.
- **`app.Main` vs `app.ConsoleApp`** — a second, smaller SRP split on top
  of the first one: even after extracting `console/*Action` classes,
  `Main` still mixed "build the dependency graph" with "run the
  interactive loop," and the loop was bound to `System.in` in a
  `static final Scanner` field, which made it untestable no matter how
  the rest of the app was refactored. Splitting the loop out into
  `ConsoleApp` - an ordinary instance class taking its `Scanner` as a
  constructor argument - means `Main` is now too thin to need its own
  test, and `ConsoleApp` is fully unit-testable with a fake `Scanner` and
  a captured `System.out` (see `tests/app/ConsoleAppTest.java`).
- **`dto`/`mapper`, scoped narrowly** — only Search Students' results and
  the exported detailed report go through a DTO. Add Student and Record
  Grade intentionally keep using real domain objects, because they still
  need the full validation surface (`utils.validators`) that a
  display-only DTO has no reason to carry.
