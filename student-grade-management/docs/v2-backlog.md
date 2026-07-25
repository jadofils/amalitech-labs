# Product Backlog — Student Grade Management v2

## Product Vision

An enhanced student grade management system that extends Lab 1 with file main.export, GPA calculation, bulk CSV import, class statistics, and student search — all built with SOLID principles, comprehensive testing, and proper Git workflow.

---

## Backlog Items

### PBI-1: Enhanced Exception Handling
| Field | Value |
|-------|-------|
| **Priority** | High |
| **Story Points** | 3 |
| **Sprint** | 1 |

> **As a** developer
> **I want to** handle all errors with custom main.exceptions
> **So that** the application never crashes unexpectedly

**Acceptance Criteria:**
- [ ] Custom main.exceptions for all error scenarios (StudentNotFoundException, GradeNotFoundException, InvalidGradeException, InvalidFileFormatException, ExportException, ImportException)
- [ ] Informative error messages with recovery suggestions
- [ ] No generic Exception catching (replace RuntimeException extends)
- [ ] All main.exceptions logged with timestamps
- [ ] Input validation prevents most main.exceptions before they occur

### PBI-2: Export Grade Report
| Field | Value |
|-------|-------|
| **Priority** | High |
| **Story Points** | 5 |
| **Sprint** | 1 |

> **As a** teacher
> **I want to** main.export grade reports to files
> **So that** I can share them with students and parents

**Acceptance Criteria:**
- [ ] Export summary, detailed, or both report types
- [ ] Save to text files in `reports/` directory
- [ ] Include student info, all grades, averages, performance analysis
- [ ] Handle file I/O main.exceptions properly
- [ ] Confirm main.export success with file location and size

**New Classes:** `ReportGenerator`, `FileExporter`, `Exportable` interface

### PBI-3: Calculate Student GPA
| Field | Value |
|-------|-------|
| **Priority** | High |
| **Story Points** | 5 |
| **Sprint** | 1 |

> **As a** teacher
> **I want to** calculate GPA on a 4.0 scale
> **So that** I can provide standardized grade reporting

**Acceptance Criteria:**
- [ ] Convert percentage grades to 4.0 GPA scale per grading table
- [ ] Display grade breakdown by subject with GPA points
- [ ] Show letter grades (A, A-, B+, B, etc.)
- [ ] Calculate cumulative GPA
- [ ] Display class rank (X of Y)
- [ ] Performance analysis message

**New Classes:** `GPACalculator`, `Calculable` interface

### PBI-4: Bulk Import Grades from CSV
| Field | Value |
|-------|-------|
| **Priority** | Medium |
| **Story Points** | 8 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** import multiple grades from a CSV file
> **So that** I can efficiently enter grades for the whole class

**Acceptance Criteria:**
- [ ] Read CSV files from `main.imports/` directory
- [ ] Validate file format before processing
- [ ] Validate each row (student exists, grade in range, subject valid)
- [ ] Skip invalid rows but continue processing
- [ ] Generate import summary with success/failure counts
- [ ] Create detailed log file of import process

**New Classes:** `CSVParser`, `BulkImportService`, `CSVImportException`

### PBI-5: View Class Statistics
| Field | Value |
|-------|-------|
| **Priority** | Medium |
| **Story Points** | 8 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** view class-wide statistics
> **So that** I can understand overall class performance

**Acceptance Criteria:**
- [ ] Display grade distribution (A, B, C, D, F counts and percentages)
- [ ] Show visual bar chart of distribution
- [ ] Statistical measures (mean, median, mode, standard deviation)
- [ ] Display highest and lowest grades with student names
- [ ] Show average performance by subject
- [ ] Compare Regular vs Honors student performance

**New Classes:** `StatisticsCalculator`

### PBI-6: Search Students
| Field | Value |
|-------|-------|
| **Priority** | Medium |
| **Story Points** | 5 |
| **Sprint** | 2 |

> **As a** teacher
> **I want to** search for students
> **So that** I can quickly find specific students or groups

**Acceptance Criteria:**
- [ ] Search by student ID (exact match)
- [ ] Search by name (partial match, case-insensitive)
- [ ] Search by grade range (e.g., 80-90%)
- [ ] Search by student type (Regular/Honors)
- [ ] Display search results in a formatted table
- [ ] Actions on search results: view details, main.export, new search

**New Classes:** `Searchable` interface, enhanced StudentManager

---

## Estimation

| PBI | Description | Optimistic | Most Likely | Pessimistic | Expected (PERT) |
|-----|------------|-----------|-------------|-------------|-----------------|
| 1 | Exception Handling | 0.5h | 1h | 2h | 1.1h |
| 2 | Export Grade Report | 1h | 2h | 3h | 2h |
| 3 | GPA Calculator | 1h | 1.5h | 3h | 1.7h |
| 4 | Bulk Import | 1.5h | 3h | 5h | 3.1h |
| 5 | Class Statistics | 2h | 3h | 5h | 3.2h |
| 6 | Search Students | 1h | 1.5h | 3h | 1.7h |
| | **Testing suite** | 2h | 3h | 5h | 3.2h |
| | **Logging + Docs + Polish** | 1h | 2h | 3h | 2h |
| | **Total** | **10h** | **17h** | **29h** | **18h** |
