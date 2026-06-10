
import java.sql.*;

public class QueryOptimization {
    public void fetch2024Records() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/audit","user","pass");

        String sql = "SELECT * FROM records WHERE created_at >= ? AND created_at < ?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDate(1, Date.valueOf("2024-01-01"));
        ps.setDate(2, Date.valueOf("2025-01-01"));

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getLong(1));
        }
        conn.close();
    }
}
