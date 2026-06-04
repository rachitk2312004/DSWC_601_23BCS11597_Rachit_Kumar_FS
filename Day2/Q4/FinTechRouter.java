// ============================================================
// Q4: FinTech Payment Routing Engine
// Concepts: Strategy Pattern, Composition over Inheritance,
//           Open/Closed Principle (SOLID), Runtime Behaviour Swap
// ============================================================

// ---------- Strategy Interface ----------
interface PaymentStrategy {
    /**
     * Processes a payment of the given amount.
     * @return true if the payment was successful, false otherwise.
     */
    boolean processPayment(double amount);
}

// ---------- Concrete Strategy 1: Credit Card ----------
class CreditCardStrategy implements PaymentStrategy {

    private String cardNumber;
    private String cardHolderName;

    public CreditCardStrategy(String cardHolderName, String cardNumber) {
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.printf("  [CREDIT CARD] Charging $%.2f to card ending in %s "
                + "(Holder: %s)...%n", amount,
                cardNumber.substring(cardNumber.length() - 4), cardHolderName);
        System.out.println("  [CREDIT CARD] Transaction approved by payment network.");
        return true;
    }
}

// ---------- Concrete Strategy 2: Cryptocurrency ----------
class CryptoStrategy implements PaymentStrategy {

    private String walletAddress;
    private String coinType;

    public CryptoStrategy(String coinType, String walletAddress) {
        this.coinType = coinType;
        this.walletAddress = walletAddress;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.printf("  [CRYPTO] Sending $%.2f worth of %s to wallet %s...%n",
                amount, coinType, walletAddress);
        System.out.println("  [CRYPTO] Transaction broadcast to blockchain. Awaiting confirmation.");
        return true;
    }
}

// ---------- Concrete Strategy 3: PayPal (extension — zero changes to core) ----------
class PayPalStrategy implements PaymentStrategy {

    private String email;

    public PayPalStrategy(String email) {
        this.email = email;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.printf("  [PAYPAL] Charging $%.2f to PayPal account: %s...%n",
                amount, email);
        System.out.println("  [PAYPAL] Payment processed via PayPal gateway.");
        return true;
    }
}

// ---------- Context Class: TransactionProcessor ----------
class TransactionProcessor {

    // Composition — holds a REFERENCE to the strategy, not an inheritance chain
    private PaymentStrategy strategy;

    // Constructor injection
    public TransactionProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    // Setter injection — allows runtime swap
    public void setPaymentStrategy(PaymentStrategy strategy) {
        System.out.println("  [PROCESSOR] Switching payment strategy to: "
                + strategy.getClass().getSimpleName());
        this.strategy = strategy;
    }

    // Delegates entirely to whichever strategy is currently loaded
    public void executeTransaction(double amount) {
        System.out.printf("%n[TRANSACTION] Processing payment of $%.2f...%n", amount);
        boolean success = strategy.processPayment(amount);
        if (success) {
            System.out.printf("[TRANSACTION] SUCCESS — $%.2f processed.%n", amount);
        } else {
            System.out.printf("[TRANSACTION] FAILED — $%.2f was not processed.%n", amount);
        }
    }
}

// ---------- Entry Point ----------
public class FinTechRouter {
    public static void main(String[] args) {

        System.out.println("===== FINTECH PAYMENT ROUTING ENGINE =====");

        // Initial strategy: Credit Card
        PaymentStrategy ccStrategy =
                new CreditCardStrategy("Venkatesh Kumar", "4111111111111234");

        TransactionProcessor processor = new TransactionProcessor(ccStrategy);
        processor.executeTransaction(299.99);

        // Runtime swap → Crypto (strategy pattern in action)
        processor.setPaymentStrategy(
                new CryptoStrategy("Bitcoin", "1A2b3C4d5E6f7G8h9I0j"));
        processor.executeTransaction(1499.00);

        // Runtime swap → PayPal (new strategy, ZERO changes to TransactionProcessor)
        processor.setPaymentStrategy(new PayPalStrategy("user@example.com"));
        processor.executeTransaction(75.50);

        System.out.println("\n===== ENGINE SHUTDOWN =====");
    }
}
