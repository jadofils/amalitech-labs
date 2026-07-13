# Student Grade Management System

**Complexity:** Medium

**Time Estimate:** 6-7 hours

---

## Objectives

By completing this project, you will be able to:

1. **Create robust, production-ready Java applications** that implement comprehensive exception handling strategies, unit tests with JUnit and Mockito, and follow SOLID principles for maintainability
2. **Apply Git workflows and branching strategies** to collaborate effectively on team projects, managing feature development, code integration, and version history with proper commit practices
3. **Evaluate code quality** by analyzing adherence to clean code principles, assessing test coverage adequacy, and using static analysis tools to identify areas for improvement
4. **Analyze complex bugs** using IDE debugging tools combined with systematic testing approaches to identify root causes and implement appropriate fixes
5. **Create comprehensive test suites** that effectively isolate dependencies through mocking, validate business logic through unit tests, and can be integrated into automated build and CI/CD pipelines

---

## What You'll Build

An **enhanced** Student Grade Management System with:

### New Features (Building on Lab 1)

- **Export Grade Report** - Save student reports to text files
- **Calculate GPA** - Convert percentage grades to 4.0 GPA scale
- **Bulk Grade Import** - Load multiple grades from CSV file
- **Grade Statistics** - View class-wide analytics (highest, lowest, median, standard deviation)
- **Student Search** - Find students by name or ID with partial matching

### Quality Improvements

- **Exception Handling** - Custom exceptions for all error scenarios
- **Unit Tests** - Comprehensive JUnit tests with 80%+ coverage
- **SOLID Principles** - Refactored code following SOLID
- **Git Workflow** - Feature branch workflow with proper commits
- **Input Validation** - Robust validation with helpful error messages
- **Logging** - Application-wide logging for debugging

---

## Console Output Examples

### Screenshot 1: Enhanced Main Menu

```
╔════════════════════════════════════════════╗
║   STUDENT GRADE MANAGEMENT - MAIN MENU    ║
╚════════════════════════════════════════════╝

1. Add Student
2. View Students
3. Record Grade
4. View Grade Report
5. Export Grade Report      [NEW]
6. Calculate Student GPA    [NEW]
7. Bulk Import Grades       [NEW]
8. View Class Statistics    [NEW]
9. Search Students          [NEW]
10. Exit

Enter choice: _
```

### Screenshot 2: Export Grade Report

```
Enter choice: 5

EXPORT GRADE REPORT
─────────────────────────────────────────────

Enter Student ID: STU001

Student: STU001 - Alice Johnson
Type: Regular Student
Total Grades: 5

Export options:
1. Summary Report (overview only)
2. Detailed Report (all grades)
3. Both

Select option (1-3): 2

Enter filename (without extension): alice_johnson_report

✓ Report exported successfully!
  File: alice_johnson_report.txt
  Location: ./reports/
  Size: 2.4 KB
  Contains: 5 grades, averages, performance summary

Press Enter to continue...
```

### Screenshot 3: Calculate Student GPA

```
Enter choice: 6

CALCULATE STUDENT GPA
─────────────────────────────────────────────

Enter Student ID: STU002

Student: STU002 - Bob Smith
Type: Honors Student
Overall Average: 87.4%

GPA CALCULATION (4.0 Scale)
─────────────────────────────────────────────
Subject      | Grade  | GPA Points
─────────────────────────────────────────────
Mathematics  | 92%    | 4.0 (A)
English      | 85%    | 3.7 (A-)
Science      | 88%    | 3.7 (A-)
Music        | 91%    | 4.0 (A)
Art          | 81%    | 3.3 (B+)
─────────────────────────────────────────────

Cumulative GPA: 3.74 / 4.0
Letter Grade: A-
Class Rank: 2 of 5

Performance Analysis:
✓ Excellent performance (3.5+ GPA)
✓ Honors eligibility maintained
✓ Above class average (3.12 GPA)

Press Enter to continue...
```

### Screenshot 4: Bulk Import Grades

```
Enter choice: 7

BULK IMPORT GRADES
─────────────────────────────────────────────

Place your CSV file in: ./imports/

CSV Format Required:
StudentID,SubjectName,SubjectType,Grade
Example: STU001,Mathematics,Core,85

Enter filename (without extension): october_grades

Validating file... ✓
Processing grades...

IMPORT SUMMARY
─────────────────────────────────────────────
Total Rows: 15
Successfully Imported: 13
Failed: 2

Failed Records:
  Row 7: Invalid student ID (STU999)
  Row 12: Grade out of range (105)

✓ Import completed!
  13 grades added to system
  See import_log_20251103.txt for details

Press Enter to continue...
```

### Screenshot 5: View Class Statistics

```
Enter choice: 8

CLASS STATISTICS
─────────────────────────────────────────────

Total Students: 5
Total Grades Recorded: 47

GRADE DISTRIBUTION
─────────────────────────────────────────────
90-100% (A): ████████████████░░░░ 42.6% (20 grades)
80-89%  (B): ██████████████░░░░░░ 36.2% (17 grades)
70-79%  (C): ██████░░░░░░░░░░░░░░ 14.9% (7 grades)
60-69%  (D): ██░░░░░░░░░░░░░░░░░░  4.3% (2 grades)
0-59%   (F): █░░░░░░░░░░░░░░░░░░░  2.1% (1 grade)

STATISTICAL ANALYSIS
─────────────────────────────────────────────
Mean (Average):      81.4%
Median:              83.0%
Mode:                85.0%
Standard Deviation:  10.2%
Range:               58.0% (42% - 100%)

Highest Grade:  100% (STU004 - Science)
Lowest Grade:    42% (STU003 - Mathematics)

SUBJECT PERFORMANCE
─────────────────────────────────────────────
Core Subjects:     83.2% average
  Mathematics:     79.5%
  English:         84.1%
  Science:         86.0%

Elective Subjects: 78.9% average
  Music:           82.3%
  Art:             77.8%
  Physical Ed:     76.5%

STUDENT TYPE COMPARISON
─────────────────────────────────────────────
Regular Students:  75.8% average (3 students)
Honors Students:   89.1% average (2 students)

Press Enter to continue...
```

### Screenshot 6: Search Students

```
Enter choice: 9

SEARCH STUDENTS
─────────────────────────────────────────────

Search options:
1. By Student ID
2. By Name (partial match)
3. By Grade Range
4. By Student Type

Select option (1-4): 2

Enter name (partial or full): John

SEARCH RESULTS (2 found)
─────────────────────────────────────────────
STU ID  | NAME            | TYPE    | AVG
─────────────────────────────────────────────
STU001  | Alice Johnson   | Regular | 81.2%
STU002  | Bob Johnson     | Honors  | 87.4%
─────────────────────────────────────────────

Actions:
1. View full details for a student
2. Export search results
3. New search
4. Return to main menu

Enter choice: _
```

### Screenshot 7: Exception Handling Example

```
Enter choice: 3

RECORD GRADE
─────────────────────────────────────────────

Enter Student ID: STU999

✗ ERROR: StudentNotFoundException
  Student with ID 'STU999' not found in the system.

  Available student IDs: STU001, STU002, STU003, STU004, STU005

  Try again? (Y/N): Y

Enter Student ID: STU001

Student Details:
  Name: Alice Johnson
  Type: Regular Student
  Current Average: 81.2%

Subject type:
1. Core Subject
2. Elective Subject

Select type (1-2): 1

Available Core Subjects:
1. Mathematics
2. English
3. Science

Select subject (1-3): 1

Enter grade (0-100): 150

✗ ERROR: InvalidGradeException
  Grade must be between 0 and 100.
  You entered: 150

  Try again? (Y/N): Y

Enter grade (0-100): 95

✓ Grade recorded successfully!
```

---

## User Stories

### US-1: Enhanced Exception Handling (NEW)

**As a** developer
**I want to** handle all errors gracefully
**So that** the application never crashes unexpectedly

**Acceptance Criteria:**

- Custom exceptions for all error scenarios
- Informative error messages with recovery suggestions
- No use of generic Exception catching
- All exceptions logged with timestamps
- Input validation prevents most exceptions before they occur

---

### US-2: Export Grade Report (NEW)

**As a** teacher
**I want to** export grade reports to files
**So that** I can share them with students and parents

**Acceptance Criteria:**

- Export summary or detailed reports
- Save to text files in `reports/` directory
- Include student info, all grades, averages, and performance analysis
- Handle file I/O exceptions properly
- Confirm export success with file location and size

---

### US-3: Calculate Student GPA (NEW)

**As a** teacher
**I want to** calculate GPA on a 4.0 scale
**So that** I can provide standardized grade reporting

**Acceptance Criteria:**

- Convert percentage grades to 4.0 GPA scale
- Display grade breakdown by subject
- Show letter grades (A, A-, B+, etc.)
- Calculate cumulative GPA
- Display class rank

**Grading Scale:**

| Percentage | GPA | Letter |
|-----------|-----|--------|
| 93-100% | 4.0 | A |
| 90-92% | 3.7 | A- |
| 87-89% | 3.3 | B+ |
| 83-86% | 3.0 | B |
| 80-82% | 2.7 | B- |
| 77-79% | 2.3 | C+ |
| 73-76% | 2.0 | C |
| 70-72% | 1.7 | C- |
| 67-69% | 1.3 | D+ |
| 60-66% | 1.0 | D |
| Below 60% | 0.0 | F |

---

### US-4: Bulk Import Grades (NEW)

**As a** teacher
**I want to** import multiple grades from a CSV file
**So that** I can efficiently enter grades for the whole class

**Acceptance Criteria:**

- Read CSV files from `imports/` directory
- Validate file format before processing
- Validate each row (student exists, grade in range, subject valid)
- Skip invalid rows but continue processing
- Generate import summary with success/failure counts
- Create detailed log file of import process

**CSV Format:**

```csv
StudentID,SubjectName,SubjectType,Grade
STU001,Mathematics,Core,85
STU002,English,Core,92
STU001,Music,Elective,78
```

---

### US-5: View Class Statistics (NEW)

**As a** teacher
**I want to** view class-wide statistics
**So that** I can understand overall class performance

**Acceptance Criteria:**

- Display grade distribution (A, B, C, D, F counts and percentages)
- Show statistical measures (mean, median, mode, standard deviation)
- Display highest and lowest grades with student names
- Show average performance by subject
- Compare Regular vs Honors student performance

---

### US-6: Search Students (NEW)

**As a** teacher
**I want to** search for students
**So that** I can quickly find specific students or groups

**Acceptance Criteria:**

- Search by student ID (exact match)
- Search by name (partial match, case-insensitive)
- Search by grade range (e.g., 80-90%)
- Search by student type (Regular/Honors)
- Display search results in a table
- Allow actions on search results (view details, export)

---

## New Architecture Requirements

### SOLID Principles Implementation

#### Single Responsibility Principle (SRP)

- `ReportGenerator`: Only generates reports
- `FileExporter`: Only handles file operations
- `GPACalculator`: Only performs GPA calculations
- `StatisticsCalculator`: Only performs statistical calculations
- `CSVParser`: Only parses CSV files

#### Open/Closed Principle (OCP)

- `Subject` hierarchy can be extended with new subject types without modifying existing code
- `Student` hierarchy can be extended with new student types
- Strategy pattern for grade calculation (can add different grading scales)

#### Liskov Substitution Principle (LSP)

- Any `Student` subclass can be used wherever `Student` is expected
- Any `Subject` subclass can be used wherever `Subject` is expected

#### Interface Segregation Principle (ISP)

- Create focused interfaces: `Searchable`, `Exportable`, `Calculable`
- Clients only depend on methods they use

#### Dependency Inversion Principle (DIP)

- High-level modules (`BulkImportService`) depend on abstractions (`CSVParser` interface)
- Use dependency injection in service classes

---

## Testing Requirements

### JUnit Tests (80%+ Coverage Required)

**Test Coverage Goals:**

- Overall: 80%+
- Critical business logic (GPA calculation, statistics): 95%+
- Exception handling paths: 100%
- Service classes: 85%+

---

## Git Workflow Requirements

### Branch Strategy

- `main`: Production-ready code (protected)
- `develop`: Integration branch
- `feature/*`: Feature branches (e.g., `feature/export-reports`, `feature/bulk-import`)
- `bugfix/*`: Bug fix branches
- `hotfix/*`: Emergency fixes

### Commit Requirements

- **Minimum 20 meaningful commits**
- Follow conventional commit format:

```
feat: add GPA calculation feature
fix: correct median calculation for even count
test: add unit tests for statistics calculator
```

### Required Git Workflow

1. Create feature branch from `develop`
2. Implement feature with regular commits
3. Write tests for the feature
4. Merge `develop` into feature branch (resolve conflicts if any)
5. Create pull request with description
6. Merge to `develop`
7. Eventually merge `develop` to `main`

---

## Minimum Requirements

### Implementation

- [ ] All custom exceptions implemented
- [ ] All 6 new user stories working
- [ ] SOLID principles applied throughout
- [ ] Exception handling on all user inputs and file operations
- [ ] Logging implemented for debugging
- [ ] All service classes use dependency injection

### Testing

- [ ] JUnit and Mockito dependencies added (Maven/Gradle)
- [ ] Minimum 15 unit tests
- [ ] Minimum 5 integration tests with mocking
- [ ] 80%+ code coverage achieved
- [ ] All tests pass
- [ ] Test reports generated

### Git

- [ ] Feature branch workflow followed
- [ ] Minimum 20 meaningful commits
- [ ] Conventional commit messages
- [ ] `.gitignore` properly configured
- [ ] CHANGELOG.md maintained
- [ ] Pull requests documented (screenshots/descriptions)

### Code Quality

- [ ] No code duplication
- [ ] Single Responsibility Principle followed
- [ ] Proper dependency injection
- [ ] Consistent naming conventions
- [ ] Comprehensive JavaDoc comments
- [ ] No hardcoded values (use constants)

### Documentation

- [ ] README with setup and testing instructions
- [ ] CHANGELOG documenting all features
- [ ] JavaDoc for all public methods
- [ ] Test execution guide
- [ ] Git workflow documentation

---

## Grading Rubric

| # | Criteria | Points | What We're Looking For |
|---|----------|--------|----------------------|
| 1 | Exception Handling | 15 | All custom exceptions implemented, proper error handling, informative messages, no generic catches |
| 2 | Testing (JUnit/Mockito) | 20 | 80%+ coverage, meaningful tests, proper mocking, tests pass, integration tests included |
| 3 | SOLID Principles | 15 | SRP evident, OCP in hierarchies, LSP maintained, ISP with focused interfaces, DIP with injection |
| 4 | Git Workflow | 15 | Feature branches, 20+ commits, conventional messages, pull requests, proper branching strategy |
| 5 | New Functionality | 20 | All 6 user stories work correctly, export reports, GPA calculation, bulk import, statistics, search |
| 6 | Code Quality | 10 | Clean code, no duplication, proper naming, JavaDoc comments, constants used |
| 7 | Documentation | 5 | README, CHANGELOG, test instructions, Git workflow docs |
| 8 | **Total** | **100** | |

---

## Testing the Application

### Test Scenario 1: Exception Handling

1. Try to record grade for non-existent student
2. Verify `StudentNotFoundException` is thrown and caught
3. Verify helpful error message is displayed
4. Try to enter grade > 100
5. Verify `InvalidGradeException` is thrown and caught
6. Try to import CSV with invalid format
7. Verify `InvalidFileFormatException` is thrown

### Test Scenario 2: Export Grade Report

1. Add student and record 5+ grades
2. Select option 5 (Export Grade Report)
3. Enter student ID
4. Select detailed report
5. Provide filename
6. Verify file is created in `reports/` directory
7. Open file and verify content is correct

### Test Scenario 3: Calculate GPA

1. Select option 6 (Calculate Student GPA)
2. Enter student ID with multiple grades
3. Verify GPA calculation is correct
4. Verify letter grades match percentages
5. Verify class rank is displayed
6. Test with different grade ranges (A, B, C, etc.)

### Test Scenario 4: Bulk Import

1. Create CSV file with 10 valid records
2. Add 2 invalid records (invalid student ID, grade > 100)
3. Place file in `imports/` directory
4. Select option 7 (Bulk Import Grades)
5. Enter filename
6. Verify 10 succeed, 2 fail
7. Check import log file is created
8. Verify grades appear in system

### Test Scenario 5: Class Statistics

1. Ensure at least 5 students with 5+ grades each
2. Select option 8 (View Class Statistics)
3. Verify grade distribution is accurate
4. Verify mean, median, mode calculations
5. Verify highest/lowest grades shown correctly
6. Verify subject averages are correct

### Test Scenario 6: Search Students

1. Select option 9 (Search Students)
2. Search by name with partial match ("John")
3. Verify all matching students appear
4. Search by grade range (80-90%)
5. Verify only students in range appear
6. Search by student type (Honors)
7. Verify only honors students appear

### Test Scenario 7: Unit Tests

1. Run all JUnit tests: `mvn test` or IDE test runner
2. Verify all tests pass
3. Generate coverage report
4. Verify 80%+ coverage achieved

### Test Scenario 8: Git Workflow

1. Review commit history: `git log --oneline`
2. Verify 20+ commits
3. Verify conventional commit messages
4. Check branch structure: `git branch -a`
5. Verify feature branches exist
6. Review CHANGELOG.md for documented changes

---

## Submission Link

Submit your project here: [Tally](https://tally.so)