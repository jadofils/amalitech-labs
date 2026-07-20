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
- Service implementations moved into `service.serviceimpl` (`6a0329e`).
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
