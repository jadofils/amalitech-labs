# Effort Estimation

This document estimates, before writing the code, how big each file will be and how long each
backlog item will take — including a worst-case ("longest possible") total for the whole
project. Estimates were revisited once at the end of Sprint 1 (see
[sprint-1-retro.md](sprint-1-retro.md)) but are otherwise the original Sprint 0 numbers, so the
plan-vs-actual comparison in the retros is meaningful.

## Estimation Formula (PERT)

Each estimated item gets three time estimates in hours:

- **O — Optimistic**: if everything goes smoothly.
- **M — Most Likely**: the realistic case.
- **P — Pessimistic**: if it hits real trouble (debugging, re-reading the spec, rework).

The **Expected time** for each item uses the classic PERT (Program Evaluation Review Technique)
weighted-average formula, which weights the most-likely case four times heavier than either
extreme because it's the single most probable outcome:

```text
E = (O + 4M + P) / 6
```

Summing every item's `E` gives the project's **realistic total time**. Summing every item's `P`
gives the **longest total time the project would take** if every single item ran into its
worst case simultaneously — a useful upper bound for planning a deadline buffer.

## File Inventory (what gets built, and where)

| File | Description | Est. LOC | Est. Methods | Delivered In |
|------|-------------|---------:|--------------:|--------------|
| `src/model/Contact.java` | Plain data model: id, name, email, phone + getters/setters + `toString`. | 30 | 9 | Sprint 0 |
| `src/exceptions/ContactNotFoundException.java` | Thrown when a contact ID doesn't exist. | 7 | 1 | Sprint 0 |
| `src/exceptions/ContactValidationException.java` | Thrown when input data fails validation. | 7 | 1 | Sprint 0 |
| `src/repository/ContactRepository.java` | Storage contract: save/findById/findAll/update/deleteById/existsByEmail. | 13 | 6 | Sprint 0 |
| `src/repository/InMemoryContactRepository.java` | `Map`-backed implementation of the contract above. | 50 | 6 | Sprint 0 |
| `src/service/ContactService.java` | Business-facing CRUD contract. | 12 | 5 | Sprint 0 |
| `src/service/ContactServiceImpl.java` | Validates input, calls the repository, generates IDs, logs every operation. | 110 | 7 | Sprint 0 (skeleton) → PBI-1, PBI-3, PBI-4, PBI-5, PBI-8 |
| `src/validation/ContactValidator.java` | Static name/email rules shared by create & update. | 25 | 2 | PBI-1 |
| `src/monitoring/HealthCheck.java` | Reports status `UP` + current contact count. | 22 | 2 | PBI-7 |
| `src/Main.java` | Console menu wiring every operation to user input. | 160 | 8 | Sprint 0 (skeleton) → every PBI |
| `test/model/ContactTest.java` | Unit tests for the model. | 20 | 2 | Sprint 0 |
| `test/repository/InMemoryContactRepositoryTest.java` | Unit tests for every repository method. | 65 | 8 | Sprint 0 |
| `test/validation/ContactValidatorTest.java` | Unit tests for name/email rules. | 35 | 5 | PBI-1 |
| `test/service/ContactServiceImplTest.java` | Unit/integration tests for every service method. | 140 | 16 | PBI-1, PBI-2, PBI-3, PBI-4, PBI-5, PBI-8 |
| `test/monitoring/HealthCheckTest.java` | Unit test for health status + count. | 18 | 2 | PBI-7 |
| `scripts/test.sh` | Downloads the JUnit console jar if missing, compiles, runs tests. | 22 | — | Sprint 0 |
| `.github/workflows/ci.yml` | GitHub Actions job: compile + test on every push/PR. | 18 | — | Sprint 0 |
| `docs/*.md` (10 files) | Vision, backlog, structure, estimation, sprint plans/reviews/retros. | — | — | Sprint 0 / wrap-ups |

**Totals: ~281 lines in `src/`, ~278 lines in `test/` — roughly one line of test per line of
production code.**

## Per-Story Time Estimate

| Deliverable | Sprint | O (h) | M (h) | P (h) | E = PERT (h) |
|-------------|--------|------:|------:|------:|-------------:|
| Project setup (scaffold + CI + Sprint 0 docs) | Sprint 0 | 5.00 | 8.50 | 13.00 | 8.67 |
| PBI-1 — Add a new contact | Sprint 1 | 1.25 | 2.00 | 3.25 | 2.08 |
| PBI-2 — List all contacts | Sprint 1 | 0.40 | 0.75 | 1.25 | 0.78 |
| PBI-3 — Get contact by ID | Sprint 1 | 0.50 | 0.85 | 1.40 | 0.88 |
| Sprint 1 wrap-up (review + retro) | Sprint 1 | 0.75 | 1.25 | 2.00 | 1.29 |
| PBI-4 — Update contact | Sprint 2 | 1.00 | 1.75 | 3.00 | 1.83 |
| PBI-5 — Delete contact | Sprint 2 | 0.50 | 0.90 | 1.50 | 0.93 |
| PBI-7 — Health-check | Sprint 2 | 0.50 | 0.85 | 1.40 | 0.88 |
| PBI-8 — Structured logging | Sprint 2 | 0.50 | 0.90 | 1.50 | 0.93 |
| Sprint 2 wrap-up (review + retro) | Sprint 2 | 0.75 | 1.25 | 2.00 | 1.29 |
| **Subtotal — planned & scheduled work** | | **11.15** | **18.00** | **30.30** | **19.56** |
| PBI-6 — Reject duplicate emails *(stretch, unscheduled)* | — | 0.75 | 1.25 | 2.00 | 1.29 |
| **Grand total — full backlog** | | **11.90** | **19.25** | **32.30** | **20.85** |

### Reading these numbers

- **Realistic total (E): ~19.6 hours** for everything currently scheduled across both sprints —
  roughly 2–3 focused workdays, or spread over more evenings around other coursework.
- **Longest possible total (ΣP): ~30.3 hours** if every single item hits its worst case — the
  number to budget against if a hard deadline exists.
- Sprint 0 setup is intentionally the single biggest bucket: it includes the entire in-memory
  repository, both interfaces, the model, exceptions, CI pipeline, and all Sprint 0 docs — later
  stories are small because they only add validation, one service method, and one menu case
  each.
