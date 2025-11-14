import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AquaTrack - main UI only. SensorReading and WaterLinkedList are in separate files.
 */
public class AquaTrack {
    private static final String DATA_FILE = "readings.csv";
    private final WaterLinkedList readings = new WaterLinkedList();

    public static void main(String[] args) {
        new AquaTrack().run();
    }

    private void run() {
        Scanner in = new Scanner(System.in);
        try { readings.loadCsv(new File(DATA_FILE)); } catch (Exception ignored) {}

        String banner = """
+------------------+
|   AQUA TRACK     |
+------------------+
""";

        while (true) {
            System.out.println(banner);
            System.out.println("AquaTrack - water monitoring (linked list)");
            System.out.println("1) Add reading");
            System.out.println("2) List readings");
            System.out.println("3) Stats (avg/min/max risk)");
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
            System.out.print("pH: ");
            double pH = Double.parseDouble(in.nextLine().trim());
            System.out.print("magnesium (mg/L): ");
            double mg = Double.parseDouble(in.nextLine().trim());
            System.out.print("mercury (mg/L): ");
            double hg = Double.parseDouble(in.nextLine().trim());
            System.out.print("oil (mg/L): ");
            double oil = Double.parseDouble(in.nextLine().trim());
            System.out.print("trash (items/m^3): ");
            double trash = Double.parseDouble(in.nextLine().trim());

            String id = UUID.randomUUID().toString().substring(0,8);
            SensorReading r = new SensorReading(id, Instant.now(), pH, mg, hg, oil, trash);
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
        System.out.printf("count=%d avgRisk=%.1f min=%s max=%s%n",
            readings.size(),
            readings.averageRisk(),
            readings.minRiskReading(),
            readings.maxRiskReading());
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