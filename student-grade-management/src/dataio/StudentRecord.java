package dataio;

import java.io.Serializable;

/**
 * A full-fidelity, format-agnostic snapshot of a {@link model.student.Student} - every field
 * needed to reconstruct the original object exactly (unlike {@link dto.StudentDTO}, which is a
 * thin display-only projection with just 4 fields for search results/report headers and was left
 * untouched here rather than widened for a different job). A {@code record} rather than a class:
 * this is pure data with no behavior, and records get {@code equals}/{@code hashCode}/{@code
 * toString} - and, since it {@code implements Serializable}, Java's built-in binary
 * (de)serialization - for free.
 */
public record StudentRecord(
        String studentId,
        String name,
        String studentType,
        int age,
        String email,
        String phone,
        String status
) implements Serializable {
}
