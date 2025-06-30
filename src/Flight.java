// Flight class to represent flight information
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
class Flight {
    private final String flightNumber;
    private final String airline;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final double price;
    private final int totalSeats;
    private int availableSeats;

    public Flight(String flightNumber, String airline, String origin, String destination,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, double price, int totalSeats) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    // Getters
    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public double getPrice() { return price; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }

    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    public void cancelSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format(
                """
                ╔════════════════════════════════════════════════════════════════════╗
                ║                        ✈️  FLIGHT INFORMATION                      ║
                ╠════════════════════════════════════════════════════════════════════╣
                ║ Flight      : %-10s (%s)                                           ║
                ║ Route       : %s → %s                                              ║
                ║ Departure   : %s                                                   ║
                ║ Arrival     : %s                                                   ║
                ║ Price       : $%.2f                                                ║
                ║ Seats       : %d available out of %d                               ║
                ╚════════════════════════════════════════════════════════════════════╝
                """,
                flightNumber, airline,
                origin, destination,
                departureTime.format(formatter),
                arrivalTime.format(formatter),
                price,
                availableSeats, totalSeats
        );
    }

}
