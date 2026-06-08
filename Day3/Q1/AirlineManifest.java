// Day3 Q1: Global Airline Passenger Manifest — SkyNet Airlines
// --------------------------------------------------------------
// The challenge here isn't the data structures themselves — those are
// straightforward. The real trap is the equals() and hashCode() override.
// If you get that wrong, the entire lookup system breaks silently.
// Passengers won't be found. Banned passengers will slip through security.
// The system compiles and runs, but produces completely wrong results.
//
// The second challenge is choosing the RIGHT collection for each job.
// A Set for bans (fast existence check), a List for rosters (preserves order),
// a Map for global lookup (maps a passenger to their flight instantly).

import java.util.*;


// This class is the key (literally) to the whole system.
// It's used as a key in a HashMap and as an element in a HashSet,
// so equals() and hashCode() MUST be overridden correctly.
//
// The tricky constraint: fullName is intentionally excluded.
// Passengers can legally change their name (marriage, etc.), but their
// passport number + nationality is what uniquely identifies them globally.
// If you include fullName in hashCode(), a name change makes the passenger
// unfindable in the map — they'd hash to a completely different bucket.
class Passenger {

    private String passportNumber;
    private String fullName;        // can change — NOT part of identity
    private String nationality;

    public Passenger(String passportNumber, String fullName, String nationality) {
        this.passportNumber = passportNumber;
        this.fullName = fullName;
        this.nationality = nationality;
    }

    public String getPassportNumber() { return passportNumber; }
    public String getFullName()       { return fullName; }
    public String getNationality()    { return nationality; }

    // We also allow the name to be updated without breaking map lookups.
    // Since fullName isn't part of hashCode() or equals(), changing it
    // does NOT change the passenger's position in any bucket. Safe.
    public void setFullName(String fullName) { this.fullName = fullName; }

    // Two passengers are the same person if and only if their passport number
    // AND nationality match. That's it. fullName is deliberately left out.
    // This is critical — Java's HashMap uses equals() to confirm a match
    // after finding the right bucket via hashCode(). Both must be consistent.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;          // same object in memory, obviously equal
        if (!(obj instanceof Passenger)) return false; // not even a Passenger? definitely not equal
        Passenger other = (Passenger) obj;
        return passportNumber.equals(other.passportNumber)
                && nationality.equals(other.nationality);
    }

    // hashCode() must only use the same fields as equals() — passportNumber and nationality.
    // If these two are equal per equals(), their hashCode() MUST return the same value.
    // That's the contract Java requires. Break it and HashMap becomes completely unreliable.
    //
    // Objects.hash() is the clean modern way to combine multiple fields into one hash.
    // It handles null-safety and distributes values well across buckets.
    @Override
    public int hashCode() {
        return Objects.hash(passportNumber, nationality);
    }

    @Override
    public String toString() {
        return fullName + " [" + passportNumber + "/" + nationality + "]";
    }
}


// This is the brain of the system — it manages all three data structures.
class ManifestManager {

    // WHY A HashSet?
    // We only care about ONE thing for the no-fly list: does this passenger exist on it?
    // A HashSet answers that in O(1) average time using the same hashing mechanics
    // as a HashMap. A List would scan through every entry — O(N) — which is
    // unacceptable when you're checking thousands of passengers at every gate.
    private Set<Passenger> globalNoFlyList = new HashSet<>();

    // WHY A LinkedList (via List interface) per flight?
    // The order passengers check in matters — boarding, seating allocation, upgrades
    // all depend on who arrived first. A List preserves insertion order naturally.
    // We store one list per flight number, hence the Map<String, List<Passenger>>.
    private Map<String, List<Passenger>> flightRosters = new HashMap<>();

    // WHY A HashMap for the global directory?
    // We need to go from "here's a Passenger object, what flight are they on?"
    // to an answer instantly — O(1). That's exactly what a Map<Passenger, String> does.
    // No looping through every flight roster scanning every name. Just one hash lookup.
    private Map<Passenger, String> globalPassengerDirectory = new HashMap<>();


    // Adds a passenger to the restricted set.
    // The Set uses the Passenger's hashCode() and equals() to store and find them.
    // That's why overriding those correctly is so critical.
    public void addToNoFlyList(Passenger p) {
        globalNoFlyList.add(p);
        System.out.println("[NO-FLY] " + p + " added to restricted list.");
    }


    public boolean processCheckIn(String flightNumber, Passenger p) {

        // Step 1: Check the no-fly list FIRST, before anything else.
        // contains() on a HashSet is O(1) — it computes the hash, goes to that bucket,
        // and checks equals(). Fast and safe.
        if (globalNoFlyList.contains(p)) {
            System.out.println("[REJECTED] " + p + " is on the No-Fly List. Check-in denied.");
            return false;
        }

        // Step 2: Get or create the flight roster for this flight number.
        // computeIfAbsent checks if the key exists, and only creates a new ArrayList
        // if it doesn't. Cleaner than writing the null-check yourself.
        flightRosters.computeIfAbsent(flightNumber, k -> new ArrayList<>());

        // Add the passenger to the end of the flight's list.
        // ArrayList preserves insertion order — first to check in is first in the list.
        flightRosters.get(flightNumber).add(p);

        // Step 3: Record the passenger → flight mapping in the global directory.
        // This is what makes locatePassengerFlight() run in O(1) instead of
        // scanning through every flight roster looking for this passenger.
        globalPassengerDirectory.put(p, flightNumber);

        System.out.println("[CHECKED IN] " + p + " → Flight " + flightNumber);
        return true;
    }


    // Instant lookup — no loops, no scanning. Just hash the passenger, find the bucket,
    // check equals(), return the flight number. O(1) average time.
    // Returns null if the passenger isn't checked in anywhere.
    public String locatePassengerFlight(Passenger p) {
        return globalPassengerDirectory.get(p);
    }


    // Prints every passenger on a given flight in check-in order.
    public void printFlightRoster(String flightNumber) {
        List<Passenger> roster = flightRosters.getOrDefault(flightNumber, new ArrayList<>());
        System.out.println("\n--- Roster for Flight " + flightNumber + " ---");
        if (roster.isEmpty()) {
            System.out.println("  No passengers checked in.");
        } else {
            for (int i = 0; i < roster.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + roster.get(i));
            }
        }
    }
}


public class AirlineManifest {
    public static void main(String[] args) {

        ManifestManager manager = new ManifestManager();

        // Create some passengers
        Passenger alice   = new Passenger("P1001", "Alice Johnson", "US");
        Passenger bob     = new Passenger("P1002", "Bob Smith",     "UK");
        Passenger charlie = new Passenger("P1003", "Charlie Xu",    "CN");
        Passenger dan     = new Passenger("P1004", "Dan Patel",     "IN");

        System.out.println("===== NO-FLY LIST SETUP =====");
        // Charlie is flagged — add him to the no-fly list
        manager.addToNoFlyList(charlie);

        System.out.println("\n===== CHECK-IN PROCESS =====");
        manager.processCheckIn("SK-101", alice);
        manager.processCheckIn("SK-101", bob);
        manager.processCheckIn("SK-202", dan);

        // Charlie tries to check in — should be blocked by the no-fly check
        manager.processCheckIn("SK-101", charlie);

        System.out.println("\n===== FLIGHT ROSTERS =====");
        manager.printFlightRoster("SK-101");
        manager.printFlightRoster("SK-202");

        System.out.println("\n===== GLOBAL PASSENGER LOOKUP =====");
        System.out.println("Alice's flight:   " + manager.locatePassengerFlight(alice));
        System.out.println("Bob's flight:     " + manager.locatePassengerFlight(bob));
        System.out.println("Dan's flight:     " + manager.locatePassengerFlight(dan));
        System.out.println("Charlie's flight: " + manager.locatePassengerFlight(charlie)); // null — blocked

        // Now here's the important test — does the equals/hashCode hold up
        // when we create a BRAND NEW Passenger object with the same passport + nationality?
        // This is different from the same object reference (alice).
        // If hashCode() and equals() are correct, this should find Alice's flight.
        // If we had used the default Object hashCode (memory address), this would return null.
        System.out.println("\n===== IDENTITY TEST (New Object, Same Passport) =====");
        Passenger searchAlice = new Passenger("P1001", "Alice Johnson-Williams", "US"); // name changed
        System.out.println("Lookup with new object (name changed): "
                + manager.locatePassengerFlight(searchAlice));
        // Should still print SK-101 — because equals() ignores the name.
    }
}
