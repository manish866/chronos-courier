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

public class DispatchCenter {
    private final Map<String, Rider> riders = new HashMap<>();
    private final Map<String, Package> packages = new HashMap<>();
    private final Map<String, Assignment> assignments = new HashMap<>();
    private final List<StatusChangeLog> auditTrail = new ArrayList<>();

    private final PriorityQueue<Package> pendingPackages = new PriorityQueue<>((p1, p2) -> {
        if(p1.priority != p2.priority)
            return p1.priority == Priority.EXPRESS ? -1 : 1;
        if(p1.deadLine != p2.deadLine)
            return Long.compare(p1.deadLine, p2.deadLine);
        return Long.compare(p1.orderTime, p2.orderTime);
    });

    public void registerRider(Rider rider){
        if(riders.containsKey(rider.id))
            throw new IllegalArgumentException("Rider already exists");
        riders.put(rider.id, rider);
    }

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

    public void placeOrder(Package pkg){
        if(packages.containsKey(pkg.id))
            throw new IllegalArgumentException("Package already exists");
        packages.put(pkg.id, pkg);
        pendingPackages.offer(pkg);
        logStatus(pkg.id, "PENDING");
        assignPackageIfPossible(pkg);
    }

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

    private void logStatus(String packageId, String status) {
        auditTrail.add(new StatusChangeLog(packageId, status, System.currentTimeMillis()));
    }

    public void completeDelivery(String packageId){
        Package pkg = packages.get(packageId);
        if(pkg == null || "ASSIGNED".equals(pkg.status))
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

    public List<Package> getDeliveredByRider(String riderId, long sinceMillis){
        return packages.values().stream()
                .filter(p -> "DELIVERED".equals(p.status))
                .filter(p -> riderId.equals(p.assignedRiderId))
                .filter(p -> p.deliveryTime >= sinceMillis)
                .collect(Collectors.toList());



    }
    public List<Package> getMissedExpressDeliveries(){
        return packages.values().stream()
                .filter(p -> p.priority == Priority.EXPRESS)
                .filter(p -> "DELIVERED".equals(p.status))
                .filter(p -> p.deliveryTime > p.deadLine)
                .collect(Collectors.toList());



    }

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
}
