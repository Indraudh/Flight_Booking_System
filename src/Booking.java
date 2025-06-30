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
        String border = "=============================================";
        return String.format(
                "%s\n" +
                        "|              ✈️  FLIGHT TICKET               |\n" +
                        "%s\n" +
                        "| Booking ID : %-30s |\n" +
                        "| Passenger  : %-30s |\n" +
                        "| Status     : %-30s |\n" +
                        "%s\n" +
                        "| Flight     : %-30s |\n" +
                        "| Airline    : %-30s |\n" +
                        "| Route      : %-14s → %-13s |\n" +
                        "| Departure  : %-30s |\n" +
                        "| Seat       : %-30s |\n" +
                        "%s\n" +
                        "| Amount Paid: $%-29.2f |\n" +
                        "| Booked On  : %-30s |\n" +
                        "%s",
                border,
                border,
                bookingId,
                passenger.getFullName(),
                status,
                border,
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getOrigin(), flight.getDestination(),
                flight.getDepartureTime().format(formatter),
                seatNumber,
                border,
                flight.getPrice(),
                bookingTime.format(formatter),
                border
        );
    }

}

