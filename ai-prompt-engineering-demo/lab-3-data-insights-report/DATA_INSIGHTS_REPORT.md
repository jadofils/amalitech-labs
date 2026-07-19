# Data Insights Report — Student Grade Management (Sample Run)

**Source:** [dataset/sample-grades.csv](dataset/sample-grades.csv) — 25 grade records, 5 students,
6 subjects. See [README.md](README.md) for why this is a constructed sample rather than a real
export (the app has no persistence — nothing to export).

**Method:** computed directly from the dataset using the same rules the codebase itself defines
(`Student.calculateAverageGrade`, `isPassing`, `HonorsStudent.checkHonorsEligibility`,
`GradeManager`'s core/elective split, `LetterGrade.fromNumeric`) — not generic mean/median/mode
blind to what the app considers "passing" or "eligible". All figures below are reproducible by
hand from the CSV.

---

## 1. Headline numbers

| Metric | Value |
|---|---|
| Students | 5 (3 Regular, 2 Honors) |
| Grade records | 25 (15 core, 10 elective) |
| Overall class average | **73.8%** |
| Pass rate (against each student's own passing threshold) | **4 / 5 (80%)** |
| Core subjects average | 72.4% |
| Elective subjects average | 75.9% |

## 2. Per-student summary

| ID | Name | Type | Core avg | Elective avg | Overall | Passing grade | Status |
|---|---|---|---|---|---|---|---|
| STU001 | Alice Johnson | Regular | 85.0 | 75.5 | **81.2%** | 50% | Passing |
| STU002 | Bob Smith | Honors | 78.3 | 87.5 | **82.0%** | 60% | Passing |
| STU003 | Carol Martinez | Regular | 45.0 | 54.0 | **48.6%** | 50% | **Failing** |
| STU004 | David Chen | Honors | 92.7 | 92.5 | **92.6%** | 60% | Passing |
| STU005 | Emma Wilson | Regular | 61.0 | 70.0 | **64.6%** | 50% | Passing (marginal) |

**At-risk:** Carol Martinez is the only failing student, and by a meaningful margin (1.4 points
under her 50% threshold) — every core grade she has is in the D/F range (42, 38, 55). Her lowest
mark is English (38, letter grade F), her strongest is Physical Education (48, still below the
class's weakest subject average). This is a single-student pattern across subjects, not one bad
grade — worth flagging for intervention rather than treating as noise.

**Marginal:** Emma Wilson passes (64.6% vs. a 50% bar) but sits closer to Carol than to the rest
of the class — 3 of her 5 grades (60, 65, 58) land in the C range. Not at-risk under the app's own
rule, but the next student worth watching if the bar were raised.

## 3. Subject-level breakdown

| Subject | Type | Average | Rank (highest first) |
|---|---|---|---|
| Music | Elective | 86.75 | 1 |
| Science | Core | 74.6 | 2 |
| Mathematics | Core | 72.0 | 3 |
| English | Core | 70.6 | 4 |
| Art | Elective | 70.3 | 5 |
| Physical Education | Elective | 67.0 | 6 |

Electives aren't uniformly "easier" — Music is the single highest-scoring subject in the dataset,
but Physical Education is the lowest. The elective average (75.9%) being above the core average
(72.4%) is being pulled up almost entirely by Music; Art and PE are in line with, or below, the
core subjects. A report that only compared "core vs. elective" as two buckets (as
`GradeManager.calculateCoreAverage`/`calculateElectiveAverage` do) would miss that the elective
category isn't internally consistent.

## 4. Letter grade distribution (`LetterGrade.fromNumeric`, all 25 records)

| Grade | Count | % of records |
|---|---|---|
| A (≥85) | 10 | 40% |
| B (≥70) | 5 | 20% |
| C (≥55) | 7 | 28% |
| D (≥40) | 2 | 8% |
| F (<40) | 1 | 4% |

60% of all records are B or above; the D/F records (3 total) all belong to Carol Martinez — no
other student has a sub-55 grade in this sample.

## 5. Honors eligibility: a worked example of the Lab 2 discrepancy

Lab 2's technical documentation flagged that `HonorsStudent.checkHonorsEligibility()` checks
`average >= 60` (the Honors passing grade), not `>= 85` as `ReadMe.md`'s acceptance criteria for
US-1 states. This dataset has a student where that difference isn't hypothetical:

| Student | Overall avg | Eligible under **code** (`>= 60`) | Eligible under **documented spec** (`>= 85`) |
|---|---|---|---|
| Bob Smith (STU002) | 82.0% | ✅ Yes | ❌ No |
| David Chen (STU004) | 92.6% | ✅ Yes | ✅ Yes |

Run this dataset through the app as-is and Bob Smith is reported "Honors Eligible" — the same
status as David Chen, despite averaging over 10 points lower and not meeting the 85% bar the
project's own requirements describe. If the intent really is an 85% honors bar, this is a real
student outcome the current logic gets wrong, not just a documentation nit — it changes who shows
up as "eligible" in the View Students listing.

## 6. Recommendations

1. **Decide and fix the honors-eligibility threshold** (`HonorsStudent.checkHonorsEligibility()`,
   [HonorsStudent.java:49-53](../../student-grade-management/src/model/student/HonorsStudent.java#L49-L53)).
   Either the check should compare against a distinct 85% constant, or the README's acceptance
   criteria should be corrected to say "eligible = passing" — right now the code and the spec
   disagree, and §5 shows a case where the answer actually flips.
2. **Flag Carol Martinez (STU003) for follow-up** — every subject, not one outlier grade, is below
   passing; a per-subject pattern like this is exactly the kind of signal `viewGradesByStudent`
   already has the data for but doesn't surface (it prints a table, it doesn't compute "how many
   subjects below passing").
3. **Don't report core/elective as two flat buckets when they're this uneven** — Music's 86.75
   average is carrying the entire "electives" category; PE at 67.0 would look very different
   reported on its own. If `GradeManager` ever adds an aggregate report, per-subject breakdown
   should be the default, not core-vs-elective.
4. **Watch Emma Wilson (STU005)** — currently passing but with a C-heavy grade spread; not
   actionable under current rules, but worth a note if passing thresholds are ever revisited.
