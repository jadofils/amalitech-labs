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

### PBI-2: Multi-Format File I/O (US-2, US-10) — 🟡 Part 1 done (`feature/v3-multi-format-io`, merged)
| Field | Value |
|---|---|
| **Priority** | High |
| **Story Points** | 8 |
| **Sprint** | 1 |

> **As a** teacher
> **I want to** export and import grade data as CSV, JSON, or binary
> **So that** I can exchange data with other systems and back up records efficiently

**Acceptance Criteria:**
- [x] NIO.2 `Path`/`Files.lines()` used for CSV read/write — done for the new `dataio` package
      (`StudentDataExporter`/`Importer`, `GradeDataExporter`/`Importer`); **not yet done** for the
      *existing* v2 `FileExporter`/`CSVParser` paths — still open, see note below
- [x] JSON export/import — Jackson (`jackson-databind`, chosen over Gson/hand-rolled per your
      answer; new `pom.xml` dependency)
- [x] Binary export/import via object serialization — `StudentRecord`/`GradeRecord` are
      `Serializable` records, written/read via `ObjectOutputStream`/`ObjectInputStream`
- [x] `Stream` pipelines (`map`/`filter`/`collect`) used for CSV row transformation, per US-10
- [x] All three formats round-trip (export then import reproduces the original data) — verified by
      test for both `StudentRecord` and `GradeRecord`

**Still open:** migrating the *existing* `FileExporter` (human-readable report text) and
`CSVParser`/`BulkImportService` (bulk grade import) from `java.io` to NIO.2. Deliberately done as
new, additive classes first rather than touching those - they're a different responsibility
(already-formatted report text, not structured interchange data) and this way nothing existing had
to change to get PBI-2's core deliverable working.
- [ ] Proper resource management — try-with-resources throughout, no leaked file handles

### PBI-3: Regex-Based Validation (US-3)
| Field | Value |
|---|---|
| **Priority** | High |
| **Story Points** | 3 |
| **Sprint** | 1 |

> **As a** developer
> **I want to** validate every structured input field with an explicit regex
> **So that** malformed IDs, contact info, and dates are rejected consistently and predictably

**Acceptance Criteria:**
- [ ] Student ID: `STU\d{3}` (note — decide how this interacts with v2's existing auto-incrementing
      `STU%03d` ID generation; auto-generated IDs must already satisfy this pattern)
- [ ] Email, phone (multiple accepted formats), date (`YYYY-MM-DD`), course code (`ENG101`-style)
      patterns, each with its own named constant (no inline regex literals scattered around)
- [ ] Validation failures raise the existing `StudentValidationException`/`SubjectValidationException`
      types (or a v3 equivalent) — no new generic exception type introduced
- [ ] Patterns centralized in one place (e.g. `utils/validators/`) rather than duplicated per caller

### PBI-4: Concurrent Batch Report Generation (US-4)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 5 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** generate reports for many students in parallel
> **So that** bulk reporting doesn't block the console on large classes

**Acceptance Criteria:**
- [ ] `FixedThreadPool`, configurable 2–8 threads
- [ ] Batch report generation is measurably faster than sequential for a large-enough student count
      (brief cites "10x faster" as an illustrative target, not a hard requirement to reproduce
      exactly — record actual measured speedup instead)
- [ ] Thread-safety verified for any shared state touched during generation (student/grade reads)

### PBI-5: Real-Time Statistics Dashboard (US-5)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 5 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** see class statistics update automatically while the console is open
> **So that** I don't have to manually re-run "View Class Statistics" to see current numbers

**Acceptance Criteria:**
- [ ] Background thread (`CachedThreadPool` per the brief) refreshes displayed stats every 5s
- [ ] Dashboard start/stop is explicit (menu option), not silently always-on in the background
- [ ] No race condition between the background refresh and a concurrent grade-entry menu action

### PBI-6: Scheduled Grade Processing (US-6)
| Field | Value |
|---|---|
| **Priority** | Medium |
| **Story Points** | 3 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** have GPA recalculation run on a schedule
> **So that** rankings stay current without a manual trigger

**Acceptance Criteria:**
- [ ] `ScheduledExecutorService` runs a daily GPA recalculation job
- [ ] Job status (last run, next run) visible from the console
- [ ] Scheduled job shuts down cleanly on application exit (no orphaned threads)

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
