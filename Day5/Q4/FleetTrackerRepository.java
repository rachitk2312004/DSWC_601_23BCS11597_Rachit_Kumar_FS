import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

interface TelemetryService {
    void printLatestLocations();
}

abstract class FleetDatabaseConnection {
    private final String url;
    private final String user;
    private final String password;

    protected FleetDatabaseConnection() {
        this("jdbc:postgresql://localhost:5432/fleetdb", "postgres", "password");
    }

    protected FleetDatabaseConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

public class FleetTrackerRepository extends FleetDatabaseConnection implements TelemetryService {
    @Override
    public void printLatestLocations() {
        String sql =
                "SELECT rider_id, rider_name, bike_model, latitude, longitude, recorded_at " +
                "FROM (" +
                "    SELECT r.rider_id, r.rider_name, r.bike_model, g.latitude, g.longitude, g.recorded_at, " +
                "           ROW_NUMBER() OVER (PARTITION BY r.rider_id ORDER BY g.recorded_at DESC) AS rn " +
                "    FROM riders r " +
                "    INNER JOIN gps_pings g ON r.rider_id = g.rider_id" +
                ") latest_ping " +
                "WHERE rn = 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long riderId = rs.getLong("rider_id");
                String riderName = rs.getString("rider_name");
                String bikeModel = rs.getString("bike_model");
                double latitude = rs.getDouble("latitude");
                double longitude = rs.getDouble("longitude");
                LocalDateTime recordedAt = rs.getObject("recorded_at", LocalDateTime.class);

                System.out.printf("%d | %s | %s | %.6f | %.6f | %s%n",
                        riderId, riderName, bikeModel, latitude, longitude, recordedAt);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to print latest GPS locations", e);
        }
    }

    public static void main(String[] args) {
        new FleetTrackerRepository().printLatestLocations();
    }
}
