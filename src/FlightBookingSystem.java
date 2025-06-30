import java.time.LocalDateTime;
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
    }

    // Initialize some sample flights
    private void initializeFlights() {
        flights.add(new Flight("AI101", "Air India", "Delhi", "Mumbai",
                LocalDateTime.of(2024, 7, 15, 8, 30),
                LocalDateTime.of(2024, 7, 15, 10, 45), 5500.0, 180));

        flights.add(new Flight("6E202", "IndiGo", "Mumbai", "Bangalore",
                LocalDateTime.of(2024, 7, 15, 14, 15),
                LocalDateTime.of(2024, 7, 15, 15, 30), 4200.0, 150));

        flights.add(new Flight("SG303", "SpiceJet", "Bangalore", "Chennai",
                LocalDateTime.of(2024, 7, 16, 9, 0),
                LocalDateTime.of(2024, 7, 16, 10, 30), 3800.0, 160));

        flights.add(new Flight("UK404", "Vistara", "Chennai", "Kolkata",
                LocalDateTime.of(2024, 7, 16, 16, 45),
                LocalDateTime.of(2024, 7, 16, 19, 15), 6200.0, 140));
    }

    // Search flights by origin and destination
    public List<Flight> searchFlights(String origin, String destination) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getOrigin().equalsIgnoreCase(origin) &&
                    flight.getDestination().equalsIgnoreCase(destination) &&
                    flight.getAvailableSeats() > 0) {
                result.add(flight);
            }
        }
        return result;
    }

    // Register a new passenger
    public Passenger registerPassenger(String firstName, String lastName, String email, String phoneNumber, int age) {
        String passengerId = "P" + String.format("%04d", passengerCounter++);
        Passenger passenger = new Passenger(passengerId, firstName, lastName, email, phoneNumber, age);
        passengers.add(passenger);
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

        Booking booking = new Booking(bookingId, passenger, selectedFlight, seatNumber);
        bookings.add(booking);

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
        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(bookingId) && booking.getStatus().equals("CONFIRMED")) {
                booking.cancelBooking();
                System.out.println("Booking " + bookingId + " has been cancelled successfully.");
                return true;
            }
        }
        System.out.println("Booking not found or already cancelled.");
        return false;
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

    // Display main menu
    public void displayMenu() {
        System.out.println("\n=== FLIGHT BOOKING SYSTEM MENU ===");
        System.out.println("1. Register New Passenger");
        System.out.println("2. Display All Available Flights");
        System.out.println("3. Search Flights by Route");
        System.out.println("4. Book Flight Ticket");
        System.out.println("5. View Booking Details");
        System.out.println("6. View Passenger Bookings");
        System.out.println("7. Cancel Booking");
        System.out.println("8. Display All Passengers");
        System.out.println("9. Exit");
        System.out.print("Enter your choice (1-9): ");
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

    // Main method with switch-case menu
    public static void main(String[] args) {
        FlightBookingSystem system = new FlightBookingSystem();
        Scanner scanner = new Scanner(System.in);
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

                        List<Flight> searchResults = system.searchFlights(origin, destination);
                        if (searchResults.isEmpty()) {
                            System.out.println("No flights found for the route: " + origin + " → " + destination);
                        } else {
                            System.out.println("\n=== SEARCH RESULTS: " + origin + " → " + destination + " ===");
                            for (Flight flight : searchResults) {
                                System.out.println(flight);
                                System.out.println("---");
                            }
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

                    case 9: // Exit
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