// Passenger class to store passenger information
class Passenger {
    private final String passengerId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final int age;

    public Passenger(String passengerId, String firstName, String lastName, String email, String phoneNumber, int age) {
        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.age = age;
    }

    // Getters
    public String getPassengerId() { return passengerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public int getAge() { return age; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return String.format("Passenger: %s (ID: %s, Age: %d)", getFullName(), passengerId, age);
    }
}