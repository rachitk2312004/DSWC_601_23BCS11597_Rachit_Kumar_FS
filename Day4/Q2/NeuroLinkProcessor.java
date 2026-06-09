// Day4 Q2: NeuroLink Memory Engram Sorter
// ------------------------------------------
// Same structural pattern as Q1 — custom functional interfaces injected into
// a stream pipeline — but with different business rules and domain language.
// The new wrinkle: the validator uses OR logic (corrupted OR over-clearance),
// while Q1 used AND logic. Also introduces clarity score filtering, similar
// to the value filter in Q1.
//
// Good practice question because the pattern is the same but the rules force
// you to think about the logic independently rather than copying.

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


// ---------- Engram Hierarchy ----------

// Every memory in the system has these three base properties.
// isCorrupted is the most basic safety check — always filter these first.
// clarityScore determines whether the memory is legible enough to process.
abstract class MemoryEngram {
    protected String engramId;
    protected double clarityScore; // 0.0 to 100.0 — how clear/readable the memory is
    protected boolean isCorrupted;

    public MemoryEngram(String engramId, double clarityScore, boolean isCorrupted) {
        this.engramId = engramId;
        this.clarityScore = clarityScore;
        this.isCorrupted = isCorrupted;
    }

    public String getEngramId()      { return engramId; }
    public double getClarityScore()  { return clarityScore; }
    public boolean isCorrupted()     { return isCorrupted; }
}


// Standard memories — personal, everyday, unrestricted.
// No additional fields beyond what the base class provides.
class StandardEngram extends MemoryEngram {
    public StandardEngram(String engramId, double clarityScore, boolean isCorrupted) {
        super(engramId, clarityScore, isCorrupted);
    }
}


// Classified memories have a security clearance level attached.
// If the clearance level is too high, the system can't access them — they're locked.
// This extra field is what the validator lambda needs to downcast to check.
class ClassifiedEngram extends MemoryEngram {
    private int securityClearanceLevel; // 1-5, higher = more restricted

    public ClassifiedEngram(String engramId, double clarityScore,
                            boolean isCorrupted, int securityClearanceLevel) {
        super(engramId, clarityScore, isCorrupted);
        this.securityClearanceLevel = securityClearanceLevel;
    }

    public int getSecurityClearanceLevel() { return securityClearanceLevel; }
}


// ---------- Custom Functional Interfaces ----------

// Same concept as CargoInspector in Q1 — one abstract method, named for the domain.
// Returns true if the engram is SAFE to process.
@FunctionalInterface
interface EngramValidator {
    boolean validate(MemoryEngram engram);
}

// Same concept as CargoCompressor — takes an engram, returns its text representation.
@FunctionalInterface
interface EngramTranslator {
    String translate(MemoryEngram engram);
}


// ---------- The Processing Engine ----------

// Again, this class knows nothing about what "safe" means or what the output format is.
// It just wires the stream pipeline together. Rules come in from outside via lambdas.
class CortexProcessor {

    public List<String> processMemories(List<MemoryEngram> engrams,
                                        EngramValidator validator,
                                        EngramTranslator translator) {
        return engrams.stream()

            // Step 1: Safety filter — the validator lambda decides what gets through.
            // Corrupted memories and over-clearance classified ones get dropped here.
            .filter(engram -> validator.validate(engram))

            // Step 2: Clarity filter — too blurry to read, not worth translating.
            // 50.0 is the threshold. Exactly 50.0 passes (>= not >).
            .filter(engram -> engram.getClarityScore() >= 50.0)

            // Step 3: Translate — convert surviving engrams to their text format.
            .map(engram -> translator.translate(engram))

            // Step 4: Collect into a list and return.
            .collect(Collectors.toList());
    }
}


// ---------- Entry Point ----------

public class NeuroLinkProcessor {
    public static void main(String[] args) {

        // Mix of standard and classified engrams, some corrupted, varying clarity scores.
        List<MemoryEngram> engrams = Arrays.asList(
            new StandardEngram("E-001",  85.5, false),             // clean, clear         → included
            new StandardEngram("E-002",  40.0, false),             // clean but too blurry → filtered by clarity
            new StandardEngram("E-003",  70.0, true),              // corrupted            → REJECTED
            new ClassifiedEngram("E-004", 90.0, false, 2),         // clearance 2 — OK     → included
            new ClassifiedEngram("E-005", 88.0, false, 5),         // clearance 5 — too high → REJECTED
            new ClassifiedEngram("E-006", 95.0, true, 1),          // corrupted AND classified → REJECTED
            new StandardEngram("E-007",  62.0, false),             // clean, clear         → included
            new ClassifiedEngram("E-008", 55.0, false, 3),         // clearance exactly 3 — OK (not > 3) → included
            new ClassifiedEngram("E-009", 48.5, false, 1),         // clearance fine, too blurry → filtered
            new StandardEngram("E-010",  99.0, false)              // perfect memory       → included
        );

        // THE VALIDATOR LAMBDA
        // Rule: unsafe if EITHER corrupted OR (classified with clearance level > 3).
        // This is OR logic — either condition alone is enough to reject.
        // Note: we check isCorrupted FIRST. If it's corrupted, we short-circuit and reject
        // immediately without even checking if it's classified. Efficient.
        EngramValidator securityValidator = engram -> {
            if (engram.isCorrupted()) {
                return false; // corrupted memories are always rejected
            }
            if (engram instanceof ClassifiedEngram) {
                ClassifiedEngram classified = (ClassifiedEngram) engram;
                return classified.getSecurityClearanceLevel() <= 3; // 1-3 OK, 4-5 rejected
            }
            return true; // standard non-corrupted engrams are always safe
        };

        // THE TRANSLATOR LAMBDA
        // Format: "ENGRAM-[ID] | CLARITY: [Score]%"
        // The clarityScore is printed as-is (double), no truncation needed here.
        EngramTranslator digitalTranslator = engram ->
            "ENGRAM-" + engram.getEngramId()
            + " | CLARITY: " + engram.getClarityScore() + "%";

        CortexProcessor cortex = new CortexProcessor();
        List<String> dashboard = cortex.processMemories(engrams, securityValidator, digitalTranslator);

        System.out.println("===== NEUROLINK MEMORY DASHBOARD =====\n");
        System.out.println("Total engrams submitted: " + engrams.size());
        System.out.println("Safe engrams processed:  " + dashboard.size());
        System.out.println("\n--- Translated Memories ---");
        dashboard.forEach(System.out::println);
    }
}
