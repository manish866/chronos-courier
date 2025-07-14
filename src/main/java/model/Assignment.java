package model;

/**
 * Represents the assignment of a package to a rider.
 * Tracks the rider ID, package ID, and the timestamp when the assignment occurred.
 */
public class Assignment {
    public String riderId;
    public String packageId;
    public long assignedTime;

    public Assignment(String riderId, String packageId, long assignedTime) {
        this.riderId = riderId;
        this.packageId = packageId;
        this.assignedTime = assignedTime;
    }
}
