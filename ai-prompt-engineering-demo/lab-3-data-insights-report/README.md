# Lab 3: AI-Driven Data Insights Report

**Subject project:** [student-grade-management](../../student-grade-management/) — same backend as
Lab 2.

## Why a sample dataset instead of "real" data

`student-grade-management` stores everything in fixed-size in-memory arrays with no persistence —
`StudentRepositoryImpl` seeds 5 students and `SubjectRepositoryImpl` seeds 6 subjects on every
startup, but **no grades are seeded**; a grade only exists if it's entered by hand through the
"Record Grade" menu option during that one run, and it's gone the moment the process exits. There
is no committed grade dataset anywhere in the repo to analyze.

So this lab uses [dataset/sample-grades.csv](dataset/sample-grades.csv): the 5 real seeded
students and 6 real seeded subjects, populated with one invented grade record per student per
subject they're enrolled in (3 core + 2 electives each, 25 records total) — built to exercise a
realistic spread (a failing student, a strong-across-the-board honors student, and specifically a
borderline honors student, so the honors-eligibility bug found in Lab 2 actually has a visible
effect on the numbers). It is clearly a constructed sample, not a claim about real student data.

## Approach

See [prompts.md](prompts.md) for the prompt iterations. The short version: a first pass asked for
"insights from this dataset" and got back generic descriptive stats (mean, min, max) with no
connection to the actual business logic in the codebase. The useful version of the prompt asked
the analysis to be run *through* the domain rules implemented in `model/` and `manager/`
(`isPassing()`, `checkHonorsEligibility()`, core/elective averaging) rather than computing generic
statistics blind to what the app itself considers meaningful — which is what turned up the honors
eligibility discrepancy as a data point with a concrete, named example (STU002), not just an
abstract note in Lab 2.

## Deliverable

→ [DATA_INSIGHTS_REPORT.md](DATA_INSIGHTS_REPORT.md)
