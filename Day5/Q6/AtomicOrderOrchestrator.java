
import java.sql.*;

class InsufficientStockException extends Exception {
    public InsufficientStockException(String message) { super(message); }
}

public class AtomicOrderOrchestrator {
    public void processOrder(long productId, int quantity) {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/shop","user","pass")) {
            conn.setAutoCommit(false);

            try {
                PreparedStatement updateStock = conn.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE product_id = ? AND stock >= ?");
                updateStock.setInt(1, quantity);
                updateStock.setLong(2, productId);
                updateStock.setInt(3, quantity);

                if (updateStock.executeUpdate() == 0) {
                    throw new InsufficientStockException("Insufficient stock");
                }

                PreparedStatement insertOrder = conn.prepareStatement(
                        "INSERT INTO orders(product_id, quantity) VALUES(?, ?)");
                insertOrder.setLong(1, productId);
                insertOrder.setInt(2, quantity);
                insertOrder.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
