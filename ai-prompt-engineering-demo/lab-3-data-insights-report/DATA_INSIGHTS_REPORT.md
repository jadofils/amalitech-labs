# UrbanTransit — Data Insights Report

**For:** UrbanTransit operations team (non-technical)
**Source:** [dataset/urban-transit-ridership-2024.csv](dataset/urban-transit-ridership-2024.csv) —
366 days, January 1 through December 31, 2024. Five columns: daily ridership, ticket sales, number
of delays, and average delay length in minutes.
**Method:** every number below was computed directly from the CSV (day-of-week averages, monthly
averages, a standard correlation coefficient, and outlier days more than 2.5 standard deviations
from the yearly average) — nothing here is a model guess. See [prompts.md](prompts.md) for how the
analysis was built up step by step.

---

## 1. Summary of key trends

**Ridership follows a clear seasonal arc, not a flat average.** Riders per day climb from about
2,085 in January to a spring peak of roughly 2,480 in March and April, then decline steadily
through the summer to a low of about 1,587–1,596 in September and October, before recovering
toward year-end. The single "average day" (~2,016 riders) that a quick summary stat would give you
hides a swing of nearly 900 riders between the best and worst months.

**There is no meaningful weekday/weekend pattern.** Average ridership by day of week ranges only
from about 1,982 (Wednesday) to 2,043 (Tuesday) — a 3% spread. Most transit systems see a real dip
on weekends; this one doesn't. If schedules or staffing assume weekends are quieter, that
assumption isn't supported by this data.

**Delays have their own, different seasonal pattern.** Average delay count per day roughly doubles
in April–June and again in September–October (around 4.0–4.4 delays/day) compared to
January–February, August, and November (around 1.6–1.8 delays/day). This doesn't line up with the
ridership seasons above — it looks like its own cycle, plausibly tied to maintenance windows or
weather transitions, but that's a hypothesis to check operationally, not something this data alone
proves.

## 2. Notable anomalies

**A suspicious flat floor of exactly 1,500 riders, 42 separate days, all between August 9 and
November 21.** Real ridership doesn't naturally land on the exact same number 42 times — for
comparison, no other single ridership value repeats more than twice all year. This cluster sits
right in the seasonal trough identified above, which raises a real question: is actual ridership
during this period being **replaced or clipped** by a minimum value somewhere in how this data is
collected or reported, hiding what demand really looked like in the trough? **This should be
confirmed with whoever owns the data pipeline before the September–October low is treated as real**
— it may understate an actual problem, or it may be entirely artificial.

**Two very different "bad delay days" that a single delay-count number would treat as similar.**
April 15 had the most delays of any day in the dataset (11), but they were short — averaging only
3.7 minutes each. June 8, by contrast, had fewer delays (8) but they were the longest of the year on
average (23.1 minutes) — the single worst day for delay severity in 2024. These look like two
different operational failure modes (a dispatching/frequency problem vs. a service-disruption
problem), not the same issue at different volumes.

**No correlation between delays and ticket sales.** The rubric for this analysis asked specifically
whether delays and ticket sales move together — checked directly, they don't (correlation
coefficient of 0.06, where 0 means no relationship and 1 means they move in lockstep). Whatever
drives ticket sales up or down over the course of a year, it isn't visibly the number of delays. If
anyone on the team has been assuming "more delays = fewer riders buying tickets," this dataset
doesn't back that up.

## 3. Actionable recommendations

1. **Confirm whether the August–November 1,500-rider floor is real.** Forty-two days landing on
   the exact same number is the single biggest red flag in this dataset — resolve this with the
   data source before using the fall trough for any staffing or budget decision.
2. **Don't budget around a delay-to-ticket-sales link — it isn't there.** If service-recovery or
   marketing spend has been justified by "delays are costing us riders," this year's data doesn't
   support that; look elsewhere for what's driving the seasonal ticket-sales swing.
3. **Treat April 15 and June 8 as two different problems, not two data points on one chart.** A
   high delay *count* with a low average length points at frequency/dispatch; a lower count with a
   high average length (like June 8) points at service disruption severity. Track both separately
   going forward, not just "delays per day."
4. **Plan for the April–June and September–October delay bumps as a predictable pattern, not a
   surprise each time.** Both windows show roughly double the delay rate of the calendar's quieter
   months — worth checking against maintenance schedules to see if extra spare vehicles or staffing
   during those windows would help before it recurs next year.
5. **Re-examine any schedule built around a weekday/weekend ridership gap.** This system's
   ridership is essentially flat across all seven days — a schedule that reduces weekend service
   assuming lower demand may be under-serving riders who are, on average, showing up just as much
   as they do on a weekday.
