# Sprint 1 Plan — Student Grade Management v2

## Sprint Goal

Deliver the foundation: custom exception hierarchy, SOLID refactoring with new interfaces, Export Grade Report, and GPA Calculator.

## Sprint Duration

Estimated: 5-6 hours

---

## Sprint Backlog

### PBI-1: Enhanced Exception Handling (3 SP)
**Subtasks:**
- Remove generic RuntimeException extends from existing exceptions
- Add InvalidGradeException
- Add ExportException
- Add InvalidFileFormatException
- Add ImportException
- Ensure all exceptions have useful messages

### PBI-2: SOLID Interfaces + New Classes (2 SP)

**New Interfaces:**
- `Searchable` — searchByID(id), searchByName(name), searchByGradeRange(min, max), searchByType(type)
- `Exportable` — exportSummary(studentId), exportDetailed(studentId)
- `Calculable` — cumulativeGPA(studentId), classRank(studentId)

**New Classes:**
- `ReportGenerator` — only generates formatted report content
- `FileExporter` — only handles file I/O
- `GPACalculator` — only GPA computations
- `StatisticsCalculator` — only stats
- `CSVParser` — only CSV parsing

### PBI-2: Export Grade Report (5 SP)
**Subtasks:**
- Create `Exportable` interface
- Create `ReportGenerator` class
- Create `FileExporter` class
- Add method to GradeManager for gathering report data
- Wire up export option in Main menu (option 5)
- Test with various report types

### PBI-3: GPA Calculator (5 SP)
**Subtasks:**
- Create GPA conversion table (percentage -> GPA points -> letter)
- Create `Calculable` interface
- Create `GPACalculator` class
- Calculate class rank
- Format and display GPA breakdown
- Wire up GPA option in Main menu (option 6)

---

## Definition of Done

- All code compiles and runs
- No generic Exception/Error catches
- Custom exceptions thrown for all error states
- Export creates valid text file in reports/ directory
- GPA calculations match the reference table
- SOLID interfaces implemented and used
- Committed with conventional commit messages
- Feature branches merged to develop
