package motorph_employeeapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * NotificationModule
 * ------------------
 * Simple structured notification model with line-based serialization so
 * notifications can be saved to a text file without requiring external libs.
 *
 * Line format: id|||timestamp|||read|||text
 * - id: long
 * - timestamp: ISO_LOCAL_DATE_TIME
 * - read: true|false
 * - text: escaped string where occurrences of "|||" are replaced with "[PIPE3]"
 */
public class NotificationModule {

    public static class Notification {
        public long id;
        public String text;
        public String category;
        public String timestamp; // ISO_LOCAL_DATE_TIME
        public boolean read;

        public Notification(long id, String category, String text, String timestamp, boolean read) {
            this.id = id;
            this.category = category == null ? "General" : category;
            this.text = text == null ? "" : text;
            this.timestamp = timestamp == null ? LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : timestamp;
            this.read = read;
        }

        public Notification(String category, String text) {
            this(System.currentTimeMillis(), category, text, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), false);
        }

        public String serializeLine() {
            String safeCat = category.replace("|||", "[PIPE3]");
            String safeText = text.replace("|||", "[PIPE3]");
            return id + "|||" + timestamp + "|||" + (read ? "1" : "0") + "|||" + safeCat + "|||" + safeText;
        }

        public static Notification parseLine(String line) {
            if (line == null) return null;
            String[] parts = line.split("\\Q|||\\E", 5);
            if (parts.length < 5) return null;
            try {
                long id = Long.parseLong(parts[0]);
                String ts = parts[1];
                boolean read = "1".equals(parts[2]);
                String cat = parts[3].replace("[PIPE3]", "|||");
                String txt = parts[4].replace("[PIPE3]", "|||");
                return new Notification(id, cat, txt, ts, read);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return "[" + timestamp + "] [" + category + "] " + (read ? "(Read) " : "") + text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Notification)) return false;
            Notification n = (Notification) o;
            return id == n.id;
        }

        @Override
        public int hashCode() { return Objects.hash(id); }
    }
}
