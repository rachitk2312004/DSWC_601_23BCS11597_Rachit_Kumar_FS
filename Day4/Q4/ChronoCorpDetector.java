// Day4 Q4: ChronoCorp Temporal Paradox Detector
// -------------------------------------------------
// The new concept here is flatMap(). This is where most stream questions get tricky.
//
// The problem: we have List<HistoricalEvent>, and each event contains List<TemporalEntity>.
// If we just .map(event -> event.getEntities()), we'd get Stream<List<TemporalEntity>> —
// a stream of lists, not a stream of individual entities. We can't filter individual entities
// if they're still wrapped in lists.
//
// flatMap() solves this by "flattening" each inner list into the stream, giving us
// one continuous Stream<TemporalEntity> from all events combined.
//
// The extra wrinkle: our ParadoxChecker needs BOTH the entity AND the event's year.
// So we can't just flatMap blindly and lose track of which year each entity came from.
// We need to pair them up first.

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


// ---------- Entity Hierarchy ----------

abstract class TemporalEntity {
    protected String entityName;
    protected int originYear; // the year this entity naturally belongs to

    public TemporalEntity(String entityName, int originYear) {
        this.entityName = entityName;
        this.originYear = originYear;
    }

    public String getEntityName() { return entityName; }
    public int getOriginYear()    { return originYear; }
}


// A human time traveler or historical person.
class HumanEntity extends TemporalEntity {
    public HumanEntity(String entityName, int originYear) {
        super(entityName, originYear);
    }
}


// An artifact found in a time period it shouldn't be in.
// Radioactive artifacts cause additional complications — tracked here.
class ArtifactEntity extends TemporalEntity {
    private boolean isRadioactive;

    public ArtifactEntity(String entityName, int originYear, boolean isRadioactive) {
        super(entityName, originYear);
        this.isRadioactive = isRadioactive;
    }

    public boolean isRadioactive() { return isRadioactive; }
}


// A HistoricalEvent is a snapshot of a specific year.
// It contains whatever entities were present (or shouldn't be present) during that year.
class HistoricalEvent {
    private int eventYear;                  // the actual historical year of this event
    private List<TemporalEntity> entities;  // who/what was detected in this time period

    public HistoricalEvent(int eventYear, List<TemporalEntity> entities) {
        this.eventYear = eventYear;
        this.entities = entities;
    }

    public int getEventYear()                     { return eventYear; }
    public List<TemporalEntity> getEntities()     { return entities; }
}


// ---------- Custom Functional Interfaces ----------

// Checks whether an entity is a paradox in the context of a specific event year.
// Takes TWO parameters — this is different from Q1/Q2/Q3 which took only one.
// A standard Predicate<TemporalEntity> wouldn't work here because we also need the year.
// This is exactly why custom functional interfaces exist — the built-in ones
// don't always fit the domain.
@FunctionalInterface
interface ParadoxChecker {
    boolean isParadox(TemporalEntity entity, int eventYear);
}

// Formats a paradox into a human-readable threat report string.
@FunctionalInterface
interface ThreatMapper {
    String map(TemporalEntity entity);
}


// ---------- A helper class to carry entity + year together ----------

// When we flatMap across HistoricalEvents, we need to carry the event's year alongside
// each entity. Java doesn't have built-in pairs/tuples, so we make a simple wrapper.
// This lets us preserve the "which year did this entity appear in?" context
// after the flatMap destroys the event structure.
class EntityInYear {
    final TemporalEntity entity;
    final int eventYear;

    EntityInYear(TemporalEntity entity, int eventYear) {
        this.entity = entity;
        this.eventYear = eventYear;
    }
}


// ---------- The Processing Engine ----------

class ParadoxAnalyzer {

    public List<String> detectParadoxes(List<HistoricalEvent> timeline,
                                        ParadoxChecker checker,
                                        ThreatMapper mapper) {
        return timeline.stream()

            // flatMap — this is the key step. Without it, we'd have Stream<List<TemporalEntity>>.
            // For each HistoricalEvent, we stream its entities and wrap each one in
            // an EntityInYear so we don't lose the eventYear context.
            // The result is one flat Stream<EntityInYear> covering all events.
            .flatMap(event ->
                event.getEntities().stream()
                    .map(entity -> new EntityInYear(entity, event.getEventYear()))
            )

            // Now we can filter using both the entity and its event year.
            // The checker lambda receives both pieces of information it needs.
            .filter(pair -> checker.isParadox(pair.entity, pair.eventYear))

            // Map each paradox to its threat report string.
            .map(pair -> mapper.map(pair.entity))

            // Collect into the final list.
            .collect(Collectors.toList());
    }
}


// ---------- Entry Point ----------

public class ChronoCorpDetector {
    public static void main(String[] args) {

        // Build a timeline with historical events and the entities present in each.
        // Paradoxes = entities whose originYear is LATER than the event year they're found in.
        // i.e. they've traveled back in time and are in a year before they were born/created.
        List<HistoricalEvent> timeline = Arrays.asList(

            new HistoricalEvent(1850, Arrays.asList(
                new HumanEntity("Abraham Lincoln", 1809),    // originYear 1809 < eventYear 1850 → normal
                new HumanEntity("Dr. Elena Voss",  2075),    // originYear 2075 > eventYear 1850 → PARADOX
                new ArtifactEntity("Steam Engine", 1765, false) // 1765 < 1850 → normal
            )),

            new HistoricalEvent(1969, Arrays.asList(
                new HumanEntity("Neil Armstrong",  1930),    // 1930 < 1969 → normal
                new ArtifactEntity("Smartphone",   2007, false), // 2007 > 1969 → PARADOX
                new HumanEntity("Agent X-9",       2150),    // 2150 > 1969 → PARADOX
                new ArtifactEntity("Moon Rock",    1969, false)  // same year → normal (not GREATER than)
            )),

            new HistoricalEvent(2024, Arrays.asList(
                new HumanEntity("Researcher Yuki", 1992),    // 1992 < 2024 → normal
                new ArtifactEntity("Quantum CPU",  2380, true),  // 2380 > 2024 → PARADOX (and radioactive!)
                new HumanEntity("Time Cop Maria",  2024)     // same year → normal
            ))
        );

        // THE CHECKER LAMBDA
        // A paradox is simple: the entity's origin year is GREATER than the event year.
        // They're from the future, sitting in the past. That's a paradox.
        // Note: exactly equal years are NOT paradoxes — being present in your birth year is fine.
        ParadoxChecker futureDetector = (entity, eventYear) ->
            entity.getOriginYear() > eventYear;

        // THE THREAT MAPPER LAMBDA
        // Simple format: "[Name] detected out of time!"
        ThreatMapper alertFormatter = entity ->
            entity.getEntityName() + " detected out of time!";

        ParadoxAnalyzer analyzer = new ParadoxAnalyzer();
        List<String> paradoxReports = analyzer.detectParadoxes(timeline, futureDetector, alertFormatter);

        System.out.println("===== CHRONOCORP TEMPORAL PARADOX REPORT =====\n");
        System.out.println("Historical events scanned: " + timeline.size());
        System.out.println("Paradoxes detected: " + paradoxReports.size());
        System.out.println("\n--- Threat Reports ---");
        paradoxReports.forEach(System.out::println);
    }
}
