package model;

/**
 * Represents a delivery package in the Chronos Couriers system.
 * Stores metadata such as priority, deadlines, assignment status, and timestamps.
 */
public class Package {
    public String id;
    public Priority priority;
    public long orderTime;
    public long deadLine;
    public boolean fragile;
    public String status;
    public String assignedRiderId;
    public long pickUpTime;
    public long deliveryTime;

    public Package(String id, Priority priority, long orderTime, long deadLine, boolean fragile) {
        this.id = id;
        this.priority = priority;
        this.orderTime = orderTime;
        this.deadLine = deadLine;
        this.fragile = fragile;
        this.status = "PENDING";
    }
}
