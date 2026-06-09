// Day4 Q1: Galactic Cargo Manifest Processor
// ---------------------------------------------
// This question is about custom functional interfaces and injecting them
// into a Stream pipeline. The core idea: instead of hardcoding business rules
// inside your processing engine, you pass them in as lambdas. The engine stays
// generic and reusable. The rules live outside it.
//
// Two OOP concepts collide here: abstract classes + instanceof for the cargo
// hierarchy, and @FunctionalInterface + lambdas for the processing rules.

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


// ---------- Cargo Hierarchy ----------

// Abstract base class — every cargo container in the fleet has these three fields.
// Making it abstract means no one can create a raw "Cargo" — it must be Standard or Biological.
abstract class Cargo {
    protected String containerId;
    protected double valueInCredits;
    protected boolean isHazardous;

    public Cargo(String containerId, double valueInCredits, boolean isHazardous) {
        this.containerId = containerId;
        this.valueInCredits = valueInCredits;
        this.isHazardous = isHazardous;
    }

    public String getContainerId()    { return containerId; }
    public double getValueInCredits() { return valueInCredits; }
    public boolean isHazardous()      { return isHazardous; }
}


// Standard cargo — nothing special, just inherits the base fields.
// Could be machinery, textiles, food — anything non-biological.
class StandardCargo extends Cargo {
    public StandardCargo(String containerId, double valueInCredits, boolean isHazardous) {
        super(containerId, valueInCredits, isHazardous);
    }
}


// Biological cargo has one extra concern: is it properly shielded?
// An unshielded hazardous biological container is a biohazard risk.
// This extra field is what the inspector lambda will need to downcast to check.
class BiologicalCargo extends Cargo {
    private boolean isShielded;

    public BiologicalCargo(String containerId, double valueInCredits,
                           boolean isHazardous, boolean isShielded) {
        super(containerId, valueInCredits, isHazardous);
        this.isShielded = isShielded;
    }

    public boolean isShielded() { return isShielded; }
}


// ---------- Custom Functional Interfaces ----------

// A functional interface is just a regular interface with exactly ONE abstract method.
// The @FunctionalInterface annotation enforces this — the compiler will yell if you
// accidentally add a second abstract method. It also signals intent to other developers.
//
// CargoInspector is behaviorally identical to Java's built-in Predicate<Cargo>.
// We define our own to make the domain language explicit. When you see CargoInspector
// in a method signature, you know immediately it's about cargo safety — not just "any predicate."
@FunctionalInterface
interface CargoInspector {
    // Implementors return true if the cargo is SAFE to transport, false if it should be rejected.
    boolean inspect(Cargo cargo);
}


// CargoCompressor is behaviorally identical to Java's built-in Function<Cargo, String>.
// Same reasoning — the name makes the intent obvious in context.
@FunctionalInterface
interface CargoCompressor {
    // Takes a cargo object and returns its compressed telemetry string.
    String compress(Cargo cargo);
}


// ---------- The Processing Engine ----------

// This class knows NOTHING about the actual business rules.
// It doesn't know what "safe" means. It doesn't know the compression format.
// That knowledge is injected from outside via the lambda parameters.
// This is the Open/Closed Principle — the engine is closed for modification
// but open for extension via new lambdas.
class ManifestProcessor {

    public List<String> processManifest(List<Cargo> manifest,
                                        CargoInspector inspector,
                                        CargoCompressor compressor) {
        return manifest.stream()

            // Step 1: Safety filter — delegate entirely to the injected inspector lambda.
            // The engine doesn't know what makes cargo safe. The caller decides that.
            .filter(cargo -> inspector.inspect(cargo))

            // Step 2: Value filter — hardcoded business rule: not worth the fuel if < 1000.
            // This one is baked in because it's a universal economic rule for the fleet.
            .filter(cargo -> cargo.getValueInCredits() >= 1000.0)

            // Step 3: Compress — delegate to the injected compressor lambda.
            // The format is decided by the caller, not by this engine.
            .map(cargo -> compressor.compress(cargo))

            // Step 4: Collect — terminal operation, produces the final List<String>.
            .collect(Collectors.toList());
    }
}


// ---------- Entry Point ----------

public class GalacticCargoProcessor {
    public static void main(String[] args) {

        // Build a diverse manifest — standard, biological, safe, hazardous, low-value.
        List<Cargo> manifest = Arrays.asList(
            new StandardCargo("ALPHA-99",  5000.50, false),   // safe, high value     → included
            new StandardCargo("BETA-12",    800.00, false),   // safe, too cheap       → filtered by value
            new StandardCargo("GAMMA-55",  3200.00, true),    // hazardous standard    → safe (not biological)
            new BiologicalCargo("DELTA-01", 7500.00, true, true),  // hazardous but shielded → safe
            new BiologicalCargo("EPSILON-7",4200.00, true, false), // hazardous, unshielded  → REJECTED
            new BiologicalCargo("ZETA-88",  2100.00, false, false),// not hazardous           → safe
            new StandardCargo("ETA-33",   12000.00, false),   // safe, high value     → included
            new BiologicalCargo("THETA-05",  500.00, false, true)  // safe but too cheap → filtered by value
        );

        // THE INSPECTOR LAMBDA
        // Rule: cargo is UNSAFE only if it's hazardous AND biological AND unshielded.
        // Any other combination is cleared for transport.
        // Notice we use instanceof to downcast only when necessary — classic OOP inside a lambda.
        CargoInspector safetyInspector = cargo -> {
            if (cargo.isHazardous() && cargo instanceof BiologicalCargo) {
                // We know it's BiologicalCargo now — safe to cast and check the shield
                BiologicalCargo bio = (BiologicalCargo) cargo;
                return bio.isShielded(); // shielded = safe, unshielded = REJECT
            }
            return true; // everything else passes
        };

        // THE COMPRESSOR LAMBDA
        // Format: first 4 chars of ID + "-" + integer value
        // "ALPHA-99" with 5000.50 → "ALPH-5000"
        // substring(0, 4) takes the first 4 characters.
        // (int) casts the double to an integer, dropping the decimal.
        CargoCompressor telemetryCompressor = cargo ->
            cargo.getContainerId().substring(0, 4) + "-" + (int) cargo.getValueInCredits();

        ManifestProcessor processor = new ManifestProcessor();
        List<String> transmissions = processor.processManifest(manifest, safetyInspector, telemetryCompressor);

        System.out.println("===== ANDROMEDA FREIGHT — CARGO TRANSMISSION =====\n");
        System.out.println("Containers processed: " + manifest.size());
        System.out.println("Transmission packets: " + transmissions.size());
        System.out.println("\n--- Compressed Telemetry ---");
        transmissions.forEach(System.out::println);
    }
}
