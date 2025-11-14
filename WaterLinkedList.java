import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class WaterLinkedList implements Iterable<SensorReading>, Serializable {
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

    public boolean removeIf(Predicate<SensorReading> predicate) {
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

    public SensorReading findFirst(Predicate<SensorReading> predicate) {
        Node cur = head;
        while (cur != null) {
            if (predicate.test(cur.data)) return cur.data;
            cur = cur.next;
        }
        return null;
    }

    public int size() { return size; }

    // Average risk across readings
    public double averageRisk() {
        if (head == null) return Double.NaN;
        double sum = 0;
        int count = 0;
        for (SensorReading r : this) {
            sum += r.riskScore();
            count++;
        }
        return count == 0 ? Double.NaN : sum / count;
    }

    // Reading with minimum risk
    public SensorReading minRiskReading() {
        Node cur = head;
        if (cur == null) return null;
        SensorReading min = cur.data;
        cur = cur.next;
        while (cur != null) {
            if (cur.data.riskScore() < min.riskScore()) min = cur.data;
            cur = cur.next;
        }
        return min;
    }

    // Reading with maximum risk
    public SensorReading maxRiskReading() {
        Node cur = head;
        if (cur == null) return null;
        SensorReading max = cur.data;
        cur = cur.next;
        while (cur != null) {
            if (cur.data.riskScore() > max.riskScore()) max = cur.data;
            cur = cur.next;
        }
        return max;
    }

    public void saveCsv(File file) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            for (SensorReading r : this) w.write(r.toCsv() + System.lineSeparator());
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