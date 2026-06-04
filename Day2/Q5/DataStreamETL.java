// ============================================================
// Q5: DataStream ETL Pipeline
// Concepts: Java Generics, Upper Bounded Type Parameters,
//           Compile-Time Type Safety, ClassCastException Prevention
// ============================================================

// ---------- Domain Model: Abstract Payload ----------
abstract class DataPayload {
    private String source;

    public DataPayload(String source) {
        this.source = source;
    }

    // Subclasses must provide their raw content representation
    public abstract String getRawContent();

    public String getSource() {
        return source;
    }
}

// ---------- Concrete Payload 1: JSON ----------
class JsonPayload extends DataPayload {

    private String jsonBody;

    public JsonPayload(String source, String jsonBody) {
        super(source);
        this.jsonBody = jsonBody;
    }

    @Override
    public String getRawContent() {
        return jsonBody;
    }
}

// ---------- Concrete Payload 2: XML ----------
class XmlPayload extends DataPayload {

    private String xmlBody;

    public XmlPayload(String source, String xmlBody) {
        super(source);
        this.xmlBody = xmlBody;
    }

    @Override
    public String getRawContent() {
        return xmlBody;
    }
}

// ---------- Generic Pipeline Processor (Upper Bounded) ----------
// <T extends DataPayload> means:
//   • T can ONLY be DataPayload or any of its subclasses
//   • This gives the compiler a guarantee that T has getRawContent()
//   • Strings, Integers, or random objects are REJECTED at compile-time
class PipelineProcessor<T extends DataPayload> {

    private String processorName;

    public PipelineProcessor(String processorName) {
        this.processorName = processorName;
    }

    // Because of upper bound, calling .getRawContent() is legal — no cast needed!
    public void process(T payload) {
        System.out.println("\n[" + processorName + "] Received payload from source: "
                + payload.getSource());
        System.out.println("[" + processorName + "] Extracting raw content...");

        String content = payload.getRawContent(); // no (DataPayload) cast required
        System.out.println("[" + processorName + "] Content: " + content);

        System.out.println("[" + processorName + "] Transforming data...");
        String transformed = content.trim().toUpperCase();
        System.out.println("[" + processorName + "] Transformed: " + transformed);

        System.out.println("[" + processorName + "] Loading into data warehouse... DONE.");
    }

    // A utility method demonstrating type safety in a mixed list scenario
    public void processBatch(java.util.List<T> payloads) {
        System.out.println("\n[" + processorName + "] --- BATCH START ("
                + payloads.size() + " payloads) ---");
        int index = 1;
        for (T payload : payloads) {
            System.out.println("  Payload #" + index++ + ":");
            process(payload);
        }
        System.out.println("[" + processorName + "] --- BATCH END ---");
    }
}

// ---------- Entry Point ----------
public class DataStreamETL {
    public static void main(String[] args) {

        System.out.println("===== DATASTREAM ETL PIPELINE =====");

        // ── JSON Processor: only accepts JsonPayload ──────────
        PipelineProcessor<JsonPayload> jsonProcessor =
                new PipelineProcessor<>("JSON-Processor-01");

        JsonPayload jp1 = new JsonPayload("REST API v2",
                "{ \"userId\": 42, \"action\": \"purchase\", \"amount\": 150.00 }");
        JsonPayload jp2 = new JsonPayload("Mobile App",
                "{ \"event\": \"login\", \"timestamp\": \"2025-06-01T10:00:00Z\" }");

        jsonProcessor.process(jp1);

        // ── XML Processor: only accepts XmlPayload ────────────
        PipelineProcessor<XmlPayload> xmlProcessor =
                new PipelineProcessor<>("XML-Processor-01");

        XmlPayload xp1 = new XmlPayload("Legacy ERP System",
                "<order><id>5001</id><product>Widget</product><qty>10</qty></order>");

        xmlProcessor.process(xp1);

        // ── Batch processing demonstration ────────────────────
        java.util.List<JsonPayload> jsonBatch = java.util.Arrays.asList(jp1, jp2);
        jsonProcessor.processBatch(jsonBatch);

        // ── Compile-Time Safety Demo (COMMENTED OUT INTENTIONALLY) ──────
        // Uncomment the lines below to see COMPILE-TIME errors — they will
        // never reach runtime, proving Generics shift errors left to compile time.

        // jsonProcessor.process(xp1);
        // ↑ ERROR: incompatible types: XmlPayload cannot be converted to JsonPayload

        // PipelineProcessor<String> bad = new PipelineProcessor<>("Bad");
        // ↑ ERROR: type argument String is not within bounds of type-variable T
        //   (String does not extend DataPayload)

        System.out.println("\n===== ETL PIPELINE COMPLETE =====");
    }
}
