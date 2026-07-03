# Project Guide — Student Grade Management System

This is the "how it actually works" reference for the codebase as it stands
today. `architecturer.md` and `Plan.md` in this same folder capture the
*original* design plan (which assumed Maven and a `controller/` package);
this document describes what was actually built, which diverged from that
plan in a few places (noted below).

---

## 1. What the app does

A console (terminal) application for managing students and their grades:

- Add / view / update / delete students (Regular or Honors)
- Record a grade for a student in a Core or Elective subject
- View a student's full grade report (per-subject averages, overall average,
  passing status)

It's backed by a real PostgreSQL database rather than in-memory arrays —
see [Section 6](#6-deviations-from-the-original-plan) for why, and what that
trades off against the original lab spec.

---

## 2. Libraries used

The project has **no Maven/Gradle build file**. Dependencies are plain
`.jar` files wired in as IntelliJ project libraries (see
`.idea/libraries/*.xml` and `student-management-system.iml`). To build or
run outside IntelliJ, put these three jars on the classpath:

| Library | Jar | Why it's here |
|---|---|---|
| PostgreSQL JDBC Driver | `postgresql-42.7.11.jar` | The actual database driver — everything in `repository/*/impl` and `config/DatabaseConfig` goes through this to talk to Postgres via `java.sql.*`. |
| java-dotenv | `java-dotenv-5.2.2.jar` | Loads `DB_URL`, `DB_USER`, `DB_PASSWORD` from a local `.env` file at startup (`config/DatabaseConfig`), so credentials aren't hardcoded or committed. |
| Kotlin stdlib | `kotlin-stdlib-2.2.0.jar` | Not used directly by any of our code — it's a **transitive runtime dependency of java-dotenv**, which is written in Kotlin. It has to be on the classpath or `Dotenv.load()` fails with a `NoClassDefFoundError`. |

`.env` (not committed — see `.gitignore`) needs:
```
DB_URL=jdbc:postgresql://localhost:5432/<your_db>
DB_USER=<your_user>
DB_PASSWORD=<your_password>
```

---

## 3. Folder structure (current)

```
src/
├── Main.java                     composition root + console menu loop
├── config/
│   └── DatabaseConfig.java       loads .env, opens JDBC connections, creates/migrates tables, seeds subjects
├── model/
│   ├── student/                  Student (abstract), RegularStudent, HonorsStudent
│   ├── subject/                  Subject (abstract), CoreSubject, ElectiveSubject
│   ├── grade/                    Grade, Gradable (interface)
│   └── enums/                    StudentType, StudentStatus, SubjectType, LetterGrade, GradeStatus
├── manager/
│   ├── StudentManager.java       facade over StudentService/GradeManager; the class Main actually talks to for students
│   └── GradeManager.java         facade over GradeService/SubjectRepository; the class Main actually talks to for grades
├── service/                      business-logic interfaces (StudentService, GradeService)
├── serviceimpl/                  business-logic implementations
├── repository/                   persistence interfaces (StudentRepository, SubjectRepository, GradeRepository)
│   ├── impl/                     StudentRepositoryImpl (JDBC)
│   ├── subject/impl/             SubjectRepositoryImpl (JDBC)
│   └── grade/impl/               GradeRepositoryImpl (JDBC)
├── validation/                   StudentValidator, SubjectValidator, GradeValidator — plain static range/format checks
└── exceptions/                   StudentNotFoundException, StudentValidationException,
                                   exceptions/grades/GradeException,
                                   exceptions/subjects/{SubjectNotFoundException, SubjectValidationException}
```

---

## 4. How the folders interact

Request flow for any menu action, e.g. **Record Grade**:

```
Main (menu loop, Scanner I/O)
  -> manager.GradeManager           "what the app-level feature needs"
       -> service.GradeService (interface)
            -> serviceimpl.GradeServiceImpl   business rules: student exists?
                 subject exists? grade in range?
                 -> repository.StudentRepository / SubjectRepository (existence checks)
                 -> validation.GradeValidator  (0-100 range check)
                 -> repository.GradeRepository (interface)
                      -> repository.grade.impl.GradeRepositoryImpl (JDBC)
                           -> config.DatabaseConfig.getConnection()
                                -> PostgreSQL
```

Rules of the layering:

- **`Main`** is the only class allowed to `new` up concrete implementations
  (`StudentRepositoryImpl`, `GradeServiceImpl`, etc.) and wire them together.
  It never contains SQL and never touches a repository directly — it only
  calls `StudentManager` / `GradeManager`.
- **`manager/`** is the layer Main actually depends on. `StudentManager`
  wraps `StudentService` and, on every read, asks `GradeManager` to
  "hydrate" a `Student` object's transient grade list (grades live in their
  own table — see [Section 5](#5-why-studentgetgrades-is-hydrated-on-read))
  so `calculateAverageGrade()` / `isPassing()` / honors eligibility are
  correct without the caller having to know that.
- **`service` / `serviceimpl`** hold the actual business rules (does the
  student/subject exist, is the grade in range) and are the only callers of
  `validation/`.
- **`repository` (interfaces) / `repository/*/impl` (JDBC)** are the only
  place SQL exists in the app.
- **`model/`** (`Student`, `Subject`, `Grade`, the enums) has no dependency
  on anything else — it's pure data + the domain rules that don't need a
  database (e.g. `Student.isPassing()`, `Grade.getLetterGrade()`).
- **`config/DatabaseConfig`** is only ever called from `repository/*/impl`.

---

## 5. Why `Student.getGrades()` is hydrated on read

`Student` keeps a transient `List<Double> grades` in memory (used by
`calculateAverageGrade()` / `isPassing()` / `HonorsStudent.checkHonorsEligibility()`),
but grades are actually persisted in their own `grades` table, not on the
`students` row. A `Student` object built straight from a DB row therefore
starts with an *empty* grade list.

`StudentManager` fixes this: every time it hands back a `Student` (from
`findStudent`, `viewAllStudents`, `getAverageClassGrade`), it first clears
and re-populates that list from `GradeManager.getGradesForStudent(id)` and
re-runs `checkHonorsEligibility()` for `HonorsStudent`s. Any code path that
gets a `Student` from `StudentManager` sees an accurate average — no caller
has to remember to do this manually.

---

## 6. Deviations from the original plan

`Plan.md` in this folder planned for a `controller/` package and Maven.
What actually shipped instead:

| Planned | Actual | Why |
|---|---|---|
| Maven build (`pom.xml`) | Plain `.jar` files as IntelliJ project libraries | Never set up; see Section 2 for the manual classpath if building outside the IDE. |
| `controller/StudentController`, `controller/GradeController` | `Main.java` does menu I/O directly, delegating straight to `manager/` | The controller layer was folded into `Main` — one less indirection for a console app this size. |
| `StudentManager` / `GradeManager` backed by **arrays** (the original lab spec's requirement) | Backed by the database (`StudentService`/`GradeService`/repositories) | Explicit choice for now — the array-based version is a planned future migration. **This is the one place the lab rubric's "use arrays" requirement is not met**; call this out if submitting against that rubric. |

---

## 7. Database schema

Created and self-healed automatically by `config.DatabaseConfig` on
startup (`CREATE TABLE IF NOT EXISTS` for each table). Six subjects are
seeded once (idempotent, `ON CONFLICT DO NOTHING`): Mathematics, English,
Science (Core) and Music, Art, Physical Education (Elective).

```
students                          subjects                    grades
─────────                         ────────                    ──────
student_id   VARCHAR PK           subject_code  VARCHAR PK     grade_id      VARCHAR PK
name         VARCHAR              subject_name  VARCHAR        student_id    VARCHAR FK -> students
age          INT                  subject_type  VARCHAR        subject_code  VARCHAR FK -> subjects
email        VARCHAR UNIQUE                                    grade         NUMERIC(5,2)
phone        VARCHAR                                           date          DATE
status       VARCHAR
student_type VARCHAR
```

`DatabaseConfig` also runs a one-time self-check on startup: if a `grades`
table already exists from an older version of the schema (before
`subject_code` was added) it drops and recreates just that table, so
upgrading an existing database doesn't require a manual `DROP TABLE`.

---

## 8. ID generation

`Student` and `Grade` each keep a `private static int` counter
(`studentCounter`, `gradeCounter`) that generates `STU001`, `GRD001`, etc.
Since these reset to their initial value on every JVM restart but the
database does not, `StudentManager` and `GradeManager` each call
`Student.initializeCounter(...)` / `Grade.initializeCounter(...)` once at
startup, scanning the existing rows for the highest sequence number already
in use. Without this, restarting the app would eventually try to re-issue
an ID that already exists and fail with a primary-key violation.

---

## 9. Running it

1. Create a Postgres database and a `.env` file as shown in Section 2.
2. Add the three jars from Section 2 to the classpath (already configured
   as IntelliJ project libraries — just open the project and run `Main`).
3. Run `Main`. On first run you'll see `Tables initialized successfully.`
   followed by `Database connected successfully.` and the menu.
