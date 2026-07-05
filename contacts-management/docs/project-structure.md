# Project Structure

Core Java, plain OOP — no framework, no build tool. Compiled with `javac`, tested with the
JUnit Platform Console Standalone jar (see [plan.md](plan.md) for why).

```
users-contacts-management-system/
├── src/
│   ├── model/
│   │   └── Contact.java                    # id, name, email, phone — plain data holder
│   ├── exceptions/
│   │   ├── ContactNotFoundException.java   # unknown contact ID
│   │   └── ContactValidationException.java # bad input data
│   ├── repository/
│   │   ├── ContactRepository.java          # storage contract
│   │   └── InMemoryContactRepository.java  # Map<String, Contact>-backed implementation
│   ├── validation/
│   │   └── ContactValidator.java           # static name/email rules
│   ├── service/
│   │   ├── ContactService.java             # business-facing CRUD contract
│   │   └── ContactServiceImpl.java         # validates, calls the repository, logs, generates IDs
│   ├── monitoring/
│   │   └── HealthCheck.java                # status "UP" + current contact count
│   └── Main.java                           # console menu wiring every operation
├── test/                                    # mirrors src/, one *Test.java per class
├── scripts/
│   └── test.sh                             # download JUnit jar (if missing) → javac → run tests
├── lib/                                     # gitignored; JUnit console jar lands here
├── .github/workflows/ci.yml                # GitHub Actions: same steps as scripts/test.sh
└── docs/                                    # this folder
```

## Layering

`Main` → `ContactService` → `ContactRepository`

- **`Main`** only handles console I/O (prompt, print, catch-and-report exceptions). It never
  touches `Contact` fields or the repository directly.
- **`ContactService`** owns every business rule: it calls `ContactValidator` before writing
  anything, translates "not found" into `ContactNotFoundException`, generates each contact's
  ID, and logs every operation (PBI-8). This is the layer nearly all unit tests target, since
  it's where every acceptance criterion in [backlog.md](backlog.md) actually lives.
- **`ContactRepository` / `InMemoryContactRepository`** is a thin, dumb, fully-generic
  `Map<String, Contact>` store with no validation or business logic of its own — it is built
  once in Sprint 0 rather than incrementally, because none of the backlog's acceptance criteria
  are about storage itself.

This mirrors the layering already used in the sibling `student-management-system` project
(model / repository / service / validation), minus the database — everything here lives only
in a `Map` for the lifetime of the running process.

## Why no database

The assessment brief explicitly asks for an in-memory store, and it keeps the whole prototype
runnable with zero setup (`javac` + `java`, no credentials, no external service) — appropriate
for a two-sprint demo whose grading criteria are about Agile/DevOps process, not data
durability.

## Why no "health endpoint" over HTTP

There's no web server in this prototype (core Java console app only), so `HealthCheck` is
exposed as a console menu action instead of an HTTP endpoint — it still satisfies the brief's
"basic monitoring... such as a health endpoint" by reporting live status + contact count on
demand.
