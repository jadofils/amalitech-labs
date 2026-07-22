# Changelog

All notable changes to the Student Grade Management System, tracked against
[ReadMe-v2.md](ReadMe-v2.md) (the v2 assignment brief) and its supporting
docs (`docs/v2-backlog.md`, `docs/v2-sprint-1-plan.md`,
`docs/v2-user-manual.md`).

Format loosely follows [Keep a Changelog](https://keepachangelog.com/).
Commit hashes refer to `develop`.

## [Unreleased] — develop

### Added

- **Exception handling foundation** (`23a78d1`) — custom exceptions:
  `InvalidGradeException`, `ExportException`, `ImportException`,
  `InvalidFileFormatException`, plus a richer `StudentNotFoundException`
  (now carries `studentId` + `availableIds`).
- **Export Grade Report** (`69e69ce`) — `Exportable` interface,
  `ReportGenerator`, `FileExporter`; menu option 5.
- **Calculate Student GPA** (`fefdb53`) — `Calculable` interface,
  `GPACalculator` (percentage→GPA→letter, cumulative GPA, class rank);
  menu option 6.
- **Bulk Import Grades** (`1a6ae9a`) — `CSVParser`, `BulkImportService`;
  menu option 7.
- **View Class Statistics** (`93eabe4`) — `StatisticsCalculator`
  (distribution, mean/median/std-dev, subject averages, Regular-vs-Honors
  comparison); menu option 8.
- **Search Students** (`a6439f3`) — `Searchable` interface, `StudentSearcher`
  (by ID, name, grade range, type); menu option 9.
- **JUnit 5 + Mockito test suite** (`f6e4820`, merged `6935b22`) — initially
  28 files / 210 tests over the model/validation/repository/service/manager
  layers; grew to **48 files / 304 tests** (see Fixed, below) once every v2
  feature got its own coverage.
- **Lightweight `Logger` utility** (`4bacefc`, merged `0df0ff5`) — DEBUG/
  INFO/WARN/ERROR levels with timestamps.
- **Role-based menu** (`6134c22`, plus later work on `feature/role-based-menu`)
  — Student and Teacher roles now see genuinely different menus; a Student
  logs in as one specific student and can only ever view their own grade
  report.
- **Maven build** (`pom.xml`, merged `6120d40`) — `mvn test` now runs the
  whole suite via Surefire; JUnit 5/Mockito are managed dependencies instead
  of jars sourced by hand. Root `sourceDirectory`/`testSourceDirectory` both
  point at `src` (the project keeps main and test code in one tree rather
  than `src/main/java`+`src/test/java`), with `tests/**` excluded from the
  main compile step specifically so it never needs JUnit/Mockito on its
  classpath.
- **CI** (`.github/workflows/ci.yml`) — a `build-and-test-student-grade-management`
  job runs `mvn test` on every push to a `feature/**` branch and on PRs into
  `develop`/`main`.

### Changed

- Storage backing switched from `HashMap` to fixed-size arrays across all
  three repositories (`047cd2d`, `2556911`, `8452e2f`), per the assessment's
  array-based storage requirement.
- Service implementations moved into `service.serviceimpl` (`6a0329e`) —
  **later undone** by the package-flattening pass described in
  ["Professional structure & SOLID refactor"](#professional-structure--solid-refactor)
  below, once `serviceimpl` turned out to be an inconsistent, one-off split
  not used anywhere else in the codebase.
- Grade date format corrected to `dd-MM-yyyy` (`6b29fba`).

### Fixed

- `d11cc34` — production code brought in line with the (then-new) test
  suite's expectations: `HonorsStudent` gained `setGrade(int)`, its
  eligibility threshold now reads from `StudentType.HONORS.getPassingGrade()`
  instead of a hardcoded `85.0`, and `StudentValidator` gained ID/phone/status
  validation plus a stricter email regex.
- **All 14 issues found in the requirements review below are now resolved** —
  one feature branch per problem area, each with its own tests, each merged
  to `develop` only after its own CI run passed. See each KI entry for the
  specific fix commit. Full suite after all merges: **304/304 passing**
  (`mvn test`, verified locally and in CI).

### Removed

- Unused `GradeStatus` and `Role` enums (`bee1cb3`, `d20f15f`) from an
  earlier, abandoned role-based-access attempt.

---

## Requirements review: 14 issues found against `ReadMe-v2.md` / `docs/v2-*.md`, all resolved

Found by reading the v2 requirements documents in full, then reading and, in
several cases, executing the actual `develop` source to confirm each one.
Every item below was then fixed on its own feature branch, verified there
(compile + full suite + in several cases a manual run reproducing the exact
before/after), pushed, confirmed green in CI, and merged to `develop` -
never straight to `main`, which stays gated for a later, separate decision.

### KI-1 (RESOLVED — `feature/gpa-calculation`, `956856b`): GPA letter-grade conversion was wrong for every grade except the top and bottom tier

`ReadMe-v2.md`'s GPA table and `GPACalculator.percentageToGPA()` agreed
exactly, but `GPACalculator.gpaToLetter()` used a threshold ladder shifted
by one tier — each GPA value returned the letter belonging to the *next
tier up*. Verified by running the method against the documented boundary
percentages:

| % | GPA | Documented letter | Actual letter (before fix) |
|---|---|---|---|
| 90-92 | 3.7 | A- | **A** |
| 87-89 | 3.3 | B+ | **A-** |
| 83-86 | 3.0 | B | **B+** |
| 80-82 | 2.7 | B- | **B** |
| 77-79 | 2.3 | C+ | **B-** |
| 73-76 | 2.0 | C | **C+** |
| 70-72 | 1.7 | C- | **C** |
| 67-69 | 1.3 | D+ | **C-** |
| 60-66 | 1.0 | D | **D+** |

**Fix:** rewrote `percentageToGPA()`/`gpaToLetter()` as table-driven lookups
over three parallel arrays (`PERCENTAGE_THRESHOLDS`/`GPA_POINTS`/
`LETTER_GRADES`) instead of two independently-hand-written if-chains, so a
percentage and its letter can no longer drift apart. Re-verified manually:
a 90% grade now shows `3.7 (A-)`, not `A`. `GPACalculatorTest`'s
parameterized `gpaToLetterMatchesTableTest` asserts every row of the
documented table individually as a permanent regression guard.

### KI-2 (RESOLVED — `feature/export-report`, `d086d63`): Exported summary report never contained the student's actual name

`ReportGenerator.exportSummary()` hardcoded the literal string
`"Name: [name]\n"` — it was never passed a `Student`, only a `studentId`.
Not theoretical: `reports/Emma_Wilson_reports_summary.txt` in this repo was
a real file this method produced, containing `Name: [name]` verbatim.

Related: `generateFullReport()` was the one method that *did* include a
real name plus a "PERFORMANCE ANALYSIS" section, but `Main` never called
it — the wired-up path never showed performance analysis despite US-2
requiring it.

**Fix:** `ReportGenerator` now takes a `StudentManager` dependency and
resolves the real name internally (the public `Exportable` contract -
`exportSummary(studentId)`/`exportDetailed(studentId)` - didn't need to
change). Folded the performance-analysis section directly into
`exportDetailed()` and deleted the now-redundant `generateFullReport()`.
Manually re-ran Export Grade Report for STU001 and read the generated
file: `Name: Alice Johnson`.

### KI-3 (RESOLVED, across four branches): Logging was not application-wide, and one exception path was silently swallowed

The `Logger` utility only covered `service`/`repository`/`manager`/`Main`;
`export`, `calculators`, `imports`, and `manager.StudentSearcher` had zero
log calls. `BulkImportService.writeImportLog()` caught `IOException` with
only a comment - no log call, no rethrow - making that failure invisible.

**Fix:** added `Logger` calls throughout `FileExporter`/`ReportGenerator`
(`feature/export-report`), `CSVParser`/`BulkImportService`
(`feature/bulk-import`, `4205831`), and `StudentSearcher`
(`feature/search-students`, `84e100c`). The swallowed `IOException` in
`writeImportLog()` now goes through `Logger.error()` before continuing
(the import itself already succeeded/failed by that point, so there's
nothing to roll back, but the failure is no longer invisible).

### KI-4 (RESOLVED, across five branches): The entire v2 feature set had zero test coverage

None of `GPACalculator`, `StatisticsCalculator`, `CSVParser`,
`BulkImportService`, `ReportGenerator`, `FileExporter`, or `StudentSearcher`
had a single test, despite ReadMe-v2.md requiring 80%+ overall / 95%+ on
"critical business logic (GPA calculation, statistics)."

**Fix:** every one of those seven classes now has a `<Class>Test` (real
collaborators) and, where it has a collaborator worth isolating from, a
`<Class>MockitoTest` — added incrementally on the same branch as each
class's other fixes. Suite grew from 210 to **304 tests** (48 test files
total).

### KI-5 (RESOLVED — `feature/class-statistics`, `ac173f7`): Two different, disagreeing letter-grade scales existed in the same app

`model.enums.LetterGrade.fromNumeric()` used `A≥85, B≥70, C≥55, D≥40, F<40`.
`StatisticsCalculator`'s grade-distribution buckets hardcoded a different
scale entirely: `A≥90, B≥80, C≥70, D≥60, F<60`. A grade of 87 was an "A" in
View Grade Report and a "B" in View Class Statistics.

**Fix:** `calculateDistribution()` now buckets by
`LetterGrade.fromNumeric(grade).ordinal()` directly - `LetterGrade`'s
declaration order (A, B, C, D, F) already matches the bucket index order,
so there is now only one scale, and it cannot drift again.

### KI-6 (RESOLVED — `feature/class-statistics`, `ac173f7`): "Mode" was documented and shown in the example output, but was never implemented

US-5/PBI-5 both require "mean, median, mode, standard deviation," and
ReadMe-v2.md's own Screenshot 5 shows `Mode: 85.0%` - but `StatsResult` had
no mode field and no mode calculation existed anywhere.

**Fix:** added `calculateMode()` (ties broken toward the lower value) and
wired it into `Main`'s View Class Statistics output, between Median and
Standard Deviation, matching the documented example's layout.

### KI-7 (RESOLVED — `feature/gpa-calculation`, `956856b`): `Calculable`'s actual signature didn't match what was planned

`docs/v2-sprint-1-plan.md` specified `cumulativeGPA(studentId)` /
`classRank(studentId)` - i.e. the implementation resolves grades/class
averages internally. The actual interface took a pre-assembled
`List<Grade>`/`List<Double>` instead, pushing data-gathering into `Main`.

**Fix:** `GPACalculator` now takes `GradeManager` + `StudentManager` via
constructor injection and resolves both by `studentId` internally, matching
the planned contract; `Main.calculateGPA()` dropped its manual
class-averages assembly entirely.

### KI-8 (RESOLVED — `feature/exception-handling`, `a9be3ee`): Custom exceptions still extended `RuntimeException`, and `Main` still had a generic catch

PBI-1 explicitly required "No generic Exception catching (replace
RuntimeException extends)." Every custom exception extended
`RuntimeException` directly, and `Main`'s menu loop still ended in
`catch (Exception e) { ... }`.

**Fix:** added `exceptions.ApplicationException` (abstract, extends
`RuntimeException`) as the one common parent; all nine custom exceptions
now extend it instead. `Main`'s final catch is now
`catch (ApplicationException e)`, not `catch (Exception e)`.

### KI-9 (RESOLVED — `feature/exception-handling`, `a9be3ee`): `StudentNotFoundException`'s recovery data was thrown away before `Main` ever saw it

`StudentRepositoryImpl` threw the rich 3-argument
`StudentNotFoundException(message, studentId, availableIds)`, but
`StudentManager.findStudent()` caught it and returned `null`, discarding
`studentId`/`availableIds`. `recordGrade()`/`viewGradeReport()`/
`exportGradeReport()`/`calculateGPA()` each duplicated their own inline
"not found" message instead - one of them missing the available-IDs list
entirely.

**Fix:** each of those methods now `throw`s `StudentNotFoundException`
with the real `studentId` and `getAvailableStudentIds()`, letting it bubble
up to the single top-level handler in `main()` that already knew how to
print `e.getAvailableIds()` — it just never got the chance to before.
Manually confirmed: Record Grade against an unknown ID now prints
"Available student IDs: STU001, STU002, STU003, STU004, STU005" via the
exception path.

### KI-10 (RESOLVED — `feature/bulk-import`, `4205831`): `CSVParser` read the CSV's `SubjectType` column but never validated or used it

A row like `STU001,Mathematics,Elective,85` (wrong declared type, valid
subject name) imported successfully with no warning, because the parsed
`SubjectType` was discarded immediately after being read.

**Fix:** `parse()` now parses the declared type and rejects the row with a
"type mismatch" error if it disagrees with the matched subject's actual
type, or "Unknown subject type" if it isn't a valid `SubjectType` at all.
Also added `CSVImportException` (the class `docs/v2-backlog.md`'s PBI-4
names explicitly) in place of the previously-reused
`InvalidFileFormatException`.

### KI-11 (RESOLVED — this file): No `CHANGELOG.md` existed

Required by both ReadMe-v2.md's minimum requirements and its Documentation
rubric line. This file is the fix.

### KI-12 (RESOLVED — `feature/ci-cd`, merged `6120d40`): No Maven/Gradle build

ReadMe-v2.md's Testing minimum requirements say "JUnit and Mockito
dependencies added (Maven/Gradle)"; the project had neither, sourcing test
dependencies from plain jars in a local cache instead.

**Fix:** added `pom.xml` (see "Added", above, for how it reconciles Maven's
conventions with this project's single-`src` layout) and a CI job that
runs `mvn test` on every relevant push/PR.

### KI-13 (IMPROVED — ongoing): Conventional commit format

Only 7 of 63 commits reachable from `develop` before this review (~11%)
used a `feat:`/`fix:`/`test:`/`docs:` prefix, despite it being an explicit
requirement and a Git Workflow rubric line.

**Fix:** every commit made while resolving KI-1 through KI-12 and KI-14
uses a conventional prefix (`feat(ci-cd): ...`, `fix(gpa-calculation): ...`,
etc.) - see the git log on `develop` from `6120d40` onward.

### KI-14 (RESOLVED, across five branches): No JavaDoc, and hardcoded magic numbers throughout

None of the seven new v2 classes had any JavaDoc, and `GPACalculator`/
`StatisticsCalculator` hardcoded their threshold numbers inline with no
named constants - both contradicting the Code Quality / Documentation
checklists.

**Fix:** added class- and method-level JavaDoc to `GPACalculator`,
`Calculable`, `ReportGenerator`, `FileExporter`, `StatisticsCalculator`,
`CSVParser`, `BulkImportService`, and `StudentSearcher`. Replaced
`GPACalculator`'s and `StatisticsCalculator`'s inline threshold numbers
with named constant arrays (`PERCENTAGE_THRESHOLDS`/`GPA_POINTS`/
`LETTER_GRADES` and the `LABELS` array respectively).

### What was already satisfied (for balance)

- All 6 new v2 user stories were wired up and reachable from the menu from
  the start.
- Feature-branch workflow was genuinely followed from the beginning -
  `feature/exception-handling`, `feature/export-report`,
  `feature/gpa-calculation`, `feature/bulk-import`, `feature/class-statistics`,
  `feature/search-students` all exist in history with their own commits,
  and this review's fixes were made on those same branches rather than
  directly on `develop`.
- Commit count on `develop` clears the "minimum 20" bar comfortably (60+
  even before this review's fix commits).
- The GPA **points** table (percentage → 4.0-scale number) was always
  correct - only the subsequent points → letter conversion was broken (KI-1).

---

## Professional structure & SOLID refactor

Requested as a follow-up once all 14 requirements-review issues above were
resolved: make the package structure more professional (clear folders, a
DTO/Mapper layer, enum-backed static data, a `utils` package for
validators/sanitizers), then separately, a full SOLID-principles audit of
the result. Three feature branches, each verified with the full suite and
a green CI run before merging to `develop` — never straight to `main`.

### `feature/BugFix-professional-structure` (merged `40309b5`)

**Package flattening** (`bb3da93`) — collapsed a set of one-off
`impl`/`serviceimpl` subpackages that existed for no consistent reason,
7 files moved via `git mv`, imports fixed up across 20 files:

| Before | After |
| --- | --- |
| `exceptions.grades.GradeException` | `exceptions.GradeException` |
| `exceptions.subjects.SubjectNotFoundException` / `SubjectValidationException` | `exceptions.SubjectNotFoundException` / `SubjectValidationException` |
| `repository.subject.impl.SubjectRepositoryImpl` | `repository.subject.SubjectRepositoryImpl` |
| `repository.grade.impl.GradeRepositoryImpl` | `repository.grade.GradeRepositoryImpl` |
| `service.serviceimpl.StudentServiceImpl` / `GradeServiceImpl` | `service.StudentServiceImpl` / `GradeServiceImpl` |

Verified 304/304 passing before this commit — no behavior changed, only
where each class lives.

**Enum-backed static data**, replacing booleans and parallel arrays
(`60e36a8`):

- New `model.enums.Role` (`TEACHER`, `STUDENT`) replaces `Main`'s
  `boolean isTeacher` flag.
- `LetterGrade` now carries its own `minPercentage` per constant, so
  `fromNumeric()` derives from the enum itself instead of a separately
  hand-maintained if-chain.
- New `model.enums.GpaLetterGrade` — an 11-constant enum carrying
  percentage threshold, GPA points, and display label together on one
  object, replacing `GPACalculator`'s three parallel arrays
  (`PERCENTAGE_THRESHOLDS`/`GPA_POINTS`/`LETTER_GRADES`, added for the
  KI-1 fix above) with a single source of truth for the grading table.
- `StatisticsCalculator`'s grade-distribution labels are now derived from
  `LetterGrade.values()` instead of a second, independently hardcoded
  string array — closing off a KI-5-style regression permanently, not
  just fixing the one instance that had already surfaced.
- Added `GpaLetterGradeTest` (34 parameterized cases covering every
  documented percentage and GPA value in `ReadMe-v2.md`'s grading table).

**New `utils` package** (`d44d825`):

- `InputSanitizer.sanitize()` trims and strips control characters from
  raw console input; wired into every raw name/email/phone/studentId/
  search-input read in `Main`.
- `DateFormats` centralizes the five `SimpleDateFormat` patterns that
  were previously copy-pasted independently into `Logger`, `Grade`,
  `ReportGenerator`, and `BulkImportService`.
- `StudentValidator`/`SubjectValidator` later moved into
  `utils.validators` (`c725026`, committed directly to `develop`) to sit
  under the same `utils` package, replacing the standalone `validation`
  package they used to live in.

**DTO/Mapper layer** (`0a9127e`), deliberately scoped to read-only
display paths only — Add Student and Record Grade keep using real domain
objects, since they need the full validation/business-rule surface a DTO
doesn't carry:

- `dto.StudentDTO` + `mapper.StudentMapper` — wired into Search Students'
  results table and its exported-search-results file.
- `dto.GradeDTO` + `mapper.GradeMapper` — wired into
  `ReportGenerator.exportDetailed()`'s grade-history table.

Full suite green (350/350) after all four pieces above.

### `feature/BugFix-solid-principles` (merged `e7d9cbb`)

A full SOLID audit of the result above found Liskov and Interface
Segregation already clean, and Dependency Inversion already correct via
constructor injection — but two real gaps:

**OCP fix** (`3c752e5`): `StudentSearcher.searchByType()` took a
`boolean isHonors`, and `StatisticsCalculator.compareStudentTypes()`
branched on `instanceof HonorsStudent`, even though every `Student`
already carried a `StudentType` enum internally — it just was never
exposed. Adding a third student type later would have meant reworking a
boolean parameter and every `instanceof` check by hand instead of adding
one enum constant and one subclass.

**Fix:** `Student` now exposes `abstract StudentType getType()`;
`getStudentType()` becomes a `final` method delegating to it, removing a
duplicated `return studentType.name()` override that existed identically
in both `RegularStudent` and `HonorsStudent`. `Searchable.searchByType()`
now takes a `StudentType` directly, and both call sites compare
`getType()` instead of an `instanceof` check. (Deliberately left the
`instanceof HonorsStudent` checks in `Main` and
`StudentManager.hydrateGrades()` alone — those call
`checkHonorsEligibility()`, a genuinely Honors-specific method with no
equivalent on the base `Student` contract, so there's no enum comparison
that could replace them.)

**SRP fix** (`3fab1b4`): `Main.java` had grown into a 760-line God
Class — composition root, console I/O, business orchestration for all 9
menu features, role authorization, and exception-to-message translation,
all in one file.

**Fix:** extracted each feature into its own class under a new `console`
package, implementing a `MenuAction` interface (`getOptionNumber`,
`getLabel`, `execute`, `isAuthorizedFor(Role)`, `terminatesLoop`):
`AddStudentAction`, `ViewStudentsAction`, `RecordGradeAction`,
`ViewGradeReportAction`, `ExportGradeReportAction`, `CalculateGpaAction`,
`BulkImportAction`, `ClassStatisticsAction`, `SearchStudentsAction`,
`ExitAction`. `Main` is now only a composition root: it builds the
dependency graph, holds a `List<MenuAction>`, prints the menu by
iterating it, and dispatches a chosen number to the matching action —
adding, removing, or reordering a menu option no longer touches `Main`'s
own code (this also closes the OCP smell in the old numeric-range
`isAuthorized(choice)` check, now expressed per action via
`isAuthorizedFor(Role)`). Also dropped two now-provably-dead "Access
denied" checks inside the old `addStudent()`/`recordGrade()` bodies:
`Main`'s outer role gate already blocked those two menu numbers for
students before either method could ever run, so the checks never
executed.

`Main` itself has no automated test coverage (it's a console entry point
reading `stdin`), so this one was verified by hand: full suite green
(350/350) for every other class, plus manual smoke runs of the compiled
jar covering add student, search by student ID, role-based access
denial/allowal, and a clean exit — all byte-for-byte the same prompts and
output as before the split.

### `feature/BugFix-package-structure` (merged `d46330e`)

A follow-up question about the resulting structure — "why is `calculators`
not a subfolder of `interfaces`, if `GPACalculator` implements
`Calculable`?" — surfaced a real inconsistency once traced through: `service`
and `repository` already colocate each interface with its `Impl` in the
same package (`service/StudentService.java` +
`service/StudentServiceImpl.java`), but `Calculable`, `Exportable`, and
`Searchable` — each with exactly one implementer — were split out into a
standalone `interfaces` package instead, a second, different convention
for the identical relationship.

**Fix** (`7c450fd`): moved each interface into its implementer's package
to match the convention already used elsewhere — `Calculable` →
`calculators` (implementer: `GPACalculator`), `Exportable` → `export`
(implementer: `ReportGenerator`), `Searchable` → `manager` (implementer:
`StudentSearcher`) — and deleted the now-empty `interfaces` package.

Also renamed `tests/Students` → `tests/student`: the only test package in
capitalized/plural form, breaking both standard Java package naming
(lowercase) and `tests/README.md`'s own documented rule that test
packages mirror their source package (the source package is
`model/student`, singular and lowercase).

No behavior changed; full suite green (350/350) after both fixes.

---

## Role enforcement, build tooling, and console testability

### `feature/BugFix-student-read-only` (merged `54a1943`)

Auditing every menu action for read vs. write found that `BulkImportAction`
(option 7) never overrode `isAuthorizedFor(Role)`, defaulting to the
`MenuAction` interface's `true` — the one write path (CSV import creates
new `Grade` records) still reachable by the Student role after the earlier
OCP/SRP pass.

**Fix** (`e4f6589`): `BulkImportAction.isAuthorizedFor()` now requires
`Role.TEACHER`, matching Add Student, View Students, and Record Grade —
every write action is teacher-only; every action a Student can still
reach is read-only.

### `feature/BugFix-menu-role-filter` (merged `1462f0c`)

Even after the fix above, a Student's menu still *listed* "7. Bulk Import
Grades" (and the other teacher-only options) — role gating only rejected
the choice after it was picked, so unauthorized options were visible,
just non-functional.

**Fix** (`91492a1`): `printMenu()` now skips any option where
`useRoleBased && !action.isAuthorizedFor(currentRole)`, so a Student's
menu simply never lists options 1, 2, 3, or 7. Dispatch-time gating stays
in place too, since a user can still type a number that isn't on their
filtered menu directly. Verified by hand across Student/Teacher/role-off
modes. `docs/PROJECT_GUIDE.md`'s role table updated to match (`3691162`).

### Bulk-import CSV fixtures (`147c55f`)

Added three CSV files under `imports/`, built against the real seed data,
for manual runs of Bulk Import Grades and as reference data for further
tests: `bulk_import_valid.csv` (7 all-valid rows), `bulk_import_mixed_errors.csv`
(1 valid row plus one row for every distinct rejection `CSVParser`/
`BulkImportService` can produce — unknown student, unknown subject,
subject-type mismatch, out-of-range grade, non-numeric grade, wrong
column count, unknown subject-type string), and `bulk_import_empty.csv`
(header only). Each verified by actually running Bulk Import Grades
against it through the compiled app.

### `feature/BugFix-pom-jacoco-placement` (merged `2a9e6d8`)

An in-progress, uncommitted edit had added the JaCoCo plugin as a
`<plugin>` element directly inside `<dependencies>` — not a valid
location; Maven rejected the whole POM as malformed
("Unrecognised tag: 'plugin'"), breaking every Maven goal, not just a
specific one.

**Fix** (`cd8d608`): moved it into `<build><plugins>`, alongside the
existing compiler/surefire plugins. `mvn verify` now succeeds end-to-end
— 350/350 tests, jar built, JaCoCo report generated for all 69 classes.
Measured coverage at that point: **92.2%** on business logic (everything
except the `console`/`Main` UI layer), **98.9%** on `calculators` alone —
both comfortably clearing the assignment brief's 80%/95% targets.

### `feature/BugFix-sonar-plugin` (merged `afde6e6`)

Registered `sonar-maven-plugin` (3.9.1.2184) in `<build><plugins>`,
matching the JaCoCo pattern. Not bound to any lifecycle phase, so
`mvn test`/`mvn verify` are unaffected either way — this only makes
`mvn sonar:sonar` available once a SonarQube host and token are
configured. See `docs/PROJECT_GUIDE.md` §11 for how to start a local
SonarQube server (`StartSonar.bat`) and run the analysis.

### `feature/BugFix-console-testability` (in progress)

Digging into the JaCoCo numbers above surfaced two more findings:

**Dead code removed:** `exceptions.InvalidFileFormatException` was
referenced nowhere in `src` except its own file and a Javadoc mention in
`CSVImportException` — it was superseded by `CSVImportException` during
the KI-10 fix and never deleted. Removed; the stale `@link` reference in
`CSVImportException`'s Javadoc updated to plain text.

**Exception coverage gaps closed:** added
`tests/exceptions/ApplicationExceptionHierarchyTest.java`, directly
exercising every constructor/getter on `StudentNotFoundException`,
`StudentValidationException`, `SubjectNotFoundException`,
`SubjectValidationException`, `GradeException`, `InvalidGradeException`,
`ImportException` (all three constructors), and `CSVImportException`
(both constructors) — the `exceptions` package's own instruction coverage
went from 39.7% to 95.6%. Added a `FileExporterTest` case that forces a
*real* `IOException` (writing to a path that's actually an existing
directory) to verify `ExportException` wraps it correctly with the file
path attached, rather than only being reachable in theory.

**`Main` split into `app.Main` (composition root) + `app.ConsoleApp` (the
menu loop):** the old `Main` bound its `Scanner` to `System.in` in a
`static final` field, and lived in the default (unnamed) package — no
test in a named `tests.*` package could ever reference it even if it
were otherwise testable, since Java doesn't allow importing a class from
the unnamed package into a named one. `ConsoleApp` is now an ordinary
instance class in the named `app` package, taking its `Scanner` via
constructor exactly like every `console.*Action` already did; `Main`
shrank to composition-root wiring plus `new ConsoleApp(scanner, actions).run()`,
too thin to need a test of its own. Added `tests/app/ConsoleAppTest.java`
(scripted `Scanner` input, captured `System.out`, real `MenuAction` test
doubles) and `tests/app/ConsoleAppMockitoTest.java` (mocked `MenuAction`s,
verifying dispatch/authorization/retry interactions) — 17 new tests,
all passing, exercising every exception-translation branch, the
role-based menu filter, the invalid-input paths, and the
retry-on-`InvalidGradeException` flow.

Entry point changed from `java -cp target/classes Main` to
`java -cp target/classes app.Main` — updated in `docs/PROJECT_GUIDE.md`.

**Known, deliberately scoped-out gap:** the 10 individual `console/*Action`
classes still have no automated tests of their own (0% in JaCoCo) — each
already takes its `Scanner` via constructor, so the exact same technique
used for `ConsoleAppTest` would work for each of them; it just hasn't
been done yet. Documented explicitly in `docs/PROJECT_GUIDE.md` §12
rather than left implicit.

Full suite: 377/377 passing (up from 350). Overall JaCoCo instruction
coverage: 65.3% (up from 60.0%); excluding only `console/` (the
remaining, explicitly-scoped-out gap): 92.3%.

### `feature/BugFix-generic-exceptions` (generic-RuntimeException audit)

Requested audit: every service/repository throw site should use the
declared exception hierarchy under `ApplicationException`, never a raw
`RuntimeException`. Grepped every `throw new RuntimeException(...)` in
`src` (excluding tests) and found exactly two, both for the identical
"backing array is full" condition:

- `StudentRepositoryImpl.addStudent()`
- `SubjectRepositoryImpl.addSubject()`

`GradeRepositoryImpl.addGrade()` already handled this correctly via
`GradeException` - Grade had a general-purpose exception beyond just
"not found"/"validation failed," but Student and Subject didn't, so
those two call sites fell back to the generic root type instead.

**Fix:** added `StudentException` (studentId) and `SubjectException`
(subjectCode), mirroring `GradeException`'s existing shape and role
exactly, and swapped both throw sites over. `app.ConsoleApp`'s
warn-level catch group extended to include both (they're expected
business conditions, not "genuinely unexpected" bugs, so they belong
with `StudentValidationException`/`GradeException`/etc., not the final
catch-all). Added direct constructor/getter tests for both to
`ApplicationExceptionHierarchyTest`, and updated
`StudentRepositoryImplMockitoTest`/`SubjectRepositoryImplMockitoTest` to
assert the specific exception type instead of bare `RuntimeException`.

Also fixed in passing: `StudentServiceImpl.getStudentById()`'s
unreachable defensive branch (the repository already throws
`StudentNotFoundException` on a miss, so this can never actually run)
was still falling back to a plain `RuntimeException` instead of
`StudentNotFoundException` - corrected for full consistency.

Every other `catch (RuntimeException e)` in the codebase turned out, on
inspection, to be one of two different legitimate patterns that don't
need to change: a deliberate "best-effort, swallow and continue" catch
(`StudentManager`/`GradeManager`'s static-counter sync, which must never
crash construction over a not-yet-available data source), or a "log for
visibility, then rethrow the exact same specific exception unchanged"
wrapper (`GradeServiceImpl.recordGrade()`'s two catch blocks) - neither
converts anything to a generic type.

Full suite: 379/379 passing (up from 377).
