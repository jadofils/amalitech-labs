# Contacts Management System

A small in-memory contact-book CRUD prototype in **core Java (plain OOP, no framework)**,
built as the individual deliverable for the "Agile & DevOps in Practice" assessment
(see [agile-devops-practice.md](agile-devops-practice.md)).

Every change lands through its own feature branch and pull request, gated by a GitHub Actions
CI check, and **auto-merges to `main` on its own once that check passes** — no manual merge
click. See [docs/plan.md](docs/plan.md) for the full delivery workflow.

## Running it

No build tool is installed or required — just `javac`/`java` plus a downloaded JUnit jar,
handled automatically by the test script:

```bash
# Compile + run the full JUnit test suite (downloads the JUnit console jar into lib/ on first run)
bash scripts/test.sh

# Compile and run the console app
javac -d out $(find src -name "*.java")
java -cp out Main
```

The same `scripts/test.sh` is what CI runs on every push/PR (`.github/workflows/ci.yml`).

## Docs

| Doc | What's in it |
|-----|---------------|
| [docs/plan.md](docs/plan.md) | Vision, scope decisions, sprint overview, links to everything else |
| [docs/project-structure.md](docs/project-structure.md) | Package layout, layering, why no DB / no build tool |
| [docs/backlog.md](docs/backlog.md) | User stories, priorities, points, acceptance criteria, Definition of Done |
| [docs/estimation.md](docs/estimation.md) | Per-file LOC/method estimates, per-story time estimates, and the estimation formula |
| [docs/sprint-1-plan.md](docs/sprint-1-plan.md) / [sprint-2-plan.md](docs/sprint-2-plan.md) | What each sprint delivers and why |
| `docs/sprint-*-review.md` / `docs/sprint-*-retro.md` | Added as each sprint completes |

## Estimation, in short

Every task got a three-point time estimate — Optimistic / Most Likely / Pessimistic — combined
with the classic **PERT formula**:

```
Expected time  E = (O + 4M + P) / 6
```

which weights the most-likely case four times heavier than either extreme. Summing every
item's Pessimistic estimate gives the **longest the whole project could take** in the worst
case. Current totals (full detail in [docs/estimation.md](docs/estimation.md)):

| | Optimistic | Expected (PERT) | Longest / worst case |
|---|-----------:|-----------------:|----------------------:|
| **Scheduled work (Sprint 0–2)** | ~11.15 h | **~19.56 h** | ~30.30 h |
| **Full backlog incl. stretch (PBI-6)** | ~11.90 h | ~20.85 h | ~32.30 h |

## Architecture at a glance

```
Main (console I/O) → ContactService (validation, IDs, logging) → ContactRepository (in-memory Map)
```

Full breakdown: [docs/project-structure.md](docs/project-structure.md).
