import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * CanteenEase - Smart Canteen Order Management System
 *
 * A console-based Java application that manages a college canteen menu,
 * customer orders, billing and order statuses.
 *
 * NOTE: All data is stored in memory (collections) only.
 * When the program exits, every order is lost and the menu is reset.
 */

// =====================================================================
//  ENUM: FoodCategory
// =====================================================================
enum FoodCategory {
    BREAKFAST("Breakfast"),
    MAIN_COURSE("Main Course"),
    SNACKS("Snacks"),
    BEVERAGES("Beverages"),
    DESSERTS("Desserts");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

// =====================================================================
//  ENUM: OrderStatus
// =====================================================================
enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED;

    /**
     * Returns the one status that is allowed to come after this one.
     * Returns null when there is no next status (COMPLETED / CANCELLED).
     */
    public OrderStatus getNextStatus() {
        switch (this) {
            case PLACED:
                return PREPARING;
            case PREPARING:
                return READY;
            case READY:
                return COMPLETED;
            default:
                return null;
        }
    }

    /** True if moving from this status to 'newStatus' is a valid forward step. */
    public boolean canMoveTo(OrderStatus newStatus) {
        return getNextStatus() == newStatus;
    }
}

// =====================================================================
//  CUSTOM EXCEPTION: InvalidOrderException
// =====================================================================
class InvalidOrderException extends Exception {
    public InvalidOrderException(String message) {
        super(message);
    }
}

// =====================================================================
//  CLASS: FoodItem
// =====================================================================
class FoodItem {
    private final int id;
    private final String name;
    private final FoodCategory category;
    private final BigDecimal price;
    private final boolean available;

    public FoodItem(int id, String name, FoodCategory category, BigDecimal price, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }
}

// =====================================================================
//  CLASS: Order
// =====================================================================
class Order {
    private final String orderId;
    // HashMap: FoodItem -> quantity. A LinkedHashMap (a type of HashMap) is used
    // so that items are shown in the same order in which they were added.
    private final HashMap<FoodItem, Integer> items = new LinkedHashMap<>();
    private final LocalDateTime orderDate;
    private OrderStatus status;

    public Order(String orderId) {
        this.orderId = orderId;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PLACED;
    }

    /** Adds an item. If the item is already in the order, the quantities are added together. */
    public void addItem(FoodItem item, int quantity) {
        int existingQuantity = items.getOrDefault(item, 0);
        items.put(item, existingQuantity + quantity);
    }

    public String getOrderId() {
        return orderId;
    }

    public HashMap<FoodItem, Integer> getItems() {
        return items;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /** Subtotal for a single item = unit price x quantity. */
    public BigDecimal getSubtotal(FoodItem item) {
        return item.getPrice().multiply(BigDecimal.valueOf(items.get(item)));
    }

    /** Grand total = sum of all item subtotals. */
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (FoodItem item : items.keySet()) {
            total = total.add(getSubtotal(item));
        }
        return total;
    }

    /** Total number of individual units in the order. */
    public int getTotalQuantity() {
        int count = 0;
        for (int quantity : items.values()) {
            count += quantity;
        }
        return count;
    }
}

// =====================================================================
//  CLASS: OrderService  (business logic)
// =====================================================================
class OrderService {
    public static final int MAX_QUANTITY_PER_ITEM = 20;
    private static final String ORDER_ID_PREFIX = "ORD";
    // Static counter shared by the whole program: gives every order a unique number.
    private static int nextOrderNumber = 1001;

    private final ArrayList<FoodItem> menu = new ArrayList<>();
    private final ArrayList<Order> orders = new ArrayList<>();

    public OrderService() {
        loadMenu();
    }

    private void loadMenu() {
        menu.add(new FoodItem(1, "Masala Dosa", FoodCategory.BREAKFAST, new BigDecimal("45.00"), true));
        menu.add(new FoodItem(2, "Idli (2 pcs)", FoodCategory.BREAKFAST, new BigDecimal("30.00"), true));
        menu.add(new FoodItem(3, "Veg Fried Rice", FoodCategory.MAIN_COURSE, new BigDecimal("80.00"), true));
        menu.add(new FoodItem(4, "Chicken Biryani", FoodCategory.MAIN_COURSE, new BigDecimal("120.00"), true));
        menu.add(new FoodItem(5, "Paneer Meals", FoodCategory.MAIN_COURSE, new BigDecimal("110.00"), true));
        menu.add(new FoodItem(6, "Veg Sandwich", FoodCategory.SNACKS, new BigDecimal("40.00"), true));
        menu.add(new FoodItem(7, "Samosa (2 pcs)", FoodCategory.SNACKS, new BigDecimal("20.00"), true));
        menu.add(new FoodItem(8, "French Fries", FoodCategory.SNACKS, new BigDecimal("50.00"), true));
        menu.add(new FoodItem(9, "Tea", FoodCategory.BEVERAGES, new BigDecimal("12.00"), true));
        menu.add(new FoodItem(10, "Filter Coffee", FoodCategory.BEVERAGES, new BigDecimal("15.00"), true));
        menu.add(new FoodItem(11, "Cold Coffee", FoodCategory.BEVERAGES, new BigDecimal("45.00"), false));
        menu.add(new FoodItem(12, "Gulab Jamun (2 pcs)", FoodCategory.DESSERTS, new BigDecimal("30.00"), true));
        menu.add(new FoodItem(13, "Ice Cream Cup", FoodCategory.DESSERTS, new BigDecimal("35.00"), true));
    }

    public List<FoodItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }

    public FoodItem findFoodById(int id) throws InvalidOrderException {
        for (FoodItem item : menu) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new InvalidOrderException("Food item ID " + id + " does not exist on the menu.");
    }

    /**
     * Creates a new order from a map of (food item ID -> quantity).
     * Every ID and quantity is validated before the order is saved.
     */
    public Order placeOrder(HashMap<Integer, Integer> selections) throws InvalidOrderException {
        if (selections == null || selections.isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one food item.");
        }

        Order order = new Order(generateOrderId());
        for (int foodId : selections.keySet()) {
            int quantity = selections.get(foodId);
            FoodItem item = findFoodById(foodId);

            if (!item.isAvailable()) {
                throw new InvalidOrderException(item.getName() + " is currently unavailable.");
            }
            if (quantity < 1 || quantity > MAX_QUANTITY_PER_ITEM) {
                throw new InvalidOrderException("Quantity for " + item.getName()
                        + " must be between 1 and " + MAX_QUANTITY_PER_ITEM + ".");
            }
            order.addItem(item, quantity);
        }

        orders.add(order);
        nextOrderNumber++; // only consumed once the order is successfully saved
        return order;
    }

    private String generateOrderId() {
        return ORDER_ID_PREFIX + nextOrderNumber;
    }

    /** Accepts "ORD1001", "ord1001" or just "1001". */
    public Order findOrderById(String input) throws InvalidOrderException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidOrderException("Order ID cannot be empty.");
        }
        String id = input.trim().toUpperCase();
        if (id.matches("\\d+")) {
            id = ORDER_ID_PREFIX + id;
        }
        for (Order order : orders) {
            if (order.getOrderId().equals(id)) {
                return order;
            }
        }
        throw new InvalidOrderException("No order found with ID " + id + ".");
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) throws InvalidOrderException {
        Order order = findOrderById(orderId);
        OrderStatus current = order.getStatus();

        if (current == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order " + order.getOrderId() + " is cancelled and cannot be updated.");
        }
        if (current == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Order " + order.getOrderId() + " is already completed.");
        }
        if (current == newStatus) {
            throw new InvalidOrderException("Order is already in " + current + " status.");
        }
        if (!current.canMoveTo(newStatus)) {
            throw new InvalidOrderException("Invalid transition: " + current + " -> " + newStatus
                    + ". The next allowed status is " + current.getNextStatus() + ".");
        }
        order.setStatus(newStatus);
    }

    public void cancelOrder(String orderId) throws InvalidOrderException {
        Order order = findOrderById(orderId);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Order " + order.getOrderId() + " is completed and cannot be cancelled.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order " + order.getOrderId() + " is already cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    // ---------------- Summary helpers ----------------

    public int countByStatus(OrderStatus status) {
        int count = 0;
        for (Order order : orders) {
            if (order.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /** Orders that are still in progress: PLACED, PREPARING or READY. */
    public int countPending() {
        return countByStatus(OrderStatus.PLACED)
                + countByStatus(OrderStatus.PREPARING)
                + countByStatus(OrderStatus.READY);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.COMPLETED) {
                revenue = revenue.add(order.getTotalAmount());
            }
        }
        return revenue;
    }

    /**
     * Finds the food item with the highest total quantity across all
     * non-cancelled orders. Returns null if there are none.
     */
    public String getMostOrderedItemDescription() {
        HashMap<FoodItem, Integer> totals = new HashMap<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            for (FoodItem item : order.getItems().keySet()) {
                int quantity = order.getItems().get(item);
                totals.put(item, totals.getOrDefault(item, 0) + quantity);
            }
        }

        FoodItem best = null;
        int bestQuantity = 0;
        for (FoodItem item : menu) { // loop over the menu so ties are resolved by menu order
            int quantity = totals.getOrDefault(item, 0);
            if (quantity > bestQuantity) {
                best = item;
                bestQuantity = quantity;
            }
        }
        if (best == null) {
            return null;
        }
        return best.getName() + " (" + bestQuantity + " ordered)";
    }
}

// =====================================================================
//  MAIN CLASS: CanteenEase  (console user interface)
// =====================================================================
public class CanteenEase {

    private static final Scanner scanner = new Scanner(System.in);
    private static final OrderService orderService = new OrderService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
    private static final String LINE = "========================================================";
    private static final String THIN_LINE = "--------------------------------------------------------";

    public static void main(String[] args) {
        try {
            boolean running = true;
            while (running) {
                printMainMenu();
                int choice = readInt("Enter your choice: ");
                switch (choice) {
                    case 1:
                        viewFoodMenu();
                        break;
                    case 2:
                        placeNewOrder();
                        break;
                    case 3:
                        viewAllOrders();
                        break;
                    case 4:
                        searchOrder();
                        break;
                    case 5:
                        updateOrderStatus();
                        break;
                    case 6:
                        cancelOrder();
                        break;
                    case 7:
                        generateBill();
                        break;
                    case 8:
                        viewOrderSummary();
                        break;
                    case 9:
                        running = false;
                        System.out.println("\nThank you for using CanteenEase. Goodbye!");
                        break;
                    default:
                        System.out.println("\n[!] Invalid choice. Please enter a number from 1 to 9.");
                }
            }
        } catch (NoSuchElementException e) {
            // Happens only if the input stream is closed (for example Ctrl+D / piped input ended).
            System.out.println("\nInput ended. Exiting CanteenEase.");
        } finally {
            scanner.close(); // always release the Scanner, however the program ends
        }
    }

    // ---------------- Menu screens ----------------

    private static void printMainMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("                      CANTEENEASE");
        System.out.println("               SMART CANTEEN MANAGEMENT");
        System.out.println(LINE);
        System.out.println("  1. View Food Menu");
        System.out.println("  2. Place New Order");
        System.out.println("  3. View All Orders");
        System.out.println("  4. Search Order");
        System.out.println("  5. Update Order Status");
        System.out.println("  6. Cancel Order");
        System.out.println("  7. Generate Bill");
        System.out.println("  8. View Order Summary");
        System.out.println("  9. Exit");
        System.out.println(LINE);
    }

    private static void viewFoodMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("                      FOOD MENU");
        System.out.println(LINE);
        System.out.printf("%-4s %-22s %-13s %9s  %s%n", "ID", "Item", "Category", "Price", "Status");
        System.out.println(THIN_LINE);
        for (FoodItem item : orderService.getMenu()) {
            System.out.printf("%-4d %-22s %-13s %9s  %s%n",
                    item.getId(),
                    item.getName(),
                    item.getCategory().getDisplayName(),
                    "Rs." + item.getPrice(),
                    item.isAvailable() ? "Available" : "Unavailable");
        }
        System.out.println(LINE);
    }

    private static void placeNewOrder() {
        viewFoodMenu();
        System.out.println("Add items to your order. Enter item ID 0 when you are done.");

        HashMap<Integer, Integer> selections = new LinkedHashMap<>();
        boolean addingItems = true;

        while (addingItems) {
            int foodId = readInt("\nEnter food item ID (0 to finish): ");
            if (foodId == 0) {
                addingItems = false;
                continue;
            }

            try {
                FoodItem item = orderService.findFoodById(foodId);
                if (!item.isAvailable()) {
                    System.out.println("[!] Sorry, " + item.getName() + " is currently unavailable.");
                    continue;
                }

                int quantity = readInt("Enter quantity for " + item.getName() + ": ");
                if (quantity < 1 || quantity > OrderService.MAX_QUANTITY_PER_ITEM) {
                    System.out.println("[!] Quantity must be between 1 and "
                            + OrderService.MAX_QUANTITY_PER_ITEM + ".");
                    continue;
                }

                int alreadyAdded = selections.getOrDefault(foodId, 0);
                if (alreadyAdded + quantity > OrderService.MAX_QUANTITY_PER_ITEM) {
                    System.out.println("[!] You can order at most " + OrderService.MAX_QUANTITY_PER_ITEM
                            + " of one item (you already added " + alreadyAdded + ").");
                    continue;
                }

                selections.put(foodId, alreadyAdded + quantity);
                System.out.println("Added: " + quantity + " x " + item.getName());
            } catch (InvalidOrderException e) {
                System.out.println("[!] " + e.getMessage());
            }
        }

        if (selections.isEmpty()) {
            System.out.println("\nNo items selected. Order not placed.");
            return;
        }

        String confirm = readLine("\nConfirm and place this order? (Y/N): ");
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Order discarded.");
            return;
        }

        try {
            Order order = orderService.placeOrder(selections);
            System.out.println("\n[OK] Order placed successfully!");
            printOrderDetails(order);
        } catch (InvalidOrderException e) {
            System.out.println("[!] Could not place order: " + e.getMessage());
        }
    }

    private static void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        System.out.println();
        System.out.println(LINE);
        System.out.println("                      ALL ORDERS");
        System.out.println(LINE);

        if (orders.isEmpty()) {
            System.out.println("No orders have been placed yet.");
            System.out.println(LINE);
            return;
        }

        for (Order order : orders) {
            System.out.println("Order ID : " + order.getOrderId());
            System.out.println("Items    : " + buildItemsSummary(order));
            System.out.println("Total    : Rs." + order.getTotalAmount());
            System.out.println("Status   : " + order.getStatus());
            System.out.println(THIN_LINE);
        }
        System.out.println("Total orders: " + orders.size());
        System.out.println(LINE);
    }

    private static void searchOrder() {
        String orderId = readLine("\nEnter Order ID to search (e.g. ORD1001): ");
        try {
            Order order = orderService.findOrderById(orderId);
            printOrderDetails(order);
        } catch (InvalidOrderException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void updateOrderStatus() {
        String orderId = readLine("\nEnter Order ID to update: ");
        try {
            Order order = orderService.findOrderById(orderId);
            System.out.println("Current status of " + order.getOrderId() + ": " + order.getStatus());

            if (order.getStatus().getNextStatus() == null) {
                System.out.println("[!] This order is " + order.getStatus() + " and cannot be updated further.");
                return;
            }

            System.out.println("\nSelect new status:");
            System.out.println("  1. PLACED");
            System.out.println("  2. PREPARING");
            System.out.println("  3. READY");
            System.out.println("  4. COMPLETED");
            int choice = readInt("Enter choice: ");

            OrderStatus newStatus;
            switch (choice) {
                case 1:
                    newStatus = OrderStatus.PLACED;
                    break;
                case 2:
                    newStatus = OrderStatus.PREPARING;
                    break;
                case 3:
                    newStatus = OrderStatus.READY;
                    break;
                case 4:
                    newStatus = OrderStatus.COMPLETED;
                    break;
                default:
                    System.out.println("[!] Invalid status choice. Please enter 1 to 4.");
                    return;
            }

            orderService.updateOrderStatus(order.getOrderId(), newStatus);
            System.out.println("[OK] Order " + order.getOrderId() + " is now " + order.getStatus() + ".");
        } catch (InvalidOrderException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void cancelOrder() {
        String orderId = readLine("\nEnter Order ID to cancel: ");
        try {
            Order order = orderService.findOrderById(orderId);
            String confirm = readLine("Cancel order " + order.getOrderId() + " (status: "
                    + order.getStatus() + ")? (Y/N): ");
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Cancellation aborted.");
                return;
            }
            orderService.cancelOrder(order.getOrderId());
            System.out.println("[OK] Order " + order.getOrderId() + " has been cancelled.");
        } catch (InvalidOrderException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void generateBill() {
        String orderId = readLine("\nEnter Order ID to generate bill: ");
        try {
            Order order = orderService.findOrderById(orderId);
            printBill(order);
        } catch (InvalidOrderException e) {
            System.out.println("[!] " + e.getMessage());
        }
    }

    private static void viewOrderSummary() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("                    ORDER SUMMARY");
        System.out.println(LINE);
        System.out.println("Total orders      : " + orderService.getAllOrders().size());
        System.out.println("Completed orders  : " + orderService.countByStatus(OrderStatus.COMPLETED));
        System.out.println("Pending orders    : " + orderService.countPending()
                + "  (placed / preparing / ready)");
        System.out.println("Cancelled orders  : " + orderService.countByStatus(OrderStatus.CANCELLED));
        System.out.println("Total revenue     : Rs." + orderService.getTotalRevenue()
                + "  (completed orders only)");

        String mostOrdered = orderService.getMostOrderedItemDescription();
        System.out.println("Most ordered item : " + (mostOrdered == null ? "N/A (no orders yet)" : mostOrdered));
        System.out.println(LINE);
    }

    // ---------------- Printing helpers ----------------

    private static void printOrderDetails(Order order) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("                    ORDER DETAILS");
        System.out.println(LINE);
        System.out.println("Order ID : " + order.getOrderId());
        System.out.println("Date     : " + order.getOrderDate().format(DATE_FORMAT));
        System.out.println("Status   : " + order.getStatus());
        System.out.println(THIN_LINE);
        printItemTable(order);
        System.out.println(LINE);
    }

    private static void printBill(Order order) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("                    CANTEENEASE BILL");
        System.out.println(LINE);
        System.out.println("Order ID : " + order.getOrderId());
        System.out.println("Date     : " + order.getOrderDate().format(DATE_FORMAT));
        System.out.println(THIN_LINE);
        printItemTable(order);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("NOTE: This order was CANCELLED. No payment is due.");
        }
        System.out.println("       Thank you for ordering with CanteenEase!");
        System.out.println(LINE);
    }

    /** Prints the item / quantity / unit price / subtotal table and the grand total. */
    private static void printItemTable(Order order) {
        System.out.printf("%-22s %5s %10s %11s%n", "Item", "Qty", "Unit Price", "Subtotal");
        System.out.println(THIN_LINE);
        for (FoodItem item : order.getItems().keySet()) {
            System.out.printf("%-22s %5d %10s %11s%n",
                    item.getName(),
                    order.getItems().get(item),
                    item.getPrice(),
                    order.getSubtotal(item));
        }
        System.out.println(THIN_LINE);
        System.out.printf("%-22s %5d %10s %11s%n", "GRAND TOTAL (Rs.)",
                order.getTotalQuantity(), "", order.getTotalAmount());
    }

    private static String buildItemsSummary(Order order) {
        StringBuilder builder = new StringBuilder();
        for (FoodItem item : order.getItems().keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(item.getName()).append(" x").append(order.getItems().get(item));
        }
        return builder.toString();
    }

    // ---------------- Input helpers ----------------

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Keeps asking until the user types a valid whole number. */
    private static int readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a whole number.");
            }
        }
    }
}
