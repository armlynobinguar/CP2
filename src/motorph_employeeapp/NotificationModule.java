package motorph_employeeapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * NotificationModule
 * ------------------
 * Data model for in-app notifications persisted as plain-text lines (no JSON/XML libs).
 *
 * <p>Each line in {@code resources/notifications.txt} uses pipe-delimited fields:</p>
 * <pre>
 *   id|||timestamp|||readFlag|||category|||messageText
 * </pre>
 * <ul>
 *   <li>{@code id} — unique long (typically {@link System#currentTimeMillis()})</li>
 *   <li>{@code timestamp} — {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}</li>
 *   <li>{@code readFlag} — {@code "1"} read, {@code "0"} unread</li>
 *   <li>{@code category} — e.g. Payroll, Attendance, Birthday</li>
 *   <li>{@code messageText} — body; literal {@code |||} escaped as {@code [PIPE3]}</li>
 * </ul>
 *
 * <p>Load/save is handled by {@link FileHandlerModule#loadStructuredNotifications()} and
 * {@link FileHandlerModule#saveStructuredNotifications}.</p>
 */
public class NotificationModule {

    /**
     * One notification row. Mutable fields are updated in the GUI when marking read/unread.
     */
    public static class Notification {

        /** Unique identifier; used for equality and list deduplication. */
        public long id;

        /** Human-readable notification body shown in the list and detail dialog. */
        public String text;

        /** Grouping label (Payroll, General, etc.) shown in brackets in {@link #toString()}. */
        public String category;

        /** When the notification was created, ISO-8601 local date-time string. */
        public String timestamp;

        /** {@code true} after the user opens or dismisses the notification in the UI. */
        public boolean read;

        /**
         * Full constructor used when deserializing from disk or rebuilding state.
         *
         * @param id        unique id
         * @param category  notification type; defaults to {@code "General"} when null
         * @param text      message body; defaults to empty string when null
         * @param timestamp ISO local date-time; defaults to now when null
         * @param read      read flag from file ({@code "1"}/{@code "0"})
         */
        public Notification(long id, String category, String text, String timestamp, boolean read) {
            this.id = id;
            this.category = category == null ? "General" : category;
            this.text = text == null ? "" : text;
            this.timestamp = timestamp == null
                    ? LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : timestamp;
            this.read = read;
        }

        /**
         * Convenience constructor for newly generated notifications (unread, id = current time).
         *
         * @param category notification type
         * @param text     message body
         */
        public Notification(String category, String text) {
            this(System.currentTimeMillis(), category, text,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), false);
        }

        /**
         * Converts this notification to one line for append-only file storage.
         * Escapes {@code |||} in category and text so delimiters in user content do not break parsing.
         *
         * @return serialized line ready to write to {@code notifications.txt}
         */
        public String serializeLine() {
            String safeCat = category.replace("|||", "[PIPE3]");
            String safeText = text.replace("|||", "[PIPE3]");
            return id + "|||" + timestamp + "|||" + (read ? "1" : "0") + "|||" + safeCat + "|||" + safeText;
        }

        /**
         * Parses one line from the notifications file back into a {@link Notification} object.
         *
         * @param line raw line from disk (no trailing newline)
         * @return parsed notification, or {@code null} if the line is malformed
         */
        public static Notification parseLine(String line) {
            if (line == null) {
                return null;
            }
            // Limit split to 5 parts so message text may contain escaped delimiters
            String[] parts = line.split("\\Q|||\\E", 5);
            if (parts.length < 5) {
                return null;
            }
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

        /** Short debug/list label: {@code [timestamp] [category] (Read) text}. */
        @Override
        public String toString() {
            return "[" + timestamp + "] [" + category + "] " + (read ? "(Read) " : "") + text;
        }

        /** Notifications are equal when their {@link #id} matches (same inbox entry). */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Notification)) {
                return false;
            }
            Notification n = (Notification) o;
            return id == n.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
