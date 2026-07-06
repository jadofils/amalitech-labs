# Sprint 1 Plan

## Sprint Goal

Deliver a working, testable **Create + Read** slice of the contact book: a user can add a
contact and immediately see it, both individually and in the full list.

## Selected Stories

| ID | Story | Points | Acceptance Criteria |
|----|-------|-------:|----------------------|
| PBI-1 | Add a new contact | 3 | See [backlog.md](backlog.md#acceptance-criteria) |
| PBI-2 | List all contacts | 2 | See [backlog.md](backlog.md#acceptance-criteria) |
| PBI-3 | Get a contact by ID | 2 | See [backlog.md](backlog.md#acceptance-criteria) |

**Sprint capacity:** 7 points. **Estimated effort (PERT):** ~3.74 hours realistic, ~5.9 hours
worst case (see [estimation.md](estimation.md#per-story-time-estimate)).

## Why these three

They form the smallest possible end-to-end vertical slice: without Create there's nothing to
list or look up, and Read (both "all" and "by ID") is what proves Create actually persisted
something — together they're independently demoable without needing Update/Delete yet, which
is why they're front-loaded into Sprint 1 rather than spread across both sprints.

## Task Breakdown

Each story ships on its own feature branch, PR'd against `main`, merged only after the
`build-and-test` GitHub Actions check passes (auto-merge, no manual click):

1. **`feature/create-contact`** (PBI-1): `ContactValidator` (name/email rules) →
   `ContactServiceImpl.addContact` → `Main` menu case → tests.
2. **`feature/list-contacts`** (PBI-2): `ContactServiceImpl.getAllContacts` → `Main` menu case →
   tests.
3. **`feature/get-contact-by-id`** (PBI-3): `ContactServiceImpl.getContactById` → `Main` menu
   case → tests.

Sprint 0's scaffold (model, repository, interfaces, CI) is a prerequisite and already merged
before this sprint starts.

## Definition of Done

Unchanged from [backlog.md](backlog.md#definition-of-done): own branch, local tests green, PR
with a passing required CI check, auto-merge, docs updated.

## Out of Scope for Sprint 1

Update, Delete, health-check, and logging (Sprint 2); duplicate-email rejection (PBI-6,
unscheduled stretch).
