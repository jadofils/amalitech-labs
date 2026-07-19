# Lab 3: AI-Driven Data Insights Report

**Scenario:** UrbanTransit, a mid-sized city's public transportation authority (per the official
lab brief) — a year of daily ridership, ticket sales, and delay data, needing a plain-English
report for a non-technical operations team.

## Dataset

[dataset/urban-transit-ridership-2024.csv](dataset/urban-transit-ridership-2024.csv) — the actual
dataset provided for this lab (366 days, January–December 2024: daily ridership, ticket sales,
delay count, average delay length in minutes).

## Task

Explore the data for peak/seasonal ridership patterns, check whether delays correlate with ticket
sales, surface any anomalies a quick summary would miss, and turn all of it into 3–5 actionable
recommendations a non-technical operations team can actually use.

## Approach

1. Started broad ("give me insights") and got back a technically-correct but useless summary — no
   connection to the specific comparisons (day of week, month, delay/ticket-sales correlation) the
   brief actually asks for.
2. Named those comparisons explicitly (see [prompts.md](prompts.md)), which surfaced the real
   seasonal pattern and a genuine null result: delays and ticket sales don't correlate (0.06).
3. Asked for patterns *across* outliers rather than a flat top/bottom list — this is what found
   the report's headline anomaly: 42 separate days between August and November sitting at the
   *exact same* ridership value (1,500), which no other value in the dataset repeats more than
   twice.
4. Verified every reported number independently against the raw CSV (means, standard deviations,
   correlation, and an exact-match count for the 1,500-floor claim) rather than trusting the
   model's first read of the data — see "How accuracy was checked" in [prompts.md](prompts.md).

## Deliverable

→ [DATA_INSIGHTS_REPORT.md](DATA_INSIGHTS_REPORT.md)
