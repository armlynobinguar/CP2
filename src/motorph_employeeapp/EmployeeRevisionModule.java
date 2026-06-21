package motorph_employeeapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * EmployeeRevisionModule
 * ----------------------
 * Audit trail for HR employee-record changes. Before each add/edit/delete, the full
 * employee CSV is snapshotted so HR can review history and revert to a prior state.
 *
 * <p>Storage layout:</p>
 * <ul>
 *   <li>{@code resources/employee_revision_index.txt} — one metadata line per change</li>
 *   <li>{@code resources/revisions/rev_&lt;timestamp&gt;.csv} — full CSV copy before the change</li>
 * </ul>
 *
 * <p>Index line format: {@code timestampMs|action|employeeId|summary|snapshotFileName}</p>
 */
public class EmployeeRevisionModule {

    /** Maximum revision entries kept in memory and on disk; oldest snapshots are deleted. */
    private static final int MAX_ENTRIES = 100;

    /** Text index listing every revision metadata row. */
    private static final String INDEX_FILE = "resources/employee_revision_index.txt";

    /** Folder containing full CSV snapshots taken before each HR mutation. */
    private static final String SNAP_DIR = "resources/revisions";

    /** In-memory list of revisions, newest first (index 0 = latest). */
    private static final List<RevisionEntry> entries = new ArrayList<>();

    /**
     * Immutable metadata for one revision snapshot shown in the HR revision history dialog.
     */
    public static class RevisionEntry {

        /** Unix epoch milliseconds when the change was logged. */
        public final long timestampMs;

        /** Short action label, e.g. {@code "Edit"}, {@code "Add"}, {@code "Delete"}. */
        public final String action;

        /** Employee number affected by the change (may be empty for bulk operations). */
        public final String employeeId;

        /** Human-readable description shown in the history table. */
        public final String summary;

        /** Filename under {@link #SNAP_DIR} containing the pre-change CSV copy. */
        public final String snapshotFile;

        RevisionEntry(long timestampMs, String action, String employeeId, String summary, String snapshotFile) {
            this.timestampMs = timestampMs;
            this.action = action;
            this.employeeId = employeeId;
            this.summary = summary;
            this.snapshotFile = snapshotFile;
        }

        /** Formats {@link #timestampMs} for display in the revision history UI. */
        public String formattedTime() {
            return new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date(timestampMs));
        }
    }

    /**
     * Loads revision index lines from disk into {@link #entries} at app startup.
     * Missing index file is treated as an empty history (no error thrown).
     */
    public static void loadFromDisk() {
        entries.clear();
        File index = resolve(INDEX_FILE);
        if (!index.isFile()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(index))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Pipe-delimited metadata; summary may not contain unescaped pipes
                String[] parts = line.split("\\|", 5);
                if (parts.length < 5) {
                    continue;
                }
                entries.add(new RevisionEntry(
                        Long.parseLong(parts[0].trim()),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim()));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not load revision index: " + e.getMessage());
        }
    }

    /**
     * Saves a CSV snapshot of {@code beforeSnapshot} and appends an index entry.
     * Called by HR CRUD handlers in {@link MotorPH_GUI} immediately before writing changes.
     *
     * @param action         label for the history table (Add / Edit / Delete)
     * @param employeeId     primary employee number involved
     * @param summary        short description for HR review
     * @param beforeSnapshot complete employee file rows before the mutation
     */
    public static void logChange(String action, String employeeId, String summary, List<String[]> beforeSnapshot) {
        if (beforeSnapshot == null) {
            return;
        }
        long ts = System.currentTimeMillis();
        String snapName = "rev_" + ts + ".csv";
        File snapFile = resolve(SNAP_DIR + "/" + snapName);
        snapFile.getParentFile().mkdirs();

        // Write header + every row exactly as stored in the live CSV
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(snapFile)))) {
            out.println(FileHandlerModule.getEmployeeFileHeader());
            for (String[] row : beforeSnapshot) {
                out.println(FileHandlerModule.joinCsvLine(row));
            }
        } catch (IOException e) {
            System.out.println("Could not save revision snapshot: " + e.getMessage());
            return;
        }

        RevisionEntry entry = new RevisionEntry(ts, action, nz(employeeId), nz(summary), snapName);
        entries.add(0, entry); // newest at front for UI list

        // Trim oldest entries and delete their snapshot files to cap disk use
        while (entries.size() > MAX_ENTRIES) {
            RevisionEntry removed = entries.remove(entries.size() - 1);
            resolve(SNAP_DIR + "/" + removed.snapshotFile).delete();
        }
        appendIndexLine(entry);
    }

    /** Returns an unmodifiable view of loaded revision entries (newest first). */
    public static List<RevisionEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Restores the employee CSV from a chosen snapshot file.
     *
     * @param entry revision row selected in the HR history dialog
     * @return {@code true} when the snapshot was read and {@link FileHandlerModule#rewriteEmployeeFile} succeeded
     */
    public static boolean revert(RevisionEntry entry) {
        if (entry == null) {
            return false;
        }
        File snap = resolve(SNAP_DIR + "/" + entry.snapshotFile);
        if (!snap.isFile()) {
            return false;
        }
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(snap))) {
            br.readLine(); // skip CSV header line
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(FileHandlerModule.smartSplit(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read revision snapshot: " + e.getMessage());
            return false;
        }
        // Pad/truncate each row to current schema before rewrite
        List<String[]> normalized = new ArrayList<>();
        for (String[] row : rows) {
            normalized.add(FileHandlerModule.normalizeEmployeeRow(row));
        }
        return FileHandlerModule.rewriteEmployeeFile(normalized);
    }

    /** Appends one metadata line to the persistent revision index file. */
    private static void appendIndexLine(RevisionEntry entry) {
        File index = resolve(INDEX_FILE);
        index.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(index, true)))) {
            out.println(entry.timestampMs + "|" + entry.action + "|" + entry.employeeId + "|"
                    + entry.summary.replace("|", "/") + "|" + entry.snapshotFile);
        } catch (IOException e) {
            System.out.println("Could not append revision index: " + e.getMessage());
        }
    }

    /**
     * Resolves a path under {@code resources/} whether cwd is project root or {@code bin/}.
     *
     * @param relativePath path relative to project root
     * @return {@link File} handle for read/write
     */
    private static File resolve(String relativePath) {
        File direct = new File(relativePath);
        if (direct.getParentFile() != null && direct.getParentFile().exists()) {
            return direct;
        }
        String userDir = System.getProperty("user.dir");
        File fromCwd = new File(userDir, relativePath);
        if (fromCwd.getParentFile() != null || fromCwd.exists()) {
            return fromCwd;
        }
        return new File(userDir, "../" + relativePath);
    }

    /** Null-safe trim: null → empty string. */
    private static String nz(String value) {
        return value == null ? "" : value.trim();
    }
}
