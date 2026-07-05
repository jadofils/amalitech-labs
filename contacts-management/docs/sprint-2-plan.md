# Sprint 2 Plan (initial draft)

> This is the Sprint 0 forward-looking draft. It will be revised once Sprint 1 finishes,
> under a **"Retro Adjustments"** section below, applying the improvements from
> [sprint-1-retro.md](sprint-1-retro.md) — per the brief's requirement to actually apply
> Sprint 1's retrospective in Sprint 2, not just write one.

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

## Task Breakdown

1. **`feature/update-contact`** (PBI-4): reuse `ContactValidator` from PBI-1 →
   `ContactServiceImpl.updateContact` → `Main` menu case → tests.
2. **`feature/delete-contact`** (PBI-5): `ContactServiceImpl.deleteContact` → `Main` menu case →
   tests.
3. **`feature/health-check`** (PBI-7): new `monitoring/HealthCheck.java` → `Main` menu case →
   test.
4. **`feature/structured-logging`** (PBI-8): `java.util.logging` calls added to every
   `ContactServiceImpl` method (INFO on success, WARNING on validation/not-found failure) →
   logging test using a custom `Handler` to capture emitted records.

## Definition of Done

Unchanged from [backlog.md](backlog.md#definition-of-done).

## Out of Scope for Sprint 2

PBI-6 (duplicate-email rejection) remains an unscheduled stretch item — see the retro for
whether it's worth a Sprint 3 in a real (non-simulated) continuation.

## Retro Adjustments

*(To be filled in after Sprint 1's retrospective, before Sprint 2 work starts.)*
