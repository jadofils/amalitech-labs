# Prompt Log — Lab 2 (Technical Documentation)

Four iterations, each fixing a specific failure mode of the previous one. Model used: Claude
(Sonnet). Source grounded in [taskflow-source-materials.md](taskflow-source-materials.md) — the
actual current route code plus the scattered Slack/wiki notes describing it (not all of which
agree with the code).

## v1 — naive prompt

> "Write technical documentation for TaskFlow, a task management web app."

**Result:** Generic, structurally plausible documentation that repeated what a task-management
API "usually" looks like — a `/register` endpoint that doesn't exist in this app, task fields
(`priority`, `tags`) never mentioned anywhere in the source material, a made-up `/api/projects`
endpoint. No path, field, or status value matched TaskFlow's actual code. Unusable — it documents
an imaginary app, not this one.

**Diagnosis:** no source grounding. The prompt never pointed the model at
`taskflow-source-materials.md`, so it pattern-matched to the most common shape of "task app API"
from training data.

## v2 — add role + explicit source grounding

> "You are a backend engineer writing onboarding docs for a new developer joining TaskFlow. Read
> `taskflow-source-materials.md` in full before writing anything — the 'Actual current code'
> section is ground truth; the Slack/wiki notes are what the team *believes*, which may not
> match. Document only endpoints, fields, and status values that actually appear in the code."

**Result:** Much closer — correct paths (`/api/auth/login`, `/api/tasks`, `/api/tasks/:id`),
correct HTTP methods, correct status codes. But the output still repeated the wiki's claim that
`DELETE /api/tasks/:id` "returns the deleted object" and the Slack note's claim about a `token`
field — it read both the code and the notes, but treated them as equally authoritative instead of
checking one against the other.

**Diagnosis:** grounding fixed the hallucinated-features problem, but a passive "read the
material" instruction doesn't produce a *critical* read — it produces a faithful paraphrase of
whichever source it saw last, including faithfully repeating the notes' wrong claims.

## v3 — add a verification/chain-of-thought instruction

> "Same as above, but: for every claim in the Slack/wiki notes, check it line-by-line against the
> 'Actual current code' section before writing it down. Where they disagree, trust the code, and
> add the disagreement to an explicit 'Known Issues / Outdated Docs' list instead of silently
> picking one version."

**Result:** This is the pass that surfaced the four real discrepancies now documented under
"Known Issues" in [API_DOCUMENTATION.md](API_DOCUMENTATION.md):
- The login response field is `accessToken`, not `token` as the onboarding Slack thread claims.
- `requireAuth` middleware applies to every `/api/tasks` route — there is no way to "hit
  `/api/tasks` without auth first," contrary to the same Slack thread's advice.
- `TASK_STATUSES` only defines `todo`, `in_progress`, `done` — the wiki's four-state lifecycle
  (including `blocked`) doesn't exist in code.
- `DELETE /api/tasks/:id` returns `204 No Content` with an empty body — the wiki's claim that it
  "returns the deleted object" is wrong.

## v4 — final: add an explicit output schema + chaining for tone

> "Structure the final document as three sections in one file: (1) a 'Getting Started' guide for
> a new developer — no jargon, task-oriented, covers logging in and creating/updating/deleting a
> task end-to-end; (2) an API reference — one subsection per endpoint with method, path, request
> body, and every response code the code actually returns; (3) a 'Troubleshooting' section built
> from the Known Issues list plus the undocumented rate limiter — what error a developer will
> actually see and why. Then take all three sections and rewrite them for one consistent tone and
> heading level (H2 sections, H3 subsections) — right now they read like they were written by three
> different people, because they were three different prompts."

**Result:** [API_DOCUMENTATION.md](API_DOCUMENTATION.md) — the schema constraint is what produced
all three deliverable sections the rubric requires in one pass instead of an API-reference-only
document, and the final chaining step is what made the Getting Started guide's tone match the more
formal API reference instead of reading like two unrelated documents stapled together.

## Techniques demonstrated

| Technique | Where |
|---|---|
| Role prompting | v2 |
| Explicit source grounding (read the material, don't summarize memory) | v2 |
| Chain-of-thought / verification instruction (check notes against code) | v3 |
| Constraint against a known failure mode (don't trust the notes) | v3 |
| Output schema constraint (three required sections, one file) | v4 |
| Chaining for consistent style across independently-generated sections | v4 |
| Iterative refinement from a diagnosed failure, not a guess | v1→v2→v3→v4 |
