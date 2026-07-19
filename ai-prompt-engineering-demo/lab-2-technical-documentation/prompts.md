# Prompt Log — Lab 2 (Technical Documentation)

Four iterations, each fixing a specific failure mode of the previous one. Model used: Claude
(Sonnet). Technique added at each step is called out explicitly.

## v1 — naive prompt

> "Write technical documentation for the student-grade-management project."

**Result:** Generic, structurally plausible documentation that repeated what a Java student
management system "usually" looks like (CRUD verbs, a `Student` class with plausible-sounding
fields) instead of what *this* codebase actually contains. No class names matched the real
package structure. Unusable — it documents an imaginary project, not this one.

**Diagnosis:** no source grounding. The prompt never told the model to read the actual files, so
it pattern-matched to the most common shape of "student management system" from training data.

## v2 — add role + explicit source grounding

> "You are a backend engineer writing a technical reference for another engineer joining this
> project. Read every file under `student-grade-management/src/` before writing anything — do
> not describe generic patterns, describe what this specific codebase does. Cover: the layering
> (model/repository/service/manager), what each class's public methods actually do, and what
> validation rules gate each write operation."

**Result:** Much closer — correct class names, correct layering, correct method signatures. But
the output stopped at "what the code does" and didn't flag anywhere the code's actual behavior
disagreed with the project's own README/PROJECT_GUIDE (e.g., it repeated the README's claim about
honors eligibility without checking it against `HonorsStudent.checkHonorsEligibility()`).

**Diagnosis:** grounding fixed the hallucination problem, but a passive "describe what it does"
instruction doesn't produce a *critical* read — it produces a faithful paraphrase, including
faithfully paraphrasing wrong assumptions carried over from the docs.

## v3 — add a verification/chain-of-thought instruction

> "Same as above, but: before writing each section, trace the actual runtime behavior in code
> (don't trust the README's description of it) and note explicitly if what you find in the code
> disagrees with `docs/PROJECT_GUIDE.md`, `ReadMe.md`, or the seeded sample data. Treat those
> discrepancies as findings to report, not something to silently fix."

**Result:** This is the pass that surfaced the two real issues now documented under "Known Issues"
in [API_DOCUMENTATION.md](API_DOCUMENTATION.md):
- `HonorsStudent.checkHonorsEligibility()` checks `average >= 60` (the Honors passing grade), not
  `>= 85` as the README's acceptance criteria for US-1 states — so "Honors Eligible" is currently
  always true whenever an honors student is passing.
- `StudentValidator.validatePhone()` requires exactly 10 digits, which rejects the phone format
  used in the README's own "Add Student" example (`+1-555-1234`) — that example would throw
  `StudentValidationException` if actually run through the menu.

## v4 — final: add an explicit output schema

> "Structure the final document as: (1) layer-by-layer responsibility map, (2) one reference
> section per class — signature, purpose, preconditions/validation, exceptions thrown, (3) a
> 'Known Issues' section listing every code-vs-doc discrepancy found in v3, with the exact file
> and line-level reasoning. No filler sections, no restating PROJECT_GUIDE.md's architecture
> narrative — this doc is the method-level layer underneath it."

**Result:** [API_DOCUMENTATION.md](API_DOCUMENTATION.md) — the schema constraint is what kept the
final output from re-deriving into another prose architecture overview (which already exists) and
forced it to stay at the method/contract level, which is the gap that actually needed filling.

## Techniques demonstrated

| Technique | Where |
|---|---|
| Role prompting | v2 |
| Explicit source grounding (read files, don't summarize memory) | v2 |
| Chain-of-thought / verification instruction | v3 |
| Constraint against a known failure mode (don't trust the docs) | v3 |
| Output schema constraint | v4 |
| Iterative refinement from a diagnosed failure, not a guess | v1→v2→v3→v4 |
