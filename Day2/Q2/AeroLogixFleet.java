// ============================================================
// Q2: AeroLogix Drone Fleet
// Concepts: Interface Segregation, Default Methods (Java 8+),
//           Multiple Interface Implementation, Abstract Classes
// ============================================================

// ---------- Abstract Base Class ----------
abstract class DeliveryDrone {
    protected String droneId;

    public DeliveryDrone(String droneId) {
        this.droneId = droneId;
    }

    public abstract void deliverPackage();
}

// ---------- Interface 1: Flying capability ----------
interface Airborne {
    void flyToDestination();

    // Default method — shared logic, no need to override unless desired
    default void requestAirTrafficClearance() {
        System.out.println("  [ATC REQUEST] Drone requesting airspace clearance. "
                + "Standby for authorization...");
        System.out.println("  [ATC GRANTED] Clearance confirmed. Safe to proceed.");
    }
}

// ---------- Interface 2: Ground capability ----------
interface GroundBased {
    void navigateSidewalks();
}

// ---------- Concrete 1: Quadcopter (flying only) ----------
class Quadcopter extends DeliveryDrone implements Airborne {

    public Quadcopter(String droneId) {
        super(droneId);
    }

    @Override
    public void deliverPackage() {
        System.out.println("\n[" + droneId + " | QUADCOPTER] Starting delivery...");
        requestAirTrafficClearance();   // inherited default method
        flyToDestination();
        System.out.println("[" + droneId + "] Package delivered via air route.");
    }

    @Override
    public void flyToDestination() {
        System.out.println("  [FLY] Ascending and navigating aerial waypoints.");
    }
}

// ---------- Concrete 2: CityRover (ground only) ----------
class CityRover extends DeliveryDrone implements GroundBased {

    public CityRover(String droneId) {
        super(droneId);
    }

    @Override
    public void deliverPackage() {
        System.out.println("\n[" + droneId + " | CITY ROVER] Starting delivery...");
        navigateSidewalks();
        System.out.println("[" + droneId + "] Package delivered via ground route.");
    }

    @Override
    public void navigateSidewalks() {
        System.out.println("  [GROUND] Mapping pedestrian paths and avoiding obstacles.");
    }
}

// ---------- Concrete 3: HybridVTOL (air + ground) ----------
class HybridVTOL extends DeliveryDrone implements Airborne, GroundBased {

    public HybridVTOL(String droneId) {
        super(droneId);
    }

    @Override
    public void deliverPackage() {
        System.out.println("\n[" + droneId + " | HYBRID VTOL] Starting delivery...");
        requestAirTrafficClearance();   // default method from Airborne
        flyToDestination();
        System.out.println("  [VTOL] Switching to ground mode for final 200m...");
        navigateSidewalks();
        System.out.println("[" + droneId + "] Package delivered via hybrid route.");
    }

    @Override
    public void flyToDestination() {
        System.out.println("  [FLY] Vertical take-off. Cruising at altitude.");
    }

    @Override
    public void navigateSidewalks() {
        System.out.println("  [GROUND] Rolling on retractable wheels to front door.");
    }
}

// ---------- Entry Point ----------
public class AeroLogixFleet {
    public static void main(String[] args) {
        System.out.println("===== AEROLOGIX DISPATCH SYSTEM =====");

        DeliveryDrone[] fleet = {
            new Quadcopter("QC-101"),
            new CityRover("CR-202"),
            new HybridVTOL("HV-303")
        };

        for (DeliveryDrone drone : fleet) {
            drone.deliverPackage();   // polymorphic dispatch
        }

        System.out.println("\n===== ALL DELIVERIES COMPLETE =====");
    }
}
