import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


// Abstract base class
abstract class Goods {
    protected String productCode;
    protected String name;
    protected int quantity;  
    protected double unitPrice;

    public Goods(String productCode, String name, int quantity, double unitPrice) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        this.productCode = productCode;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalValue() {
        return quantity * unitPrice;
    }

    // Each subclass has own VAT rate
    public abstract double getVatRate();

    public double getVatAmount() {
        return getTotalValue() * getVatRate();
    }

    // Requirement 2: consumption/sale evaluation, overridden per type
    public abstract String evaluateConsumption();

    @Override
    public String toString() {
        return String.format("[%s] %s | code=%s | qty=%d | price=%.2f | VAT=%.2f | eval=%s",
                this.getClass().getSimpleName(), name, productCode, quantity, unitPrice,
                getVatAmount(), evaluateConsumption());
    }
}

// Food subclass
class Food extends Goods {
    private static final double VAT_RATE = 0.05;
    private LocalDate manufactureDate;
    private LocalDate expirationDate;
    private String supplier;

    public Food(String productCode, String name, int quantity, double unitPrice,
                LocalDate manufactureDate, LocalDate expirationDate, String supplier) {
        super(productCode, name, quantity, unitPrice);
        if (expirationDate.isBefore(manufactureDate)) {
            throw new IllegalArgumentException("Expiration date must be after manufacture date");
        }
        this.manufactureDate = manufactureDate;
        this.expirationDate = expirationDate;
        this.supplier = supplier;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    @Override
    public double getVatRate() {
        return VAT_RATE;
    }

    @Override
    public String evaluateConsumption() {
        if (quantity > 0 && isExpired()) {
            return "Hard to sell (expired, still in stock)";
        }
        return "Not evaluated";
    }
}

// ---------- Electronics ----------
class Electronics extends Goods {
    private static final double VAT_RATE = 0.10; 
    private int warrantyMonths; 
    private double capacityKW;  

    public Electronics(String productCode, String name, int quantity, double unitPrice,
                        int warrantyMonths, double capacityKW) {
        super(productCode, name, quantity, unitPrice);
        if (warrantyMonths < 0) {
            throw new IllegalArgumentException("Warranty months must be >= 0");
        }
        if (capacityKW < 0) {
            throw new IllegalArgumentException("Capacity must be >= 0");
        }
        this.warrantyMonths = warrantyMonths;
        this.capacityKW = capacityKW;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public double getCapacityKW() {
        return capacityKW;
    }

    @Override
    public double getVatRate() {
        return VAT_RATE;
    }

    @Override
    public String evaluateConsumption() {
        if (quantity < 3) {
            return "Considered sold (low stock < 3)";
        }
        return "Not evaluated";
    }
}

// Crockery 
class Crockery extends Goods {
    private static final double VAT_RATE = 0.10;
    private String manufacturerInfo;
    private LocalDate arrivalDate;

    public Crockery(String productCode, String name, int quantity, double unitPrice,
                     String manufacturerInfo, LocalDate arrivalDate) {
        super(productCode, name, quantity, unitPrice);
        this.manufacturerInfo = manufacturerInfo;
        this.arrivalDate = arrivalDate;
    }

    public String getManufacturerInfo() {
        return manufacturerInfo;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public long getStorageDays() {
        return ChronoUnit.DAYS.between(arrivalDate, LocalDate.now());
    }

    @Override
    public double getVatRate() {
        return VAT_RATE;
    }

    @Override
    public String evaluateConsumption() {
        if (quantity > 50 && getStorageDays() > 10) {
            return "Slow sale (qty>50 and storage>10 days)";
        }
        return "Not evaluated";
    }
}

// Requirement 3: DSHH management using an array 
class GoodsManager {
    private Goods[] list;
    private int size;

    public GoodsManager(int capacity) {
        list = new Goods[capacity];
        size = 0;
    }

    private boolean isDuplicateCode(String productCode) {
        for (int i = 0; i < size; i++) {
            if (list[i].getProductCode().equalsIgnoreCase(productCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean addGoods(Goods g) {
        if (size >= list.length) {
            System.out.println("Inventory list is full.");
            return false;
        }
        if (isDuplicateCode(g.getProductCode())) {
            System.out.println("Add failed: duplicate product code '" + g.getProductCode() + "'.");
            return false;
        }
        list[size++] = g;
        System.out.println("Add success: " + g.getName());
        return true;
    }

    public int getQuantityByType(Class<? extends Goods> type) {
        int total = 0;
        for (int i = 0; i < size; i++) {
            if (type.isInstance(list[i])) {
                total += list[i].getQuantity();
            }
        }
        return total;
    }

    public double getTotalVatByType(Class<? extends Goods> type) {
        double total = 0;
        for (int i = 0; i < size; i++) {
            if (type.isInstance(list[i])) {
                total += list[i].getVatAmount();
            }
        }
        return total;
    }

    public void displayAll() {
        System.out.println("----- Inventory list (" + size + " items) -----");
        for (int i = 0; i < size; i++) {
            System.out.println(list[i]);
        }
    }

    public int getSize() {
        return size;
    }
}

// Simple interactive menu (Requirement 3: let user choose the type to add)
public class Practice2 {
    public static void main(String[] args) {
        GoodsManager manager = new GoodsManager(100);

        // Seed with sample data
        manager.addGoods(new Food("F001", "Milk 1L", 20, 25000,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1), "Vinamilk"));
        manager.addGoods(new Electronics("E001", "Electric Fan", 2, 850000, 12, 0.06));
        manager.addGoods(new Crockery("C001", "Ceramic Bowl", 60, 15000,
                "ABC Ceramics", LocalDate.of(2026, 5, 1)));

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nSupermarket Inventory Menu");
            System.out.println("1. Add Food");
            System.out.println("2. Add Electronics");
            System.out.println("3. Add Crockery");
            System.out.println("4. Display all goods");
            System.out.println("5. Show inventory totals & VAT by type");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = sc.hasNextLine() ? sc.nextLine().trim() : "0";

            switch (choice) {
                case "1" -> {
                    System.out.print("Code, name, qty, price: ");
                    manager.addGoods(new Food("F00" + (manager.getSize() + 1), "New Food Item", 5, 10000,
                            LocalDate.now().minusDays(30), LocalDate.now().plusDays(30), "Local Supplier"));
                }
                case "2" -> manager.addGoods(new Electronics("E00" + (manager.getSize() + 1),
                        "New Electronics Item", 1, 500000, 6, 1.2));
                case "3" -> manager.addGoods(new Crockery("C00" + (manager.getSize() + 1),
                        "New Crockery Item", 55, 12000, "Some Manufacturer", LocalDate.now().minusDays(15)));
                case "4" -> manager.displayAll();
                case "5" -> {
                    System.out.printf("Food: qty=%d, VAT=%.2f%n",
                            manager.getQuantityByType(Food.class), manager.getTotalVatByType(Food.class));
                    System.out.printf("Electronics: qty=%d, VAT=%.2f%n",
                            manager.getQuantityByType(Electronics.class), manager.getTotalVatByType(Electronics.class));
                    System.out.printf("Crockery: qty=%d, VAT=%.2f%n",
                            manager.getQuantityByType(Crockery.class), manager.getTotalVatByType(Crockery.class));
                }
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }

        sc.close();
        System.out.println("Goodbye!");
    }
}
