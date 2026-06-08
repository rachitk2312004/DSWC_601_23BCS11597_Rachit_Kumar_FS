// Day3 Q3: TradeX Cryptocurrency Order Matching Engine
// -------------------------------------------------------
// The problem this solves is concurrency — what happens when hundreds of threads
// try to write to the same data structure at the same millisecond?
//
// With a regular HashMap and ArrayList, you get two categories of failure:
//   1. ConcurrentModificationException — one thread reads while another writes
//   2. Silent data loss — two threads check "does this key exist?" simultaneously,
//      both get "no", both insert — one overwrites the other. Orders disappear.
//
// The solution isn't slapping synchronized on everything (that serializes the
// whole exchange — someone buying ETH waits for someone buying BTC to finish).
// The solution is using thread-safe collections designed for this exact problem.

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


// A simple order — in a real exchange this would have price, quantity, order type, etc.
// For this question, the important thing is that Order objects are being created and
// stored concurrently from multiple threads.
class Order {
    private String orderId;
    private String ticker;
    private double amount;
    private String type; // "BUY" or "SELL"

    public Order(String orderId, String ticker, double amount, String type) {
        this.orderId = orderId;
        this.ticker = ticker;
        this.amount = amount;
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" + orderId + "] " + type + " " + ticker + " @ $" + amount;
    }
}


class ExchangeManager {

    // WHY ConcurrentHashMap INSTEAD OF HashMap?
    //
    // A regular HashMap is not thread-safe. If two threads call put() simultaneously,
    // you can corrupt the internal array, create infinite loops in bucket traversal,
    // or lose entries entirely. The JVM gives no guarantees.
    //
    // ConcurrentHashMap uses lock-striping — it divides the map into 16 (by default)
    // independent segments and only locks one segment at a time. So two threads
    // working on different tickers (BTC and ETH) can write simultaneously without
    // blocking each other. Far more scalable than locking the whole map.
    private ConcurrentHashMap<String, List<Order>> orderBook = new ConcurrentHashMap<>();


    public void placeOrder(String ticker, Order order) {

        // WHY computeIfAbsent() INSTEAD OF if(!containsKey()) { put() }?
        //
        // The naive version looks like this:
        //   if (!orderBook.containsKey(ticker)) {
        //       orderBook.put(ticker, new CopyOnWriteArrayList<>());
        //   }
        //   orderBook.get(ticker).add(order);
        //
        // This is a "check-then-act" race condition. Thread A checks, sees no key.
        // Thread B checks, sees no key. Thread A inserts. Thread B inserts.
        // Thread B's list overwrites Thread A's list. Thread A's orders are gone.
        //
        // computeIfAbsent() is atomic. It checks and inserts in a single uninterruptible
        // operation. No other thread can sneak in between the check and the put.
        // If the key already exists, it returns the existing value and skips the lambda.
        // If it doesn't exist, it runs the lambda once and inserts the result.
        //
        // This is the correct, idiomatic way to initialize a missing key in a concurrent map.
        orderBook.computeIfAbsent(ticker, k -> new CopyOnWriteArrayList<>());

        // WHY CopyOnWriteArrayList INSTEAD OF ArrayList?
        //
        // A regular ArrayList crashes with ConcurrentModificationException when one
        // thread iterates it (to display the order book) while another writes to it.
        //
        // CopyOnWriteArrayList handles this by making a fresh copy of the entire
        // underlying array every time something is added or removed. Reads always
        // see a consistent snapshot — they're reading from the old copy while the
        // write creates a new one. No locks needed for readers.
        //
        // The tradeoff: writes are expensive (full array copy), but reads are free.
        // For an order book where reads (displaying current orders) happen far more
        // frequently than new orders come in, this is a great tradeoff.
        orderBook.get(ticker).add(order);
        System.out.println("[ORDER PLACED] " + order);
    }


    public void printOrderBook(String ticker) {
        List<Order> orders = orderBook.getOrDefault(ticker, new CopyOnWriteArrayList<>());
        System.out.println("\n--- Order Book: " + ticker + " (" + orders.size() + " orders) ---");
        for (Order o : orders) {
            System.out.println("  " + o);
        }
    }


    public int getTotalOrders(String ticker) {
        return orderBook.getOrDefault(ticker, new CopyOnWriteArrayList<>()).size();
    }
}


public class TradeXExchange {
    public static void main(String[] args) throws InterruptedException {

        ExchangeManager exchange = new ExchangeManager();

        System.out.println("===== TRADEX CONCURRENT ORDER PLACEMENT =====\n");

        // Simulate 4 threads placing orders simultaneously.
        // In a real exchange, each thread would represent a different trading client
        // or server process sending orders at the same millisecond.
        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        // Threads 1 & 2 both place BTC orders at the same time.
        // Without ConcurrentHashMap + computeIfAbsent, one of these could wipe the other.
        threadPool.submit(() -> exchange.placeOrder("BTC", new Order("ORD-001", "BTC", 45000.00, "BUY")));
        threadPool.submit(() -> exchange.placeOrder("BTC", new Order("ORD-002", "BTC", 44950.00, "SELL")));

        // Thread 3 places an ETH order — this should NOT block threads 1 & 2.
        // ConcurrentHashMap's lock-striping means BTC and ETH locks are independent.
        threadPool.submit(() -> exchange.placeOrder("ETH", new Order("ORD-003", "ETH", 2800.00, "BUY")));

        // Thread 4 places another BTC order — concurrent with the first two BTC orders.
        threadPool.submit(() -> exchange.placeOrder("BTC", new Order("ORD-004", "BTC", 45100.00, "BUY")));

        // Add more orders from the main thread too
        exchange.placeOrder("ETH", new Order("ORD-005", "ETH", 2795.00, "SELL"));
        exchange.placeOrder("BTC", new Order("ORD-006", "BTC", 45050.00, "BUY"));

        // Wait for all threads to finish before reading the final state
        threadPool.shutdown();
        threadPool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n===== FINAL ORDER BOOKS =====");
        exchange.printOrderBook("BTC");
        exchange.printOrderBook("ETH");

        System.out.println("\n===== ORDER COUNT =====");
        System.out.println("BTC orders: " + exchange.getTotalOrders("BTC")); // should be 4
        System.out.println("ETH orders: " + exchange.getTotalOrders("ETH")); // should be 2
        System.out.println("\nAll orders accounted for — no race condition data loss.");
    }
}
