# Test Suite

JUnit 5 (Jupiter) + Mockito tests for the backend. `pom.xml` at the project
root now manages these dependencies (`mvn test` is the simplest way to run
the suite - see below); the plain-jar setup further down still works too and
is what CI used before the Maven build existed.

## Layout and naming convention

Each class under test has (at most) two files, mirroring the source package
under `tests/`:

- `<Class>Test.java` - behavioural tests using real collaborators (or, for the
  service/manager layers, a hand-wired real dependency chain).
- `<Class>MockitoTest.java` - the same class verified through mocked
  collaborators: interaction/order verification (`verify`, `InOrder`),
  short-circuit checks (`verify(x, never())...`), and edge cases (e.g. a
  repository returning `null`) that the real implementation can never
  actually produce but that defensive code still guards against.

A few classes are the exception to the two-file rule, because they have no
collaborators to mock in the first place - each just verifies a pure
function or a mapping over a real domain object, so no `*MockitoTest`
exists for it:

- `LetterGradeTest`, `GpaLetterGradeTest` - pure enum lookup functions
- `InputSanitizerTest`, `DateFormatsTest` - pure static utility functions
- `StudentMapperTest`, `GradeMapperTest` - pure mapping functions over a real
  domain object (nothing to mock; there's no branching to verify in isolation)
- `FileExporterTest` - wraps `java.io.FileWriter` directly, with no
  injected collaborator of its own to mock

## A note on shared static counters

`Student.studentCounter` and `Grade.gradeCounter` are `private static` fields,
so they are shared across every test in the same JVM run - not reset between
test classes. Tests in this suite never assert an exact ID (`"STU001"`,
`"GRD001"`); they assert the ID *format* (regex) and, where uniqueness
matters, compare freshly-generated IDs against each other rather than against
a literal.

## Running

### Maven

```
mvn test
```

That's it - `pom.xml` pulls JUnit 5 and Mockito from Maven Central, compiles
`src` (excluding `tests/**`) as main code and all of `src` as test code, and
runs everything via Surefire. This is also what CI runs on every push to a
`feature/**` branch and on PRs into `develop`/`main`
(`.github/workflows/ci.yml`, job `build-and-test-student-grade-management`).

### IntelliJ

The required libraries (`junit-platform-console-standalone`, `mockito-core`,
`mockito-junit-jupiter`, `byte-buddy`, `byte-buddy-agent`, `objenesis`) are
already registered under `.idea/libraries` and attached to the module - just
run any test class or right-click `src/tests` -> Run.

### Command line

Compile `src` (excluding `tests`) and `src/tests` against the same jars, then
run with the JUnit Platform Console Launcher, e.g.:

```
javac -d out/main $(all files under src except src/tests)
javac -cp "out/main;<jars>" -d out/test $(all files under src/tests)
java -jar junit-platform-console-standalone-*.jar execute \
     -cp "out/main;out/test;<jars>" --scan-classpath --include-classname=".*Test"
```

where `<jars>` is `junit-platform-console-standalone`, `mockito-core`,
`mockito-junit-jupiter`, `byte-buddy`, `byte-buddy-agent`, and `objenesis`,
joined with `;` (Windows) or `:` (Unix).
