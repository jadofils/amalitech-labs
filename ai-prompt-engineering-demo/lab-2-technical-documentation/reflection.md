# Reflection — Lab 2

**What the prompting process got right:** the single highest-leverage change across all four
iterations wasn't a wording tweak, it was forcing source grounding before generation (v1 → v2).
Without it, the model produces a fluent, well-formatted, entirely wrong document, and nothing
about its tone signals that — a generic-but-confident wrong answer is more dangerous for
documentation than an obviously broken one, because it reads as trustworthy.

**What needed correction:** v2's "describe what the code does" instruction was still passive
enough that it inherited the project's own documentation errors instead of catching them. Telling
the model to actively distrust the existing docs and re-derive behavior from source (v3) is what
actually found the two real discrepancies (honors eligibility threshold, phone validation vs. seed
data) — those weren't things I already knew going in; they surfaced from the verification step
itself. That's the actual argument for "AI-assisted" documentation over "AI-generated": the value
isn't producing prose faster, it's using the read-everything capability to catch drift between
docs and implementation that a human skimming the README would reasonably miss.

**Implication for using this on a real backend:** an instruction to "document X" defaults to
summarizing what X *claims* to be. Getting documentation that's actually useful for onboarding
requires explicitly asking for adversarial verification against source — otherwise you've just
paid an AI to retype the README.
