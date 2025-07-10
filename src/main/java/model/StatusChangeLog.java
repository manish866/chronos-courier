package model;

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
