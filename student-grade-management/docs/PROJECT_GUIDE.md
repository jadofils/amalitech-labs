# Project Guide — Student Grade Management System

This is the "how it actually works" reference for the codebase as it stands
today. `architecturer.md` and `Plan.md` in this same folder capture the
*original* design plan (which assumed PostgreSQL, Maven, and a
`controller/` package); this document describes what was actually built,
which diverged from that plan in a few places (noted below).

---

## 1. What the app does

A console (terminal) application for managing students and their grades:

- Add / view students (Regular or Honors)
- Record a grade for a student in a Core or Elective subject
- View a student's full grade report (per-subject averages, overall average,
  passing status)

It's backed by an in-memory array storage layer — no external database
required.

---

## 2. What this project does NOT use

- **No Maven / Gradle** — pure plain-Java compilation (`javac` or any IDE).
- **No database driver** — no JDBC, no PostgreSQL, no external `.jar` dependencies.
- **No `.env` file** — credentials are irrelevant because there is no external database.

---

## 3. Folder structure (current)

```
src/
├── Main.java                     composition root + console menu loop
├── model/
│   ├── student/                  Student (abstract), RegularStudent, HonorsStudent
│   ├── subject/                  Subject (abstract), CoreSubject, ElectiveSubject
│   ├── grade/                    Grade, Gradable (interface)
│   └── enums/                    StudentType, StudentStatus, SubjectType, LetterGrade
├── manager/
│   ├── StudentManager.java       facade over StudentService/GradeManager; the class Main actually talks to for students
│   └── GradeManager.java         facade over GradeService/SubjectRepository; the class Main actually talks to for grades
├── service/                      business-logic interfaces (StudentService, GradeService)
├── service/serviceimpl/          business-logic implementations (GradeServiceImpl, StudentServiceImpl)
├── repository/                   persistence interfaces (StudentRepository, SubjectRepository, GradeRepository)
│   ├── impl/                     StudentRepositoryImpl (in-memory Student[50] array)
│   ├── subject/impl/             SubjectRepositoryImpl (in-memory Subject[50] array)
│   └── grade/impl/               GradeRepositoryImpl (in-memory Grade[200] array)
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
                      -> repository.grade.impl.GradeRepositoryImpl (in-memory HashMap)
```

Rules of the layering:

- **`Main`** is the only class allowed to `new` up concrete implementations
  (`StudentRepositoryImpl`, `GradeServiceImpl`, etc.) and wire them together.
  It never contains SQL and never touches a repository directly — it only
  calls `StudentManager` / `GradeManager`.
- **`manager/`** is the layer Main actually depends on. `StudentManager`
  wraps `StudentService` and, on every read, asks `GradeManager` to
  "hydrate" a `Student` object's transient grade list (grades live in a
  separate `GradeRepository` — see [Section 6](#6-why-studentgetgrades-is-hydrated-on-read))
  so `calculateAverageGrade()` / `isPassing()` / honors eligibility are
  correct without the caller having to know that.
- **`service` / `serviceimpl`** hold the actual business rules (does the
  student/subject exist, is the grade in range) and are the only callers of
  `validation/`.
- **`repository` (interfaces) / `repository/*/impl` (HashMap)** are the only
  place data-access logic lives.
- **`model/`** (`Student`, `Subject`, `Grade`, the enums) has no dependency
  on anything else — it's pure data + the domain rules that don't need any
  storage layer (e.g. `Student.isPassing()`, `Grade.getLetterGrade()`).

---

## 5. Data Mapping & Integrity

### Three independent stores (normalized)

Data is split across three separate `HashMap` repositories — each owns one
entity type and has no knowledge of the others' internals:

```
StudentRepositoryImpl          SubjectRepositoryImpl          GradeRepositoryImpl
  HashMap<String, Student>       HashMap<String, Subject>       HashMap<String, Grade>
  key: student_id                key: subject_code              key: grade_id
  e.g. "STU001" -> Student       e.g. "MATH01" -> CoreSubject   e.g. "GRD001" -> Grade
```

### How a Grade connects a Student to a Subject

A `Grade` object carries two references that link it back to its student and
subject:

```
Grade
├── gradeId:     "GRD001"          ← its own primary key (unique, no collision)
├── studentId:   "STU001"          ← foreign-key reference to Student
├── subject:     Subject ref       ← object reference to a Subject instance
│   ├── subjectCode: "MATH01"
│   └── subjectName: "Mathematics"
├── gradeValue:  85.0
└── date:        "2026-07-07"
```

The `studentId` is a `String` that must match a key in `StudentRepositoryImpl`.
The `subject` field holds a direct object reference to the `Subject` from
`SubjectRepositoryImpl` — so `grade.getSubject().getSubjectName()` works
without any lookup.

### Querying: retrieving a specific student's grades

Since all grades share one `HashMap`, filtering is done by stream:

```
GradeRepositoryImpl
  .findGradesByStudentId(studentId)
    → gradesMap.values().stream()
        .filter(g -> g.getStudentId().equals(studentId))
        .collect(toList())
```

This returns only the grades whose `studentId` matches the requested student.
No other student's grades can leak in — the filter guarantees isolation.

### Data integrity (no overlapping, no orphans)

| Concern | How it's enforced |
|---|---|
| **No duplicate keys** | Each entity type uses its own ID namespace (`STU001`, `SUB001`, `GRD001`) stored as `HashMap` keys — by definition unique. |
| **No overlapping IDs** | Prefixes are hard-coded per type (`STU` / `GRD`), so a student ID and a grade ID can never collide even though they share a counter-like pattern. |
| **Referential integrity (student exists)** | `GradeServiceImpl.recordGrade()` calls `studentRepository.findStudentById(id)` *before* persisting the grade — throws `StudentNotFoundException` if the student doesn't exist. |
| **Referential integrity (subject exists)** | Same check against `subjectRepository.findByCode(code)` before persisting. |
| **Grade value integrity** | `Grade.recordGrade()` delegates to `validateGrade()` (the `Gradable` contract) which enforces `0 <= value <= 100`. |
| **Transient grade list** | `Student.grades` is a `List<Double>` hydrated on read (see §6 below). The source of truth is always `GradeRepositoryImpl` — the list is a denormalized snapshot for computing averages, not the canonical store. |

### Why three maps instead of embedding grades in Student

If each `Student` owned its own `List<Grade>`, queries like "find all students
who scored above 90 in Mathematics" would require scanning every student's
list. By keeping grades in their own `HashMap`, the data is *normalized*:
grades are queryable by student, subject, value range, or date without
touching any other data structure. The transient `List<Double>` on `Student`
is a read-time convenience copy (see §6 below).

---

## 6. Why `Student.getGrades()` is hydrated on read

`Student` keeps a transient `List<Double> grades` in memory (used by
`calculateAverageGrade()` / `isPassing()` / `HonorsStudent.checkHonorsEligibility()`),
but grades are stored in a separate `GradeRepository` (its own `HashMap`),
not on the `Student` object directly. A newly loaded `Student` therefore
starts with an *empty* grade list.

`StudentManager` fixes this: every time it hands back a `Student` (from
`findStudent`, `viewAllStudents`, `getAverageClassGrade`), it first clears
and re-populates that list from `GradeManager.getGradesForStudent(id)` and
re-runs `checkHonorsEligibility()` for `HonorsStudent`s. Any code path that
gets a `Student` from `StudentManager` sees an accurate average — no caller
has to remember to do this manually.

---

## 7. Deviations from the original plan

`Plan.md` in this folder planned for PostgreSQL, a `controller/` package,
Maven, and a `config/` package. What actually shipped instead:

| Planned | Actual | Why |
|---|---|---|
| PostgreSQL persistence | In-memory `HashMap` storage in `repository/*/impl` | The assessment brief required in-memory storage. No external DB needed. |
| `config/DatabaseConfig` / `ConnectionManager` | Not created | No database to configure. |
| Maven build (`pom.xml`) | Plain `.java` files, compile with `javac` or any IDE | No external dependencies to manage. |
| `StudentManager` / `GradeManager` backed by **arrays** (the original lab spec's requirement) | Backed by **arrays** (`Student[50]`, `Grade[200]`) in repository layer | Uses primitive arrays for storage as required by the README spec. |
| `controller/StudentController`, `controller/GradeController` | `Main.java` does menu I/O directly, delegating straight to `manager/` | The controller layer was folded into `Main` — one less indirection for a console app this size. |
| Role-based access always enabled | Optional role-based access control prompted on startup | Defaults to simple 5-option menu; user can opt into role-based mode. |
| 9 required classes (arrays) | ~20 classes with layered architecture | Kept the layered architecture but switched repositories from HashMap to arrays. |

---

## 8. ID generation

`Student` and `Grade` each keep a `private static int` counter
(`studentCounter`, `gradeCounter`) that generates `STU001`, `GRD001`, etc.
On startup, `StudentManager` and `GradeManager` call
`Student.initializeCounter(...)` / `Grade.initializeCounter(...)`, scanning
the existing in-memory entries for the highest sequence number already in
use. Since data is not persisted across restarts, this is primarily useful
when seed data has been pre-loaded.

---

## 9. Role-Based Access (Optional)

On startup, the app asks whether to enable role-based access control. If
the user answers `N` (default), the simple 5-option menu is shown with no
restrictions — matching the README specification exactly.

If the user answers `Y`, a role prompt appears, and the selected role
(`TEACHER` or `STUDENT`) is preserved for the session. Menu rendering and
action authorization both depend on it.

| Action | TEACHER | STUDENT |
|---|---|---|
| Add Student | ✓ | ✗ |
| View Students | ✓ | ✓ |
| Record Grade | ✓ | ✗ |
| View Grade Report | ✓ | ✓ |
| Exit | ✓ | ✓ |

Authorization is enforced at two points:
- **Menu rendering** (`printMenu`) — only authorized options are printed.
- **Action gate** (`isAuthorized`) — if a STUDENT enters an unauthorized
  option number directly, the request is rejected before any action runs.

This keeps the switch statement in `Main` unchanged (all 8 cases) while
the role logic stays in two small helper methods — no permissions leak
into any service or repository layer.

---

## 10. Running it

1. Open the project in any Java IDE (IntelliJ, VS Code, Eclipse, etc.).
2. Compile and run `Main.java` — no special classpath or external setup
   required.
3. Optionally enable **role-based access** — if you answer `Y` at the prompt,
   you choose `TEACHER` or `STUDENT`, which determines available menu options.
   Answer `N` to skip and access all features.
4. As a **Teacher** you have full access: add students, view students, record
   grades, view grade reports.
5. As a **Student** you can only view students and grade reports (options 2, 4).
6. 5 sample students (3 Regular, 2 Honors) and 6 subjects are pre-loaded on
   every start.
