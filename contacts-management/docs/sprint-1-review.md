# Sprint 1 Review

## Sprint Goal (recap)

Deliver a working, testable Create + Read slice: add a contact and immediately see it, both
individually and in the full list.

## What was delivered

All 3 planned stories (7 points) shipped, each on its own branch, each auto-merged into `main`
only after its GitHub Actions `build-and-test` check passed:

| Story | PR | Merged |
|-------|----|--------|
| Sprint 0 scaffold (prerequisite) | [#1](https://github.com/jadofils/users-contacts-management-system/pull/1) | 2026-07-05 08:17 UTC |
| PBI-1 — Add a new contact | [#2](https://github.com/jadofils/users-contacts-management-system/pull/2) | 2026-07-05 08:21 UTC |
| PBI-2 — List all contacts | [#3](https://github.com/jadofils/users-contacts-management-system/pull/3) | 2026-07-05 08:30 UTC |
| PBI-3 — Get a contact by ID | [#4](https://github.com/jadofils/users-contacts-management-system/pull/4) | 2026-07-05 08:34 UTC |

Every acceptance criterion in [backlog.md](backlog.md#acceptance-criteria) for PBI-1 through
PBI-3 is covered by a passing JUnit test; the suite grew from 10 tests (Sprint 0 scaffold) to
24 tests by the end of Sprint 1, all green.

## Demo

```bash
javac -d out $(find src -name "*.java")
java -cp out Main
```

```
===== Contacts Management Menu =====
1. Add Contact
2. List All Contacts
3. Get Contact by ID
4. Exit
Choose an option: 1
Enter name: Ada Lovelace
Enter email: ada@example.com
Enter phone: 555-0100

✓ Contact added successfully!
Contact{id='C001', name='Ada Lovelace', email='ada@example.com', phone='555-0100'}
```

Choosing option 2 immediately after lists that same contact; choosing option 3 and entering
`C001` retrieves it individually; entering an unknown ID (e.g. `C999`) prints a clear
"Contact with ID C999 not found." error instead of crashing — satisfying PBI-3's negative
acceptance criterion.

## What went well

- The one-story-per-branch, CI-gated auto-merge workflow worked exactly as planned on the
  first try (no manual merge clicks across 4 PRs).
- Stubbing `ContactServiceImpl` with `UnsupportedOperationException` in Sprint 0, then
  "de-stubbing" one method per story, kept every PR's diff small and focused — each one only
  touched the single method (plus its menu case and tests) that story owned.
- Estimates were close: PBI-1–3 combined estimate was 3.74h (PERT) / 5.9h (worst case); actual
  effort tracked closely with the "Most Likely" column, not the pessimistic one.

## What didn't go well / friction points

- `scripts/test.sh` initially failed twice before it worked: once because `--fail-if-no-tests`
  correctly failed the Sprint 0 PR when no tests existed yet, and once because `javac` wasn't
  given the JUnit jar on its classpath (only the runtime step had it). Both were caught locally
  before pushing, but they cost avoidable iteration.
- The editor (VS Code) flagged every test file as "unresolved import" because nothing told it
  where the JUnit jar lives — not caught until after PBI-1 was already open as a PR.
- The GitHub CLI token was missing the `workflow` scope by default, which would have blocked
  pushing `.github/workflows/ci.yml` had it not been caught proactively.

## Retrospective — improvements for Sprint 2

See [sprint-1-retro.md](sprint-1-retro.md) for the full retrospective; the two headline
improvements carried into [sprint-2-plan.md](sprint-2-plan.md#retro-adjustments) are:

1. Write the JUnit-jar-aware `.vscode/settings.json` and verify `scripts/test.sh` end-to-end
   *before* the first feature branch of a sprint, not reactively after hitting friction.
2. Add structured logging (PBI-8) earlier in Sprint 2's task order so later stories in the same
   sprint can already rely on it while being implemented, rather than bolting it on last.
