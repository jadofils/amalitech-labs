# Student Grade Management System — Build Plan
### Plain Java (no frameworks) + In-Memory Array Storage + Layered OOP Architecture

---

## 0. Context

This plan extends the original **Student Grade Management** lab spec
(`Java Basics/Student-Grade-Mgt-I.md`), which asked for an in-memory
main.console main.app with 9 classes and arrays as storage. We keep an in-memory
approach with array-based repositories, organized in a layered architecture.

**Key conventions:**
- Storage: a **main.repository layer** (array-backed) replaces the simple array
  approach from the original spec with proper separation of concerns.
  `StudentManager` / `GradeManager`.
- Two packages beyond the original list: **`main.repository`** (data access)
  and **`main.manager`** (facade over services).
- **`Main`** does menu I/O directly (no separate `controller` package).
- Plain Java, no build tools, no external dependencies.

**How we will work:** same as before -- phase by phase, you write the code,
I review and explain, no finished classes handed to you.

---

## 1. Tech Stack

Plain Java (JDK 17+), `Scanner` for main.console I/O, `HashMap` for in-memory
storage. No build tools, no databases, no external dependencies.

---

## 2. Final Package Structure

```
com.amalitech.studentgrades
├── main.model/            -> Student, RegularStudent, HonorsStudent, Subject, CoreSubject, ElectiveSubject, Grade, Gradable
├── validation/         -> StudentValidator, GradeValidator
├── main.exceptions/         -> custom exception hierarchy
├── main.repository/         -> interfaces + in-memory HashMap impls
│   └── impl/
├── main.service/            -> business-logic interfaces
├── serviceimpl/        -> business-logic implementations
├── main.manager/            -> facade over services (what Main actually calls)
└── Main.java           -> composition root, menu loop
```

---

## 3. Original Spec Classes -> New Layered Home

| Original class (spec) | New location | Notes |
|---|---|---|---|
| `Student` (abstract) | `main.model` | unchanged role |
| `RegularStudent`, `HonorsStudent` | `main.model` | unchanged role |
| `Subject` (abstract) | `main.model` | unchanged role |
| `CoreSubject`, `ElectiveSubject` | `main.model` | unchanged role |
| `Gradable` (interface) | `main.model` | kept with the domain objects it describes |
| `Grade` | `main.model` | unchanged role |
| `StudentManager` | split -> `main.repository.StudentRepository` (+ impl) and `main.service`/`serviceimpl` | array storage becomes `HashMap`-backed main.repository; class-average logic moves to main.service |
| `GradeManager` | split -> `main.repository.GradeRepository` (+ impl) and `main.service`/`serviceimpl` | same pattern |
| *(new)* `SubjectRepository` | `main.repository` | subjects seeded as reference data in-memory |
| *(new)* validators | `validation` | grade range (0-100), student existence, required fields |
| *(new)* main.exceptions | `main.exceptions` | e.g. "invalid grade", "student not found" -- implied by spec, never named |
| *(new)* `StudentManager`, `GradeManager` | `main.manager` | facades that Main actually calls; hydrate grades, compute averages |
| `Main` | root | wiring + menu loop only |

**Class count grows from 9 to roughly 20-22**: 3 repositories (+3 impls), 2 services (+2 impls), 2 managers, 2 validators, 3-4 exception types, plus the original 8 main.model classes (`Student` hierarchy, `Subject` hierarchy, `Grade`, `Gradable`).

---

## 4. In-Memory Storage Design

Three main.repository interfaces, each backed by a `HashMap`:

- **`StudentRepositoryImpl`** — `HashMap<String, Student>` keyed by student ID.
- **`SubjectRepositoryImpl`** — `HashMap<String, Subject>` keyed by subject code.
- **`GradeRepositoryImpl`** — `HashMap<String, Grade>` keyed by grade ID.

Subjects are seeded at construction time with 6 fixed entries (Mathematics,
English, Science as Core; Music, Art, Physical Education as Elective).
Students are seeded with 3 sample entries. All data lives only for the
duration of the JVM process — no persistence across restarts.

**Design decisions:**
1. **Subjects** — seeded in code rather than from a database table. Adding a
   new subject requires a Java change.
2. **Single in-memory store** — no need for table-per-subclass since objects
   are stored by reference, not serialized to rows.
3. **ID generation** — `private static int` counters (`studentCounter`,
   `gradeCounter`) generate `STU001`, `GRD001`, etc. On startup the managers
   scan existing entries to pick up after seed data.
4. **Averages** — computed in Java by `GradeManager` (iterate grades list,
   aggregate by subject type). No SQL involved.

---

## 5. Phased Roadmap

### Phase 0 -- Environment Setup
**Goal:** a plain-Java project that compiles and runs.
- Create the project skeleton with the package tree from Section 2
- Init git, `.gitignore`, first commit
  **Definition of done:** `Main.java` compiles with `javac` and prints a hello-world.

### Phase 1 -- Domain Model (`main.model`)
**Goal:** the class hierarchy from the original spec.
- `Student` (abstract) -> `RegularStudent`, `HonorsStudent`
- `Subject` (abstract) -> `CoreSubject`, `ElectiveSubject`
- `Grade`, `Gradable` interface
- Implement `Grade.getLetterGrade()` (numeric -> A/B/C/D/F)
- Implement `Student.calculateAverageGrade()` / `isPassing()`
  **Definition of done:** in a scratch `main`, build a `HonorsStudent` with a few `Grade` objects and correctly print their average and passing status.

### Phase 2 -- Exceptions (`main.exceptions`)
**Goal:** a small, purposeful hierarchy.
- `StudentNotFoundException`, `SubjectNotFoundException`, `GradeException` (grade outside 0-100)
- Decide checked vs. unchecked per scenario
  **Definition of done:** you can explain who catches each exception and why.

### Phase 3 -- Validation (`validation`)
**Goal:** keep bad data out before it reaches services.
- `StudentValidator`: name/email/phone non-blank, age in a sane range
- `GradeValidator`: grade must be within 0-100, student ID and subject code must be well-formed
- Validators throw Phase 2 main.exceptions
  **Definition of done:** bad input (e.g. grade = 150) reliably throws `GradeException` before any main.repository call.

### Phase 4 -- Repository Layer (`main.repository`)
**Goal:** in-memory persistence.
- `StudentRepository`, `SubjectRepository`, `GradeRepository` (interfaces)
- `StudentRepositoryImpl` — `HashMap<String, Student>`
- `SubjectRepositoryImpl` — `HashMap<String, Subject>` with 6 seeded subjects
- `GradeRepositoryImpl` — `HashMap<String, Grade>`
- Solve ID generation (seed static counters from existing entries on startup)
  **Definition of done:** from a scratch main, insert a student and a grade and read them back through repositories only.

### Phase 5 -- Service Interfaces (`main.service`)
**Goal:** define the business contract.
- `StudentService`: `addStudent(...)`, `getAllStudents()`, `getAverageClassGrade()`
- `GradeService`: `recordGrade(...)`, `getGradeReport(studentId)`, `calculateCoreAverage(studentId)`, `calculateElectiveAverage(studentId)`, `calculateOverallAverage(studentId)`
- No implementation yet — just method contracts
  **Definition of done:** one-sentence pre/post-conditions for every method.

### Phase 6 -- Service Implementations (`serviceimpl`)
**Goal:** the real business logic.
- `GradeServiceImpl.recordGrade(...)`: validate -> look up student & subject via repositories -> persist grade -> **recompute the student's `honorsEligible` flag** (average >= 85%)
- `StudentServiceImpl`: wraps `StudentRepository`, enforces passing-grade business rules
- Averages computed in Java
  **Definition of done:** recording a grade that pushes an Honors student's average past 85% flips their eligibility on the next "View Students" call.

### Phase 7 -- Manager Layer (`main.manager`)
**Goal:** facades that Main actually calls.
- `StudentManager`: facade over `StudentService` + `GradeManager`; hydrates transient `grades` list on every read, syncs `studentCounter`
- `GradeManager`: facade over `GradeService` + `SubjectRepository`; computes core/elective/overall averages, formats grade reports
  **Definition of done:** `Main` can call `studentManager.findStudent(id)` and get back a fully-hydrated `Student` with accurate averages.

### Phase 8 -- Main / Composition Root
**Goal:** wire it all together.
- `Main.java`: construct repositories -> services -> managers by hand
- Menu loop (8 options, loop until exit, invalid-input handling)
  **Definition of done:** full main.app runs end-to-end, matching all expected main.console screenshots.

### Phase 9 -- Testing
**Goal:** confidence the rules hold.
- Run all test scenarios: add regular/honors, record core/elective grade, grade validation bounds, empty report, populated report, honors-eligibility flip, ID auto-generation
  **Definition of done:** all scenarios pass.

### Phase 10 -- Documentation & Submission
**Goal:** a repo a stranger could run.
- `README.md` with setup steps and architecture overview
- Comments only where the *why* is not obvious
- Push to a public GitHub repo
  **Definition of done:** runnable from the README alone with just a JDK installed.

---

## 6. Stretch Goals

- Swap the `HashMap` repositories for a different storage engine (e.g., a file-backed store or a real database) by implementing the same main.repository interfaces — no other layer needs to change.
- Add persistence by serializing the HashMaps to a JSON file on shutdown and reloading on startup.
- Sort grade history in reverse chronological order (already implemented in `GradeManager.viewGradesByStudent`).
- Write JUnit 5 tests for `GradeValidator` bounds and the honors-eligibility recomputation logic.

---

