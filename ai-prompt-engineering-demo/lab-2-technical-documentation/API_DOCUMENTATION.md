# Student Grade Management — Technical / API Reference

Method-level reference for `student-grade-management/src/`. This assumes you've already read
`docs/PROJECT_GUIDE.md` for the high-level architecture; this document is the layer underneath it —
what each class actually exposes, what has to be true before you call it, and what it throws when
that isn't the case. Source of truth is the code as of this writing, not the README.

## 1. Layer responsibility map

```
Main            menu I/O only; the only class allowed to `new` up concrete impls and wire them
manager/        app-facing facade; StudentManager re-hydrates a Student's transient grade list
                on every read (grades live in a separate repository, not on Student)
service/        business rules — existence checks, orchestration; only caller of validation/
repository/     persistence — fixed-size arrays wrapped in List-returning methods
model/          pure data + domain rules that need no storage (isPassing(), getLetterGrade())
validation/     static precondition checks, called only from service/
exceptions/     one exception type per failure mode, all unchecked (RuntimeException subclasses)
```

Nothing below `manager/` prints to `System.out` except `GradeManager.viewGradesByStudent`, which
is a console-formatting convenience method living on the wrong layer (see Known Issues).

---

## 2. `model.student`

### `Student` (abstract)

| Member | Detail |
|---|---|
| Fields | `studentId, name, age, email, phone, status` (private) + transient `List<Double> grades` |
| Static | `studentCounter` (int) — source of `STU%03d` IDs, shared across all subclasses |
| `calculateAverageGrade()` | `0.0` if `grades` is empty, else arithmetic mean |
| `isPassing()` | `calculateAverageGrade() >= getPassingGrade()` |
| `getPassingGrade()` | abstract — `50.0` (Regular) / `60.0` (Honors), see `StudentType` |
| `initializeCounter(int highest)` | sets `studentCounter = max(current, highest)` — called once per repository construction to keep IDs monotonic across a re-seeded run; **does not persist across JVM restarts** |

`grades` is **transient in the sense that it is not the source of truth** — it's populated by
`StudentManager.hydrateGrades()` from `GradeRepository` on every read. A `Student` fetched any
other way (e.g. constructed directly) starts with an empty list.

### `RegularStudent extends Student`

`getStudentType()` → `"REGULAR"`, `getPassingGrade()` → `50.0`. No additional state.

### `HonorsStudent extends Student`

| Member | Detail |
|---|---|
| `getPassingGrade()` | `60.0` |
| `checkHonorsEligibility()` | recomputes and returns `honorsEligible = calculateAverageGrade() >= StudentType.HONORS.getPassingGrade()` (**i.e. `>= 60.0`** — see Known Issues) |
| `setGrade(int i)` | thin wrapper over `Student.addGrade(double)`; only used by `HonorsStudent`, not part of the `Student` contract |

`honorsEligible` is recomputed as a side effect of calling `checkHonorsEligibility()` — it is not
kept in sync automatically when grades change; `StudentManager.hydrateGrades()` calls it after
every rehydration so callers going through `StudentManager` always see a fresh value.

---

## 3. `model.subject`

`Subject` (abstract) holds `subjectName`/`subjectCode` and declares `getSubjectType()`.
`CoreSubject` → `SubjectType.CORE`, `isMandatory()` always `true`; `ElectiveSubject` →
`SubjectType.ELECTIVE`, `isMandatory()` always `false` — both match `ReadMe.md`'s class spec
exactly. `mandatory` is a `final` field set at construction, not derived from `SubjectType`, so the
two flags (`getSubjectType()` and `isMandatory()`) are independently hard-coded per subclass rather
than one being computed from the other.

Seeded at `SubjectRepositoryImpl` construction (6 subjects, hard-coded, not configurable):

| Code | Name | Type |
|---|---|---|
| `MATH01` | Mathematics | CORE |
| `ENGL01` | English | CORE |
| `SCIE01` | Science | CORE |
| `MUSC01` | Music | ELECTIVE |
| `ART01` | Art | ELECTIVE |
| `PHED01` | Physical Education | ELECTIVE |

---

## 4. `model.grade.Grade`

```java
new Grade(String studentId, Subject subject, double gradeValue)
```

- Validates `0 <= gradeValue <= 100` via the `Gradable` contract (`validateGrade` →
  `recordGrade`) **before** assigning `gradeId`/`date` — an out-of-range value throws
  `GradeException` and no ID is ever minted for it (the `gradeCounter` is not consumed either,
  since the increment happens after the guard).
- `gradeId` auto-generated as `GRD%03d` from a static counter (same monotonic-ID caveat as
  `Student`).
- `date` auto-generated as `dd-MM-yyyy` at construction time — **there is no way to backdate a
  grade** through the public constructor.
- `Grade.reconstruct(...)` is a separate factory that skips ID/date generation, for rebuilding a
  `Grade` that already has a persisted ID and date. Nothing under `repository/` currently calls it
  (grades are only ever created fresh via `new Grade(...)`), so it's dead code in the current
  build — presumably left over from, or written ahead of, a persistence layer that isn't wired up
  yet.
- `getLetterGrade()` → `LetterGrade.fromNumeric(grade)`: `A ≥ 85`, `B ≥ 70`, `C ≥ 55`, `D ≥ 40`,
  else `F`.

---

## 5. `validation`

Both validators are stateless static-method classes, called only from the `service` layer — never
from `repository` (seed data bypasses validation entirely, see Known Issues) and never from
`Main`.

### `StudentValidator.validateStudent(Student)` — throws `StudentValidationException`

| Rule | Constraint |
|---|---|
| Name | 4–100 chars, letters and spaces only (regex `^[a-zA-Z ]+$`) |
| Age | `5–100` inclusive |
| Email | must match `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` |
| Phone | **exactly 10 digits**, regex `^[0-9]{10}$` — no `+`, `-`, or spaces allowed |
| Status | non-null |
| Grades | `calculateAverageGrade()` must be `0–100` (always true in practice since `Grade` already range-checks each entry) |

### `SubjectValidator.validateSubject(Subject)` — throws `SubjectValidationException`

| Rule | Constraint |
|---|---|
| Name | 3–100 chars |
| Code | must match `^[A-Z]{2,}[0-9]{2,}$`, e.g. `MATH01` |

Called from `SubjectRepositoryImpl.addSubject()` only — **not** called for the 6 seeded subjects
(they're constructed directly in the repository constructor), so a code change to the seed data
that violated the pattern would not be caught anywhere.

---

## 6. `repository` — storage layer

All three repositories are fixed-size in-memory arrays, not resizable collections:

| Repository | Backing array | Capacity | Overflow behavior |
|---|---|---|---|
| `StudentRepositoryImpl` | `Student[]` | 50 | `addStudent` throws unchecked `RuntimeException("...Storage is full.")` |
| `SubjectRepositoryImpl` | `Subject[]` | 50 | same pattern, generic `RuntimeException` |
| `GradeRepositoryImpl` | `Grade[]` | 200 | `addGrade` throws `GradeException("...Storage is full.")` |

`deleteStudent`/`deleteSubject`/`deleteGrade` all use swap-with-last-and-null (`O(1)`, doesn't
preserve insertion order). Nothing in `manager`/`service`/`Main` currently calls delete for
students or subjects (no menu option exposes it); `GradeService.deleteGrade` exists but is
likewise unreached from `Main`.

`findStudentById` / `findSubjectByCode` / `findGradeById` all throw a `*NotFoundException` rather
than returning `null` or `Optional` — callers must catch, not null-check. `StudentManager.findStudent`
is the one place that translates the thrown exception back into a `null` return, specifically so
`Main` can do a simple `if (student == null)` check at the UI layer.

Nothing persists across a JVM restart — every repository re-seeds from scratch on `Main` startup.

---

## 7. `service` — business rules

### `StudentServiceImpl`
- `addStudent` → validates via `StudentValidator`, then delegates to the repository. This is the
  only path where an invalid student is rejected — direct repository access (as the seed data
  uses) skips it.
- `getStudentById` re-wraps a `null` repository return in a generic `RuntimeException` — dead code
  in practice, since `StudentRepositoryImpl.findStudentById` never returns `null` (it throws
  `StudentNotFoundException` first).

### `GradeServiceImpl`
- `recordGrade(Grade)`: checks the student exists (`StudentNotFoundException` if not), checks the
  subject exists (`SubjectNotFoundException` if not — dead branch, since
  `SubjectRepositoryImpl.findSubjectByCode` already throws before returning `null`), then persists.
  Grade-value range is not re-checked here — it was already enforced in the `Grade` constructor
  before this method is ever reached.
- Has three constructors: a no-arg one that builds all three repositories itself (`new
  GradeServiceImpl()`), one that takes shared `StudentRepository`/`SubjectRepository` (what `Main`
  actually uses, so students/subjects are the same instances across both services), and the full
  three-repository constructor the other two delegate to.

---

## 8. `manager` — app-facing facade

### `StudentManager`
- `findStudent`, `viewAllStudents`, `getAverageClassGrade`, `getAllStudents` all call
  `hydrateGrades()` first — **any other way of obtaining a `Student` will show a stale/empty grade
  list**. There is no public method that returns a `Student` without hydration.
- `hydrateGrades()` clears `student.getGrades()` and repopulates it from
  `GradeManager.getGradesForStudent(id)`, then re-runs `checkHonorsEligibility()` for
  `HonorsStudent`s.

### `GradeManager`
- `viewGradesByStudent` both computes **and prints** the grade history table directly to
  `System.out` — it's the one manager method that does console formatting instead of returning
  data (see Known Issues).
- `calculateCoreAverage` / `calculateElectiveAverage` / `calculateOverallAverage` are pure and
  return `0.0` for a student with no matching grades (never throw).
- Constructor calls `syncGradeCounter()`, which scans all existing grades for the highest numeric
  suffix and calls `Grade.initializeCounter()` — the same monotonic-ID pattern as `StudentManager`.

---

## 9. Exceptions

All unchecked (`extends RuntimeException`), one per failure mode:

| Exception | Thrown by |
|---|---|
| `StudentNotFoundException` | `StudentRepositoryImpl` lookups/update/delete when the ID doesn't exist |
| `StudentValidationException` | `StudentValidator` |
| `exceptions.subjects.SubjectNotFoundException` | `SubjectRepositoryImpl` lookups/delete |
| `exceptions.subjects.SubjectValidationException` | `SubjectValidator` |
| `exceptions.grades.GradeException` | `Grade` (range check), `GradeRepositoryImpl` (not found / storage full) |

`Main.main` catches all five together in one `catch` clause and prints `"Error: " +
e.getMessage()`, then a catch-all `Exception` for anything else (`"An unexpected error
occurred..."`). The app never crashes out of the menu loop on a caught exception.

---

## 10. Known Issues (code vs. documented behavior)

Found by tracing actual runtime behavior against `ReadMe.md` / `docs/PROJECT_GUIDE.md`, not by
assumption — each is reproducible by reading the cited method.

1. **Honors eligibility threshold doesn't match spec.**
   `ReadMe.md` (US-1 acceptance criteria) specifies `checkHonorsEligibility()` should return true
   "if average >= 85%". The actual implementation
   (`HonorsStudent.checkHonorsEligibility()`, [HonorsStudent.java:49-53](../../student-grade-management/src/model/student/HonorsStudent.java#L49-L53))
   checks `average >= StudentType.HONORS.getPassingGrade()`, i.e. `>= 60.0` — the same threshold
   as `isPassing()`. **Effect:** every currently-passing Honors student is reported as "Honors
   Eligible", since the two checks are now equivalent. See Lab 3's data insights report for a
   worked example of a student this changes the outcome for.

2. **Seeded phone numbers would fail the app's own validation.**
   `StudentValidator.validatePhone()` requires exactly 10 digits
   (`^[0-9]{10}$`, [StudentValidator.java:13](../../student-grade-management/src/validation/StudentValidator.java#L13)).
   The 5 seeded students use phones like `+1-555-0101`
   ([StudentRepositoryImpl.java:18-22](../../student-grade-management/src/repository/student/StudentRepositoryImpl.java#L18-L22)),
   and `ReadMe.md`'s own "Add Student" walkthrough example uses `+1-555-1234`. Neither would pass
   validation if entered through the live "Add Student" menu — they only exist because seeding
   constructs `Student` objects directly, bypassing `StudentServiceImpl.addStudent()` (the only
   code path that calls the validator).

3. **`Grade.reconstruct(...)` is unreferenced.** No caller in the current codebase uses it; every
   grade is created through the validating `new Grade(...)` constructor. Not a bug, but worth
   flagging before anyone builds a persistence layer against it assuming it's already wired up.

4. **`GradeManager.viewGradesByStudent` mixes computation with console output**, unlike every
   other method on the two manager classes (which return data and let `Main` do the printing).
   Not incorrect, but inconsistent with the rest of the layer's contract — if a future caller
   needs the grade history as data (e.g. to export it, or for the kind of analysis in Lab 3's
   report), they'd need a non-printing variant.
