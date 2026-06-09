// Day4 Q10: Thread-Safe State Mutation — reduce() vs parallelStream() Race Condition
// -------------------------------------------------------------------------------------
// This question proves that parallelStream() + shared mutable state = broken results.
// Then shows the correct fix: .reduce(), which has no shared state to corrupt.
//
// Race condition: two threads read the same value (e.g., total = 50), both add to it,
// both write back. One write overwrites the other. You lose data silently.
// Every run produces a different wrong answer. That's a race condition.
//
// reduce() is pure functional: it takes two inputs, returns a new value, touches no
// external state. The JVM can safely parallelize this without any locking at all.

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


// ---------- A mutable counter — the WRONG way to accumulate in parallel ----------

// This is what junior developers write when they try to use parallelStream():
// a shared object that gets incremented by multiple threads simultaneously.
// Because increment (read + modify + write) isn't atomic, multiple threads
// can overwrite each other's work.
class UnsafeCounter {
    private int total = 0;

    // NOT synchronized — multiple threads calling this simultaneously
    // will produce race conditions. Two threads can read the same total,
    // both add to it independently, and one result gets overwritten.
    public void add(int value) {
        total += value; // this is actually three operations: read, add, write
    }

    public int getTotal() { return total; }
    public void reset()   { total = 0; }
}


// ---------- The Inventory Calculator ----------

class InventoryCalculator {

    // BROKEN VERSION — demonstrates the race condition.
    // Run this multiple times and you'll get different answers every time
    // because threads are racing to update the shared counter.
    public int calculateTotalBroken(List<Integer> quantities) {
        UnsafeCounter badCounter = new UnsafeCounter();

        // parallelStream() splits the list across multiple threads.
        // Each thread calls badCounter.add() on its chunk.
        // The problem: badCounter.total is a single int in memory.
        // Thread A reads total=50, Thread B reads total=50 at the same time.
        // Thread A writes total=55 (added 5). Thread B writes total=52 (added 2).
        // Thread B's write wins. Thread A's +5 is permanently lost. Data corrupted.
        quantities.parallelStream().forEach(qty -> badCounter.add(qty));

        return badCounter.getTotal();
    }


    // CORRECT VERSION — using reduce() for pure, side-effect-free aggregation.
    // This is thread-safe by design, not by synchronization.
    public int calculateTotalSafe(List<Integer> quantities) {
        return quantities.parallelStream()

            // reduce(identity, accumulator) is the correct way to aggregate in parallel.
            //
            // Identity: the starting value — here 0, because 0 + anything = anything.
            //   It's called "identity" because applying the operation with this value
            //   doesn't change the other operand. For addition, 0 is the identity.
            //   For multiplication, 1 would be the identity.
            //
            // Accumulator: a PURE function — takes two inputs, returns a new value,
            //   modifies NOTHING external. (acc, curr) -> acc + curr is pure.
            //   No shared state. No side effects. No race conditions possible.
            //
            // With parallelStream(), the JVM splits the list, reduces each chunk
            // independently on separate threads, then merges the chunk results.
            // Because the accumulator is pure, this is mathematically safe to parallelize.
            .reduce(0, (acc, curr) -> acc + curr);
    }


    // ALSO CORRECT — method reference version, even cleaner
    public int calculateTotalClean(List<Integer> quantities) {
        return quantities.parallelStream()
            .reduce(0, Integer::sum); // Integer.sum(a, b) is just a + b, but named
    }
}


// ---------- Entry Point ----------

public class ReduceVsRaceCondition {
    public static void main(String[] args) throws InterruptedException {

        // Use enough numbers that parallel processing actually splits them
        List<Integer> quantities = Arrays.asList(
            10, 25, 15, 40, 30, 5, 20, 35, 50, 45,
            12, 18, 22, 8, 60, 3, 17, 44, 9, 11
        );

        // Expected total: 10+25+15+40+30+5+20+35+50+45+12+18+22+8+60+3+17+44+9+11 = 479

        int expected = quantities.stream().reduce(0, Integer::sum); // sequential, guaranteed correct

        System.out.println("===== INVENTORY COUNTER: RACE CONDITION DEMO =====\n");
        System.out.println("Items: " + quantities.size());
        System.out.println("Expected total: " + expected);

        InventoryCalculator calc = new InventoryCalculator();

        // Run the broken version multiple times to show it produces different (wrong) results.
        System.out.println("\n--- BROKEN (parallelStream + mutable counter) ---");
        System.out.println("Running 5 times. With a race condition, results will vary:");
        for (int i = 1; i <= 5; i++) {
            int broken = calc.calculateTotalBroken(quantities);
            String verdict = broken == expected ? "correct (got lucky)" : "WRONG (race condition)";
            System.out.println("  Run " + i + ": " + broken + " → " + verdict);
        }

        // The safe version always produces the correct result, every single time.
        System.out.println("\n--- CORRECT (parallelStream + reduce) ---");
        System.out.println("Running 5 times. reduce() is always consistent:");
        for (int i = 1; i <= 5; i++) {
            int safe = calc.calculateTotalSafe(quantities);
            System.out.println("  Run " + i + ": " + safe + " → "
                    + (safe == expected ? "correct ✓" : "wrong ✗"));
        }

        // Also show sequential stream for comparison
        System.out.println("\n--- SEQUENTIAL .stream().reduce() ---");
        int sequential = quantities.stream().reduce(0, Integer::sum);
        System.out.println("  Result: " + sequential + " (sequential streams are always correct but single-threaded)");

        System.out.println("\n--- THE RULE ---");
        System.out.println("If you use parallelStream(), NEVER modify shared mutable state.");
        System.out.println("Use reduce(), collect(), or other purely functional terminal ops instead.");
    }
}
