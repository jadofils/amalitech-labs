package mapper;

import dto.GradeDTO;
import model.grade.Grade;

public final class GradeMapper {

    private GradeMapper() {
    }

    public static GradeDTO toDto(Grade grade) {
        return new GradeDTO(
                grade.getGradeId(),
                grade.getDate(),
                grade.getSubject().getSubjectName(),
                grade.getSubjectType(),
                grade.getGrade()
        );
    }
}
