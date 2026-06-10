
import java.sql.*;

public class FraudDetection {
    public void findGhostEmployees(Connection conn) throws Exception {
        String sql =
            "SELECT e.emp_name AS ghost_employee, m.emp_name AS manager_name, d.dept_name " +
            "FROM employees e " +
            "INNER JOIN employees m ON e.manager_id=m.emp_id " +
            "LEFT JOIN departments d ON e.dept_id=d.dept_id " +
            "WHERE e.dept_id IS NULL";

        ResultSet rs = conn.createStatement().executeQuery(sql);

        while(rs.next()){
            System.out.println(rs.getString("ghost_employee") +
                    " -> " + rs.getString("manager_name"));
        }
    }
}
