package model;

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
