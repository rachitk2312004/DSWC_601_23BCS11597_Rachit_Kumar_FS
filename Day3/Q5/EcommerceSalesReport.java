// Day3 Q5: Global E-Commerce Sales Analyzer
// --------------------------------------------
// The constraint here is strict: no for loops, no while loops, no if statements.
// You must use the Java 8 Stream API to filter, transform, and aggregate data.
//
// This might feel restrictive but it's actually liberating — the declarative
// approach is shorter, more readable, and can be parallelized with a one-word change.
// A for-loop with nested ifs doing the same thing would be 10+ lines.
// The Stream version is 4 chained method calls.

import java.util.Arrays;
import java.util.List;


// Transaction represents one order in the system.
// In a real backend this would come from a database query result.
class Transaction {

    private String transactionId;
    private String status;   // "COMPLETED", "PENDING", "REFUNDED", etc.
    private String category; // "ELECTRONICS", "CLOTHING", "BOOKS", etc.
    private double amount;   // revenue in dollars

    public Transaction(String transactionId, String status, String category, double amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.category = category;
        this.amount = amount;
    }

    // Getters — these are what the Stream pipeline calls
    public String getTransactionId() { return transactionId; }
    public String getStatus()        { return status; }
    public String getCategory()      { return category; }
    public double getAmount()        { return amount; }

    @Override
    public String toString() {
        return "[" + transactionId + "] " + status + " | " + category + " | $" + amount;
    }
}


class SalesAnalyzer {

    // This method calculates total revenue from COMPLETED ELECTRONICS transactions.
    // Notice: no loops, no ifs, no mutable accumulator variable outside the pipeline.
    // Everything is expressed as a series of operations on a stream of data.
    public double calculateElectronicsRevenue(List<Transaction> transactions) {

        return transactions.stream()

            // filter() passes through only the transactions we care about.
            // Think of it as a WHERE clause in SQL.
            // Each lambda here is the "no if statement" version of an if check.
            // t -> "COMPLETED".equals(t.getStatus()) is exactly equivalent to:
            //   if (t.getStatus().equals("COMPLETED")) { keep it } else { discard it }
            // We write it this way to satisfy the "no if statements" constraint —
            // the logic is identical, just expressed as a predicate.
            .filter(t -> "COMPLETED".equals(t.getStatus()))
            .filter(t -> "ELECTRONICS".equals(t.getCategory()))

            // mapToDouble() converts each Transaction into a primitive double —
            // specifically, its amount. This creates a DoubleStream instead of
            // a Stream<Transaction>. We need DoubleStream to call .sum() at the end.
            // mapToDouble is more efficient than map() + reduce() for numeric work
            // because it avoids boxing/unboxing to Double objects.
            .mapToDouble(Transaction::getAmount)

            // sum() is a terminal operation — it consumes the stream and returns one result.
            // Internally it adds up all the doubles. Simple, built-in, no manual accumulator.
            .sum();
    }


    // Bonus: the exact same logic but using parallelStream() instead of stream().
    // This one line change distributes the work across all available CPU cores.
    // For millions of transactions, this can be dramatically faster.
    // Try doing that safely with a for-loop and a shared mutable double total — you can't.
    public double calculateElectronicsRevenueParallel(List<Transaction> transactions) {
        return transactions.parallelStream()
            .filter(t -> "COMPLETED".equals(t.getStatus()))
            .filter(t -> "ELECTRONICS".equals(t.getCategory()))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }


    // Additional: revenue breakdown per category — shows how streams compose naturally
    // for more complex aggregations too.
    public void printRevenueByCategory(List<Transaction> transactions) {
        System.out.println("\n--- Revenue by Category (COMPLETED only) ---");

        // Get distinct categories first
        transactions.stream()
            .filter(t -> "COMPLETED".equals(t.getStatus()))
            .map(Transaction::getCategory)
            .distinct()
            .sorted()
            .forEach(category -> {
                double total = transactions.stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .filter(t -> category.equals(t.getCategory()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
                System.out.printf("  %-15s $%.2f%n", category, total);
            });
    }
}


public class EcommerceSalesReport {
    public static void main(String[] args) {

        // A mixed batch of transactions — different statuses, different categories.
        // The analyzer should only count COMPLETED + ELECTRONICS ones.
        List<Transaction> transactions = Arrays.asList(
            new Transaction("TXN-001", "COMPLETED", "ELECTRONICS",  1200.00), // ✓ counts
            new Transaction("TXN-002", "COMPLETED", "CLOTHING",      350.00), // wrong category
            new Transaction("TXN-003", "PENDING",   "ELECTRONICS",   800.00), // wrong status
            new Transaction("TXN-004", "COMPLETED", "ELECTRONICS",  2500.00), // ✓ counts
            new Transaction("TXN-005", "REFUNDED",  "ELECTRONICS",   600.00), // wrong status
            new Transaction("TXN-006", "COMPLETED", "BOOKS",          45.00), // wrong category
            new Transaction("TXN-007", "COMPLETED", "ELECTRONICS",  3100.00), // ✓ counts
            new Transaction("TXN-008", "COMPLETED", "ELECTRONICS",   750.00), // ✓ counts
            new Transaction("TXN-009", "PENDING",   "CLOTHING",      200.00), // both wrong
            new Transaction("TXN-010", "COMPLETED", "ELECTRONICS",  1850.00)  // ✓ counts
        );

        // Expected: TXN-001 + TXN-004 + TXN-007 + TXN-008 + TXN-010
        //         = 1200 + 2500 + 3100 + 750 + 1850 = 9400.00

        System.out.println("===== ECOMMERCE SALES ANALYZER =====\n");
        System.out.println("Total transactions in batch: " + transactions.size());

        SalesAnalyzer analyzer = new SalesAnalyzer();

        double revenue = analyzer.calculateElectronicsRevenue(transactions);
        System.out.printf("%nCompleted Electronics Revenue (sequential): $%.2f%n", revenue);

        double revenueParallel = analyzer.calculateElectronicsRevenueParallel(transactions);
        System.out.printf("Completed Electronics Revenue (parallel):   $%.2f%n", revenueParallel);
        // Both should produce exactly 9400.00

        analyzer.printRevenueByCategory(transactions);

        System.out.println("\n===== REPORT COMPLETE =====");
    }
}
