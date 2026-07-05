# Sprint 2 Plan (finalized after Sprint 1 retro)

> Sprint 1 delivered PBI-1, PBI-2, and PBI-3 (see [sprint-1-review.md](sprint-1-review.md)).
> This plan has been revised per [sprint-1-retro.md](sprint-1-retro.md) — see
> **"Retro Adjustments"** below for what changed from the original draft.

## Sprint Goal

Complete the CRUD set (Update, Delete) and add the basic monitoring/logging the brief requires.

## Selected Stories

| ID | Story | Points | Acceptance Criteria |
|----|-------|-------:|----------------------|
| PBI-4 | Update an existing contact | 3 | See [backlog.md](backlog.md#acceptance-criteria) |
| PBI-5 | Delete a contact | 2 | See [backlog.md](backlog.md#acceptance-criteria) |
| PBI-7 | Health-check | 1 | See [backlog.md](backlog.md#acceptance-criteria) |
| PBI-8 | Structured logging | 2 | See [backlog.md](backlog.md#acceptance-criteria) |

**Sprint capacity:** 8 points. **Estimated effort (PERT):** ~4.57 hours realistic, ~7.4 hours
worst case (see [estimation.md](estimation.md#per-story-time-estimate)).

## Why these four

Update and Delete complete the CRUD acronym the brief asks for. Health-check and structured
logging are pulled forward specifically because the brief requires "basic monitoring/logging"
in Sprint 2 — bundling both into this sprint (rather than deferring further) keeps that
requirement from being a last-minute scramble.

## Task Breakdown (reordered per retro — see Retro Adjustments)

1. **`feature/structured-logging`** (PBI-8, moved first): `java.util.logging` calls added to
   every existing `ContactServiceImpl` method (INFO on success, WARNING on validation/not-found
   failure) → logging test using a custom `Handler` to capture emitted records. Landing this
   first means `updateContact` and `deleteContact` are written *with* logging from the start
   instead of it being retrofitted afterwards.
2. **`feature/update-contact`** (PBI-4): reuse `ContactValidator` from PBI-1 →
   `ContactServiceImpl.updateContact` (including its own logging calls) → `Main` menu case →
   tests.
3. **`feature/delete-contact`** (PBI-5): `ContactServiceImpl.deleteContact` (including its own
   logging calls) → `Main` menu case → tests.
4. **`feature/health-check`** (PBI-7): new `monitoring/HealthCheck.java` → `Main` menu case →
   test.

## Definition of Done

Unchanged from [backlog.md](backlog.md#definition-of-done).

## Out of Scope for Sprint 2

PBI-6 (duplicate-email rejection) remains an unscheduled stretch item — see the retro for
whether it's worth a Sprint 3 in a real (non-simulated) continuation.

## Retro Adjustments

Applying [sprint-1-retro.md](sprint-1-retro.md)'s two improvements:

1. **Toolchain verified before any Sprint 2 story code was written**: re-ran
   `bash scripts/test.sh` clean (24/24 passing) and confirmed `.vscode/settings.json` resolves
   JUnit imports with no red squiggles, *before* branching `feature/structured-logging` —
   instead of discovering gaps reactively mid-sprint like in Sprint 1.
2. **Logging reordered to land first**: PBI-8 was originally last in the task order; it's now
   first, specifically so PBI-4 and PBI-5 are implemented with their logging calls already
   part of the method from the start rather than added back in afterwards.
