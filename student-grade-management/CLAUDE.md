# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Compile and run all tests
mvn test

# Build the JAR
mvn package

# Run all tests + generate JaCoCo coverage report (target/site/jacoco/)
mvn verify

# Run a single test class
mvn test -Dtest=tests.manager.StudentManagerTest

# Run a single test method
mvn test -Dtest=tests.manager.StudentManagerTest#addStudent_shouldPersistStudent

# Enable debug-level logging during a run
mvn test -Dlog.level=DEBUG

# Run the application
java -jar target/student-grade-management-2.0.0.jar
```

## Source Layout

All Java lives under a single `src/` root — Maven is configured with both `<sourceDirectory>` and `<testSourceDirectory>` pointing at `src`. There is **no** `src/main/java` / `src/test/java` split.

```
src/
  main/          # production code (package prefix: main.*)
    app/         # entry point (Main, ConsoleApp)
    calculators/ # GPACalculator, StatisticsCalculator + Calculable interface
    console/     # MenuAction implementations (one file per menu option)
    dto/         # StudentDTO, GradeDTO (mapper output)
    exceptions/  # custom exception hierarchy (root: ApplicationException)
    export/      # FileExporter, ReportGenerator + Exportable interface
    imports/     # BulkImportService, CSVParser
    logging/     # Logger (writes to System.err, not System.out)
    manager/     # StudentManager, GradeManager, StudentSearcher + Searchable
    mapper/      # StudentMapper, GradeMapper
    model/       # Student, Grade, Subject hierarchies + enums
    repository/  # in-memory array-backed repositories (interfaces + Impl)
    service/     # StudentService, GradeService (interfaces + Impl)
    utils/       # InputSanitizer, DateFormats, validators/
  tests/         # test code (package prefix: tests.*)
```

The Maven compiler `<excludes>` block strips `tests/**` from the main compile so test-scope dependencies (JUnit, Mockito) never reach production code.

## Architecture

`Main.java` is the composition root — it builds every dependency and wires them together, then hands the assembled `List<MenuAction>` to `ConsoleApp`. Nothing else does construction.

**Layer flow:**

```
ConsoleApp  →  MenuAction implementations  →  Manager layer
                                               ↓
                                           Service layer  →  Repository layer
```

- **Console layer** (`main.console`): Each menu option is a `MenuAction` implementation. `ConsoleApp` dispatches by option number and catches all `ApplicationException` subclasses, printing them as user-facing errors. `System.out` is the UI; `Logger` writes diagnostics to `System.err`.
- **Manager layer** (`main.manager`): `StudentManager` and `GradeManager` are the primary business-logic facades used by console actions. `StudentManager.hydrateGrades()` re-populates a student's transient grade list from `GradeManager` on every load, because grades live in a separate in-memory store.
- **Service layer** (`main.service`): Validates inputs via `StudentValidator`/`SubjectValidator`, delegates storage to repositories, and emits log events.
- **Repository layer** (`main.repository`): In-memory fixed-size arrays (`Student[50]`, `Grade[200]`). `StudentRepositoryImpl` seeds 5 students on construction (3 Regular, 2 Honors). There is no database.

**Key design points:**
- `Student` and `Grade` use `private static int` counters for ID generation (`STU001…`, `GRD001…`). These counters are shared across the JVM — `StudentManager` calls `Student.initializeCounter()` at startup to keep them ahead of stored IDs after a restart.
- `MenuAction.isAuthorizedFor(Role)` gates actions when role-based mode is enabled at startup. Teachers get full access; Students get read-only access.
- `BulkImportService` reads CSVs from the `imports/` directory; exported reports go to `reports/`.
- `Logger` level is controlled by the `-Dlog.level=DEBUG|INFO|WARN|ERROR` JVM property (default: `INFO`).

## Test Conventions

Each class under `src/tests/` mirrors the source package and follows a two-file pattern:

- `<Class>Test.java` — behavioural tests with real collaborators
- `<Class>MockitoTest.java` — interaction/isolation tests with mocked collaborators

**Never assert exact generated IDs** (`"STU001"`, `"GRD001"`). The static counters are not reset between test classes in the same JVM run. Assert ID format with a regex (e.g., `matches("STU\\d{3}")`) or compare two freshly generated IDs against each other for uniqueness. This is documented in `src/tests/README.md`.

Surefire picks up all files matching `**/*Test.java`, so both naming variants are discovered automatically.

## SonarQube

Sonar config lives in `pom.xml` properties. `java:S106` (System.out usage) is suppressed for `src/main/console/**` and `src/main/app/**` — `System.out` is the intentional UI output channel in those packages and routing it through `Logger` would corrupt the menu rendering.