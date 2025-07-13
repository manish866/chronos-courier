# Chronos Couriers - Intelligent Delivery Dispatch System

Welcome to **Chronos Couriers**, a simulation of a smart dispatch system for high-priority, time-sensitive city 
deliveries. This CLI-based Java application handles real-time order assignment, rider management, and delivery tracking
using in-memory data structures no databases, no network, just clean logic.

---

## How to Run

### Prerequisites:
- Java 17 or higher (tested on OpenJDK 21+)

### Steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/manish866/chronos-courier.git
   cd chronos-courier

2. Compile:
If using IntelliJ or VSCode, just run the Main.java file.

3. Interact via CLI:
   Type commands like:
    ``` bash
    REGISTER_RIDER R1 AVAILABLE 4.7 true
    PLACE_ORDER P1 EXPRESS 1899999999999 false
    UPDATE_RIDER R1 AVAILABLE 4.8 true
    DELIVER P1
    REPORT_RIDER_PACKAGES R1 86400000
    REPORT_EXPRESS_MISSED

### System Design Overview:
### Core Classes:
1. DispatchCenter: Orchestrates all package and rider logic.
2. Package: Represents an order with priority, deadline, and status.
3. Rider: Holds status, reliability, fragile-capability, and current load.
4. PriorityQueue<Package>: Manages dispatch order.
5. Map<String, Rider> and Map<String, Package>: Fast lookup and storage.

### Example Input
1. REGISTER_RIDER R1 AVAILABLE 4.7 true
2. PLACE_ORDER P1 EXPRESS 1899999999999 false
3. UPDATE_RIDER R1 AVAILABLE 4.8 true
4. DELIVER P1
5. REPORT_RIDER_PACKAGES R1 86400000
6. REPORT_EXPRESS_MISSED
7. RIDER_OFFLINE R1





