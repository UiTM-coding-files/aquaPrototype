import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * SensorReading for AquaTrack: pH + contamination readings (mg/L) + trash.
 * Supports legacy CSV (5 fields) and full CSV (7 fields).
 *
 * CSV formats accepted:
 * - legacy: id,timestamp,pH,magnesium,mercury         (oil/trash assumed 0)
 * - full:   id,timestamp,pH,magnesium,mercury,oil,trash
 */
public class SensorReading {
    private final String id;
    private final Instant timestamp;
    private final double pH;
    private final double magnesium; // mg/L
    private final double mercury;   // mg/L
    private final double oil;       // mg/L
    private final double trash;     // items per cubic metre

    public SensorReading(String id, Instant timestamp, double pH, double magnesium, double mercury, double oil, double trash) {
        this.id = id;
        this.timestamp = timestamp;
        this.pH = pH;
        this.magnesium = magnesium;
        this.mercury = mercury;
        this.oil = oil;
        this.trash = trash;
    }

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public double getPH() { return pH; }
    public double getMagnesium() { return magnesium; }
    public double getMercury() { return mercury; }
    public double getOil() { return oil; }
    public double getTrash() { return trash; }

    // to/from CSV (full)
    // full: id,timestamp,pH,magnesium,mercury,oil,trash
    public String toCsv() {
        return String.join(",",
            id,
            DateTimeFormatter.ISO_INSTANT.format(timestamp),
            Double.toString(pH),
            Double.toString(magnesium),
            Double.toString(mercury),
            Double.toString(oil),
            Double.toString(trash)
        );
    }

    public static SensorReading fromCsv(String csv) {
        String[] parts = csv.split(",", -1);
        if (parts.length == 7) {
            return new SensorReading(
                parts[0],
                Instant.parse(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                Double.parseDouble(parts[5]),
                Double.parseDouble(parts[6])
            );
        } else if (parts.length == 5) {
            // legacy: id,timestamp,pH,magnesium,mercury  -> assume oil=0, trash=0
            return new SensorReading(
                parts[0],
                Instant.parse(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                0.0,
                0.0
            );
        } else {
            throw new IllegalArgumentException("Invalid CSV line (expected 5 or 7 columns): " + csv);
        }
    }

    /**
     * Compute risk score 0..100:
     * - pH deviation from 7 contributes up to 30
     * - magnesium up to 20 (safe <=30)
     * - mercury up to 30 (very sensitive)
     * - oil up to 10
     * - trash up to 10
     */
    public double riskScore() {
        double score = 0.0;

        // pH (max 30) — linear by deviation, cap at deviation 4
        double phDev = Math.abs(pH - 7.0);
        score += Math.min(1.0, phDev / 4.0) * 30.0;

        // magnesium (max 20)
        if (magnesium > 50) score += 20.0;
        else if (magnesium > 30) score += 10.0 * ((magnesium - 30) / 20.0); // up to 10
        // else 0

        // mercury (max 30) — normalized to 0.002 mg/L as critical
        double hgNorm = mercury / 0.002;
        score += Math.min(1.0, Math.max(0.0, hgNorm)) * 30.0;

        // oil (max 10) — linear up to 1.0 mg/L
        score += Math.min(1.0, Math.max(0.0, oil / 1.0)) * 10.0;

        // trash (max 10) — linear up to 20 items/m^3
        score += Math.min(1.0, Math.max(0.0, trash / 20.0)) * 10.0;

        return Math.max(0.0, Math.min(100.0, score));
    }

    public String riskLevel() {
        double s = riskScore();
        if (s < 20) return "LOW";
        if (s < 50) return "MEDIUM";
        if (s < 75) return "HIGH";
        return "CRITICAL";
    }

    @Override
    public String toString() {
        return String.format("%s | %s | pH=%.2f mg=%.2fmg/L hg=%.4fmg/L oil=%.2fmg/L trash=%.1f risk=%.1f(%s)",
            id,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(timestamp),
            pH, magnesium, mercury, oil, trash,
            riskScore(), riskLevel());
    }
}