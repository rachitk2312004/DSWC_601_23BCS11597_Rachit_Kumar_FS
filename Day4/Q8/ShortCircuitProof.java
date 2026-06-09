// Day4 Q8: Short-Circuit Optimization Proof
// --------------------------------------------
// This question proves something subtle about how Java Streams evaluate data.
//
// Most people assume a stream processes ALL elements through each step before
// moving to the next:
//   "filter all 10 elements... then limit to 3... then collect"
//
// That's WRONG. Streams are LAZY and process VERTICALLY.
// Each element travels through the ENTIRE pipeline before the next element starts.
// And short-circuit operations like limit() STOP the pipeline the moment their
// condition is satisfied — no more elements are even looked at.
//
// peek() is our spy tool here. It lets us see exactly which elements were
// inspected and at what point the pipeline shut down.

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


// ---------- The Fraud Detector ----------

class FraudDetector {

    public List<Double> findFirstFraudulentTransactions(List<Double> transactions, double threshold, int limit) {
        return transactions.stream()

            // peek() is a non-interfering intermediate operation.
            // It lets us observe each element as it enters the pipeline without changing it.
            // This one fires for every element that the stream STARTS to process.
            // Once limit() is satisfied, no more elements reach this peek — proving lazy evaluation.
            .peek(amount -> System.out.println("  [INSPECTING] Transaction: $" + amount))

            // filter() checks if this transaction exceeds the fraud threshold.
            // If yes, it passes through. If no, this element is discarded here —
            // it never reaches the limit() or collect() steps.
            .filter(amount -> amount > threshold)

            // PEEK AFTER FILTER — only fires for transactions that PASSED the filter.
            // This lets us see which transactions survived the threshold check.
            .peek(amount -> System.out.println("  [ALERT] Flagged as potentially fraudulent: $" + amount))

            // limit() is a short-circuit operation.
            // The moment exactly 'limit' elements have passed through, the stream terminates.
            // Everything downstream is shut down. Elements still in the source list are
            // never even handed to the peek() step above. This is the optimization.
            .limit(limit)

            // collect() materializes the result. This is the terminal operation that
            // actually triggers the pipeline to start running (streams are lazy — nothing
            // executes until a terminal operation is called).
            .collect(Collectors.toList());
    }
}


// ---------- Entry Point ----------

public class ShortCircuitProof {
    public static void main(String[] args) {

        // 1 million records in a real system. We use a smaller list here to show the proof
        // clearly, but the principle scales to any size.
        List<Double> transactions = Arrays.asList(
             1500.0,   // below threshold
             3000.0,   // below threshold
            12000.0,   // ABOVE threshold → 1st fraud flag
             5000.0,   // below threshold
            18000.0,   // ABOVE threshold → 2nd fraud flag
             2500.0,   // below threshold
            15000.0,   // ABOVE threshold → 3rd fraud flag (limit reached HERE)
            25000.0,   // NEVER INSPECTED — pipeline shut down before reaching this
              400.0,   // NEVER INSPECTED
            11000.0    // NEVER INSPECTED
        );

        System.out.println("===== FRAUD DETECTION: SHORT-CIRCUIT PROOF =====");
        System.out.println("Total transactions in dataset: " + transactions.size());
        System.out.println("Fraud threshold: > $10,000.00");
        System.out.println("Limit: first 3 fraud hits\n");
        System.out.println("--- Stream Processing Log ---");

        FraudDetector detector = new FraudDetector();
        List<Double> fraudulentTransactions = detector.findFirstFraudulentTransactions(
                transactions, 10000.0, 3);

        System.out.println("\n--- Result ---");
        System.out.println("Fraudulent transactions found: " + fraudulentTransactions);
        System.out.println("\nKey observation: $25,000.00, $400.00, and $11,000.00 were");
        System.out.println("NEVER inspected. The pipeline stopped the moment the 3rd");
        System.out.println("fraud hit ($15,000) was found. That's short-circuit evaluation.");

        System.out.println("\n--- Why this matters at scale ---");
        System.out.println("With 1,000,000 transactions, the pipeline would stop");
        System.out.println("after processing exactly enough records to find 3 fraudulent ones.");
        System.out.println("A sort-then-limit approach would process ALL 1M records first.");
    }
}
