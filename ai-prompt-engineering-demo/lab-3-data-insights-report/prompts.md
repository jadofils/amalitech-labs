# Prompt Log — Lab 3 (Data Insights Report)

Three iterations. Model used: Claude (Sonnet).

## v1 — naive prompt

> "Give me insights from this student grade dataset."

**Result:** Generic descriptive statistics — overall mean, min, max, a note that "some students
are performing better than others." Technically correct, analytically empty: it treated the CSV
as an anonymous table of numbers, with no connection to what "passing", "honors eligible", or
"core vs. elective" mean *in this specific application*. Nothing in the output could be acted on
by whoever maintains `student-grade-management`.

**Diagnosis:** no domain grounding. A generic-stats prompt gets you generic stats; the numbers
that matter here are the ones gated by business rules already implemented in the codebase, and the
prompt never pointed at them.

## v2 — ground the analysis in the app's own rules

> "Compute this using the actual rules the codebase defines, not generic statistics: each
> student's passing threshold from `StudentType` (50/60), `HonorsStudent.checkHonorsEligibility()`
> for eligibility, and the core/elective split `GradeManager` computes. Tell me which students are
> at risk *by the app's own definition of passing*, not by an arbitrary cutoff you pick."

**Result:** Correctly identified Carol Martinez as failing and computed real per-student
core/elective splits. But it evaluated honors eligibility using the *README's stated* 85% rule,
because that's what "the rules" sounded like without being told to check which rule is actually
implemented in code versus documented.

**Diagnosis:** "the app's own rules" is ambiguous between the spec and the implementation when the
two disagree — and Lab 2 had already established that they do, for this exact method. The prompt
needed to name that disagreement explicitly rather than let the model pick one silently.

## v3 — final: force the code/spec discrepancy into the analysis

> "Same as above, but specifically: run honors eligibility both ways — once using what
> `HonorsStudent.checkHonorsEligibility()` actually computes in code (`>= 60`), and once using the
> README's documented `>= 85` rule — and tell me if any student's eligibility flips between the
> two. If one does, use it as a concrete example, not an abstract caveat."

**Result:** Surfaced Bob Smith (STU002) as the student whose eligibility flips depending on which
rule is applied — turning Lab 2's abstract code-vs-doc finding into a concrete "this specific
student's status changes" data point, which is what §5 of
[DATA_INSIGHTS_REPORT.md](DATA_INSIGHTS_REPORT.md) is built around.

## Techniques demonstrated

| Technique | Where |
|---|---|
| Domain/business-rule grounding (vs. generic stats) | v2 |
| Naming an ambiguity explicitly instead of letting the model resolve it silently | v3 |
| Requesting a concrete worked example over an abstract finding | v3 |
| Cross-referencing an earlier lab's finding (Lab 2) as input to this one | v3 |
