# Project Plan

## Purpose

This repository is the individual deliverable for the "Agile & DevOps in Practice" assessment
(see [agile-devops-practice.md](../agile-devops-practice.md) at the repo root). It must show —
not just describe — Agile planning and a working DevOps pipeline across two simulated sprints.

## Product Vision

> A lightweight in-memory contact book prototype that lets a user create, view, update, and
> delete personal contacts through a simple console app, built to demonstrate Agile delivery
> and a real CI/CD gate on every change.

## Scope Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Language/stack | Core Java, plain OOP, no framework | Assessment weights process over product size; a framework would add setup noise without demonstrating more Agile/DevOps practice. |
| Persistence | In-memory `Map` only | Explicitly requested; keeps the demo runnable with zero external setup. |
| Build tool | None — plain `javac` + JUnit Platform Console Standalone jar | Consistent with "core Java basics"; no Maven/Gradle installed locally, and a build tool isn't needed for a project this size. |
| CI/CD | GitHub Actions | Native to where the repo is hosted; free, and supports the required auto-merge-on-green-check workflow with no extra infrastructure. |
| Delivery workflow | One feature branch per backlog item → PR → required CI check → GitHub auto-merge | Directly satisfies the brief's "commit history must show iterative progress" and "functional CI/CD pipeline" criteria. |

## Sprint Overview

| Sprint | Goal | Stories | Points |
|--------|------|---------|-------:|
| Sprint 0 | Plan the backlog, scaffold the codebase, stand up CI | (planning + scaffold, no PBIs) | — |
| Sprint 1 | Deliver a working Create + Read slice | PBI-1, PBI-2, PBI-3 | 7 |
| Sprint 2 | Complete CRUD and add monitoring/logging | PBI-4, PBI-5, PBI-7, PBI-8 | 8 |

Full backlog, priorities, and acceptance criteria: [backlog.md](backlog.md).
Effort estimates and the estimation formula: [estimation.md](estimation.md).
Codebase layout and layering rationale: [project-structure.md](project-structure.md).
Per-sprint detail: [sprint-1-plan.md](sprint-1-plan.md), [sprint-2-plan.md](sprint-2-plan.md)
(and their matching `-review.md` / `-retro.md` files, added as each sprint completes).

## Estimation Method (summary)

Every backlog item and setup task got a three-point time estimate (Optimistic / Most Likely /
Pessimistic), combined with the PERT formula `E = (O + 4M + P) / 6` for a realistic expected
duration, while the sum of every Pessimistic estimate gives the longest the whole project could
take in the worst case. Full breakdown, per-file LOC/method counts, and the numbers themselves:
[estimation.md](estimation.md).

## Definition of Done

See [backlog.md](backlog.md#definition-of-done) — applies uniformly to every backlog item:
own feature branch, tests passing locally, PR with a green required CI check, auto-merge (no
manual click), docs updated if scope changed.

## Delivery Discipline

Every backlog item — including Sprint 0's own scaffold — lands through a pull request gated by
the same CI check (`build-and-test`). Nothing is pushed directly to `main`. This is what
produces the iterative, reviewable commit history the brief's "Delivery Discipline" criterion
grades.
