
import java.sql.*;

public class PreparedStatementSecurity {
    public void searchUser(String input) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/bank","user","pass");

        String sql = "SELECT * FROM users WHERE email LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + input + "%");

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("email"));
        }
        conn.close();
    }
}
