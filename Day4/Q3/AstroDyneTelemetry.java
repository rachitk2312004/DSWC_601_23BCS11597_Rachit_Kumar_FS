// Day4 Q3: AstroDyne Thruster Telemetry Analyzer
// -------------------------------------------------
// Two new things this question introduces compared to Q1/Q2:
//
// 1. mapToDouble() — instead of mapping to a String, we map to a primitive double.
//    This creates a DoubleStream instead of a Stream<Object>. The difference matters
//    because DoubleStream gives you mathematical terminal operations like .max(), .sum(),
//    .average() directly — no need to implement them yourself.
//
// 2. OptionalDouble — .max() on a DoubleStream doesn't return a double directly.
//    It returns OptionalDouble because the stream might be empty (no logs passed the filter).
//    You must handle this safely with .orElse(0.0) instead of assuming a value exists.

import java.util.Arrays;
import java.util.List;


// ---------- Engine Log Hierarchy ----------

// Base class for all telemetry logs from the thruster arrays.
// Every log has a timestamp, the core temperature at that moment,
// and a flag indicating whether something unusual was detected.
abstract class EngineLog {
    protected String timestamp;
    protected double coreTemperature; // in Kelvin
    protected boolean isAnomaly;

    public EngineLog(String timestamp, double coreTemperature, boolean isAnomaly) {
        this.timestamp = timestamp;
        this.coreTemperature = coreTemperature;
        this.isAnomaly = isAnomaly;
    }

    public String getTimestamp()         { return timestamp; }
    public double getCoreTemperature()   { return coreTemperature; }
    public boolean isAnomaly()           { return isAnomaly; }
}


// Nominal logs — routine readings during normal operation. No error code needed.
class NominalLog extends EngineLog {
    public NominalLog(String timestamp, double coreTemperature, boolean isAnomaly) {
        super(timestamp, coreTemperature, isAnomaly);
    }
}


// Critical logs carry an error code that identifies the type of failure.
// "OVERHEAT" is the code that triggers the peak temperature audit.
// This extra field is what the auditor lambda will need to downcast to check.
class CriticalLog extends EngineLog {
    private String errorCode;

    public CriticalLog(String timestamp, double coreTemperature,
                       boolean isAnomaly, String errorCode) {
        super(timestamp, coreTemperature, isAnomaly);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}


// ---------- Custom Functional Interfaces ----------

// Decides whether a log entry is "critical" enough to include in the analysis.
// Returns true = include this log, false = skip it.
@FunctionalInterface
interface LogAuditor {
    boolean audit(EngineLog log);
}

// Extracts a temperature value from a log entry.
// Returns double instead of an Object/String — this is what enables mapToDouble()
// to produce a primitive DoubleStream rather than a boxed Stream<Double>.
@FunctionalInterface
interface HeatExtractor {
    double extract(EngineLog log);
}


// ---------- The Processing Engine ----------

class TelemetryProcessor {

    // Returns the highest temperature found in "critical" logs.
    // Returns 0.0 if no logs pass the audit filter (empty stream case).
    public double getPeakCriticalTemp(List<EngineLog> logs,
                                      LogAuditor auditor,
                                      HeatExtractor extractor) {
        return logs.stream()

            // Step 1: Keep only the logs the auditor considers critical.
            // Nominal logs and non-OVERHEAT criticals get dropped here.
            .filter(log -> auditor.audit(log))

            // Step 2: mapToDouble() converts Stream<EngineLog> → DoubleStream.
            // This is important: we're not creating Double wrapper objects (auto-boxing).
            // We're producing a stream of primitive doubles. For massive telemetry
            // processing, this significantly reduces garbage collection pressure.
            // The HeatExtractor lambda just says "give me the temperature of this log."
            .mapToDouble(log -> extractor.extract(log))

            // Step 3: max() on a DoubleStream — this is a terminal operation.
            // It returns OptionalDouble, not double, because the stream might be empty.
            // .orElse(0.0) says "if there were no results, return 0.0 as a safe default."
            // Never use .getAsDouble() without checking first — that throws if the stream was empty.
            .max()
            .orElse(0.0);
    }
}


// ---------- Entry Point ----------

public class AstroDyneTelemetry {
    public static void main(String[] args) {

        // A mix of nominal and critical logs at various temperatures.
        // Some are anomalies but not OVERHEAT. Some are OVERHEAT.
        // The auditor captures both isAnomaly=true AND OVERHEAT error code cases.
        List<EngineLog> logs = Arrays.asList(
            new NominalLog("T+001", 3200.0, false),               // routine — not critical
            new NominalLog("T+002", 3450.0, true),                // anomaly flagged      → auditor catches it
            new CriticalLog("T+003", 4800.0, true, "OVERHEAT"),   // overheat anomaly     → auditor catches it
            new CriticalLog("T+004", 3900.0, false, "PRESSURE"),  // not anomaly, not OVERHEAT → passes through
            new NominalLog("T+005", 3100.0, false),               // routine — not critical
            new CriticalLog("T+006", 5200.0, true, "OVERHEAT"),   // highest temp so far  → auditor catches it
            new CriticalLog("T+007", 4100.0, false, "VIBRATION"), // not anomaly          → passes through
            new NominalLog("T+008", 6000.0, false),               // highest temp in set, but NOT critical — missed
            new CriticalLog("T+009", 4950.0, true, "OVERHEAT"),   // overheat             → auditor catches it
            new NominalLog("T+010", 3300.0, true)                 // anomaly, no error code → auditor catches it
        );

        // THE AUDITOR LAMBDA
        // A log is "critical" if it's marked as an anomaly OR if it's a CriticalLog
        // with an OVERHEAT error code. Either condition qualifies it.
        //
        // Note: T+004 (PRESSURE) and T+007 (VIBRATION) pass through because they're
        // CriticalLog instances but isAnomaly=false and errorCode != "OVERHEAT".
        // That's the OR logic — both conditions need to fail for rejection.
        LogAuditor thermalAuditor = log -> {
            if (log.isAnomaly()) {
                return true; // any anomaly is critical, regardless of type
            }
            if (log instanceof CriticalLog) {
                CriticalLog crit = (CriticalLog) log;
                return "OVERHEAT".equals(crit.getErrorCode()); // only OVERHEAT qualifies
            }
            return false; // nominal, non-anomaly logs are not critical
        };

        // THE EXTRACTOR LAMBDA
        // Simple — just return the temperature. The stream does the math (max).
        HeatExtractor tempExtractor = log -> log.getCoreTemperature();

        TelemetryProcessor processor = new TelemetryProcessor();
        double peakTemp = processor.getPeakCriticalTemp(logs, thermalAuditor, tempExtractor);

        System.out.println("===== ASTRODYNE THRUSTER TELEMETRY REPORT =====\n");
        System.out.println("Total logs analyzed: " + logs.size());
        System.out.println("Peak Critical Temperature: " + peakTemp + " K");

        // The NominalLog at T+008 has 6000K but is NOT an anomaly and NOT a CriticalLog.
        // So it gets filtered out. The actual peak among critical/OVERHEAT logs is 5200K (T+006).
        System.out.println("\nNote: T+008 (6000K) was excluded — not flagged as critical.");
        System.out.println("Highest confirmed critical temperature: " + peakTemp + " K (T+006)");
    }
}
