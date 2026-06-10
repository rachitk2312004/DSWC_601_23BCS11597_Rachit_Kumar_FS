import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

interface ReportGenerator {
    void printDelayedReport();
}

abstract class DatabaseRepository {
    private final String url;
    private final String user;
    private final String password;

    protected DatabaseRepository() {
        this("jdbc:postgresql://localhost:5432/cargologix", "postgres", "password");
    }

    protected DatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

public class LogisticsRepository extends DatabaseRepository implements ReportGenerator {
    @Override
    public void printDelayedReport() {
        String sql = "SELECT s.shipment_id, c.company_name, s.dispatch_date " +
                     "FROM shipments s " +
                     "INNER JOIN couriers c ON s.courier_id = c.courier_id " +
                     "WHERE s.status = ? " +
                     "ORDER BY s.dispatch_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "DELAYED");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long shipmentId = rs.getLong("shipment_id");
                    String companyName = rs.getString("company_name");
                    java.sql.Date dispatchDate = rs.getDate("dispatch_date");
                    System.out.printf("%d | %s | %s%n", shipmentId, companyName, dispatchDate);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to print delayed shipment report", e);
        }
    }

    public static void main(String[] args) {
        new LogisticsRepository().printDelayedReport();
    }
}
