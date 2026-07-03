package repository.subject.impl;

import config.DatabaseConfig;
import exceptions.subjects.SubjectNotFoundException;
import exceptions.subjects.SubjectValidationException;
import model.subject.Subject;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;
import model.enums.SubjectType;
import repository.subject.SubjectRepository;
import validation.SubjectValidator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectRepositoryImpl implements SubjectRepository {

    @Override
    public void addSubject(Subject subject) {
        // Validate subject before saving
        SubjectValidator.validateSubject(subject);

        String sql = "INSERT INTO subjects (subject_code, subject_name, subject_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject.getSubjectCode());
            pstmt.setString(2, subject.getSubjectName());
            pstmt.setString(3, subject.getSubjectType().name());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SubjectValidationException("Error adding subject: " + e.getMessage());
        }
    }

    @Override
    public Subject findSubjectByCode(String subjectCode) {
        String sql = "SELECT subject_code, subject_name, subject_type FROM subjects WHERE subject_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subjectCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("subject_type");
                if (type.equalsIgnoreCase(SubjectType.CORE.name())) {
                    return new CoreSubject(rs.getString("subject_name"), rs.getString("subject_code"));
                } else {
                    return new ElectiveSubject(rs.getString("subject_name"), rs.getString("subject_code"));
                }
            }
        } catch (SQLException e) {
            throw new SubjectNotFoundException("Error finding subject: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT subject_code, subject_name, subject_type FROM subjects";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String type = rs.getString("subject_type");
                Subject subject = type.equalsIgnoreCase(SubjectType.CORE.name())
                        ? new CoreSubject(rs.getString("subject_name"), rs.getString("subject_code"))
                        : new ElectiveSubject(rs.getString("subject_name"), rs.getString("subject_code"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            throw new SubjectValidationException("Error retrieving subjects: " + e.getMessage());
        }
        return subjects;
    }

    @Override
    public void deleteSubject(String subjectCode) {
        String sql = "DELETE FROM subjects WHERE subject_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subjectCode);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SubjectNotFoundException("Subject with code " + subjectCode + " not found.");
            }
        } catch (SQLException e) {
            throw new SubjectValidationException("Error deleting subject: " + e.getMessage());
        }
    }
}
