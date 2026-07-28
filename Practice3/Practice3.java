import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// Owner 
class Owner {
    private String cmndNumber;
    private String fullName;
    private String email;

    private static final Pattern CMND_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public Owner(String cmndNumber, String fullName, String email) {
        if (!CMND_PATTERN.matcher(cmndNumber).matches()) {
            throw new IllegalArgumentException("ID number must be exactly 12 digits.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.cmndNumber = cmndNumber;
        this.fullName = fullName;
        this.email = email;
    }

    public String getCmndNumber() {
        return cmndNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("Owner[cmnd=%s, name=%s, email=%s]", cmndNumber, fullName, email);
    }
}

// Abstract Vehicle
abstract class Vehicle {
    protected String vehicleNumber;
    protected String manufacturer;
    protected int year;
    protected String color;
    protected Owner owner;

    private static final List<String> ALLOWED_MANUFACTURERS =
            List.of("Honda", "Yamaha", "Toyota", "Suzuki");

    public Vehicle(String vehicleNumber, String manufacturer, int year, String color, Owner owner) {
        if (vehicleNumber == null || vehicleNumber.length() != 5) {
            throw new IllegalArgumentException("Vehicle number must have exactly 5 characters.");
        }
        if (!ALLOWED_MANUFACTURERS.contains(manufacturer)) {
            throw new IllegalArgumentException(
                    "Manufacturer must be one of: " + ALLOWED_MANUFACTURERS);
        }
        int currentYear = java.time.Year.now().getValue();
        if (year <= 2000 || year > currentYear) {
            throw new IllegalArgumentException(
                    "Year of manufacture must be > 2000 and <= " + currentYear);
        }
        this.vehicleNumber = vehicleNumber;
        this.manufacturer = manufacturer;
        this.year = year;
        this.color = color;
        this.owner = owner;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getYear() {
        return year;
    }

    public String getColor() {
        return color;
    }

    public Owner getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("%s[number=%s, manufacturer=%s, year=%d, color=%s, owner=%s]",
                this.getClass().getSimpleName(), vehicleNumber, manufacturer, year, color,
                owner.getFullName());
    }
}

// Car
class Car extends Vehicle {
    private int seats;
    private String engineType;

    public Car(String vehicleNumber, String manufacturer, int year, String color, Owner owner,
               int seats, String engineType) {
        super(vehicleNumber, manufacturer, year, color, owner);
        this.seats = seats;
        this.engineType = engineType;
    }

    public int getSeats() {
        return seats;
    }

    public String getEngineType() {
        return engineType;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [seats=%d, engine=%s]", seats, engineType);
    }
}

// Motorbike
class Motorbike extends Vehicle {
    private double capacity; // engine displacement, cc

    public Motorbike(String vehicleNumber, String manufacturer, int year, String color, Owner owner,
                      double capacity) {
        super(vehicleNumber, manufacturer, year, color, owner);
        this.capacity = capacity;
    }

    public double getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [capacity=%.1fcc]", capacity);
    }
}

// Truck
class Truck extends Vehicle {
    private double tonnage;

    public Truck(String vehicleNumber, String manufacturer, int year, String color, Owner owner,
                 double tonnage) {
        super(vehicleNumber, manufacturer, year, color, owner);
        this.tonnage = tonnage;
    }

    public double getTonnage() {
        return tonnage;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [tonnage=%.1ft]", tonnage);
    }
}

// Manager
class VehicleManager {
    private List<Vehicle> vehicles = new ArrayList<>();

    private boolean vehicleNumberExists(String number) {
        return vehicles.stream().anyMatch(v -> v.getVehicleNumber().equalsIgnoreCase(number));
    }

    // 1. Add a vehicle
    public boolean addVehicle(Vehicle v) {
        if (vehicleNumberExists(v.getVehicleNumber())) {
            System.out.println("Add failed: vehicle number '" + v.getVehicleNumber() + "' already exists.");
            return false;
        }
        vehicles.add(v);
        System.out.println("Added: " + v);
        return true;
    }

    // 2. Search transport by vehicle number
    public Vehicle searchByVehicleNumber(String number) {
        return vehicles.stream()
                .filter(v -> v.getVehicleNumber().equalsIgnoreCase(number))
                .findFirst()
                .orElse(null);
    }

    // 3. Find vehicles owned by a person with the given cmnd number
    public List<Vehicle> findByOwnerCmnd(String cmndNumber) {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.getOwner().getCmndNumber().equals(cmndNumber)) {
                result.add(v);
            }
        }
        return result;
    }

    // 4. Delete all vehicles of a given manufacturer
    public int deleteByManufacturer(String manufacturer) {
        int before = vehicles.size();
        vehicles.removeIf(v -> v.getManufacturer().equalsIgnoreCase(manufacturer));
        return before - vehicles.size();
    }

    // 5. Which manufacturer has the most vehicles
    public String manufacturerWithMostVehicles() {
        Map<String, Long> counts = countByManufacturer();
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Map<String, Long> countByManufacturer() {
        Map<String, Long> counts = new HashMap<>();
        for (Vehicle v : vehicles) {
            counts.merge(v.getManufacturer(), 1L, Long::sum);
        }
        return counts;
    }

    // 6. Sort manufacturers by number of vehicles, descending
    public List<Map.Entry<String, Long>> sortManufacturersByCountDesc() {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(countByManufacturer().entrySet());
        entries.sort(Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed());
        return entries;
    }

    // 7. Statistics: how many vehicles per type are managed
    public Map<String, Long> countByType() {
        Map<String, Long> counts = new HashMap<>();
        for (Vehicle v : vehicles) {
            counts.merge(v.getClass().getSimpleName(), 1L, Long::sum);
        }
        return counts;
    }

    public void displayAll() {
        System.out.println("\nAll managed vehicles (" + vehicles.size() + ")");
        System.out.printf("%-10s %-10s %-12s %-6s %-12s %-20s %s%n",
                "Type", "Number", "Manufacturer", "Year", "Color", "Owner", "Details");
        System.out.println("-".repeat(120));

        for (Vehicle vehicle : vehicles) {
            String details;
            if (vehicle instanceof Car car) {
                details = String.format("seats=%d, engine=%s", car.getSeats(), car.getEngineType());
            } else if (vehicle instanceof Motorbike motorbike) {
                details = String.format("capacity=%.1fcc", motorbike.getCapacity());
            } else if (vehicle instanceof Truck truck) {
                details = String.format("tonnage=%.1ft", truck.getTonnage());
            } else {
                details = "";
            }

            System.out.printf("%-10s %-10s %-12s %-6d %-12s %-20s %s%n",
                    vehicle.getClass().getSimpleName(),
                    vehicle.getVehicleNumber(),
                    vehicle.getManufacturer(),
                    vehicle.getYear(),
                    vehicle.getColor(),
                    vehicle.getOwner().getFullName(),
                    details);
        }
    }
}

// Demo
public class Practice3 {
    public static void main(String[] args) {
        VehicleManager manager = new VehicleManager();

        Owner owner1 = new Owner("123456789012", "Dao Minh Thien", "minhthien@example.com");
        Owner owner2 = new Owner("123456789013", "Phan Nam Thanh", "namthanh@example.com");
        Owner owner3 = new Owner("123456789014", "Tran Thanh Vinh", "thanvinh@example.com");

        manager.addVehicle(new Car("51A12", "Toyota", 2020, "White", owner1, 5, "Gasoline"));
        manager.addVehicle(new Motorbike("59B34", "Honda", 2019, "Black", owner2, 125.0));
        manager.addVehicle(new Truck("60C56", "Honda", 2015, "Blue", owner3, 3.5));
        manager.addVehicle(new Car("61D78", "Honda", 2022, "Red", owner1, 4, "Diesel"));
        manager.addVehicle(new Motorbike("62E90", "Yamaha", 2021, "Silver", owner2, 150.0));

        manager.displayAll();

        System.out.println("\nSearch by vehicle number '59B34'");
        System.out.println(manager.searchByVehicleNumber("59B34"));

        System.out.println("\nVehicles owned by cmnd 123456789012");
        manager.findByOwnerCmnd("123456789012").forEach(System.out::println);

        System.out.println("\nManufacturer with most vehicles");
        System.out.println(manager.manufacturerWithMostVehicles());

        System.out.println("\nManufacturers sorted by vehicle count (desc)");
        manager.sortManufacturersByCountDesc()
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

        System.out.println("\nVehicle count by type");
        manager.countByType().forEach((type, count) -> System.out.println(type + ": " + count));

        System.out.println("\nDelete all Honda vehicles");
        int deleted = manager.deleteByManufacturer("Honda");
        System.out.println("Deleted " + deleted + " Honda vehicle(s).");
        manager.displayAll();
    }
}
