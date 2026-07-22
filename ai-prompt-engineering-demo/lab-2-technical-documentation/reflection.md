# Reflection — Lab 2

**What was the hardest part of this task?** Fact-checking, not tone. Once the model had the
source material, the API reference itself came out clean on the first try — endpoints, fields,
and status codes matched the code exactly. The hard part was catching where the team's own
Slack/wiki notes disagreed with that code (the login field name, the "test without auth" advice,
the fourth task status, the `DELETE` response shape). A passive "document this" prompt happily
repeated all four wrong claims as fact, because they read like plausible documentation, not like
errors.

**How did iterative prompting change the quality of the documentation?** v1, ungrounded, invented
an entire imaginary API. v2 grounded it in the source material but still trusted the notes as much
as the code, so it inherited the four inaccuracies. Only v3 — instructing the model to check every
note against the code line-by-line and flag disagreements instead of silently picking one — turned
those four inaccuracies into a named "Known Issues" list anchoring the Troubleshooting section.
v4's schema instruction got all three required sections into one consistently-toned document
instead of three separately-styled fragments.
