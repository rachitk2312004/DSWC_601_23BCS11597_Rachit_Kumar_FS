// Day4 Q6: E-Commerce Nested Payload Parser
// -------------------------------------------
// Two things this question tests:
// 1. flatMap to extract items from a nested Order → List<Item> structure
// 2. Null-safety throughout the pipeline using Objects::nonNull and defensive string comparison
//
// The scenario mirrors a real microservice receiving a JSON payload with nested objects.
// Some orders in the list might be null (network corruption). Some items have null categories.
// The pipeline must handle all of this without a single NullPointerException.

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


// ---------- Domain Classes ----------

// An individual product in an order.
// category can be null in corrupted payloads — we handle this defensively.
class Item {
    private String name;
    private String category;
    private double price;

    public Item(String name, String category, double price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }
}


// An order contains a list of items.
// The list itself might contain null item references — defensive filtering needed.
class Order {
    private String orderId;
    private List<Item> items;

    public Order(String orderId, List<Item> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public String getOrderId()   { return orderId; }
    public List<Item> getItems() { return items; }
}


// ---------- The Revenue Calculator ----------

class RevenueCalculator {

    public double calculateElectronicsRevenue(List<Order> orders) {
        return orders.stream()

            // First guard: some Order objects in the list might be null.
            // Objects::nonNull is a method reference equivalent to: order -> order != null
            // This prevents a NullPointerException when we call order.getItems() below.
            .filter(Objects::nonNull)

            // flatMap: each Order has a List<Item>. We want one continuous stream of ALL items
            // across ALL orders, not a Stream<List<Item>>.
            //
            // order.getItems().stream() turns one order's items into a stream.
            // flatMap merges all those per-order streams into one flat stream of Items.
            //
            // Without flatMap: Stream<List<Item>> — we'd be filtering lists, not items.
            // With flatMap:    Stream<Item>       — we can now filter individual items.
            .flatMap(order -> order.getItems().stream())

            // Second guard: individual Item references inside the list might be null.
            .filter(Objects::nonNull)

            // Third guard: filter for ELECTRONICS only.
            // "ELECTRONICS".equals(item.getCategory()) is written this way deliberately.
            // If getCategory() returns null, "ELECTRONICS".equals(null) returns false — safe.
            // Writing it as item.getCategory().equals("ELECTRONICS") would NullPointerException
            // the moment any item has a null category. Always put the literal first.
            .filter(item -> "ELECTRONICS".equals(item.getCategory()))

            // Also filter out items with negative or zero prices — corrupted price data.
            .filter(item -> item.getPrice() > 0)

            // mapToDouble converts to primitive DoubleStream — avoids boxing Double objects.
            // Item::getPrice is a method reference equivalent to: item -> item.getPrice()
            .mapToDouble(Item::getPrice)

            // sum() — terminal operation. Adds everything up and returns the total.
            .sum();
    }
}


// ---------- Entry Point ----------

public class EcommercePayloadParser {
    public static void main(String[] args) {

        // Simulate a messy real-world payload with nulls and corrupted data scattered in.
        List<Order> orders = Arrays.asList(

            // Order 1: clean mix of categories
            new Order("ORD-001", Arrays.asList(
                new Item("Laptop",      "ELECTRONICS", 1200.00),
                new Item("T-Shirt",     "CLOTHING",     35.00),
                new Item("Phone",       "ELECTRONICS",  850.00)
            )),

            // null Order — simulates a corrupted/missing JSON record in the batch
            null,

            // Order 3: some null items inside the list
            new Order("ORD-003", Arrays.asList(
                new Item("Headphones",  "ELECTRONICS",  200.00),
                null,  // corrupted item reference
                new Item("Novel",       "BOOKS",          18.00)
            )),

            // Order 4: item with null category — should be safely skipped
            new Order("ORD-004", Arrays.asList(
                new Item("Mystery Item", null,           500.00),  // null category
                new Item("Tablet",      "ELECTRONICS",   650.00)
            )),

            // Order 5: item with negative price — data corruption
            new Order("ORD-005", Arrays.asList(
                new Item("SmartWatch",  "ELECTRONICS",   -300.00), // negative price — corrupted
                new Item("Camera",      "ELECTRONICS",    950.00)
            ))
        );

        // Expected: Laptop(1200) + Phone(850) + Headphones(200) + Tablet(650) + Camera(950)
        //         = 3850.00
        // Excluded: T-Shirt (wrong category), null order, null item, Mystery Item (null category),
        //           SmartWatch (negative price), Novel (wrong category)

        RevenueCalculator calc = new RevenueCalculator();
        double revenue = calc.calculateElectronicsRevenue(orders);

        System.out.println("===== E-COMMERCE REVENUE REPORT =====\n");
        System.out.println("Total orders in payload: " + orders.size());
        System.out.printf("Electronics Revenue (excl. nulls/corrupted): $%.2f%n", revenue);
        System.out.println("\nBreakdown of ELECTRONICS items counted:");
        System.out.println("  Laptop    (ORD-001): $1200.00");
        System.out.println("  Phone     (ORD-001): $850.00");
        System.out.println("  Headphones(ORD-003): $200.00");
        System.out.println("  Tablet    (ORD-004): $650.00");
        System.out.println("  Camera    (ORD-005): $950.00");
        System.out.println("  ─────────────────────────────");
        System.out.printf("  Total:               $%.2f%n", revenue);
    }
}
