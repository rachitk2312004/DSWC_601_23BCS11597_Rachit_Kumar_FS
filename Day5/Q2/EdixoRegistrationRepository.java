import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

interface RegistrationManager {
    void enrollAtRiskStudents();
}

abstract class DatabaseConnectionProvider {
    private final String url;
    private final String user;
    private final String password;

    protected DatabaseConnectionProvider() {
        this("jdbc:postgresql://localhost:5432/edixo", "postgres", "password");
    }

    protected DatabaseConnectionProvider(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

public class EdixoRegistrationRepository extends DatabaseConnectionProvider implements RegistrationManager {
    @Override
    public void enrollAtRiskStudents() {
        String findMissingStudents =
                "SELECT s.student_id, s.full_name " +
                "FROM students s " +
                "LEFT JOIN course_registrations cr ON s.student_id = cr.student_id " +
                "WHERE cr.student_id IS NULL";

        String insertCourse =
                "INSERT INTO course_registrations (student_id, course_code, semester) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement selectPs = conn.prepareStatement(findMissingStudents);
             PreparedStatement insertPs = conn.prepareStatement(insertCourse)) {

            conn.setAutoCommit(false);

            try (ResultSet rs = selectPs.executeQuery()) {
                int batchCount = 0;

                while (rs.next()) {
                    long studentId = rs.getLong("student_id");

                    insertPs.setLong(1, studentId);
                    insertPs.setString(2, "Orientation101");
                    insertPs.setString(3, "Current Semester");
                    insertPs.addBatch();
                    batchCount++;

                    if (batchCount % 1000 == 0) {
                        insertPs.executeBatch();
                    }
                }

                insertPs.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to enroll at-risk students", e);
        }
    }

    public static void main(String[] args) {
        new EdixoRegistrationRepository().enrollAtRiskStudents();
    }
}
