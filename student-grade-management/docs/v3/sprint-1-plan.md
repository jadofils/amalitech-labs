# Sprint 1 Plan — Student Grade Management v3 (Advanced Edition)

## Sprint Goal

Deliver the foundation the rest of v3 builds on: collection types matched to actual access
patterns (PBI-1), multi-format NIO.2 file I/O (PBI-2), and regex-based validation (PBI-3) — with
zero behavior change visible to any existing caller or test.

## Ground rule for this whole sprint

Every backing-storage change is an **Open/Closed** change: the public interface
(`StudentRepository`, `SubjectRepository`, `GradeRepository`, `StudentValidator`, ...) does not
change shape, so nothing above the repository/validation layer needs to change, and every existing
test keeps passing unmodified. New capability is added *alongside* — a new method, a new class —
never by deleting or narrowing something that already works. Where a change is genuinely
observable (e.g. iteration order), it's called out explicitly and checked against the existing
test suite before merging, not assumed safe.

---

## Sprint Backlog

### PBI-1: Collections Optimization (5 SP)

Working one repository at a time, each its own small commit, each verified against the full
existing suite before moving to the next:

1. **`StudentRepositoryImpl`: `Student[]` → `LinkedHashMap<String, Student>`.**
   `findStudentById`/`updateStudent`/`deleteStudent` go from O(n) linear scan to O(1) keyed
   lookup/replace/remove. `LinkedHashMap` specifically (not `HashMap`) to keep `getAllStudents()`
   in insertion order — several existing tests use `getAllStudents().get(0)` to grab "some seeded
   student," and that has to keep meaning "Alice Johnson" the way it does today.
2. **`SubjectRepositoryImpl`: `Subject[]` → `LinkedHashMap<String, Subject>`**, same rationale, plus
   a `HashSet<String>` tracking subject codes for an O(1) duplicate-code check on `addSubject`
   (today's array version never actually checks for duplicate codes — this is a genuine gap being
   closed, not just a perf change, so it needs its own test and a note in the sprint review).
3. **`GradeRepositoryImpl`: add a `HashMap<String, LinkedList<Grade>>` secondary index** keyed by
   student ID, populated alongside the existing array store (not replacing it — grade lookup by ID
   and "storage full" behavior both still need the array's insertion-order semantics).
   `getGradesByStudentId` switches from an O(n) full-array scan to an O(1) map lookup returning the
   O(k) list for that student (k = that student's grade count) — `LinkedList` because insertion
   order within a student's own history is exactly what "grade history" needs, and it's cheap to
   append to.
4. **`TreeMap<Double, List<Student>>` for GPA ranking** — a new read-only view (likely a small new
   method on `GPACalculator` or a new class, decided when this task starts) rather than a storage
   change; sorted GPA-to-students grouping for O(log n) insert during ranking instead of the
   current full-resort.
5. **Document Big-O per operation** — either Javadoc on each method or a table in this folder,
   decided when PBI-1 is implemented.
6. **`PriorityQueue<Task>` deferred to PBI-4/PBI-6** (concurrent batch reports / scheduled
   processing) — it has nothing to queue until those stories exist.

**Verification per step:** run the existing `StudentRepositoryImplTest`/`MockitoTest` (or the
Subject/Grade equivalents) after each swap, unmodified, before writing anything new. If an existing
test needs to change, that's a signal the "no visible behavior change" rule was broken somewhere
and needs a second look, not a reason to just update the test.

### PBI-2: Multi-Format File I/O (8 SP)
**Subtasks:**
- Migrate existing CSV export/import (`FileExporter`, `BulkImportService`/`CSVParser`) from
  `FileWriter`/`BufferedReader` to NIO.2 `Path`/`Files.lines()` — same output, same tests, new
  mechanism underneath
- Pick and record a JSON library (none is currently a dependency)
- Add JSON export/import
- Add binary export/import via serialization
- Rewrite the transformation steps (row → domain object, domain object → formatted row) as
  `Stream` `map`/`filter`/`collect` pipelines

### PBI-3: Regex-Based Validation (3 SP)
**Subtasks:**
- Centralize the student-ID, email, phone, date, and course-code patterns as named constants
- Reconcile `STU\d{3}` (3 digits) against the existing unbounded `STU%03d` ID generator — decide
  before writing the pattern, not after
- Extend `StudentValidator`/`SubjectValidator`, keep throwing the existing exception types

---

## Definition of Done (this sprint)

- Full existing suite (495/495 as of the last v2 merge) still green, unmodified, after every step
- New collection-backed behavior has its own new tests (not a replacement of old ones)
- JaCoCo coverage does not regress below its current 96.2%
- Big-O documented for every collection swapped in under PBI-1
