# Reflection — Lab 3

**What was the biggest challenge in using AI for data analysis?** Getting past answers that were
technically true but useless. The first pass gave a correct overall mean and a vague "ridership
varies" comment — nothing an operations team could act on. The real work was naming the specific
comparisons the rubric needed (day of week, month, delays vs. ticket sales) instead of accepting a
generic summary as "insights."

**How did I ensure the AI's insights were accurate and not hallucinations?** Every number in the
report — monthly averages, the 0.06 delay/ticket-sales correlation, the 42-day count on the
1,500-rider floor — was recomputed independently from the raw CSV, not accepted from the model's
first description. The 1,500-floor claim was checked by counting every exact match across all 366
rows — a claim that surprising needed real proof, not a plausible sentence.

**How did your prompts evolve during this lab?** From "give me insights" (too vague to be wrong or
right), to naming exact computations, to asking for patterns *across* outliers instead of a flat
top/bottom list (which found the 1,500-floor anomaly), to splitting a conflated delay metric once a
follow-up revealed it hid two problems.
