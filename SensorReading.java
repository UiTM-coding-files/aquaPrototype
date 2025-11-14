import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Objects;

public class SensorReading {
    private final String id;
    private final Instant timestamp;
    private final double level;      // water level (meters)
    private final double pH;         // pH value
    private final double turbidity;  // turbidity (NTU)

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensorReading)) return false;
        SensorReading that = (SensorReading) o;
        return Objects.equals(id, that.id) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp);
    }
}