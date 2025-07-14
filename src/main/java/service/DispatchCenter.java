package service;

import model.Assignment;
import model.Package;
import model.Priority;
import model.Rider;
import model.Status;
import model.StatusChangeLog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * The DispatchCenter class manages package deliveries and rider assignments.
 * It handles registering riders, assigning packages based on priority and constraints,
 * tracking delivery statuses, logging changes, and managing rider availability.
 */

public class DispatchCenter {
    private final Map<String, Rider> riders = new HashMap<>();
    private final Map<String, Package> packages = new HashMap<>();
    private final Map<String, Assignment> assignments = new HashMap<>();
    private final List<StatusChangeLog> auditTrail = new ArrayList<>();

    /**
     * Priority queue of pending packages, sorted by priority, deadline, then order time.
     */
    private final PriorityQueue<Package> pendingPackages = new PriorityQueue<>((p1, p2) -> {
        if(p1.priority != p2.priority)
            return p1.priority == Priority.EXPRESS ? -1 : 1;
        if(p1.deadLine != p2.deadLine)
            return Long.compare(p1.deadLine, p2.deadLine);
        return Long.compare(p1.orderTime, p2.orderTime);
    });

    /**
     * Registers a new rider.
     * @param rider the rider to register
     * @throws IllegalArgumentException if a rider with the same ID already exists
     */
    public void registerRider(Rider rider){
        if(riders.containsKey(rider.id))
            throw new IllegalArgumentException("Rider already exists");
        riders.put(rider.id, rider);
    }

    /**
     * Updates the status and attributes of a rider.
     * If the rider becomes available, tries to assign a pending package to them.
     *
     * @param riderId the ID of the rider to update
     * @param status new availability status
     * @param reliability updated reliability score
     * @param canHandleFragile whether rider can handle fragile packages
     * @throws IllegalArgumentException if the rider does not exist
     */
    public void updateRiderStatus(String riderId, Status status, double reliability, boolean canHandleFragile){
        Rider rider = riders.get(riderId);
        if(rider == null)
            throw new IllegalArgumentException("Rider not found");

        rider.status = status;
        rider.reliability = reliability;
        rider.canHandleFragile = canHandleFragile;

        if (status == Status.AVAILABLE){
            tryAssignToAvailableRider(rider);
        }
    }

    /**
     * Tries to assign one pending package to the given available rider,
     * considering fragile-handling capability.
     *
     * @param rider the available rider
     */
    private void tryAssignToAvailableRider(Rider rider) {
        Iterator<Package> iterator = pendingPackages.iterator();
        List<Package> assignable = new ArrayList<>();

        while (iterator.hasNext()){
            Package pkg = iterator.next();
            if (pkg.fragile && !rider.canHandleFragile)
                continue;
            assignable.add(pkg);
            iterator.remove();
            break;
        }

        for (Package pkg : assignable) {
            rider.status = Status.BUSY;
            rider.currentLoad++;
            pkg.status = "ASSIGNED";
            pkg.assignedRiderId = rider.id;
            pkg.pickUpTime = System.currentTimeMillis();

            assignments.put(pkg.id, new Assignment(rider.id, pkg.id, System.currentTimeMillis()));
            logStatus(pkg.id, "ASSIGNED");
        }
    }

    /**
     * Places a new package order and attempts to assign it to an available rider.
     *
     * @param pkg the package to place
     * @throws IllegalArgumentException if a package with the same ID already exists
     */
    public void placeOrder(Package pkg){
        if(packages.containsKey(pkg.id))
            throw new IllegalArgumentException("Package already exists");
        packages.put(pkg.id, pkg);
        pendingPackages.offer(pkg);
        logStatus(pkg.id, "PENDING");
        assignPackageIfPossible(pkg);
    }

    /**
     * Assigns the given package to the most suitable available rider, if one exists.
     * Preference is given to higher reliability and fragile-handling capability.
     *
     * @param pkg the package to assign
     */
    private void assignPackageIfPossible(Package pkg) {
        Optional<Rider> candidate = riders.values().stream()
                .filter(r -> r.status == Status.AVAILABLE)
                .filter(r -> !pkg.fragile || r.canHandleFragile)
                .sorted(Comparator.comparingDouble((Rider r) -> -r.reliability))
                .findFirst();

        if (candidate.isPresent()){
            Rider rider = candidate.get();
            rider.status = Status.BUSY;
            rider.currentLoad++;

            pkg.status = "ASSIGNED";
            pkg.assignedRiderId = rider.id;
            pkg.pickUpTime = System.currentTimeMillis();

            assignments.put(pkg.id, new Assignment(rider.id, pkg.id, System.currentTimeMillis()));
            logStatus(pkg.id, "ASSIGNED");

        }
    }

    /**
     * Records a status change for the package in the audit log.
     *
     * @param packageId the ID of the package
     * @param status    the new status to log
     */
    private void logStatus(String packageId, String status) {
        auditTrail.add(new StatusChangeLog(packageId, status, System.currentTimeMillis()));
    }

    /**
     * Marks the given package as delivered and updates rider's load/status.
     *
     * @param packageId the package to mark as delivered
     * @throws IllegalArgumentException if the package doesn't exist or is not assigned
     */
    public void completeDelivery(String packageId){
        Package pkg = packages.get(packageId);
        if(pkg == null || !"ASSIGNED".equals(pkg.status))
            throw new IllegalArgumentException("Package not found or not assigned");
        pkg.status = "DELIVERED";
        pkg.deliveryTime = System.currentTimeMillis();
        logStatus(pkg.id, "DELIVERED");
        Rider rider = riders.get(pkg.assignedRiderId);
        if (rider != null){
            rider.currentLoad--;
            if (rider.currentLoad == 0){
                rider.status = Status.AVAILABLE;
                tryAssignToAvailableRider(rider);
            }
        }
    }

    /**
     * Returns a list of all packages delivered by a specific rider since a given time.
     *
     * @param riderId     the ID of the rider
     * @param sinceMillis timestamp (ms) to filter deliveries
     * @return list of delivered packages
     */
    public List<Package> getDeliveredByRider(String riderId, long sinceMillis){
        return packages.values().stream()
                .filter(p -> "DELIVERED".equals(p.status))
                .filter(p -> riderId.equals(p.assignedRiderId))
                .filter(p -> p.deliveryTime > 0 &&  p.deliveryTime >= sinceMillis)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of express packages that were delivered late (after their deadline).
     *
     * @return list of missed express deliveries
     */
    public List<Package> getMissedExpressDeliveries(){
        return packages.values().stream()
                .filter(p -> p.priority == Priority.EXPRESS)
                .filter(p -> "DELIVERED".equals(p.status))
                .filter(p -> p.deliveryTime > p.deadLine)
                .collect(Collectors.toList());
    }

    /**
     * Marks the rider as offline and reassigns all packages currently assigned to them.
     *
     * @param riderId the ID of the rider to mark offline
     */
    public void setRiderOffline(String riderId){
        Rider rider = riders.get(riderId);
        if (rider == null)
            return;
        rider.status = Status.OFFLINE;
        List<Package> reassigned = packages.values().stream()
                .filter(p -> "ASSIGNED".equals(p.status))
                .filter(p -> riderId.equals(p.assignedRiderId))
                .collect(Collectors.toList());

        for (Package p : reassigned) {
            p.status = "PENDING";
            p.assignedRiderId = null;
            p.pickUpTime = 0;
            pendingPackages.offer(p);
            logStatus(p.id, "REASSIGNED");
        }
        rider.currentLoad = 0;
    }

    /**
     * Returns the package with the given ID.
     *
     * @param id the package ID
     * @return the package, or null if not found
     */
    public Package getPackage(String id) {
        return packages.get(id);
    }

    /**
     * Returns a list of all pending packages.
     *
     * @return list of packages in PENDING status
     */
    public List<Package> getPendingPackages() {
        return new ArrayList<>(pendingPackages);
    }
}
