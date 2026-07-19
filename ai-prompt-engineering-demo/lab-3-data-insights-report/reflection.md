# Reflection — Lab 3

**What the prompting process got right:** insisting the analysis run through the app's actual
business rules (§passing thresholds, honors eligibility, core/elective split) instead of generic
`mean`/`min`/`max` is what made this report specific to *this* system rather than a template that
would fit any grades CSV. A "data insights" ask defaults to spreadsheet-style descriptive stats
unless told which numbers the domain actually cares about.

**What needed correction:** v2's instruction to use "the app's own rules" quietly assumed there was
one unambiguous rule to use for honors eligibility, when Lab 2 had already shown the code and the
docs disagree on it. The model didn't flag that ambiguity on its own — it picked the documented
85% rule and moved on. Only forcing an explicit both-ways comparison in v3 turned that into a
concrete finding (Bob Smith's eligibility literally flips) instead of a footnote.

**Implication for using this on real data:** analytical prompts inherit whatever silent
assumptions the underlying system has, unless a prior step (here, Lab 2's documentation pass) has
already made those assumptions visible enough to name in the prompt. Data insight work and
technical documentation work aren't separable tasks on a codebase like this one — the insights
report only found something real because it was pointed at a discrepancy the documentation lab had
already surfaced.
