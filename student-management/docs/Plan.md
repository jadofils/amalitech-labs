# Student Grade Management System — Build Plan
### Plain Java (no frameworks) + PostgreSQL (JDBC) + Layered OOP Architecture

---

## 0. Context

This plan extends the original **Student Grade Management** lab spec
(`Java Basics/Student-Grade-Mgt-I.md`), which asked for an in-memory
console app with 9 classes and arrays as storage. Same treatment as the
[Bank Account Management](../bank-account-management/PLAN.md) project:
layered architecture, real PostgreSQL persistence, plain Java throughout.

**Conventions carried over from the Bank Account plan** (so we are not
re-deciding the same things twice):
- Storage: a **repository layer** (JDBC) replaces the array-based
  `StudentManager` / `GradeManager`.
- Two packages beyond your original list: **`repository`** (data access)
  and **`config`** (DB connection setup).
- **`controller`** means console/menu controllers -- read input, call
  services, print output.
- Maven, JDK 17+, raw JDBC (no ORM), `Scanner` for console I/O.

**Rubric note** (same caveat as before): the lab spec grades "arrays" and
"static counters" explicitly. If this is for that grading rubric, call out
the DB swap in your README. If it is for your own learning/portfolio, no
issue.

**How we will work:** same as before -- phase by phase, you write the code,
I review and explain, no finished classes handed to you.

---

## 1. Tech Stack

Identical to the Bank Account project -- Maven, JDK 17+, PostgreSQL, raw JDBC, `Scanner`, JUnit 5 as a stretch. Reuse the same local Postgres install; just create a second database (e.g. `studentdb`) alongside `bankdb`.

---

## 2. Final Package Structure

```
com.amalitech.studentgrades
├── model/            -> Student, RegularStudent, HonorsStudent, Subject, CoreSubject, ElectiveSubject, Grade, Gradable
├── validation/         -> StudentValidator, GradeValidator
├── exceptions/         -> custom exception hierarchy
├── repository/         -> interfaces + JDBC impls, all SQL lives here
│   └── impl/
├── service/            -> business-logic interfaces
├── serviceimpl/        -> business-logic implementations
├── controller/         -> console I/O, menu handling
├── config/             -> DB connection management, constants
└── Main.java           -> composition root, menu loop
```

---

## 3. Original Spec Classes -> New Layered Home

| Original class (spec) | New location | Notes |
|---|---|---|
| `Student` (abstract) | `model` | unchanged role |
| `RegularStudent`, `HonorsStudent` | `model` | unchanged role |
| `Subject` (abstract) | `model` | unchanged role |
| `CoreSubject`, `ElectiveSubject` | `model` | unchanged role |
| `Gradable` (interface) | `model` | kept with the domain objects it describes |
| `Grade` | `model` | unchanged role |
| `StudentManager` | split -> `repository.StudentRepository` (+ impl) and `service`/`serviceimpl` | array storage becomes SQL storage; class-average logic moves to service |
| `GradeManager` | split -> `repository.GradeRepository` (+ impl) and `service`/`serviceimpl` | same pattern |
| *(new)* `SubjectRepository` | `repository` | see Section 4 -- subjects become reference data with their own table |
| *(new)* validators | `validation` | grade range (0-100), student existence, required fields |
| *(new)* exceptions | `exceptions` | e.g. "invalid grade", "student not found" -- implied by spec, never named |
| *(new)* `StudentController`, `GradeController` | `controller` | absorbs the menu-driven I/O implicit in `Main` |
| *(new)* `DatabaseConfig` / `ConnectionManager` | `config` | needed once persistence is real |
| `Main` | root | wiring + menu loop only |

**Class count grows from 9 to roughly 20-22**: 3 repositories (+3 impls), 2 services (+2 impls), 2 validators, 3-4 exception types, 2 controllers, plus the original 8 model classes (`Student` hierarchy, `Subject` hierarchy, `Grade`, `Gradable`).

---

## 4. Database Design (conceptual)

Three tables. The interesting design decision here (different from the bank project) is how to handle **`Subject`** -- in the original spec, subjects are a fixed, known-in-advance list (Mathematics, English, Science / Music, Art, Physical Education), never created through the UI. Two options:

- **(Recommended) A real `subjects` lookup table**, seeded once with those 6 rows. `grades.subject_code` becomes a foreign key. This is the realistic pattern (reference/lookup tables are everywhere in real schemas) and makes it trivial to add a 7th elective later without touching Java code.
- Hardcode the 6 subjects as a Java `enum` or constants and skip a `subjects` table entirely. Simpler, but you lose the FK-relationship practice and it is less "database-driven."

We will settle this in Phase 1 -- I would steer you toward the lookup table since you are specifically here to practice DB design, but it is your call.

**`students`**
- `student_id` (PK, e.g. `STU001`)
- `name`, `age`, `email`, `phone`
- `student_type` (`REGULAR` / `HONORS`) -- discriminator column
- `honors_eligible` (nullable boolean -- Honors only; recomputed after each grade, see Phase 7)
- `status` (`Active`, etc.)

**`subjects`** (reference/lookup table, seeded not user-created)
- `subject_code` (PK)
- `subject_name`
- `subject_type` (`CORE` / `ELECTIVE`)
- `mandatory` (boolean -- derived from type, but stored for quick filtering)

**`grades`**
- `grade_id` (PK, e.g. `GRD001`)
- `student_id` (FK -> students)
- `subject_code` (FK -> subjects)
- `grade` (numeric 0-100)
- `date`

**Design questions to resolve in Phase 1:**
1. Lookup table vs. hardcoded subjects (above).
2. Single `students` table with nullable Honors-only columns (recommended, consistent with the Bank project) vs. table-per-subclass.
3. **ID generation** across restarts: same nuance as the bank project -- seed the static `studentCounter` / `gradeCounter` from `MAX(id)` in the DB on startup rather than resetting to 0.
4. **Where do averages get computed?** Three options: (a) fetch all grades for a student into Java and compute core/elective/overall averages there (matches the original spec's `GradeManager.calculateCoreAverage()` etc. -- recommended for Phase 7, since it is better DSA practice), or (b) let Postgres do it with `AVG() ... GROUP BY`. Do (a) first; (b) is a good Phase-12 stretch comparison.

---

## 5. Phased Roadmap

### Phase 0 -- Environment Setup
**Goal:** a Maven project that can talk to Postgres.
- Reuse your JDK/Maven/Postgres install from the Bank project; create a new database (e.g. `studentdb`) and a dedicated user/credentials
- Create the Maven project skeleton with the package tree from Section 2
- Prove connectivity with a throwaway snippet, then delete it
- Init git, `.gitignore`, first commit
  **Definition of done:** live `Connection` to `studentdb` with no errors.

### Phase 1 -- Database Design & Schema
**Goal:** finalized schema, created in Postgres.
- Resolve the 4 design questions in Section 4
- ER diagram: students, subjects, grades -- note that `subjects` is a lookup table referenced by `grades`, not owned by a student
- Write and run the DDL yourself; seed `subjects` with the 6 known rows
- Manually insert a test student + grade via psql before Java touches it
  **Definition of done:** you can `JOIN` a grade to its subject and student in psql and get a sensible row back.

### Phase 2 -- Domain Model (`model`)
**Goal:** the class hierarchy from the original spec.
- `Student` (abstract) -> `RegularStudent`, `HonorsStudent`
- `Subject` (abstract) -> `CoreSubject`, `ElectiveSubject`
- `Grade`, `Gradable` interface
- Implement `Grade.getLetterGrade()` (numeric -> A/B/C/D/F) -- pure logic, no DB, good place to practice a clean `if`/`else` or range-based lookup
- Implement `Student.calculateAverageGrade()` / `isPassing()` -- for now these can operate on a `List<Grade>` passed in; real wiring to the DB comes in Phase 7
  **Definition of done:** in a scratch `main`, build a `HonorsStudent` with a few in-memory `Grade` objects and correctly print their average and passing status.

### Phase 3 -- Exceptions (`exceptions`)
**Goal:** a small, purposeful hierarchy.
- Candidates: `StudentNotFoundException`, `SubjectNotFoundException`, `InvalidGradeException` (grade outside 0-100), a DB-failure exception
- Decide checked vs. unchecked per category, same reasoning process as the Bank project
  **Definition of done:** you can explain who catches each exception and why.

### Phase 4 -- Validation (`validation`)
**Goal:** keep bad data out before it reaches services/DB.
- `StudentValidator`: name/email/phone non-blank, age in a sane range
- `GradeValidator`: grade must be within 0-100, student ID and subject code must be well-formed before even hitting the repository
- Validators throw your Phase 3 exceptions
  **Definition of done:** bad input (e.g. grade = 150) reliably throws `InvalidGradeException` before any SQL runs.

### Phase 5 -- Repository Layer (`repository`, `config`)
**Goal:** real persistence.
- `config`: connection manager reading credentials from a properties file
- `repository`: `StudentRepository`, `SubjectRepository`, `GradeRepository` (interfaces + JDBC impls)
- `SubjectRepository` is mostly read-only (`findByCode`, `findAllCore`, `findAllElective`) since subjects are seeded, not created via the app
- Solve ID generation (seed static counters from `MAX(id)` on startup) for both `studentCounter` and `gradeCounter`
- Map `ResultSet` rows back into Phase 2 model objects
  **Definition of done:** from a scratch main, insert a student and a grade (referencing a seeded subject) and read them back through repositories only.

### Phase 6 -- Service Interfaces (`service`)
**Goal:** define the business contract.
- `StudentService`: `addStudent(...)`, `getAllStudents()`, `getAverageClassGrade()`
- `GradeService`: `recordGrade(...)`, `getGradeReport(studentId)`, `calculateCoreAverage(studentId)`, `calculateElectiveAverage(studentId)`, `calculateOverallAverage(studentId)`
- No implementation yet -- just method contracts and what each can throw
  **Definition of done:** one-sentence pre/post-conditions for every method.

### Phase 7 -- Service Implementations (`serviceimpl`)
**Goal:** the real business logic.
- `GradeServiceImpl.recordGrade(...)`: validate -> look up student & subject via repositories -> persist grade -> **recompute the student's `honorsEligible` flag** (average >= 85%) and persist that update -- this is the one piece of logic that is genuinely trickier than the bank project's, since it is a derived field that must stay in sync after every write
- `StudentServiceImpl`: wraps `StudentRepository`, enforces "Regular vs Honors passing grade" business rules
- Averages (core/elective/overall) computed here in Java per the Phase-1 decision
  **Definition of done:** recording a grade that pushes an Honors student's average past 85% flips their eligibility on the next "View Students" call, and you can point to exactly where that happens.

### Phase 8 -- Controllers (`controller`)
**Goal:** console I/O matching the spec's screenshots.
- `StudentController`: add student flow, view students listing
- `GradeController`: record grade flow (subject-type submenu -> specific subject submenu, per Screenshot 5/6), view grade report
- Catch service-layer exceptions here, print friendly messages
  **Definition of done:** each controller method reproduces the corresponding spec screenshot.

### Phase 9 -- Main / Composition Root
**Goal:** wire it all together.
- `Main.java`: construct config -> repositories -> services -> controllers by hand
- Menu loop (5 options, loop until exit, invalid-input handling)
  **Definition of done:** full app runs end-to-end against Postgres, matching all 9 console screenshots.

### Phase 10 -- Testing
**Goal:** confidence the rules hold.
- Run all 10 test scenarios from the original spec (view students, add regular/honors, record core/elective grade, grade validation bounds, empty report, populated report, honors-eligibility flip, ID auto-generation)
- New DB-specific scenario: restart the app, verify students/grades persisted and IDs continue correctly
- Stretch: JUnit 5 tests for `GradeValidator` bounds and the honors-eligibility recomputation logic
  **Definition of done:** all scenarios pass, key logic has automated tests.

### Phase 11 -- Documentation & Submission
**Goal:** a repo a stranger could run.
- `README.md` with setup steps, architecture overview, ERD, screenshots
- Externalized DB credentials (git-ignored, `.example` committed)
- Comments only where the *why* is not obvious (e.g. the honors-recompute trigger point)
- Push to a public GitHub repo
  **Definition of done:** runnable from the README alone with just Postgres installed.

---

## 6. Stretch Goals

- Move average calculations from Java to SQL (`AVG() ... GROUP BY subject_type`) and compare the two approaches
- Wrap "record grade + recompute honors eligibility" in a single JDBC transaction so a failure never leaves them out of sync
- Add a subject the UI cannot currently reach (e.g., a 7th elective) purely via SQL insert into `subjects`, and confirm the app picks it up with zero code changes -- proves the lookup-table decision was worth it
- Sort grade history at the SQL level (`ORDER BY date DESC`) vs. in Java, same comparison as the bank project's transaction history

---

