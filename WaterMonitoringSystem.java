import java.io.File;
import java.time.Instant;
import java.util.Scanner;
import java.util.UUID;

public class WaterMonitoringSystem {
    private static final String DATA_FILE = "readings.csv";
    private final WaterLinkedList readings = new WaterLinkedList();

    public static void main(String[] args) {
        WaterMonitoringSystem app = new WaterMonitoringSystem();
        app.run();
    }

    private void run() {
        Scanner in = new Scanner(System.in);
        // try load existing
        try { readings.loadCsv(new File(DATA_FILE)); } catch (Exception ignored) {}

        while (true) {
            System.out.println("\nAquaTrack - water monitoring (linked list)");
            System.out.println("1) Add reading");
            System.out.println("2) List readings");
            System.out.println("3) Stats (avg/min/max level)");
            System.out.println("4) Remove by id");
            System.out.println("5) Save");
            System.out.println("0) Exit");
            System.out.print("> ");
            String cmd = in.nextLine().trim();
            switch (cmd) {
                case "1" -> addReading(in);
                case "2" -> list();
                case "3" -> stats();
                case "4" -> removeById(in);
                case "5" -> save();
                case "0" -> { save(); System.out.println("Bye."); return; }
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void addReading(Scanner in) {
        try {
            System.out.print("level (m): ");
            double level = Double.parseDouble(in.nextLine().trim());
            System.out.print("pH: ");
            double pH = Double.parseDouble(in.nextLine().trim());
            System.out.print("turbidity (NTU): ");
            double turb = Double.parseDouble(in.nextLine().trim());
            String id = UUID.randomUUID().toString().substring(0,8);
            SensorReading r = new SensorReading(id, Instant.now(), level, pH, turb);
            readings.addLast(r);
            System.out.println("Added: " + r);
        } catch (Exception e) {
            System.out.println("Bad input: " + e.getMessage());
        }
    }

    private void list() {
        if (readings.size() == 0) { System.out.println("no readings"); return; }
        System.out.println("readings:");
        System.out.println(readings);
    }

    private void stats() {
        if (readings.size() == 0) { System.out.println("no readings"); return; }
        System.out.printf("count=%d avg=%.3f min=%s max=%s%n",
            readings.size(),
            readings.averageLevel(),
            readings.minLevel(),
            readings.maxLevel());
    }

    private void removeById(Scanner in) {
        System.out.print("id: ");
        String id = in.nextLine().trim();
        boolean removed = readings.removeIf(r -> r.getId().equals(id));
        System.out.println(removed ? "removed" : "not found");
    }

    private void save() {
        try {
            readings.saveCsv(new File(DATA_FILE));
            System.out.println("Saved to " + DATA_FILE);
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }
}