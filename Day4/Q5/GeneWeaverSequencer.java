// Day4 Q5: GeneWeaver DNA Sequencer
// ------------------------------------
// The new concept here is Collectors.groupingBy() with a downstream
// Collectors.mapping() collector. This is the most advanced collector combo
// in the Java Streams API.
//
// Instead of filtering → mapping → collecting into a flat list,
// we filter → group into a Map → and simultaneously transform the values as we group.
// The result: Map<String, List<String>> where the key is the class name
// ("HumanSample" or "AlienSample") and the value is a list of formatted strings.

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// ---------- DNA Sample Hierarchy ----------

abstract class DNASample {
    protected String sampleId;
    protected double purityPercentage; // 0.0 to 100.0 — how clean the sample is

    public DNASample(String sampleId, double purityPercentage) {
        this.sampleId = sampleId;
        this.purityPercentage = purityPercentage;
    }

    public String getSampleId()          { return sampleId; }
    public double getPurityPercentage()  { return purityPercentage; }
}


// Human genetic sample — identified by blood type.
// The mapper lambda will need to downcast to access this field.
class HumanSample extends DNASample {
    private String bloodType;

    public HumanSample(String sampleId, double purityPercentage, String bloodType) {
        super(sampleId, purityPercentage);
        this.bloodType = bloodType;
    }

    public String getBloodType() { return bloodType; }
}


// Alien genetic sample — silicon-based life forms are a distinct category.
// The mapper lambda will downcast to check this flag.
class AlienSample extends DNASample {
    private boolean isSiliconBased;

    public AlienSample(String sampleId, double purityPercentage, boolean isSiliconBased) {
        super(sampleId, purityPercentage);
        this.isSiliconBased = isSiliconBased;
    }

    public boolean isSiliconBased() { return isSiliconBased; }
}


// ---------- Custom Functional Interfaces ----------

// Determines if a DNA sample is viable enough to process.
@FunctionalInterface
interface ViabilityChecker {
    boolean isViable(DNASample sample);
}

// Produces a classification label string for a given sample.
// The lambda implementation will use instanceof to include child-specific traits.
@FunctionalInterface
interface GenomeMapper {
    String classify(DNASample sample);
}


// ---------- The Processing Engine ----------

class Sequencer {

    // Returns a Map where each key is the class name of the sample type,
    // and each value is a list of formatted classification strings for that type.
    public Map<String, List<String>> classifyGenomes(List<DNASample> samples,
                                                      ViabilityChecker checker,
                                                      GenomeMapper mapper) {
        return samples.stream()

            // Step 1: Remove unviable samples (purity too low).
            .filter(sample -> checker.isViable(sample))

            // Step 2: Group AND transform simultaneously using nested collectors.
            //
            // Collectors.groupingBy() on its own would give Map<String, List<DNASample>>.
            // That's the raw sample objects grouped by class name. But we want
            // Map<String, List<String>> — the formatted strings, not raw objects.
            //
            // The second argument to groupingBy is the "downstream collector."
            // It tells the grouping operation what to do with the elements in each group.
            //
            // Collectors.mapping(mapper::classify, Collectors.toList()) says:
            //   "for each sample in this group, apply mapper.classify() to it,
            //    then collect those strings into a List."
            //
            // Without mapping(), you'd need a second pass (another stream) to transform
            // the values after grouping. mapping() lets you do both in one terminal op.
            .collect(Collectors.groupingBy(
                sample -> sample.getClass().getSimpleName(), // key: "HumanSample" or "AlienSample"
                Collectors.mapping(
                    sample -> mapper.classify(sample),       // transform each sample to a String
                    Collectors.toList()                       // collect those strings into a List
                )
            ));
    }
}


// ---------- Entry Point ----------

public class GeneWeaverSequencer {
    public static void main(String[] args) {

        List<DNASample> samples = Arrays.asList(
            new HumanSample("H-001", 95.0, "O-"),   // viable human
            new HumanSample("H-002", 72.0, "A+"),   // too impure → filtered
            new HumanSample("H-003", 88.5, "B+"),   // viable human
            new AlienSample("A-001", 91.0, true),   // viable alien, silicon-based
            new AlienSample("A-002", 65.0, false),  // too impure → filtered
            new AlienSample("A-003", 82.0, true),   // viable alien, silicon-based
            new HumanSample("H-004", 80.0, "AB-"),  // exactly 80.0 → viable (>= not >)
            new AlienSample("A-004", 79.9, false),  // just below 80.0 → filtered
            new HumanSample("H-005", 99.0, "O+")    // perfect sample
        );

        // THE VIABILITY CHECKER LAMBDA
        // Simple threshold: 80.0 or above is viable. Below that, discard.
        // >= means exactly 80.0 passes. 79.9 does not.
        ViabilityChecker purityCheck = sample -> sample.getPurityPercentage() >= 80.0;

        // THE GENOME MAPPER LAMBDA
        // Include the child-specific trait in the output string.
        // We use instanceof to safely downcast and access the child-specific field.
        // HumanSample  → "ID: H-001 (Type: O-)"
        // AlienSample  → "ID: A-001 (Silicon: true)"
        GenomeMapper traitClassifier = sample -> {
            if (sample instanceof HumanSample) {
                HumanSample human = (HumanSample) sample;
                return "ID: " + sample.getSampleId() + " (Type: " + human.getBloodType() + ")";
            } else if (sample instanceof AlienSample) {
                AlienSample alien = (AlienSample) sample;
                return "ID: " + sample.getSampleId() + " (Silicon: " + alien.isSiliconBased() + ")";
            }
            return "ID: " + sample.getSampleId() + " (Unknown)";
        };

        Sequencer sequencer = new Sequencer();
        Map<String, List<String>> report = sequencer.classifyGenomes(samples, purityCheck, traitClassifier);

        System.out.println("===== GENEWEAVER GENOME CLASSIFICATION REPORT =====\n");
        System.out.println("Total samples submitted: " + samples.size());
        System.out.println("Viable samples grouped: "
                + report.values().stream().mapToInt(List::size).sum());

        System.out.println("\n--- Classification by Species ---");
        report.forEach((species, classifications) -> {
            System.out.println("\n" + species + " (" + classifications.size() + " samples):");
            classifications.forEach(c -> System.out.println("  " + c));
        });
    }
}
