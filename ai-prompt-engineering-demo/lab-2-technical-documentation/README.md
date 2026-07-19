# Lab 2: AI-Assisted Technical Documentation

**Scenario:** TaskFlow, a fictional task management web app (per the official lab brief) — core
functionality complete, documentation scattered across old Slack threads and a wiki nobody's
updated in months. [taskflow-source-materials.md](taskflow-source-materials.md) is that starting
point: the actual current route code, plus the team's own notes describing it, deliberately not
all in agreement with each other.

## Task

Produce a single documentation file for a new developer covering three things the brief requires:
a non-technical "Getting Started" guide, a full API reference, and a Troubleshooting section — and
make sure none of it repeats the team's outdated claims about how the API behaves.

## Approach

1. Wrote [taskflow-source-materials.md](taskflow-source-materials.md) as the ground truth to
   prompt against — real route code plus scattered notes, with four deliberate discrepancies
   between them (a wrong field name, wrong auth advice, an unimplemented status value, a wrong
   response shape) so the "fact-check, don't just paraphrase" technique has something real to
   catch.
2. Prompted iteratively (see [prompts.md](prompts.md)): a naive first pass invented an imaginary
   API; adding role framing and source grounding fixed the hallucinated endpoints but still
   inherited the notes' wrong claims; adding an explicit verification instruction (check every
   note against the code, flag disagreements) is what surfaced all four discrepancies; a final
   output-schema instruction produced the three required sections in one consistently-toned
   document.
3. Those four discrepancies are kept visible in the deliverable's "Known Issues" section and
   referenced directly from the Troubleshooting section, rather than silently corrected — the
   point of the exercise is catching drift between what a team believes and what the code actually
   does, not hiding it.

## Deliverable

→ [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
