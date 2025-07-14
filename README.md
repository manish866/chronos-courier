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

### Core Algorithm Highlights
- **Package Assignment**: Priority queue based on EXPRESS > STANDARD, deadline, and order time.
- **Rider Matching**: Available, reliable riders who meet fragile constraints are prioritized.
- **Late Delivery Detection**: `deliveryTime > deadline` for EXPRESS packages.

### Example Input
1. REGISTER_RIDER R1 AVAILABLE 4.7 true
2. PLACE_ORDER P1 EXPRESS 1899999999999 false
3. UPDATE_RIDER R1 AVAILABLE 4.8 true
4. DELIVER P1
5. REPORT_RIDER_PACKAGES R1 86400000
6. REPORT_EXPRESS_MISSED
7. RIDER_OFFLINE R1

### Screen Shots
1. Basic Rider Assignment and Delivery
<img width="2694" height="574" alt="image" src="https://github.com/user-attachments/assets/70ee4170-3fc6-4d7d-8d6e-071c55d0d178" />

2. Rider Goes Offline and Package Gets Reassigned
<img width="2714" height="398" alt="image" src="https://github.com/user-attachments/assets/feac00a1-e5d5-46f7-9d26-2cf87e71c42e" />

3. Rider Reliability Affects Assignment
<img width="2714" height="772" alt="image" src="https://github.com/user-attachments/assets/3bab92fc-23ad-41aa-a079-42d1bd97f677" />

4. Rider Fragile Capability Test
<img width="2722" height="774" alt="image" src="https://github.com/user-attachments/assets/93cc09c4-e0de-4c5a-814c-96dc6bf1d49e" />

5. Rider Update Triggers Assignment
<img width="2716" height="934" alt="image" src="https://github.com/user-attachments/assets/d9f7ffd6-c043-4286-a19a-6541f954f03a" />











