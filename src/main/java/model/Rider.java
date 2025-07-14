package model;

/**
 * Represents a delivery rider in the system.
 * Contains attributes related to rider status, capability, and workload.
 */
public class Rider {
    public String id;
    public Status status;
    public double reliability;
    public boolean canHandleFragile;
    public int currentLoad;

    public Rider(String id, Status status, double reliability, boolean canHandleFragile) {
        this.id = id;
        this.status = status;
        this.reliability = reliability;
        this.canHandleFragile = canHandleFragile;
        this.currentLoad = 0;
    }
}
