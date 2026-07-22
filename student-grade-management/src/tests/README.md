# Test Suite

JUnit 5 (Jupiter) + Mockito tests for the backend. There is no Maven/Gradle in
this project (see `docs/PROJECT_GUIDE.md`), so dependencies are plain jars.

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

`LetterGradeTest` is the one exception - `LetterGrade.fromNumeric()` is a pure
static function with no collaborators, so there's nothing meaningful to mock
and no `LetterGradeMockitoTest` exists for it.

## A note on shared static counters

`Student.studentCounter` and `Grade.gradeCounter` are `private static` fields,
so they are shared across every test in the same JVM run - not reset between
test classes. Tests in this suite never assert an exact ID (`"STU001"`,
`"GRD001"`); they assert the ID *format* (regex) and, where uniqueness
matters, compare freshly-generated IDs against each other rather than against
a literal.

## Running

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
