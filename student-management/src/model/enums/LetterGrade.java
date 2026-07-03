package model.enums;

public enum LetterGrade {
    A, B, C, D, F;

    public static LetterGrade fromNumeric(double grade) {
        if (grade >= 85) return A;
        else if (grade >= 70) return B;
        else if (grade >= 55) return C;
        else if (grade >= 40) return D;
        else return F;
    }
}
