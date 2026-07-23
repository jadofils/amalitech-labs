# v3 (Advanced Edition) — Overview

Source brief: [../../REAME-V3.md](../../REAME-V3.md) (as copied from the assignment page — its
"Git Workflow" section cuts off mid-word after `* main` / `* de`; everything before that, through
the Testing Requirements section, is complete).

## Scope

Ten user stories (US-1 through US-10), grouped into four areas:

| Area | User stories | Summary |
|---|---|---|
| Collections | US-1 | `HashMap`/`TreeMap`/`HashSet` for O(1) lookup, sorted GPA ranking, unique course tracking; Big-O documented per operation |
| File I/O | US-2, US-10 | Multi-format (CSV/JSON/binary) export/import via NIO.2 `Path`/`Files.lines()`, and `Stream` `map`/`filter`/`reduce`/`collect` pipelines |
| Validation | US-3, US-7 | Regex-based validation (student ID, email, phone, date, course code) and regex-based search/filtering |
| Concurrency | US-4, US-5, US-6, US-8, US-9 | Thread pools for batch report generation, a live-updating stats dashboard, scheduled GPA recalculation, a thread-safe LRU cache, and a concurrent audit-trail logger |

## Branching plan

The source brief's own Git Workflow section is cut off before it lists anything past `main`/`de...`,
so this plan follows this repo's existing convention (documented in `docs/PROJECT_GUIDE.md` and
demonstrated across every prior `feature/*` branch) rather than guessing at the missing text:

- `main` / `develop` stay exactly as they are — no v3 work lands on either until it's ready.
- v3 work happens on `feature/v3-advanced-edition` (this branch) and, as each user story above is
  broken down further, on its own `feature/v3-<story>` branch cut from here — e.g.
  `feature/v3-collections-optimization`, `feature/v3-nio-file-io`, `feature/v3-regex-validation`,
  `feature/v3-concurrent-batch-reports`, one per user story or logical group.
- Each of those merges back into `feature/v3-advanced-edition` (`--no-ff`, CI green first) as it's
  completed, the same review discipline as every other feature branch in this repo.
- Once every US-1..US-10 story is merged and the full suite + coverage targets in the brief
  (25+ unit tests, 10+ integration tests, ≥85% coverage) are met on `feature/v3-advanced-edition`,
  *that* branch merges into `develop` — CI green, `--no-ff`, same as always.
- `develop` → `main` only with your explicit go-ahead, same standing rule as the rest of this
  project.

## Docs

Everything v3-specific lives under this folder (`docs/v3/`), one file per planning artifact,
mirroring the role `docs/v2-backlog.md`/`docs/v2-sprint-1-plan.md`/`docs/v2-user-manual.md` played
for v2 — see [backlog.md](backlog.md) for the user stories broken into workable tickets.
