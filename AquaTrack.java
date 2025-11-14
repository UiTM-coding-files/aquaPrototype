import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

// Non-public classes in the same file ------------------------------------------------

class SensorReading {
    private final String id;
    private final Instant timestamp;
    private final double level;
    private final double pH;
    private final double turbidity;

    public SensorReading(String id, Instant timestamp, double level, double pH, double turbidity) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.pH = pH;
        this.turbidity = turbidity;
    }

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public double getLevel() { return level; }
    public double getPH() { return pH; }
    public double getTurbidity() { return turbidity; }

    public String toCsv() {
        return String.join(",",
            id,
            DateTimeFormatter.ISO_INSTANT.format(timestamp),
            Double.toString(level),
            Double.toString(pH),
            Double.toString(turbidity)
        );
    }

    public static SensorReading fromCsv(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length != 5) throw new IllegalArgumentException("Invalid CSV line: " + csv);
        return new SensorReading(
            parts[0],
            Instant.parse(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Double.parseDouble(parts[4])
        );
    }

    @Override
    public String toString() {
        return String.format("%s | %s | level=%.3fm pH=%.2f turb=%.2f",
            id,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(timestamp),
            level, pH, turbidity);
    }
}

class WaterLinkedList implements Iterable<SensorReading>, Serializable {
    private static class Node implements Serializable {
        SensorReading data;
        Node next;
        Node(SensorReading data) { this.data = data; }
    }

    private Node head;
    private int size = 0;

    public void addFirst(SensorReading r) {
        Node n = new Node(r);
        n.next = head;
        head = n;
        size++;
    }

    public void addLast(SensorReading r) {
        Node n = new Node(r);
        if (head == null) head = n;
        else {
            Node cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = n;
        }
        size++;
    }

    public boolean removeIf(java.util.function.Predicate<SensorReading> predicate) {
        Node prev = null;
        Node cur = head;
        boolean removed = false;
        while (cur != null) {
            if (predicate.test(cur.data)) {
                if (prev == null) head = cur.next;
                else prev.next = cur.next;
                size--;
                removed = true;
                cur = (prev == null) ? head : prev.next;
            } else {
                prev = cur;
                cur = cur.next;
            }
        }
        return removed;
    }

    public SensorReading findFirst(java.util.function.Predicate<SensorReading> predicate) {
        Node cur = head;
        while (cur != null) {
            if (predicate.test(cur.data)) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    public int size() { return size; }

    public double averageLevel() {
        if (head == null) return Double.NaN;
        double sum = 0;
        int count = 0;
        for (SensorReading r : this) {
            sum += r.getLevel();
            count++;
        }
        return count == 0 ? Double.NaN : sum / count;
    }

    public SensorReading minLevel() {
        Node cur = head;
        if (cur == null) return null;
        SensorReading min = cur.data;
        cur = cur.next;
        while (cur != null) {
            if (cur.data.getLevel() < min.getLevel()) min = cur.data;
            cur = cur.next;
        }
        return min;
    }

    public SensorReading maxLevel() {
        Node cur = head;
        if (cur == null) return null;
        SensorReading max = cur.data;
        cur = cur.next;
        while (cur != null) {
            if (cur.data.getLevel() > max.getLevel()) max = cur.data;
            cur = cur.next;
        }
        return max;
    }

    public void saveCsv(File file) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            for (SensorReading r : this) w.write(r.toCsv() + "\n");
        }
    }

    public void loadCsv(File file) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            head = null;
            size = 0;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                addLast(SensorReading.fromCsv(line));
            }
        }
    }

    @Override
    public Iterator<SensorReading> iterator() {
        return new Iterator<>() {
            private Node cur = head;
            @Override public boolean hasNext() { return cur != null; }
            @Override public SensorReading next() {
                if (cur == null) throw new NoSuchElementException();
                SensorReading d = cur.data;
                cur = cur.next;
                return d;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SensorReading r : this) sb.append(r).append(System.lineSeparator());
        return sb.toString();
    }
}