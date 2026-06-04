// ============================================================
// Q1: EcoSmart Home Hub Controller
// Concepts: Abstract Classes, Interfaces, Polymorphism,
//           instanceof, Downcasting
// ============================================================

// ---------- Abstract Base Class ----------
abstract class SmartDevice {
    protected String deviceId;
    protected String deviceName;

    public SmartDevice(String deviceId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    // Every device MUST implement its own diagnostic
    public abstract void runDiagnostic();
}

// ---------- Interface: "Can-Do" Battery Behavior ----------
interface BatteryOperated {
    int getBatteryLevel();          // returns battery % (0-100)
    void triggerRechargeAlert();    // called when battery < 20%
}

// ---------- Concrete Device 1: SmartLight (NO battery) ----------
class SmartLight extends SmartDevice {

    public SmartLight(String deviceId, String deviceName) {
        super(deviceId, deviceName);
    }

    @Override
    public void runDiagnostic() {
        System.out.println("[DIAGNOSTIC] SmartLight '" + deviceName
                + "' (ID: " + deviceId + ") -> Toggling ON/OFF. Status: OK");
    }
}

// ---------- Concrete Device 2: SmartCamera (Battery-powered) ----------
class SmartCamera extends SmartDevice implements BatteryOperated {

    private int batteryLevel;

    public SmartCamera(String deviceId, String deviceName, int batteryLevel) {
        super(deviceId, deviceName);
        this.batteryLevel = batteryLevel;
    }

    @Override
    public void runDiagnostic() {
        System.out.println("[DIAGNOSTIC] SmartCamera '" + deviceName
                + "' (ID: " + deviceId + ") -> Capturing test frame. Status: OK");
    }

    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public void triggerRechargeAlert() {
        System.out.println("  *** RECHARGE ALERT *** SmartCamera '" + deviceName
                + "' battery critically low: " + batteryLevel + "%");
    }
}

// ---------- Concrete Device 3: SmartLock (Battery-powered) ----------
class SmartLock extends SmartDevice implements BatteryOperated {

    private int batteryLevel;

    public SmartLock(String deviceId, String deviceName, int batteryLevel) {
        super(deviceId, deviceName);
        this.batteryLevel = batteryLevel;
    }

    @Override
    public void runDiagnostic() {
        System.out.println("[DIAGNOSTIC] SmartLock '" + deviceName
                + "' (ID: " + deviceId + ") -> Testing lock/unlock cycle. Status: OK");
    }

    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }

    @Override
    public void triggerRechargeAlert() {
        System.out.println("  *** RECHARGE ALERT *** SmartLock '" + deviceName
                + "' battery critically low: " + batteryLevel + "%");
    }
}

// ---------- Hub Manager ----------
class HomeHub {

    /**
     * Nightly routine:
     *  1. Runs diagnostic on EVERY device (polymorphism).
     *  2. If the device is BatteryOperated AND battery < 20%, fires recharge alert.
     */
    public void executeNightlyRoutine(SmartDevice[] devices) {
        System.out.println("\n===== HOME HUB: NIGHTLY DIAGNOSTICS [2:00 AM] =====\n");

        for (SmartDevice device : devices) {

            // Late binding / Dynamic Method Dispatch — correct subclass method runs
            device.runDiagnostic();

            // instanceof check + safe downcast to access battery-specific behaviour
            if (device instanceof BatteryOperated) {
                BatteryOperated batteryDevice = (BatteryOperated) device;
                int level = batteryDevice.getBatteryLevel();
                System.out.println("  [BATTERY CHECK] " + device.deviceName
                        + " battery level: " + level + "%");

                if (level < 20) {
                    batteryDevice.triggerRechargeAlert();
                }
            }

            System.out.println();
        }

        System.out.println("===== ROUTINE COMPLETE =====");
    }
}

// ---------- Entry Point ----------
public class EcoSmartHub {
    public static void main(String[] args) {

        SmartDevice[] devices = {
            new SmartLight("LT-001", "Living Room Light"),
            new SmartCamera("CAM-001", "Front Door Camera", 15),   // critically low
            new SmartCamera("CAM-002", "Backyard Camera", 72),     // fine
            new SmartLock("LK-001", "Main Gate Lock", 8),          // critically low
            new SmartLock("LK-002", "Garage Lock", 55)             // fine
        };

        HomeHub hub = new HomeHub();
        hub.executeNightlyRoutine(devices);
    }
}
