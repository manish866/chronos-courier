package model;

/**
 * Represents a log entry for a status change of a package.
 * Captures the package ID, new status, and the timestamp of the change.
 */
public class StatusChangeLog {
    public String packageId;
    public String status;
    public long timestamp;

    public StatusChangeLog(String packageId, String status, long timestamp) {
        this.packageId = packageId;
        this.status = status;
        this.timestamp = timestamp;
    }
}
