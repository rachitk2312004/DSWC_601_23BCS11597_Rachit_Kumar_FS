
import java.sql.*;
import java.util.*;

class Employee {
    int id;
    String name;
}

class Department {
    int id;
    String name;
    List<Employee> employees = new ArrayList<>();
}

public class DepartmentEmployeeMapper {
    public Map<Integer, Department> loadGraph(Connection conn) throws Exception {
        Map<Integer, Department> graph = new HashMap<>();

        String sql = "SELECT d.dept_id,d.dept_name,e.emp_id,e.emp_name FROM departments d INNER JOIN employees e ON d.dept_id=e.dept_id";

        ResultSet rs = conn.createStatement().executeQuery(sql);

        while (rs.next()) {
            int deptId = rs.getInt("dept_id");

            Department dept = graph.computeIfAbsent(deptId, k -> {
                Department d = new Department();
                d.id = deptId;
                d.name = rsSafe(rs,"dept_name");
                return d;
            });

            Employee emp = new Employee();
            emp.id = rs.getInt("emp_id");
            emp.name = rs.getString("emp_name");
            dept.employees.add(emp);
        }
        return graph;
    }

    private String rsSafe(ResultSet rs,String c){
        try{return rs.getString(c);}catch(Exception e){return "";}
    }
}
