# Product Backlog

## Product Vision

> A lightweight in-memory contact book prototype that lets a user create, view, update, and
> delete personal contacts through a simple console app, built to demonstrate Agile delivery
> and a real CI/CD gate on every change.

## Backlog Items

| ID | User Story | Priority | Story Points |
|----|------------|----------|---------------|
| PBI-1 | As a user, I want to add a new contact (name, email, phone) so I can keep track of people I know. | High | 3 |
| PBI-2 | As a user, I want to view a list of all my contacts so I can see everyone I've saved. | High | 2 |
| PBI-3 | As a user, I want to look up a single contact by its ID so I can check specific details quickly. | High | 2 |
| PBI-4 | As a user, I want to update an existing contact's details so my records stay current. | Medium | 3 |
| PBI-5 | As a user, I want to delete a contact so I can remove people I no longer need. | Medium | 2 |
| PBI-6 | As a user, I want the system to reject a new contact whose email is already used so I don't create duplicates. | Low | 3 |
| PBI-7 | As an operator, I want a health-check so I can verify the service is running and see how much data it holds. | Medium | 1 |
| PBI-8 | As an operator, I want every create/update/delete to be logged (and failures logged as warnings) so I can trace what happened. | Low | 2 |

Priority and points were set together during Sprint 0 planning: High = required for a minimum
usable CRUD slice, Medium = completes CRUD and observability, Low = valuable but safe to defer
if time runs out. Points use relative Fibonacci-like sizing (1, 2, 3, 5, 8), calibrated against
PBI-7 (a 1-point "trivial") and PBI-1 (a 3-point "small, with real validation logic").

## Acceptance Criteria

**PBI-1 — Add a new contact**
- Given a valid name, email, and phone, when the contact is added, it is stored with a unique
  generated ID and can immediately be retrieved.
- Given a blank or missing name, when adding a contact, the system rejects it with a clear
  validation error and nothing is stored.
- Given a malformed email (no `@`, no domain), when adding a contact, the system rejects it
  with a clear validation error and nothing is stored.

**PBI-2 — List all contacts**
- Given zero contacts exist, listing returns an empty list, not `null` and not an error.
- Given N contacts exist, listing returns exactly N contacts.

**PBI-3 — Get a contact by ID**
- Given an ID that exists, retrieval returns the matching contact's full details.
- Given an ID that does not exist, retrieval throws a clear "not found" error instead of
  returning `null` or crashing.

**PBI-4 — Update an existing contact**
- Given an existing ID and valid new data, the contact's fields are updated in place and the
  ID does not change.
- Given an ID that does not exist, the update fails with a clear "not found" error.
- Given invalid new data (e.g. malformed email), the update is rejected and the original
  contact is left unchanged.

**PBI-5 — Delete a contact**
- Given an existing ID, deleting it removes the contact; a subsequent lookup by that ID fails
  with "not found".
- Given an ID that does not exist, delete fails with a clear "not found" error.

**PBI-6 — Reject duplicate emails**
- Given an existing contact with email `x@y.com`, adding a new contact with the same email is
  rejected with a clear validation error.

**PBI-7 — Health-check**
- Given the service is running, the health-check reports status `UP` and the current number
  of stored contacts.

**PBI-8 — Structured logging**
- Every successful create/update/delete logs one INFO line naming the operation and the
  contact ID.
- Every validation failure or not-found error logs one WARNING line with the reason.

## Definition of Done

Applies to every backlog item before it counts as delivered:

1. Implemented on its own `feature/<slug>` branch — no unrelated changes bundled in.
2. Unit tests written covering every acceptance criterion above, passing locally
   (`bash scripts/test.sh`).
3. Pull request opened against `main`; GitHub Actions CI (compile + JUnit) is green.
4. Branch protection requires that CI check, so the PR **auto-merges** on its own once green —
   no manual merge click.
5. `docs/backlog.md` status/notes updated if the story's scope changed during implementation.

## Status

| ID | Status |
|----|--------|
| PBI-1, PBI-2, PBI-3 | Delivered (Sprint 1) |
| PBI-8, PBI-4, PBI-5 | Delivered (Sprint 2) |
| PBI-7 | Planned for Sprint 2 |
| PBI-6 | Backlog — stretch, not currently scheduled |
