// Day4 Q9: Functional Interface Chaining — Banking Rule Engine
// --------------------------------------------------------------
// New concept: Predicate chaining with .and(), .or(), .negate().
//
// Instead of one giant if/else block with three conditions jammed together,
// we create three separate Predicate objects — one per rule — and chain them.
// Each rule is independently testable and understandable.
// The composite predicate reads almost like an English sentence.
//
// This is the functional equivalent of the Strategy Pattern but for validation rules.

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;


// ---------- Domain Class ----------

class BankUser {
    private String name;
    private int age;
    private boolean isActive;
    private String email;

    public BankUser(String name, int age, boolean isActive, String email) {
        this.name = name;
        this.age = age;
        this.isActive = isActive;
        this.email = email;
    }

    public String getName()    { return name; }
    public int getAge()        { return age; }
    public boolean isActive()  { return isActive; }
    public String getEmail()   { return email; }

    @Override
    public String toString() {
        return name + " (age=" + age + ", active=" + isActive + ", email=" + email + ")";
    }
}


// ---------- The Rule Engine ----------

class BankingRuleEngine {

    public void evaluateUsers(List<BankUser> users) {

        // Rule 1: Must be 18 or older to open an account.
        // Isolated as its own Predicate — if this rule changes, you change one line.
        Predicate<BankUser> isAdult = user -> user.getAge() >= 18;

        // Rule 2: Account must be active (not suspended or pending).
        Predicate<BankUser> isActive = user -> user.isActive();

        // Rule 3: Must use a corporate email domain — only enterprise customers qualify.
        // contains() is used here for simplicity. In production you'd use regex.
        Predicate<BankUser> isCorporate = user -> user.getEmail().contains("@enterprise.com");

        // Chain the three rules into one composite predicate using .and().
        // .and() says "this AND that" — ALL conditions must return true.
        // The chaining creates a new Predicate that represents the full set of rules.
        //
        // This reads almost like English: "is adult AND is active AND is corporate"
        // A flat if statement with three conditions doesn't read this clearly.
        Predicate<BankUser> compositeRuleEngine = isAdult.and(isActive).and(isCorporate);

        System.out.println("===== BANKING USER ELIGIBILITY ENGINE =====\n");
        System.out.println("Rules applied:");
        System.out.println("  1. Must be 18 or older");
        System.out.println("  2. Account must be active");
        System.out.println("  3. Email must contain @enterprise.com");
        System.out.println();

        // Now apply the composite rule to each user.
        // .test() is the single abstract method of Predicate — it triggers the full chain.
        for (BankUser user : users) {
            boolean eligible = compositeRuleEngine.test(user);
            System.out.printf("  %-35s → %s%n",
                user.toString(),
                eligible ? "✓ ELIGIBLE" : "✗ REJECTED"
            );
        }

        System.out.println("\n--- Using stream pipeline with composite predicate ---");
        System.out.println("Eligible users:");
        users.stream()
            .filter(compositeRuleEngine)   // compositeRuleEngine IS a Predicate — works directly in filter()
            .map(BankUser::getName)
            .forEach(name -> System.out.println("  " + name));

        // Bonus: demonstrate .negate() — all INELIGIBLE users
        System.out.println("\nIneligible users:");
        users.stream()
            .filter(compositeRuleEngine.negate())  // flip the result of the entire chain
            .map(BankUser::getName)
            .forEach(name -> System.out.println("  " + name));
    }
}


// ---------- Entry Point ----------

public class BankingRuleEngineDemo {
    public static void main(String[] args) {

        List<BankUser> users = Arrays.asList(
            // Name                Age  Active  Email
            new BankUser("Alice Chen",      25, true,  "alice@enterprise.com"),  // ALL rules pass → eligible
            new BankUser("Bob Junior",      16, true,  "bob@enterprise.com"),    // underage → rejected
            new BankUser("Carol Webb",      30, false, "carol@enterprise.com"),  // inactive → rejected
            new BankUser("Dan Kumar",       28, true,  "dan@gmail.com"),         // wrong domain → rejected
            new BankUser("Eve Martinez",    22, true,  "eve@enterprise.com"),    // ALL rules pass → eligible
            new BankUser("Frank Zhao",      17, false, "frank@enterprise.com"),  // underage + inactive → rejected
            new BankUser("Grace Okonkwo",   35, true,  "grace@enterprise.com")  // ALL rules pass → eligible
        );

        BankingRuleEngine engine = new BankingRuleEngine();
        engine.evaluateUsers(users);
    }
}
