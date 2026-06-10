import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

interface QueueWorker {
    void processNextJob();
}

abstract class EnterpriseConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    protected EnterpriseConnectionFactory() {
        this("jdbc:postgresql://localhost:5432/enterprise", "postgres", "password");
    }

    protected EnterpriseConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

public class QueueWorkerRepository extends EnterpriseConnectionFactory implements QueueWorker {
    @Override
    public void processNextJob() {
        String fetchJob =
                "SELECT b.job_id, d.dept_name, b.created_at " +
                "FROM background_jobs b " +
                "INNER JOIN departments d ON b.dept_id = d.dept_id " +
                "WHERE b.status = 'PENDING' " +
                "AND d.dept_name = 'Engineering' " +
                "ORDER BY b.created_at ASC " +
                "FOR UPDATE SKIP LOCKED " +
                "LIMIT 1";

        String updateJob =
                "UPDATE background_jobs SET status = 'PROCESSING' WHERE job_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement selectPs = conn.prepareStatement(fetchJob);
             PreparedStatement updatePs = conn.prepareStatement(updateJob)) {

            conn.setAutoCommit(false);

            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    long jobId = rs.getLong("job_id");
                    updatePs.setLong(1, jobId);
                    updatePs.executeUpdate();
                    conn.commit();
                    System.out.println("Job " + jobId + " moved to PROCESSING");
                } else {
                    conn.commit();
                    System.out.println("No pending job available");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to process next job", e);
        }
    }

    public static void main(String[] args) {
        new QueueWorkerRepository().processNextJob();
    }
}
