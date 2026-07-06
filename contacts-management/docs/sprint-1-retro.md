# Sprint 1 Retrospective

## What went well

- **The CI-gated auto-merge workflow just worked.** All 4 PRs (Sprint 0 scaffold + PBI-1/2/3)
  passed their `build-and-test` check and merged into `main` without a single manual click,
  validating the branch-protection + auto-merge setup done once at the start of the project.
- **Small, single-responsibility PRs.** Stubbing every `ContactServiceImpl` method to throw
  `UnsupportedOperationException` in Sprint 0, then having each story "de-stub" exactly one
  method, kept every diff reviewable at a glance — nothing in a PBI-1 PR ever touched
  PBI-2/3 code.
- **Estimates held up.** The Sprint 1 PERT estimate (3.74h expected / 5.9h worst case) tracked
  close to actual effort; no story ran into its pessimistic case.

## What didn't go well

- **Tooling friction discovered late, not early.** `scripts/test.sh` needed two rounds of
  fixes after the first real run: `--fail-if-no-tests` correctly caught an empty Sprint-0 test
  suite (good — but should have been anticipated by writing at least one test *with* the
  scaffold, not after), and `javac` wasn't given the JUnit jar on its own classpath (only
  `java` was), so compilation of the very first test files failed outright.
- **IDE not wired up before opening the first PR.** VS Code flagged every JUnit import as
  unresolved because nothing told its Java tooling where the jar lives; this was only noticed
  and fixed (via `.vscode/settings.json`) partway through PBI-2, after already living with red
  squiggles through all of PBI-1.
- **A permissions gap almost blocked the pipeline before it started.** The GitHub CLI's
  default OAuth scopes didn't include `workflow`, which is required to push
  `.github/workflows/*.yml` — caught by checking `gh auth status` proactively, but it's the
  kind of thing that should be checked as a matter of course, not luck.

## Improvements for Sprint 2 (applied — see sprint-2-plan.md)

1. **Verify the full toolchain before writing any story code.** At the start of Sprint 2,
   confirm `scripts/test.sh` runs clean (compiles + at least one passing test) and that
   `.vscode/settings.json` resolves cleanly in the editor, before opening `feature/update-contact`
   — rather than discovering gaps mid-sprint.
2. **Pull logging earlier in the task order.** PBI-8 (structured logging) was planned last in
   Sprint 2's story list; moving it earlier means PBI-4 and PBI-5 can be implemented with
   logging calls already in place from the start, instead of retrofitting log statements into
   already-merged methods.
3. *(Minor, tracked but not a hard gate)* Keep an eye on whether PBI-6 (duplicate-email
   rejection) is worth pulling into a Sprint 3 if time allows — it remains the one backlog item
   with no scheduled sprint.
