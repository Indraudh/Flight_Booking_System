import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
class Booking {
    private final String bookingId;
    private final Passenger passenger;
    private final Flight flight;
    private final LocalDateTime bookingTime;
    private String status; // CONFIRMED, CANCELLED
    private final String seatNumber;

    public Booking(String bookingId, Passenger passenger, Flight flight, String seatNumber) {
        this.bookingId = bookingId;
        this.passenger = passenger;
        this.flight = flight;
        this.seatNumber = seatNumber;
        this.bookingTime = LocalDateTime.now();
        this.status = "CONFIRMED";
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public Passenger getPassenger() { return passenger; }
    public Flight getFlight() { return flight; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public String getStatus() { return status; }
    public String getSeatNumber() { return seatNumber; }

    public void cancelBooking() {
        this.status = "CANCELLED";
        flight.cancelSeat();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("=== BOOKING CONFIRMATION ===\n" +
                        "Booking ID: %s\n" +
                        "Status: %s\n" +
                        "Passenger: %s\n" +
                        "Flight: %s (%s)\n" +
                        "Route: %s → %s\n" +
                        "Departure: %s\n" +
                        "Seat: %s\n" +
                        "Total Amount: $%.2f\n" +
                        "Booking Time: %s\n" +
                        "============================",
                bookingId, status, passenger.getFullName(),
                flight.getFlightNumber(), flight.getAirline(),
                flight.getOrigin(), flight.getDestination(),
                flight.getDepartureTime().format(formatter),
                seatNumber, flight.getPrice(),
                bookingTime.format(formatter));
    }
}

