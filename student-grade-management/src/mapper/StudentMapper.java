package mapper;

import dto.StudentDTO;
import model.student.Student;

public final class StudentMapper {

    private StudentMapper() {
    }

    public static StudentDTO toDto(Student student) {
        return new StudentDTO(
                student.getStudentId(),
                student.getName(),
                student.getStudentType(),
                student.calculateAverageGrade()
        );
    }
}
