package main.export;

public interface Exportable {
    String exportSummary(String studentId);
    String exportDetailed(String studentId);
}
