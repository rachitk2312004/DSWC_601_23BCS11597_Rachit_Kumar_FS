// Day3 Q2: MediCore Emergency Triage System
// -------------------------------------------
// The core challenge here is two-tier sorting inside a PriorityQueue.
// Rule 1: CRITICAL patients always go before URGENT, URGENT before STABLE.
// Rule 2: If two patients have the same severity, whoever arrived first gets treated first.
//
// A PriorityQueue handles continuous intake efficiently — patients keep arriving
// and the queue always has the most critical one at the front without you
// having to re-sort the entire list every time someone new walks in.

import java.util.PriorityQueue;


// The severity levels. The ORDER they're declared in matters here.
// Java enums have a natural ordinal: CRITICAL = 0, URGENT = 1, STABLE = 2.
// Lower ordinal = higher priority in our sorting logic.
// We exploit this in compareTo() so we don't have to write nested if/else chains.
enum TriageLevel {
    CRITICAL,   // ordinal 0 — most urgent
    URGENT,     // ordinal 1
    STABLE      // ordinal 2 — least urgent
}


// Patient implements Comparable<Patient> so PriorityQueue knows how to order them
// without needing a separate Comparator passed in.
// Comparable is the right choice here because this ordering IS the natural ordering
// of patients in a triage context — it's not a special case.
class Patient implements Comparable<Patient> {

    private String name;
    private TriageLevel severity;
    private long arrivalTime; // milliseconds since epoch — lower means arrived earlier

    public Patient(String name, TriageLevel severity, long arrivalTime) {
        this.name = name;
        this.severity = severity;
        this.arrivalTime = arrivalTime;
    }

    public String getName()          { return name; }
    public TriageLevel getSeverity() { return severity; }
    public long getArrivalTime()     { return arrivalTime; }

    // This is the heart of the question. Two-tier sorting:
    //
    // Tier 1: Sort by severity. CRITICAL (ordinal 0) should come BEFORE STABLE (ordinal 2).
    //   We use the enum's built-in compareTo(), which compares ordinals.
    //   If this.severity is CRITICAL (0) and other is STABLE (2), compareTo returns negative.
    //   Negative means "this comes first" in Java's PriorityQueue (min-heap by default).
    //   So lower ordinal = higher priority. That's what we want.
    //
    // Tier 2: If severity is equal (compareTo returns 0), fall back to arrival time.
    //   Earlier arrival = smaller timestamp = should be treated first.
    //   Long.compare(this.arrivalTime, other.arrivalTime) returns negative if this arrived
    //   earlier — meaning this patient goes first. Exactly right.
    //
    // This avoids deeply nested if/else blocks. Clean, readable, idiomatic Java.
    @Override
    public int compareTo(Patient other) {
        int severityCompare = this.severity.compareTo(other.severity);
        if (severityCompare != 0) {
            return severityCompare; // different severities — the enum decides
        }
        // Same severity — whoever arrived first gets treated first
        return Long.compare(this.arrivalTime, other.arrivalTime);
    }

    @Override
    public String toString() {
        return name + " [" + severity + ", arrived at T+" + arrivalTime + "ms]";
    }
}


class TriageManager {

    // PriorityQueue uses the compareTo() we defined above to maintain heap order.
    // Internally it's a binary min-heap — the "smallest" element (highest priority patient)
    // is always at the root, accessible in O(1).
    //
    // Why not a sorted List? Because a List needs O(N log N) to re-sort every time
    // a new patient arrives. A PriorityQueue inserts in O(log N) by bubbling the new
    // patient up the heap until it finds the right position. Much faster for live intake.
    private PriorityQueue<Patient> waitingRoom = new PriorityQueue<>();


    // Adds a patient and lets the heap bubble them up to the right position.
    // O(log N) — the heap height is log(N), and bubbling up traverses at most that far.
    public void admitPatient(Patient p) {
        waitingRoom.offer(p);
        System.out.println("[ADMITTED] " + p);
    }


    // Removes and returns the highest-priority patient (root of the heap).
    // O(log N) — removing root requires re-heapifying downward.
    // peek() would give O(1) without removing, but doctors need to actually take the patient.
    public Patient getNextPatient() {
        Patient next = waitingRoom.poll();
        if (next != null) {
            System.out.println("[TREATING] " + next);
        } else {
            System.out.println("[WAITING ROOM EMPTY]");
        }
        return next;
    }


    public int getQueueSize() {
        return waitingRoom.size();
    }
}


public class MediCoreTriage {
    public static void main(String[] args) {

        TriageManager triage = new TriageManager();

        System.out.println("===== MASS CASUALTY INTAKE =====\n");

        // Simulate patients arriving at different times with different severities.
        // Arrival time is just a simulated millisecond offset for clarity.
        triage.admitPatient(new Patient("Alice",   TriageLevel.STABLE,    100));
        triage.admitPatient(new Patient("Bob",     TriageLevel.CRITICAL,  200));
        triage.admitPatient(new Patient("Carol",   TriageLevel.URGENT,    150));
        triage.admitPatient(new Patient("Dan",     TriageLevel.CRITICAL,  120)); // arrived before Bob, same severity
        triage.admitPatient(new Patient("Eve",     TriageLevel.URGENT,    300));
        triage.admitPatient(new Patient("Frank",   TriageLevel.STABLE,     80)); // arrived early but STABLE

        System.out.println("\n===== TREATMENT ORDER =====\n");

        // The queue should now drain in this order:
        //   1. Dan   — CRITICAL, arrived at T+120 (earliest CRITICAL)
        //   2. Bob   — CRITICAL, arrived at T+200
        //   3. Carol — URGENT,   arrived at T+150 (earliest URGENT)
        //   4. Eve   — URGENT,   arrived at T+300
        //   5. Frank — STABLE,   arrived at T+80  (earliest STABLE)
        //   6. Alice — STABLE,   arrived at T+100
        while (triage.getQueueSize() > 0) {
            triage.getNextPatient();
        }
    }
}
