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
 * Tracks revision snapshots so HR can review and revert employee CSV changes.
 */
public class EmployeeRevisionModule {

    private static final int MAX_ENTRIES = 100;
    private static final String INDEX_FILE = "resources/employee_revision_index.txt";
    private static final String SNAP_DIR = "resources/revisions";

    private static final List<RevisionEntry> entries = new ArrayList<>();

    public static class RevisionEntry {
        public final long timestampMs;
        public final String action;
        public final String employeeId;
        public final String summary;
        public final String snapshotFile;

        RevisionEntry(long timestampMs, String action, String employeeId, String summary, String snapshotFile) {
            this.timestampMs = timestampMs;
            this.action = action;
            this.employeeId = employeeId;
            this.summary = summary;
            this.snapshotFile = snapshotFile;
        }

        public String formattedTime() {
            return new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date(timestampMs));
        }
    }

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
     * Records a revision using the employee CSV state before a change is applied.
     */
    public static void logChange(String action, String employeeId, String summary, List<String[]> beforeSnapshot) {
        if (beforeSnapshot == null) {
            return;
        }
        long ts = System.currentTimeMillis();
        String snapName = "rev_" + ts + ".csv";
        File snapFile = resolve(SNAP_DIR + "/" + snapName);
        snapFile.getParentFile().mkdirs();

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
        entries.add(0, entry);
        while (entries.size() > MAX_ENTRIES) {
            RevisionEntry removed = entries.remove(entries.size() - 1);
            resolve(SNAP_DIR + "/" + removed.snapshotFile).delete();
        }
        appendIndexLine(entry);
    }

    public static List<RevisionEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

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
            br.readLine();
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
        List<String[]> normalized = new ArrayList<>();
        for (String[] row : rows) {
            normalized.add(FileHandlerModule.normalizeEmployeeRow(row));
        }
        return FileHandlerModule.rewriteEmployeeFile(normalized);
    }

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

    private static String nz(String value) {
        return value == null ? "" : value.trim();
    }
}
