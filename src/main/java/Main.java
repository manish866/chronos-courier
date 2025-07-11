import model.Package;
import model.Priority;
import model.Rider;
import model.Status;
import service.DispatchCenter;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DispatchCenter center = new DispatchCenter();
        Scanner sc = new Scanner(System.in);

        System.out.println("Chronos Couriers System Ready. Enter commands:");

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            if (input.trim().equalsIgnoreCase("exit")) break;

            try {
                handleCommand(center, input.trim());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Exiting...");
    }

    private static void handleCommand(DispatchCenter center, String input) {
        String[] parts = input.split("\\s+");

        switch (parts[0]) {
            case "PLACE_ORDER": {
                String id = parts[1];
                Priority priority = Priority.valueOf(parts[2]);
                long deadline = Long.parseLong(parts[3]);
                boolean fragile = Boolean.parseBoolean(parts[4]);
                long now = System.currentTimeMillis();

                center.placeOrder(new Package(id, priority, now, deadline, fragile));
                System.out.println("Order placed: " + id);
                break;
            }

            case "REGISTER_RIDER": {
                String id = parts[1];
                Status status = Status.valueOf(parts[2]);
                double reliability = Double.parseDouble(parts[3]);
                boolean fragileCapable = Boolean.parseBoolean(parts[4]);

                center.registerRider(new Rider(id, status, reliability, fragileCapable));
                System.out.println("Rider registered: " + id);
                break;
            }

            case "UPDATE_RIDER": {
                String id = parts[1];
                Status status = Status.valueOf(parts[2]);
                double reliability = Double.parseDouble(parts[3]);
                boolean fragile = Boolean.parseBoolean(parts[4]);

                center.updateRiderStatus(id, status, reliability, fragile);
                System.out.println("Rider updated: " + id);
                break;
            }

            case "DELIVER": {
                String pkgId = parts[1];
                center.completeDelivery(pkgId);
                System.out.println("Delivered: " + pkgId);
                break;
            }

            case "RIDER_OFFLINE": {
                String riderId = parts[1];
                center.setRiderOffline(riderId);
                System.out.println("Rider offline: " + riderId);
                break;
            }

            case "REPORT_RIDER_PACKAGES": {
                String riderId = parts[1];
                long pastMillis = Long.parseLong(parts[2]);

                center.getDeliveredByRider(riderId, System.currentTimeMillis() - pastMillis)
                        .forEach(p -> System.out.println(p.id));
                break;
            }

            case "REPORT_EXPRESS_MISSED": {
                center.getMissedExpressDeliveries().forEach(p -> System.out.println(p.id));
                break;
            }

            default:
                System.out.println("Unknown command: " + parts[0]);
        }
    }
}