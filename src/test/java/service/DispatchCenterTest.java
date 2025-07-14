package service;

import model.Package;
import model.Priority;
import model.Rider;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the DispatchCenter class, validating rider assignment,
 * delivery lifecycle, reassignment logic, and handling of fragile packages.
 */
class DispatchCenterTest {
    private DispatchCenter center;

    /**
     * Sets up a new DispatchCenter before each test.
     */
    @BeforeEach
    void setUp() {
        center = new DispatchCenter();
    }

    /**
     * Tests that a package is correctly assigned to an available rider,
     * and verifies assignment details such as status and pickup time.
     */
    @Test
    void testRegisterAndAssignPackage() {
        center.registerRider(new Rider("R1", Status.AVAILABLE, 4.9, false));
        Package pkg = new Package("P1", Priority.EXPRESS, now(), now() + 10000, false);

        center.placeOrder(pkg);

        assertEquals("ASSIGNED", pkg.status);
        assertEquals("R1", pkg.assignedRiderId);
        assertTrue(pkg.pickUpTime > 0);
    }

    /**
     * Tests that a fragile package is only assigned to a rider
     * who is capable of handling fragile items.
     */
    @Test
    void testFragileOnlyAssignedToCapableRider() {
        center.registerRider(new Rider("R1", Status.AVAILABLE, 4.9, false));
        center.registerRider(new Rider("R2", Status.AVAILABLE, 4.8, true));

        Package fragilePkg = new Package("P2", Priority.EXPRESS, now(), now() + 10000, true);
        center.placeOrder(fragilePkg);

        assertEquals("ASSIGNED", fragilePkg.status);
        assertEquals("R2", fragilePkg.assignedRiderId);
    }

    /**
     * Tests that after a rider delivers a package, the package status is updated to "DELIVERED",
     * and that the delivery time is recorded.
     */
    @Test
    void testDeliveryCompletion() {
        center.registerRider(new Rider("R1", Status.AVAILABLE, 4.9, true));
        Package pkg = new Package("P3", Priority.EXPRESS, now(), now() + 10000, false);
        center.placeOrder(pkg);

        center.updateRiderStatus("R1", Status.AVAILABLE, 4.9, true);

        Package actualPkg = center.getPackage("P3");
        assertEquals("ASSIGNED", actualPkg.status);

        center.completeDelivery("P3");

        actualPkg = center.getPackage("P3");
        assertEquals("DELIVERED", actualPkg.status);
        assertTrue(actualPkg.deliveryTime > 0);
    }

    /**
     * Tests reassignment logic when a rider goes offline after a package is assigned.
     * Ensures that the package is first unassigned, and then reassigned to another available rider.
     */
    @Test
    void testSetRiderOfflineAndReassignment() {
        center.registerRider(new Rider("R1", Status.AVAILABLE, 4.9, true));
        center.registerRider(new Rider("R2", Status.AVAILABLE, 4.5, true));

        Package pkg = new Package("P5", Priority.EXPRESS, now(), now() + 10000, false);
        center.placeOrder(pkg);

        assertEquals("R1", pkg.assignedRiderId);

        center.setRiderOffline("R1");

        assertEquals("PENDING", pkg.status);
        assertNull(pkg.assignedRiderId);

        center.updateRiderStatus("R2", Status.AVAILABLE, 4.5, true);

        assertEquals("ASSIGNED", pkg.status);
        assertEquals("R2", pkg.assignedRiderId);
    }

    /**
     * Helper method to return the current system time in milliseconds.
     */
    private long now() {
        return System.currentTimeMillis();
    }
}
