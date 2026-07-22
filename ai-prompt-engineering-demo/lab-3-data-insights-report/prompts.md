# Prompt Log — Lab 3 (Data Insights Report)

Four iterations against [dataset/urban-transit-ridership-2024.csv](dataset/urban-transit-ridership-2024.csv)
(366 days, Jan–Dec 2024: ridership, ticket sales, delay count, average delay minutes). Model used:
Claude (Sonnet). Every number quoted in the results below was independently computed from the CSV,
not asserted by the model from a first read — see "How accuracy was checked" at the bottom.

## v1 — naive prompt

> "Give me insights from this transit ridership dataset."

**Result:** Generic descriptive stats — overall mean ridership (~2,016), min/max, a comment that
"ridership varies throughout the year." Technically true, operationally useless: it didn't say
*when* ridership was high or low, didn't touch the delay or ticket-sales columns at all, and gave
the operations team nothing to act on.

**Diagnosis:** an unscoped "give me insights" prompt defaults to the easiest possible summary
(overall mean/min/max) instead of the specific comparisons — by day of week, by month, between
columns — that the rubric actually asks for (peak days, seasonality, delay/ticket-sales
correlation).

## v2 — name the specific comparisons

> "Using this CSV: (1) compute average ridership by day of week and by month, (2) compute the
> correlation coefficient between Delay_Count and Ticket_Sales, (3) list the 5 highest and 5
> lowest ridership days with their dates. Show the actual numbers, not a qualitative summary."

**Result:** Surfaced the real seasonal pattern (spring peak around 2,480 in March/April, trough
around 1,590 in September/October) and a near-zero correlation (0.06) between delays and ticket
sales — a genuine null result, not a hallucinated relationship. But the "5 lowest ridership days"
list was five different dates that were uninteresting on their own — a flat list of numbers, no
pattern connecting them.

**Diagnosis:** naming the right comparisons fixed the vagueness problem, but a "list the top/bottom
N" instruction produces a list, not a finding — it doesn't ask the model to notice that several of
those low-ridership days shared something.

## v3 — ask for patterns *within* the outliers, not just the outliers

> "Look at the lowest-ridership days as a group, not one at a time — do they share a value, a date
> range, or anything unusual, rather than just being independently low? Also check whether
> Ticket_Sales and Daily_Ridership ever diverge significantly from each other on the same day."

**Result:** This is the pass that found the actual headline anomaly — 42 separate days, all
between August 9 and November 21, sitting at the *exact same* ridership value of 1,500. No other
value in the whole dataset repeats more than twice. That's not "a quiet season," that's a
suspiciously clean floor, and it changes the recommendation from "ridership is low in fall" to
"confirm this number is real before acting on it."

## v4 — final: separate two failure-mode anomalies the model had merged into one

> "You flagged high-delay-count days as one group. Split delay count and average delay length
> apart — find the day with the most delays, and separately the day with the longest average
> delay, and tell me if they're the same day or different days."

**Result:** They're different days. April 15 had the most delays all year (11) but they were
short (3.7 min average); June 8 had fewer delays (8) but the longest average delay of the year
(23.1 min) — two distinct operational problems a single "delay count" metric would have flattened
into one.

## How accuracy was checked (not just asked for)

Every figure in [DATA_INSIGHTS_REPORT.md](DATA_INSIGHTS_REPORT.md) — the monthly averages, the
day-of-week averages, the 0.06 correlation coefficient, the 42-day count on the 1,500 floor, the
specific April 15 / June 8 numbers — was recomputed independently against the raw CSV (mean,
standard deviation, and Pearson correlation by hand from the column values) rather than accepted
from the model's first description of the data. The 1,500-floor claim in particular was verified
by counting exact matches across all 366 rows, not estimated from a sample.

## Techniques demonstrated

| Technique | Where |
|---|---|
| Naming specific, decomposed sub-questions instead of "find insights" | v2 |
| Reporting a null result (no correlation) instead of forcing a pattern that isn't there | v2 |
| Asking for group-level patterns in outliers, not just a top/bottom list | v3 |
| Following up on a found anomaly to separate two conflated metrics | v4 |
| Independently recomputing every reported figure against the source data | throughout |
| Iterative refinement from a diagnosed gap, not a guess | v1→v2→v3→v4 |
