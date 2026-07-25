# Student Grade Management v2 — User Manual

## Overview

The Student Grade Management System v2 extends the original with main.export, GPA calculation, bulk CSV import, class statistics, and student search. All errors are handled with custom main.exceptions.

## Getting Started

### Prerequisites

- Java 17+
- Terminal / Command Prompt

### Running the Application

```bash
cd student-grade-management
javac -d bin -sourcepath src src/Main.java
java -cp bin Main
```

Enable role-based access? Enter `Y` or `N` (default N).

---

## Main Menu

```
╔════════════════════════════════════════════╗
║   STUDENT GRADE MANAGEMENT - MAIN MENU    ║
╚════════════════════════════════════════════╝

1. Add Student
2. View Students
3. Record Grade
4. View Grade Report
5. Export Grade Report
6. Calculate Student GPA
7. Bulk Import Grades
8. View Class Statistics
9. Search Students
10. Exit

Enter choice: _
```

---

## Feature Details

### 1. Add Student

Creates a new student (Regular or Honors).

```
Enter student name: Alice Johnson
Enter student age: 20
Enter student email: alice@example.com
Enter student phone: 1234567890

Student type:
1. Regular Student (Passing grade: 50%)
2. Honors Student (Passing grade: 60%, honors recognition)
Select type (1-2): 1
```

### 2. View Students

Lists all students with ID, name, type, average grade, and passing status.

### 3. Record Grade

Records a grade for a student in a Core or Elective subject.

```
Enter Student ID: STU001

Subject type:
1. Core Subject (Mathematics, English, Science)
2. Elective Subject (Music, Art, Physical Education)

Select subject (1-2): 1

Available Core Subjects:
1. Mathematics
2. English
3. Science

Select subject (1-3): 1

Enter grade (0-100): 85
```

### 4. View Grade Report

Displays all grades for a student with averages.

### 5. Export Grade Report

Saves a student's report to a text file in `reports/`.

Options:
- **Summary Report** — overview with overall average
- **Detailed Report** — all grades with subject, type, and per-subject averages
- **Both** — generates two files (`_summary.txt` and `_detailed.txt`)

```
Enter Student ID: STU001

Export options:
1. Summary Report (overview only)
2. Detailed Report (all grades)
3. Both

Select option (1-3): 2

Enter filename (without extension): alice_report
```

Output location: `./reports/<filename>.txt`

### 6. Calculate Student GPA

Converts percentage grades to GPA on a 4.0 scale.

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

```
Cumulative GPA: 3.74 / 4.0
Letter Grade: A-
Class Rank: 2 of 5
```

### 7. Bulk Import Grades

Import grades from a CSV file placed in `main.imports/`.

**CSV Format:**

```
StudentID,SubjectName,SubjectType,Grade
STU001,Mathematics,Core,85
STU002,English,Core,92
STU001,Music,Elective,78
```

```
Place your CSV file in: ./main.imports/

Enter filename (without extension): october_grades
```

Invalid rows are skipped. A log file is generated: `import_log_<timestamp>.txt`

### 8. View Class Statistics

Displays class-wide statistics:

- Grade distribution (A, B, C, D, F) with visual bar chart
- Mean, median, standard deviation
- Highest and lowest grades with student/subject details
- Average performance by subject
- Regular vs Honors student comparison

### 9. Search Students

Search by:

1. **Student ID** — exact match
2. **Name** — partial match, case-insensitive
3. **Grade Range** — e.g., 80-90%
4. **Student Type** — Regular or Honors

Actions on results:
- View full student details
- Export results to file
- New search
- Return to main menu

### 10. Exit

Exits the application.

---

## Directories

| Directory | Purpose |
|-----------|---------|
| `reports/` | Exported grade report files |
| `main.imports/` | CSV files for bulk import + import logs |

---

## Error Handling

All errors display the exception type and a descriptive message with recovery suggestions:

```
✗ ERROR: StudentNotFoundException
  Student with ID 'STU999' not found.
  Available student IDs: STU001, STU002, STU003
```

```
✗ ERROR: InvalidGradeException
  Grade must be between 0 and 100. You entered: 150
```

---

## Architecture (Packages)

| Package | Responsibility |
|---------|---------------|
| `main.model` | Student, Grade, Subject hierarchies |
| `main.repository` | Data storage (array-based) |
| `main.service` | Business logic layer |
| `main.manager` | Coordination layer (StudentManager, GradeManager, StudentSearcher) |
| `main.export` | ReportGenerator, FileExporter |
| `main.calculators` | GPACalculator, StatisticsCalculator |
| `main.imports` | CSVParser, BulkImportService |
| `interfaces` | Exportable, Calculable, Searchable |
| `main.exceptions` | Custom exception classes |
| `validation` | StudentValidator, SubjectValidator |
