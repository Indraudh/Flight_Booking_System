import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class FlightBookingSystem {
    private final List<Flight> flights;
    private final List<Booking> bookings;
    private final List<Passenger> passengers;
    private int bookingCounter;
    private int passengerCounter;

    public FlightBookingSystem() {
        this.flights = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.passengers = new ArrayList<>();
        this.bookingCounter = 1;
        this.passengerCounter = 1;
        initializeFlights();
        loadPassengers();
        loadBookings();
    }

    // Initialize some sample flights
    private void initializeFlights() {
        try (Connection conn = DatabaseHelper.getConnection();
             var stmt = ((Connection) conn).createStatement();
             var rs = stmt.executeQuery("SELECT * FROM flights")) {

            while (rs.next()) {
                flights.add(new Flight(
                        rs.getString("flightNumber"),
                        rs.getString("airline"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getTimestamp("departureTime").toLocalDateTime(),
                        rs.getTimestamp("arrivalTime").toLocalDateTime(),
                        rs.getDouble("price"),
                        rs.getInt("totalSeats")
                ));
            }
            // System.out.println("✅ Flights loaded from database successfully.");
        } catch (Exception e) {
            System.out.println("❌ Error loading flights: " + e.getMessage());
        }
    }

    // Search flights by origin and destination and date
    public List<Flight> searchFlights(String origin, String destination, LocalDate date) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (
                    flight.getOrigin().equalsIgnoreCase(origin) &&
                            flight.getDestination().equalsIgnoreCase(destination) &&
                            flight.getDepartureTime().toLocalDate().equals(date) &&
                            flight.getAvailableSeats() > 0
            ) {
                result.add(flight);
            }
        }
        return result;
    }


    // Register a new passenger
    public Passenger registerPassenger(String firstName, String lastName, String email, String phoneNumber, int age) {
        if (!phoneNumber.matches("\\d{10}")) {
            System.out.println("❌ Invalid phone number! Must be 10 digits.");
            return null;
        }
        if (!email.contains("@")) {
            System.out.println("❌ Invalid email address! Must contain '@'.");
            return null;
        }
        if (age <= 0) {
            System.out.println("❌ Invalid age! Must be greater than 0.");
            return null;
        }

        String passengerId = "P" + String.format("%04d", passengerCounter++);
        Passenger passenger = new Passenger(passengerId, firstName, lastName, email, phoneNumber, age);

        try (Connection conn = DatabaseHelper.getConnection();
             var pstmt = conn.prepareStatement(
                     "INSERT INTO passengers (passengerId, firstName, lastName, email, phoneNumber, age) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, passengerId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, email);
            pstmt.setString(5, phoneNumber);
            pstmt.setInt(6, age);

            pstmt.executeUpdate();
            System.out.println("✅ Passenger stored in database.");
        } catch (Exception e) {
            System.out.println("❌ Error storing passenger: " + e.getMessage());
        }

        passengers.add(passenger); // still keep in memory to display in the menu
        return passenger;
    }


    // Book a flight ticket
    public Booking bookTicket(Passenger passenger, String flightNumber) {
        Flight selectedFlight = null;

        // Find the flight
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equals(flightNumber)) {
                selectedFlight = flight;
                break;
            }
        }

        if (selectedFlight == null) {
            System.out.println("Flight not found!");
            return null;
        }

        if (!selectedFlight.bookSeat()) {
            System.out.println("No seats available on this flight!");
            return null;
        }

        // Generate booking ID and seat number
        String bookingId = "BK" + String.format("%06d", bookingCounter++);
        String seatNumber = generateSeatNumber(selectedFlight);
        LocalDateTime bookingTime = LocalDateTime.now();

        Booking booking = new Booking(bookingId, passenger, selectedFlight, seatNumber);
        bookings.add(booking);

        // Insert booking into DB
        try (Connection conn = DatabaseHelper.getConnection();
             var pstmt = conn.prepareStatement(
                     "INSERT INTO bookings (bookingId, passengerId, flightNumber, seatNumber, bookingTime, status) VALUES (?, ?, ?, ?, ?, ?)")) {

            pstmt.setString(1, bookingId);
            pstmt.setString(2, passenger.getPassengerId());
            pstmt.setString(3, flightNumber);
            pstmt.setString(4, seatNumber);
            pstmt.setTimestamp(5, Timestamp.valueOf(bookingTime));
            pstmt.setString(6, "CONFIRMED");

            pstmt.executeUpdate();
            System.out.println("✅ Booking saved to database.");
        } catch (Exception e) {
            System.out.println("❌ Error saving booking: " + e.getMessage());
        }

        return booking;
    }


    // Generate seat number
    private String generateSeatNumber(Flight flight) {
        int seatNum = flight.getTotalSeats() - flight.getAvailableSeats();
        char row = (char) ('A' + (seatNum - 1) / 6);
        int col = ((seatNum - 1) % 6) + 1;
        return row + String.valueOf(col);
    }

    // Cancel booking
    public boolean cancelBooking(String bookingId) {
        boolean success = false;

        try (Connection conn = DatabaseHelper.getConnection();
             var pstmt = conn.prepareStatement(
                     "UPDATE bookings SET status = ? WHERE bookingId = ? AND status = ?")) {
            pstmt.setString(1, "CANCELLED");
            pstmt.setString(2, bookingId);
            pstmt.setString(3, "CONFIRMED");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Booking " + bookingId + " cancelled and updated in DB.");
                success = true;

                // also update the seats:

                // get flight number
                for (Booking b : bookings) {
                    if (b.getBookingId().equals(bookingId)) {
                        b.cancelBooking(); // only if present in memory
                    }
                }
                try (Connection conn1 = DatabaseHelper.getConnection();
                     var pstmt1 = conn1.prepareStatement("SELECT flightNumber FROM bookings WHERE bookingId = ?")) {

                    pstmt1.setString(1, bookingId);

                    try (var rs = pstmt1.executeQuery()) {
                        if (rs.next()) {
                            String flightNumber = rs.getString("flightNumber");

                            // find the flight in memory
                            for (Flight f : flights) {
                                if (f.getFlightNumber().equals(flightNumber)) {
                                    f.cancelSeat();  // increase availableSeats
                                    break;
                                }
                            }
                        }
                    }


                } catch (Exception e) {
                    System.out.println("❌ Error updating available seats after cancel: " + e.getMessage());
                }

            } else {
                System.out.println("⚠️ Booking not found or already cancelled in DB.");
            }

        } catch (Exception e) {
            System.out.println("❌ Error cancelling booking in DB: " + e.getMessage());
        }

        return success;
    }



    // Get booking details
    public Booking getBookingDetails(String bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(bookingId)) {
                return booking;
            }
        }
        return null;
    }

    // Display all available flights
    public void displayAllFlights() {
        System.out.println("\n=== AVAILABLE FLIGHTS ===");
        for (Flight flight : flights) {
            if (flight.getAvailableSeats() > 0) {
                System.out.println(flight);
                System.out.println("---");
            }
        }
    }

    // Display passenger's bookings
    public void displayPassengerBookings(String passengerId) {
        System.out.println("\n=== BOOKINGS FOR PASSENGER " + passengerId + " ===");
        boolean found = false;
        for (Booking booking : bookings) {
            if (booking.getPassenger().getPassengerId().equals(passengerId)) {
                System.out.println(booking);
                System.out.println();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No bookings found for this passenger.");
        }
    }

    // load passengers
    private void loadPassengers() {
        try (Connection conn = DatabaseHelper.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM passengers")) {

            while (rs.next()) {
                String passengerId = rs.getString("passengerId");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phoneNumber");
                int age = rs.getInt("age");

                Passenger passenger = new Passenger(passengerId, firstName, lastName, email, phoneNumber, age);
                passengers.add(passenger);
            }

            // System.out.println("✅ Loaded passengers from database: " + passengers.size());
        } catch (Exception e) {
            System.out.println("❌ Error loading passengers: " + e.getMessage());
        }

        if (!passengers.isEmpty()) {
            String lastPid = passengers.get(passengers.size() - 1).getPassengerId(); // e.g., P0004
            int num = Integer.parseInt(lastPid.substring(1));
            passengerCounter = num + 1;
        }

    }

    // Load Booking from DB
    private void loadBookings() {
        try (Connection conn = DatabaseHelper.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM bookings")) {

            while (rs.next()) {
                String bookingId = rs.getString("bookingId");
                String passengerId = rs.getString("passengerId");
                String flightNumber = rs.getString("flightNumber");
                String seatNumber = rs.getString("seatNumber");
                LocalDateTime bookingTime = rs.getTimestamp("bookingTime").toLocalDateTime();
                String status = rs.getString("status");

                // Find passenger from list
                Passenger passenger = findPassenger(passengerId);

                // Find flight from list
                Flight flight = null;
                for (Flight f : flights) {
                    if (f.getFlightNumber().equals(flightNumber)) {
                        flight = f;
                        break;
                    }
                }

                if (passenger != null && flight != null) {
                    Booking booking = new Booking(bookingId, passenger, flight, seatNumber);

                    // manually set status from DB
                    if (status.equalsIgnoreCase("CANCELLED")) {
                        booking.cancelBooking();
                    }

                    bookings.add(booking);
                }
            }

            // System.out.println("✅ Loaded bookings from database: " + bookings.size());
        } catch (Exception e) {
            System.out.println("❌ Error loading bookings: " + e.getMessage());
        }

        if (!bookings.isEmpty()) {
            // set bookingCounter to max ID + 1
            String lastId = bookings.get(bookings.size() - 1).getBookingId(); // e.g., BK000123
            int numericPart = Integer.parseInt(lastId.substring(2));  // get 123
            bookingCounter = numericPart + 1;
        }
    }



    // Display main menu
    public void displayMenu() {

        System.out.println("\n=== FLIGHT BOOKING SYSTEM MENU ===");
        System.out.println("1. Register New Passenger");
        System.out.println("2. Display All Available Flights");
        System.out.println("3. Search Flights by Route and Date");
        System.out.println("4. Book Flight Ticket");
        System.out.println("5. View Booking Details");
        System.out.println("6. View Passenger Bookings");
        System.out.println("7. Cancel Booking");
        System.out.println("8. Display All Passengers");
        System.out.println("9. Export Passenger Bookings to CSV");
        System.out.println("10. Exit");
        System.out.print("Enter your choice (1-10): ");
    }

    // Find passenger by ID
    public Passenger findPassenger(String passengerId) {
        for (Passenger passenger : passengers) {
            if (passenger.getPassengerId().equals(passengerId)) {
                return passenger;
            }
        }
        return null;
    }

    // Display all registered passengers
    public void displayAllPassengers() {
        if (passengers.isEmpty()) {
            System.out.println("No passengers registered yet.");
            return;
        }

        System.out.println("\n=== REGISTERED PASSENGERS ===");
        for (Passenger passenger : passengers) {
            System.out.println(passenger);
        }
    }

    // Export Bookings

    public void exportPassengerBookings(String passengerId) {
        Passenger passenger = findPassenger(passengerId);

        if (passenger == null) {
            System.out.println("❌ Passenger not found.");
            return;
        }

        String fileName = passenger.getPassengerId() + "_bookings.csv";

        try (PrintWriter pw = new PrintWriter(fileName)) {
            pw.println("BookingId,PassengerName,FlightNumber,Origin,Destination,Departure,SeatNumber,Status");

            for (Booking booking : bookings) {
                if (booking.getPassenger().getPassengerId().equals(passengerId)) {
                    pw.printf(
                            "%s,%s,%s,%s,%s,%s,%s,%s%n",
                            booking.getBookingId(),
                            booking.getPassenger().getFullName(),
                            booking.getFlight().getFlightNumber(),
                            booking.getFlight().getOrigin(),
                            booking.getFlight().getDestination(),
                            booking.getFlight().getDepartureTime(),
                            booking.getSeatNumber(),
                            booking.getStatus()
                    );
                }
            }
            System.out.println("✅ Bookings exported to file: " + fileName);
        } catch (Exception e) {
            System.out.println("❌ Error exporting CSV: " + e.getMessage());
        }
    }


    // Main method with switch-case menu
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== ADMIN LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (!username.equals("admin") || !password.equals("admin123")) {
            System.out.println("❌ Invalid admin credentials! Exiting...");
            return;
        }
        System.out.println("✅ Admin login successful!");


        FlightBookingSystem system = new FlightBookingSystem();
        boolean running = true;

        System.out.println("=== WELCOME TO FLIGHT BOOKING SYSTEM ===");

        while (running) {
            system.displayMenu();

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1: // Register New Passenger
                        System.out.println("\n=== PASSENGER REGISTRATION ===");
                        System.out.print("First Name: ");
                        String firstName = scanner.nextLine();
                        System.out.print("Last Name: ");
                        String lastName = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Phone Number: ");
                        String phone = scanner.nextLine();
                        System.out.print("Age: ");
                        int age = scanner.nextInt();
                        scanner.nextLine(); // consume newline

                        Passenger newPassenger = system.registerPassenger(firstName, lastName, email, phone, age);
                        System.out.println("\nPassenger registered successfully!");
                        System.out.println(newPassenger);
                        break;

                    case 2: // Display All Available Flights
                        system.displayAllFlights();
                        break;

                    case 3: // Search Flights by Route
                        System.out.println("\n=== FLIGHT SEARCH ===");
                        System.out.print("Enter origin city: ");
                        String origin = scanner.nextLine();
                        System.out.print("Enter destination city: ");
                        String destination = scanner.nextLine();
                        System.out.print("Enter departure date (yyyy-MM-dd): ");
                        String dateStr = scanner.nextLine();

                        try {
                            LocalDate date = LocalDate.parse(dateStr);
                            List<Flight> searchResults = system.searchFlights(origin, destination, date);

                            if (searchResults.isEmpty()) {
                                System.out.println("No flights found for the route: " + origin + " → " + destination + " on " + date);
                            } else {
                                System.out.println("\n=== SEARCH RESULTS: " + origin + " → " + destination + " on " + date + " ===");
                                for (Flight flight : searchResults) {
                                    System.out.println(flight);
                                    System.out.println("---");
                                }
                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("❌ Invalid date format! Please use yyyy-MM-dd.");
                        }
                        break;

                    case 4: // Book Flight Ticket
                        System.out.println("\n=== FLIGHT BOOKING ===");
                        if (system.passengers.isEmpty()) {
                            System.out.println("Please register a passenger first (Option 1).");
                            break;
                        }

                        System.out.print("Enter Passenger ID: ");
                        String passengerId = scanner.nextLine();
                        Passenger passenger = system.findPassenger(passengerId);

                        if (passenger == null) {
                            System.out.println("Passenger not found. Please check the Passenger ID.");
                            break;
                        }

                        System.out.print("Enter Flight Number: ");
                        String flightNumber = scanner.nextLine();

                        Booking booking = system.bookTicket(passenger, flightNumber);
                        if (booking != null) {
                            System.out.println("\nBooking successful!");
                            System.out.println(booking);
                        }
                        break;

                    case 5: // View Booking Details
                        System.out.println("\n=== BOOKING DETAILS ===");
                        System.out.print("Enter Booking ID: ");
                        String bookingId = scanner.nextLine();

                        Booking bookingDetails = system.getBookingDetails(bookingId);
                        if (bookingDetails != null) {
                            System.out.println(bookingDetails);
                        } else {
                            System.out.println("Booking not found with ID: " + bookingId);
                        }
                        break;

                    case 6: // View Passenger Bookings
                        System.out.println("\n=== PASSENGER BOOKINGS ===");
                        System.out.print("Enter Passenger ID: ");
                        String passengerIdForBookings = scanner.nextLine();

                        if (system.findPassenger(passengerIdForBookings) != null) {
                            system.displayPassengerBookings(passengerIdForBookings);
                        } else {
                            System.out.println("Passenger not found with ID: " + passengerIdForBookings);
                        }
                        break;

                    case 7: // Cancel Booking
                        System.out.println("\n=== CANCEL BOOKING ===");
                        System.out.print("Enter Booking ID to cancel: ");
                        String cancelBookingId = scanner.nextLine();

                        System.out.print("Are you sure you want to cancel booking " + cancelBookingId + "? (y/n): ");
                        String confirmation = scanner.nextLine();

                        if (confirmation.equalsIgnoreCase("y") || confirmation.equalsIgnoreCase("yes")) {
                            system.cancelBooking(cancelBookingId);
                        } else {
                            System.out.println("Booking cancellation aborted.");
                        }
                        break;

                    case 8: // Display All Passengers
                        system.displayAllPassengers();
                        break;


                    case 9: // export to csv
                        System.out.println("\n=== EXPORT PASSENGER BOOKINGS ===");
                        System.out.print("Enter Passenger ID: ");
                        String pid = scanner.nextLine();
                        system.exportPassengerBookings(pid);
                        break;


                    case 10: // Exit
                        System.out.println("\nThank you for using Flight Booking System!");
                        System.out.println("Have a safe journey! ✈️");
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice! Please select a number between 1-9.");
                        break;
                }

                // Pause before showing menu again (except for exit)
                if (running) {
                    System.out.print("\nPress Enter to continue...");
                    scanner.nextLine();
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a valid number.");
                scanner.nextLine(); // clear invalid input
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // clear any remaining input
            }
        }
        scanner.close();
    }
}