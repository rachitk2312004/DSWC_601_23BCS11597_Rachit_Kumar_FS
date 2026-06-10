import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

interface PortfolioManager {
    void restructurePortfolio(long investorId);
}

abstract class FinancialDatabaseConfig {
    private final String url;
    private final String user;
    private final String password;

    protected FinancialDatabaseConfig() {
        this("jdbc:postgresql://localhost:5432/firedb", "postgres", "password");
    }

    protected FinancialDatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

public class PortfolioRepository extends FinancialDatabaseConfig implements PortfolioManager {
    @Override
    public void restructurePortfolio(long investorId) {
        String aggregateSql =
                "SELECT h.asset_class, SUM(h.current_value) AS total_current_value " +
                "FROM investors i " +
                "INNER JOIN holdings h ON i.investor_id = h.investor_id " +
                "WHERE i.investor_id = ? " +
                "GROUP BY h.asset_class";

        String debitSql =
                "UPDATE holdings SET current_value = current_value - ? " +
                "WHERE investor_id = ? AND asset_class = 'Debt'";

        String equitySql =
                "UPDATE holdings SET current_value = current_value + ? " +
                "WHERE investor_id = ? AND asset_class = 'Equity'";

        BigDecimal shiftAmount = new BigDecimal("1000.00");

        try (Connection conn = getConnection();
             PreparedStatement aggregatePs = conn.prepareStatement(aggregateSql);
             PreparedStatement debtPs = conn.prepareStatement(debitSql);
             PreparedStatement equityPs = conn.prepareStatement(equitySql)) {

            conn.setAutoCommit(false);

            try {
                aggregatePs.setLong(1, investorId);

                try (ResultSet rs = aggregatePs.executeQuery()) {
                    while (rs.next()) {
                        String assetClass = rs.getString("asset_class");
                        BigDecimal total = rs.getBigDecimal("total_current_value");
                        System.out.println(assetClass + " => " + total);
                    }
                }

                debtPs.setBigDecimal(1, shiftAmount);
                debtPs.setLong(2, investorId);
                debtPs.executeUpdate();

                equityPs.setBigDecimal(1, shiftAmount);
                equityPs.setLong(2, investorId);
                equityPs.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to restructure portfolio", e);
        }
    }

    public static void main(String[] args) {
        new PortfolioRepository().restructurePortfolio(101L);
    }
}
