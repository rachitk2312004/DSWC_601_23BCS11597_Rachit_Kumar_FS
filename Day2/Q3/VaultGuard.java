// ============================================================
// Q3: VaultGuard State Machine
// Concepts: Enums, Custom Exceptions, Strict Encapsulation,
//           Fail-Fast Engineering, State Machine Design
// ============================================================

// ---------- Enum: All valid door states ----------
enum DoorState {
    OPEN,
    CLOSED,
    LOCKED
}

// ---------- Custom Unchecked Exception ----------
class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String message) {
        super(message);
    }
}

// ---------- VaultDoor: The Encapsulated State Machine ----------
class VaultDoor {

    // PRIVATE — no direct access, no setter allowed
    private DoorState state;

    public VaultDoor() {
        this.state = DoorState.CLOSED; // safe initial state
        System.out.println("[VaultDoor] Initialized. State: " + state);
    }

    // Getter is fine — READ access is safe
    public DoorState getState() {
        return state;
    }

    // ── State Transition: CLOSED → OPEN ──────────────────────
    public void openDoor() {
        if (state == DoorState.LOCKED) {
            throw new IllegalStateTransitionException(
                "Cannot open door: door is LOCKED. Unlock it first.");
        }
        if (state == DoorState.OPEN) {
            System.out.println("[VaultDoor] Door is already OPEN.");
            return;
        }
        state = DoorState.OPEN;
        System.out.println("[VaultDoor] Door opened successfully. State: " + state);
    }

    // ── State Transition: OPEN → CLOSED ──────────────────────
    public void closeDoor() {
        if (state == DoorState.CLOSED || state == DoorState.LOCKED) {
            System.out.println("[VaultDoor] Door is already CLOSED/LOCKED.");
            return;
        }
        state = DoorState.CLOSED;
        System.out.println("[VaultDoor] Door closed successfully. State: " + state);
    }

    // ── State Transition: CLOSED → LOCKED ────────────────────
    public void lockDoor() {
        // Critical security rule: must be CLOSED before locking
        if (state == DoorState.OPEN) {
            throw new IllegalStateTransitionException(
                "ILLEGAL TRANSITION: Cannot lock an OPEN door. "
                + "Close the door first before locking.");
        }
        if (state == DoorState.LOCKED) {
            System.out.println("[VaultDoor] Door is already LOCKED.");
            return;
        }
        state = DoorState.LOCKED;
        System.out.println("[VaultDoor] Door locked successfully. State: " + state);
    }

    // ── State Transition: LOCKED → CLOSED ────────────────────
    public void unlockDoor() {
        if (state != DoorState.LOCKED) {
            System.out.println("[VaultDoor] Door is not locked. Current state: " + state);
            return;
        }
        state = DoorState.CLOSED;
        System.out.println("[VaultDoor] Door unlocked. State: " + state);
    }
}

// ---------- Entry Point ----------
public class VaultGuard {
    public static void main(String[] args) {

        System.out.println("===== VAULTGUARD FIRMWARE TEST =====\n");

        VaultDoor door = new VaultDoor();

        // ── Valid sequence ────────────────────────────────────
        System.out.println("\n-- SCENARIO 1: Normal valid operations --");
        door.openDoor();
        door.closeDoor();
        door.lockDoor();
        door.unlockDoor();
        door.openDoor();

        // ── Illegal transition: OPEN → LOCKED (security flaw) ─
        System.out.println("\n-- SCENARIO 2: Illegal transition (OPEN -> LOCKED) --");
        try {
            door.lockDoor();   // door is currently OPEN
        } catch (IllegalStateTransitionException e) {
            System.out.println("[EXCEPTION CAUGHT] " + e.getMessage());
        }

        // ── Confirm state was NOT corrupted ───────────────────
        System.out.println("\n-- SCENARIO 3: State integrity check --");
        System.out.println("Current State after failed lock: " + door.getState());
        door.closeDoor();
        door.lockDoor();
        System.out.println("Final State: " + door.getState());

        System.out.println("\n===== TEST COMPLETE =====");
    }
}
