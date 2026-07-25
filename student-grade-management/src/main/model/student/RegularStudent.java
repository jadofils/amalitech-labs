package main.model.student;

import main.model.enums.StudentStatus;
import main.model.enums.StudentType;

public class RegularStudent extends Student {
    private static final StudentType studentType = StudentType.REGULAR;

    public RegularStudent(String name, int age, String email, String phone) {
        super(name, age, email, phone);
    }

    public RegularStudent(String studentId, String name, int age, String email, String phone, StudentStatus status) {
        super(studentId, name, age, email, phone, status);
    }

    @Override
    public void displayStudentDetails() {
        printDetailsDivider();
        printCommonDetails();
        printDetailsDivider();
    }

    @Override
    public StudentType getType() {
        return studentType;
    }

    @Override
    public double getPassingGrade() {
        return studentType.getPassingGrade();
    }
}
