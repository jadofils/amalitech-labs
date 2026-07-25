# Product Backlog — Student Grade Management v3 (Advanced Edition)

## Product Vision

An enterprise-grade evolution of v2: type-safe collections chosen for their actual access
patterns, multi-format NIO.2 file I/O, regex-driven validation and search, and a concurrent
subsystem (thread pools, a live dashboard, scheduled jobs, a thread-safe cache, an audit log) —
all while keeping v2's exception hierarchy, DTO/Mapper layer, and test-per-class discipline intact.
Source: [../../REAME-V3.md](../../REAME-V3.md).

---

## Backlog Items

### PBI-1: Collections Optimization (US-1) — ✅ Done (`feature/v3-collections-optimization`, merged)
| Field | Value |
|---|---|
| **Priority** | High |
| **Story Points** | 5 |
| **Sprint** | 1 |

> **As a** developer
> **I want to** back student/grade storage with the collection type suited to each access pattern
> **So that** lookups, ranking, and uniqueness checks are all O(1)/O(log n) instead of O(n) linear
> scans over an array

**Acceptance Criteria:**
- [x] `HashMap<String, Student>` (keyed on student ID) for O(1) lookup, replacing/complementing the
      current array scan in `StudentRepositoryImpl` — done as `LinkedHashMap` specifically, to
      preserve `getAllStudents()`'s insertion order for existing callers/tests
- [x] `TreeMap<Double, List<Student>>` (or equivalent) for sorted GPA ranking, O(log n) insert —
      `GPACalculator.classRankings()`
- [x] `HashSet<String>` for unique course/subject-code tracking — implemented as the
      `LinkedHashMap<String, Subject>`'s own key set rather than a second, redundant `HashSet`
      (the map's keys already are the unique-code set; addSubject() now checks `containsKey`
      before inserting, closing a real gap where the array version never checked for duplicates)
- [x] `LinkedList<Grade>` for a student's grade history where insertion order matters — the new
      `HashMap<String, LinkedList<Grade>>` secondary index in `GradeRepositoryImpl`
- [ ] `PriorityQueue<Task>` for the scheduled/batch task queue — still deferred to PBI-4/PBI-6,
      nothing to queue until those stories exist
- [x] Big-O documented per operation — Javadoc on every changed method in
      `StudentRepositoryImpl`/`SubjectRepositoryImpl`/`GradeRepositoryImpl`/`GPACalculator`
- [x] Existing `StudentRepository`/`SubjectRepository`/`GradeRepository` interfaces unchanged —
      verified by the full existing suite passing unmodified (496/496)

### PBI-2: Multi-Format File I/O (US-2, US-10) — ✅ Done (`feature/v3-multi-format-io` + `feature/v3-nio-migration`, merged)
| Field | Value |
|---|---|
| **Priority** | High |
| **Story Points** | 8 |
| **Sprint** | 1 |

> **As a** teacher
> **I want to** export and import grade data as CSV, JSON, or binary
> **So that** I can exchange data with other systems and back up records efficiently

**Acceptance Criteria:**
- [x] NIO.2 `Path`/`Files.lines()` used for CSV read/write — the new `dataio` package
      (`StudentDataExporter`/`Importer`, `GradeDataExporter`/`Importer`), **and** the existing v2
      `FileExporter`/`CSVParser`/`BulkImportService` paths, migrated in a follow-up commit
      (`Files.writeString`/`Files.createDirectories`/`Files.exists`/`Files.lines`), fully
      behavior-preserving — `FileExportResult.getFilePath()` still returns the same
      `"<reportsDir>/<filename>"` string shape regardless of OS, and `CSVParser.parse(File)` is
      retained (delegates to a new `parse(Path)`) so every existing call site kept compiling
      unmodified
- [x] JSON export/import — Jackson (`jackson-databind`, chosen over Gson/hand-rolled per your
      answer; new `pom.xml` dependency)
- [x] Binary export/import via object serialization — `StudentRecord`/`GradeRecord` are
      `Serializable` records, written/read via `ObjectOutputStream`/`ObjectInputStream`
- [x] `Stream` pipelines (`map`/`filter`/`collect`) used for the actual transformations, not manual
      loops, per US-10 — both in the new `dataio` CSV writers/readers and in
      `CSVParser.parse(Path)`'s line → `ParsedLine` → rows/errors pipeline
- [x] All three formats round-trip (export then import reproduces the original data) — verified by
      test for both `StudentRecord` and `GradeRecord`
- [x] Proper resource management — try-with-resources throughout (`Files.lines()`,
      `ObjectOutputStream`/`ObjectInputStream`, buffered writers), no leaked file handles

### PBI-3: Regex-Based Validation (US-3) — ✅ Done (`feature/v3-regex-validation`, merged)
| Field | Value |
|---|---|
| **Priority** | High |
| **Story Points** | 3 |
| **Sprint** | 1 |

> **As a** developer
> **I want to** validate every structured input field with an explicit regex
> **So that** malformed IDs, contact info, and dates are rejected consistently and predictably

**Acceptance Criteria:**
- [x] Student ID: `STU\d{3,}` — the open question is resolved by the codebase's own existing
      convention (`RegularStudentTest` already asserted `^STU\\d{3,}$`): `{3,}`, not an exact `{3}`,
      since `Student`'s `STU%03d` generator is zero-padded to 3 digits but unbounded above 999
- [x] Email, phone (multiple accepted formats — a plain 10-digit local number, or a dashed
      `+<country>-###-####` international one), date (`YYYY-MM-DD`), course code (`ENG101`-style)
      patterns, each with its own named constant in the new `ValidationPatterns` class (no inline
      regex literals scattered around)
- [x] Validation failures raise the existing `StudentValidationException`/`SubjectValidationException`/
      `InvalidGradeException` types — no new generic exception type introduced. New
      `GradeValidator.validateForImport()` closes a real gap: imported (CSV/JSON/binary)
      `GradeRecord`s were previously reconstructed via `Grade.reconstruct()` with zero validation;
      it now checks gradeId/studentId format and the 0-100 grade range before reconstruction. Date
      format is deliberately *not* enforced there — `Grade`'s own date field is `dd-MM-yyyy`, not the
      ISO shape `DATE_ISO` validates, so enforcing it would reject every real exported-then-reimported
      grade
- [x] Patterns centralized in `utils/validators/ValidationPatterns` rather than duplicated per caller
      — `StudentValidator`/`SubjectValidator` now reference it instead of declaring their own
      `Pattern`s (subject-code behavior unchanged, a pure refactor; student-ID check deliberately
      tightened, phone check deliberately widened, both per this story's own acceptance criteria)

### PBI-4: Concurrent Batch Report Generation (US-4) — ✅ Done (`feature/v3-concurrent-reports`, merged)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 5 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** generate reports for many students in parallel
> **So that** bulk reporting doesn't block the console on large classes

**Acceptance Criteria:**
- [x] `FixedThreadPool`, configurable 2–8 threads — new `BatchReportService`, constructor-validated
- [x] Batch report generation is measurably faster than sequential for a large-enough student count
      (brief cites "10x faster" as an illustrative target, not a hard requirement to reproduce
      exactly — record actual measured speedup instead) — `BatchReportServiceMockitoTest` measures
      and prints the real speedup (5.62x locally, 16 students/8 threads/25ms simulated work each)
      rather than asserting a hard-coded ratio, which would be flaky across machines/CI
- [x] Thread-safety verified for any shared state touched during generation (student/grade reads) —
      `BatchReportServiceTest` runs 24 concurrently-generated reports over the real, Map-backed
      (PBI-1) repositories and asserts every single one exactly matches a fresh sequential read of
      the same student, over real `StudentRepositoryImpl`/`GradeRepositoryImpl` instances

### PBI-5: Real-Time Statistics Dashboard (US-5) — ✅ Done (`feature/v3-statistics-dashboard`, merged)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 5 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** see class statistics update automatically while the console is open
> **So that** I don't have to manually re-run "View Class Statistics" to see current numbers

**Acceptance Criteria:**
- [x] Background thread (`CachedThreadPool` per the brief) refreshes displayed stats every 5s — new
      `StatisticsDashboard`: a dedicated daemon "ticker" thread drives the 5s-default schedule
      (configurable), each tick's computation running on a `CachedThreadPool`-configured
      `ThreadPoolExecutor` — deliberately distinct from PBI-6's `ScheduledExecutorService`, so the
      two stories demonstrate different `java.util.concurrent` tools rather than reusing one
- [x] Dashboard start/stop is explicit (menu option), not silently always-on in the background —
      `start()`/`stop()`/`isRunning()` are idempotent and return whether they actually changed state;
      console menu wiring deferred the same way PBI-1/2/3/4's service-layer capabilities were (not
      yet wired into `ConsoleApp`'s menu — ready to be, without touching its existing dispatch tests)
- [x] No race condition between the background refresh and a concurrent grade-entry menu action —
      `GradeManager` gained a `ReentrantReadWriteLock`: `addGrade()` takes the write lock, a new
      `readLocked()` lets the dashboard read under the same lock. Proven with a stress test: 200
      grades added on one thread while the dashboard refreshes concurrently on another, asserting
      the final count is never lost or duplicated

### PBI-6: Scheduled Grade Processing (US-6) — ✅ Done (`feature/v3-scheduled-gpa-job`, merged)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 3 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** have GPA recalculation run on a schedule
> **So that** rankings stay current without a manual trigger

**Acceptance Criteria:**
- [x] `ScheduledExecutorService` runs a daily GPA recalculation job — new
      `ScheduledGpaRecalculationJob` wraps `Executors.newSingleThreadScheduledExecutor()`, running
      `GPACalculator.classRankings()` at a 24h-default (configurable) fixed rate — deliberately not
      the `CachedThreadPool` PBI-5's dashboard uses, so the two stories each demonstrate a different
      `java.util.concurrent` tool
- [x] Job status (last run, next run) visible from the console — `getLastRunAt()`/`getNextRunAt()`/
      `getLastResult()` (all `Optional`, empty before the first run) expose it; console menu wiring
      deferred the same way PBI-1/2/3/4/5's service-layer capabilities were, ready to be wired in
      without touching `ConsoleApp`'s existing dispatch tests
- [x] Scheduled job shuts down cleanly on application exit (no orphaned threads) — `stop()` cancels
      the scheduled future and shuts the executor down (`awaitTermination` then `shutdownNow` on
      timeout); the scheduler thread is also daemon as a second line of defense. Reads through
      `GradeManager.readLocked()` (added in PBI-5), so a recalculation can't observe a grade
      addition mid-write — proven with a stress test: 150 grades added on one thread while the job
      runs on another, asserting every student ends up in exactly one GPA bucket

### PBI-7: Regex-Based Search (US-7)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 3 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** search students using a pattern (e.g. an email domain filter)
> **So that** I can find groups of students a simple substring match can't express

**Acceptance Criteria:**
- [ ] Extends v2's existing `Searchable`/`StudentSearcher` with a pattern-based search mode
- [ ] At least the example from the brief works: filter students by email domain
- [ ] Invalid regex input from the console is caught and reported, not an uncaught
      `PatternSyntaxException`

### PBI-8: Thread-Safe Caching (US-8)
| Field | Value |
|---|---|
| **Priority** | Low |
| **Story Points** | 5 |
| **Sprint** | 3 |

> **As a** developer
> **I want to** cache frequently-read student/grade data with an eviction policy
> **So that** repeated lookups don't repeatedly recompute or re-scan storage

**Acceptance Criteria:**
- [ ] `ConcurrentHashMap`-backed cache with an LRU eviction policy
- [ ] Cache hit rate observable (surfaced on the dashboard from PBI-5, per the brief's example)
- [ ] Cache invalidated correctly on writes (a student/grade update must not serve stale cached data)

### PBI-9: Concurrent Audit Trail (US-9)
| Field | Value |
|---|---|
| **Priority** | Low |
| **Story Points** | 3 |
| **Sprint** | 3 |

> **As an** administrator
> **I want to** have every data-changing action logged to an audit trail
> **So that** I can review who changed what, safely even under concurrent writes

**Acceptance Criteria:**
- [ ] `SingleThreadExecutor` (or equivalent) serializes audit writes so concurrent callers never
      interleave/corrupt a log entry
- [ ] Distinct from the existing `logging.Logger` (diagnostics) — this is a durable, structured
      record of data-changing actions specifically, not general debug output
- [ ] Every add/update/delete across student/subject/grade goes through it

### PBI-10: Testing & Coverage (cross-cutting)
| Field | Value |
|---|---|
| **Priority** | High |
| **Story Points** | 8 |
| **Sprint** | 3 |

> **As a** developer
> **I want to** have the concurrency- and I/O-heavy v3 additions properly tested
> **So that** thread-safety bugs and file-format regressions are caught before merge, not in
> production

**Acceptance Criteria:**
- [ ] 25+ unit tests covering collections, regex, streams, file I/O (per-class, following this
      repo's existing `<Class>Test`/`<Class>MockitoTest` convention)
- [ ] 10+ integration tests, including a mocked `ExecutorService` and a mocked file system where
      real threads/real disk I/O would make tests slow or flaky
- [ ] Minimum 85% JaCoCo coverage maintained on top of v2's existing suite (currently 495/495
      passing, 96.2% overall — v3 must not regress this)

---

## Open questions (not resolved by the source brief)

- The brief's own "Git Workflow" section cuts off before listing anything past `main`/`develop` —
  see `docs/v3/README.md`'s Branching plan for what this backlog assumes in its absence.
- No JSON library is currently a dependency (PBI-2) — needs an explicit choice (e.g. Jackson,
  Gson) before that story can start.
- US-1's `STU\d{3}` regex is only 3 digits; v2's ID generator is already unbounded beyond 999
  students — needs a decision on whether the pattern or the generator changes.
