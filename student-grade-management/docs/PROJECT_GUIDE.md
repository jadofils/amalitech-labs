# Project Guide — Student Grade Management System

This is the "how it actually works" reference for the codebase as it
stands today on `develop`. `architecturer.md` and `Plan.md` in this same
folder capture the *original* design plan (which assumed PostgreSQL and a
`controller/` package); this document describes what was actually built,
which diverged from that plan in a few places (noted below), and has
since gone through a professional-structure and SOLID-principles pass —
see [../CHANGELOG.md](../CHANGELOG.md) for the full history of both.

---

## 1. What the main.app does

A main.console (terminal) application for managing students and their grades,
via a 10-option menu:

1. Add Student
2. View Students
3. Record Grade
4. View Grade Report
5. Export Grade Report
6. Calculate Student GPA
7. Bulk Import Grades
8. View Class Statistics
9. Search Students
10. Exit

It's backed by an in-memory, fixed-size array storage layer — no external
database required, and nothing persists once the process exits.

---

## 2. What this project does NOT use

- **No database driver** — no JDBC, no PostgreSQL, no external `.jar`
  dependencies for persistence.
- **No `.env` file** — credentials are irrelevant because there is no
  external database.
- **No `src/main/java` + `src/test/java` split** — main and test code
  share one `src/` tree (see §3); `pom.xml` excludes `tests/**` from the
  main compile step specifically to keep this layout working with Maven.

It **does** use Maven (`pom.xml` at the project root) to manage the
JUnit 5 + Mockito test dependencies and run the suite via `mvn test` —
this was added after the project started (see CHANGELOG.md, KI-12), so
don't assume "plain `javac`, no build tool" from the original plan below.

---

## 3. Folder structure (current)

```
src/
├── main.app/
│   ├── Main.java                  composition root: builds the dependency graph
│   │                               and a List<MenuAction>, hands both to ConsoleApp
│   └── ConsoleApp.java            the menu loop itself - constructed with a Scanner,
│                                   so a test can hand it scripted input instead of
│                                   the real System.in (see §11)
├── main.console/                      one MenuAction implementation per menu feature
│   ├── MenuAction.java            interface: getOptionNumber, getLabel, execute,
│   │                               isAuthorizedFor(Role), terminatesLoop
│   ├── AddStudentAction.java      ...RecordGradeAction, ViewGradeReportAction,
│   │                               ExportGradeReportAction, CalculateGpaAction,
│   │                               BulkImportAction, ClassStatisticsAction,
│   │                               SearchStudentsAction, ExitAction
│   └── ConsoleUtils.java          promptEnter(), getAvailableStudentIds() - shared
│                                  by more than one action
├── main.model/
│   ├── student/                  Student (abstract), RegularStudent, HonorsStudent
│   ├── subject/                  Subject (abstract), CoreSubject, ElectiveSubject
│   ├── grade/                    Grade, Gradable (interface)
│   └── enums/                    Role, StudentType, StudentStatus, SubjectType,
│                                  LetterGrade, GpaLetterGrade
├── main.manager/
│   ├── StudentManager.java       facade over StudentService/GradeManager
│   ├── GradeManager.java         facade over GradeService/SubjectRepository
│   ├── StudentSearcher.java      implements Searchable (colocated, same package)
│   └── Searchable.java
├── main.service/                      StudentService + StudentServiceImpl,
│                                  GradeService + GradeServiceImpl -
│                                  interface and Impl colocated in one package
├── main.repository/
│   ├── student/                  StudentRepository + StudentRepositoryImpl
│   │                               (Student[50] array)
│   ├── grade/                    GradeRepository + GradeRepositoryImpl
│   │                               (Grade[200] array)
│   └── subject/                  SubjectRepository + SubjectRepositoryImpl
│                                   (Subject[50] array)
├── main.calculators/                  GPACalculator + Calculable (colocated),
│                                  StatisticsCalculator
├── main.export/                       ReportGenerator + Exportable (colocated),
│                                  FileExporter
├── main.imports/                      CSVParser, BulkImportService
├── main.dto/ + main.main.mapper/                StudentDTO/StudentMapper, GradeDTO/GradeMapper -
│                                  used only by Search Students and the exported
│                                  detailed report; Add Student/Record Grade still
│                                  use real main.model/ objects
├── main.utils/                        InputSanitizer, DateFormats
│   └── validators/               StudentValidator, SubjectValidator
├── main.logging/                      Logger (DEBUG/INFO/WARN/ERROR, no dependency)
└── main.exceptions/                   ApplicationException (common abstract root) +
                                   11 custom main.exceptions extending it - every
                                   main.service/main.repository throw site uses one of
                                   these, never a raw RuntimeException
                                   (InvalidFileFormatException was removed - dead
                                   code, superseded by CSVImportException;
                                   StudentException/SubjectException added to
                                   give Student/Subject the same general-purpose
                                   exception GradeException already had)
```

No `interfaces/`, `validation/`, `main.service/serviceimpl/`, or
`main.repository/*/impl/` package exists anymore — see CHANGELOG.md's
"Professional structure & SOLID refactor" section for why each of those
was folded into the layout above.

---

## 4. How the folders interact

Request flow for a menu action, e.g. **Record Grade** (option 3):

```
main.app.Main (builds the dependency graph, exactly once)
  -> main.app.ConsoleApp.run()                  the menu loop: print, read, dispatch,
  |                                         translate main.exceptions - constructed
  |                                         with a Scanner, so it's unit-testable
  -> main.console.RecordGradeAction.execute()   the only *Action doing Scanner I/O
       -> main.manager.GradeManager               "what the main.app-level feature needs"
            -> main.service.GradeService (interface)
                 -> main.service.GradeServiceImpl   business rules: student exists?
                      subject exists? grade in range?
                      -> main.repository.StudentRepository / SubjectRepository (existence checks)
                      -> main.model.grade.Grade constructor (0-100 range check, via Gradable)
                      -> main.repository.GradeRepository (interface)
                           -> main.repository.grade.GradeRepositoryImpl (Grade[200] array)
```

Rules of the layering:

- **`main.app.Main`** is the only class allowed to `new` up concrete
  implementations (`StudentRepositoryImpl`, `GradeServiceImpl`, etc.) and
  wire them together. It has no business logic and no main.console formatting
  of its own — it builds the `List<MenuAction>` and one `ConsoleApp`, then
  calls `run()`, and does nothing else.
- **`main.app.ConsoleApp`** owns the menu loop itself: printing the (role-filtered)
  menu, reading a choice, finding the matching action, checking
  authorization, calling `execute()`, and translating a thrown exception
  into a main.console message. It used to be inlined directly into `Main` as
  `static` fields bound to `System.in` - which made it untestable, since a
  test has no way to swap out a `static final Scanner` after the class has
  already loaded. Pulling it out into an ordinary instance class that takes
  its `Scanner` as a constructor argument means a test can hand it a
  `Scanner` wrapping scripted input and capture `System.out` to assert on
  the exact output (see `tests/main.app/ConsoleAppTest.java` /
  `ConsoleAppMockitoTest.java`).
- **`main.console/`** is the layer that actually touches `Scanner`/`System.out`
  for each individual feature. Each `MenuAction` owns exactly one menu
  feature end-to-end (I/O, calling the right main.manager/calculator, printing
  the result), so adding, removing, or reordering a menu option never
  requires touching `Main` or `ConsoleApp`. Every individual `*Action`
  class is covered by automated tests the same way `ConsoleApp` is (see
  §12).
- **`main.manager/`** is the layer `main.console/` actually depends on.
  `StudentManager` wraps `StudentService` and, on every read, asks
  `GradeManager` to "hydrate" a `Student` object's transient grade list
  (grades live in a separate `GradeRepository` — see
  [Section 6](#6-why-studentgetgrades-is-hydrated-on-read)) so
  `calculateAverageGrade()` / `isPassing()` / honors eligibility are
  correct without the caller having to know that.
- **`main.service/`** holds the actual business rules (does the
  student/subject exist, is the grade in range) and is the only caller of
  `main.utils.validators`. Its interface and `Impl` live in the same package.
- **`main.repository/{student,grade,subject}/`** are the only place
  data-access logic lives — each subpackage colocates its interface with
  its one `Impl`, which is backed by a fixed-size array, not a `HashMap`.
- **`main.model/`** (`Student`, `Subject`, `Grade`, the enums) has no
  dependency on anything else — it's pure data plus the domain rules that
  don't need any storage layer (e.g. `Student.isPassing()`,
  `Grade.getLetterGrade()`).

---

## 5. Data Mapping & Integrity

### Three independent stores (normalized), each a fixed-size array

Data is split across three separate array-backed repositories — each
owns one entity type and has no knowledge of the others' internals:

```
StudentRepositoryImpl          SubjectRepositoryImpl          GradeRepositoryImpl
  Student[50]                    Subject[50]                    Grade[200]
  matched by student_id           matched by subject_code         matched by grade_id
  e.g. "STU001" -> Student       e.g. "MATH01" -> CoreSubject   e.g. "GRD001" -> Grade
```

### How a Grade connects a Student to a Subject

A `Grade` object carries two references that link it back to its student
and subject:

```
Grade
├── gradeId:     "GRD001"          ← its own primary key (unique, no collision)
├── studentId:   "STU001"          ← foreign-key reference to Student
├── subject:     Subject ref       ← object reference to a Subject instance
│   ├── subjectCode: "MATH01"
│   └── subjectName: "Mathematics"
├── gradeValue:  85.0
└── date:        "07-07-2026"      ← dd-MM-yyyy, via main.utils.DateFormats.DISPLAY_DATE
```

The `studentId` is a `String` that must match a key already stored in
`StudentRepositoryImpl`. The `subject` field holds a direct object
reference to the `Subject` from `SubjectRepositoryImpl` — so
`grade.getSubject().getSubjectName()` works without any lookup.

### Querying: retrieving a specific student's grades

`GradeRepositoryImpl.findGradesByStudentId(studentId)` scans its array
and filters:

```
GradeRepositoryImpl
  .findGradesByStudentId(studentId)
    → for each non-null slot in the Grade[200] array
        keep only grades where g.getStudentId().equals(studentId)
```

This returns only the grades whose `studentId` matches the requested
student. No other student's grades can leak in — the filter guarantees
isolation.

### Data integrity (no overlapping, no orphans)

| Concern | How it's enforced |
|---|---|
| **No duplicate keys** | Each entity type generates its own ID prefix (`STU`/`GRD`) plus a zero-padded sequence number (`Student.studentCounter`, `Grade.gradeCounter`) — unique by construction. |
| **No overlapping IDs** | Prefixes are hard-coded per type, so a student ID and a grade ID can never collide even though both are `PREFIX###`. |
| **Referential integrity (student exists)** | `GradeServiceImpl.recordGrade()` calls `studentRepository.findStudentById(id)` *before* persisting the grade — throws `StudentNotFoundException` if the student doesn't exist. |
| **Referential integrity (subject exists)** | Same check against `subjectRepository.findSubjectByCode(code)` before persisting. |
| **Grade value integrity** | `Grade`'s constructor delegates to `recordGrade()`/`validateGrade()` (the `Gradable` contract), which enforces `0 <= value <= 100` before the object even gets an ID or date. |
| **Transient grade list** | `Student.grades` is a `List<Double>` hydrated on read (see §6 below). The source of truth is always `GradeRepositoryImpl` — the list is a denormalized snapshot for computing averages, not the canonical store. |

### Why three arrays instead of embedding grades in Student

If each `Student` owned its own `List<Grade>`, queries like "find all
students who scored above 90 in Mathematics" would require scanning
every student's list. By keeping grades in their own array, the data is
*normalized*: grades are queryable by student, subject, value range, or
date without touching any other data structure. The transient
`List<Double>` on `Student` is a read-time convenience copy (see §6
below).

---

## 6. Why `Student.getGrades()` is hydrated on read

`Student` keeps a transient `List<Double> grades` in memory (used by
`calculateAverageGrade()` / `isPassing()` /
`HonorsStudent.checkHonorsEligibility()`), but grades are stored in a
separate `GradeRepository` (its own array), not on the `Student` object
directly. A newly loaded `Student` therefore starts with an *empty*
grade list.

`StudentManager` fixes this: every time it hands back a `Student` (from
`findStudent`, `getAllStudents`), it first clears and re-populates that
list from `GradeManager.getGradesForStudent(id)` and re-runs
`checkHonorsEligibility()` for `HonorsStudent`s. Any code path that gets
a `Student` from `StudentManager` sees an accurate average — no caller
has to remember to do this manually.

---

## 7. Deviations from the original plan

`Plan.md` in this folder planned for PostgreSQL, a `controller/`
package, and a `config/` package. What actually shipped instead:

| Planned | Actual | Why |
|---|---|---|
| PostgreSQL persistence | In-memory, fixed-size arrays in `main.repository/*` | The assessment brief required in-memory, array-based storage. No external DB needed. |
| `config/DatabaseConfig` / `ConnectionManager` | Not created | No database to configure. |
| `HashMap` storage (an earlier interim step) | `Student[50]` / `Grade[200]` / `Subject[50]` arrays | Switched from `HashMap` to arrays to match the assessment brief's array-based storage requirement exactly. |
| `controller/StudentController`, `controller/GradeController` | `main.console/*Action` classes, one per menu feature | Functionally the same role as a controller layer, but named/organized around "one class per menu option" rather than "one controller per entity" — see CHANGELOG.md's SRP fix for why `Main` itself stopped doing this directly. |
| Role-based access always enabled | Optional role-based access control prompted on startup | Defaults to the full menu; user can opt into role-based mode. |
| 9 required classes | ~40 classes across a layered architecture | Kept (and later tightened) the layered architecture rather than the original spec's flat 9-class design. |

---

## 8. ID generation

`Student` and `Grade` each keep a `private static int` counter
(`studentCounter`, `gradeCounter`) that generates `STU001`, `GRD001`,
etc. On startup, `StudentManager` and `GradeManager` call
`Student.initializeCounter(...)` / `Grade.initializeCounter(...)`,
scanning the existing in-memory entries for the highest sequence number
already in use. Since data is not persisted across restarts, this is
primarily useful when seed data has been pre-loaded.

---

## 9. Role-Based Access (Optional)

On startup, the main.app asks whether to enable role-based access control. If
the user answers `N` (default), the full 10-option menu is shown with no
restrictions.

If the user answers `Y`, a role prompt appears, and the selected role
(`TEACHER` or `STUDENT`, `main.model.enums.Role`) is preserved for the
session. Menu rendering and action authorization both depend on it.

| Option | Action | Mutates data? | TEACHER | STUDENT |
|---|---|---|---|---|
| 1 | Add Student | write | ✓ | ✗ |
| 2 | View Students | read | ✓ | ✗ |
| 3 | Record Grade | write | ✓ | ✗ |
| 4 | View Grade Report | read | ✓ | ✓ |
| 5 | Export Grade Report | read | ✓ | ✓ |
| 6 | Calculate Student GPA | read | ✓ | ✓ |
| 7 | Bulk Import Grades | write | ✓ | ✗ |
| 8 | View Class Statistics | read | ✓ | ✓ |
| 9 | Search Students | read | ✓ | ✓ |
| 10 | Exit | — | ✓ | ✓ |

**Student is always read-only, by design** — every option a STUDENT can
reach only reads existing data (or exports/searches over it); the three
that mutate state (Add Student, Record Grade, Bulk Import Grades) are all
teacher-only. This wasn't true from the start: `BulkImportAction`
originally had no `isAuthorizedFor` override, defaulting to the
interface's `true`, which let a Student import grades — a write path —
until it was caught and fixed (`feature/BugFix-student-read-only`).

Authorization is enforced at two points, both in `ConsoleApp`'s loop —
never inside an action itself:

- **Menu is filtered** — `printMenu()` only lists an option when
  `!useRoleBased || action.isAuthorizedFor(currentRole)`, so a STUDENT
  never even sees "1. Add Student", "2. View Students", "3. Record
  Grade", or "7. Bulk Import Grades" in their menu at all - not just a
  denial after picking one. With role-based access off (the default),
  every option is shown, same as before.
- **Dispatch is still gated too** — even though unauthorized options
  aren't listed, `ConsoleApp` still checks
  `useRoleBased && !action.isAuthorizedFor(currentRole)` before calling
  `action.execute()`. This matters because nothing stops a user from
  typing a number that isn't on their filtered menu (e.g. a STUDENT
  typing `7` anyway) - they still get "Access denied..." rather than the
  action silently running.
- **Per-action, not per-number** — each `MenuAction` overrides
  `isAuthorizedFor(Role)` itself (`AddStudentAction`, `ViewStudentsAction`,
  `RecordGradeAction`, and `BulkImportAction` return `role == Role.TEACHER`;
  every other action accepts the interface's default `true`, since it
  never mutates anything). Adding a new menu option never requires
  touching a shared "if choice is between X and Y" check, since there
  isn't one anymore — it only requires deciding, once, whether the new
  action reads or writes.

---

## 10. Running it

1. **Via Maven:** `mvn test` from `student-grade-management/` runs the
   full suite. To run the main.app itself, compile and run `main.app.Main` (e.g.
   from an IDE, or `java -cp target/classes main.app.Main` after
   `mvn compile`).
2. Optionally enable **role-based access** — if you answer `Y` at the
   prompt, you choose `TEACHER` or `STUDENT`, which determines available
   menu options per the table in §9. Answer `N` to skip and access all
   ten features.
3. As a **Teacher** you have full access: add students, view students,
   record grades, plus everything a Student can do.
4. As a **Student** you can view/main.export/report/calculate GPA/view
   statistics/search (options 4, 5, 6, 8, 9) and exit, but cannot add
   students, view the full listing, record grades, or bulk-import grades
   (options 1, 2, 3, 7) — every write action is teacher-only.
5. 5 sample students (3 Regular, 2 Honors) and 6 subjects (3 Core, 3
   Elective) are pre-loaded on every start.

---

## 11. Code Coverage (JaCoCo) and Static Analysis (SonarQube)

`pom.xml` registers two additional plugins, alongside the compiler/
surefire plugins already covered above.

### JaCoCo — code coverage

No separate setup: JaCoCo attaches to the JVM automatically as a Java
agent during `mvn test`/`mvn verify` (the `prepare-agent` execution) and
writes a coverage report once the build reaches the `verify` phase (the
`report` execution, bound to `phase=verify`).

```
mvn clean verify
```

- `target/site/jacoco/index.html` — a browsable, per-package/per-class
  HTML report (open it directly in a browser).
- `target/site/jacoco/jacoco.csv` — the same data as CSV, useful for
  scripting a quick coverage summary.
- `target/jacoco.exec` — the raw binary execution data JaCoCo itself
  reads to build the report above; not meant to be read directly.

Current numbers (see CHANGELOG.md for the full package-by-package
breakdown): **96.2%** overall instruction coverage (up from ~65%), with
every `main.console/*Action` class now covered (see §12 — this used to be
the one remaining, deliberately scoped-out gap; it no longer is).

### SonarQube — static analysis

The `sonar-maven-plugin` is registered but not bound to any lifecycle
phase, so `mvn test`/`mvn verify` behave exactly as before regardless of
whether SonarQube is even installed. Running an actual analysis needs a
SonarQube server to send results to - locally, that means:

**1. Start a local SonarQube server (one-time setup, then start/stop as needed):**

Download the free Community Edition from
[sonarsource.com](https://www.sonarsource.com/), unzip it, then, **on
Windows**:

```
bin\windows-x86-64\StartSonar.bat
```

Wait for it to log `SonarQube is operational` (first run takes longer -
it's initializing its own embedded database), then open
`http://localhost:9000` in a browser (default login `admin` / `admin`,
which it will immediately ask you to change). Create a local project and
generate an analysis token from the UI - you'll pass that token on the
command line, never commit it to `pom.xml` or source control.

**2. Run the analysis from this project:**

```
mvn clean verify sonar:sonar -Dsonar.projectKey=<your-project-key> -Dsonar.token=<your-token>
```

`clean verify` first, in the same command, matters: Sonar's Java analyzer
reads JaCoCo's `target/jacoco.exec` to attribute *coverage* to its
findings (not just style/bug findings), so JaCoCo has to have already run
in this same build. Once it finishes, refresh the SonarQube UI - the
project now shows code smells, potential bugs, security hotspots, and
coverage, all in one dashboard.

To stop the local server afterward: `bin\windows-x86-64\StopSonar.bat`
(or just close the terminal window `StartSonar.bat` is running in).

**Findings resolved so far** (see CHANGELOG.md,
`feature/BugFix-sonar-quality-issues`, for the full list): cognitive
complexity in `ConsoleApp.run()` and `SearchStudentsAction.execute()`,
a `StatisticsCalculator` constructor with too many parameters, a nested
ternary, duplicated string literals, and a test-lambda smell. One rule
is deliberately excluded rather than fixed: `java:S106` ("replace
`System.out` by a logger") is scoped out for `src/main.console/**` and
`src/main.app/**` via `sonar.issue.ignore.multicriteria` in `pom.xml`,
because `System.out` there is the application's actual user-facing
output, not a diagnostic that belongs on `Logger`/`System.err` — see
the comment above that property block in `pom.xml` for the full
rationale.

---

## 12. `main.console/*Action` Test Coverage

The 10 classes under `main.console/` (`AddStudentAction`, `RecordGradeAction`,
etc.) each take their `Scanner` via constructor injection, the same way
`ConsoleApp` does — so each one is tested the same way `ConsoleAppTest`
tests `ConsoleApp`: a scripted `Scanner` over a fake input script, with
`System.out` captured and asserted on (see `tests/main.app/ConsoleAppTest.java`
for the pattern). Every class has the standard real-collaborator
(`<Class>Test`) + mocked-collaborator (`<Class>MockitoTest`) pair under
`src/tests/main.console/`, except `ExitAction` (no collaborators to mock,
same exception as `LetterGradeTest` — see `tests/README.md`).

This closed what used to be this project's one remaining,
deliberately-scoped-out coverage gap (the `main.console` package sat at 0%);
see CHANGELOG.md's `feature/BugFix-main.console-test-coverage` entry for the
before/after numbers and two bugs the effort turned up along the way
(one fixed — `BulkImportService`'s failed-row count; one flagged but
left as a judgment call — a filename/message mismatch in
`ExportGradeReportAction`).
