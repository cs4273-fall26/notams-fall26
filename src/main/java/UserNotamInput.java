package src.main.java;
import java.util.Scanner;

// This program collects flight information from the user and prints it out.
public class UserNotamInput {
    public static void main(String[] var0) {
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Please enter your departure airport: ");
        String startAirport = inputScanner.nextLine();
        System.out.println("Please enter your destination airport: ");
        String endAirport = inputScanner.nextLine();
        System.out.println("Please enter your departure date(EX: YYYY-MM-DD): ");
        String departureDate = inputScanner.nextLine();
        System.out.println("Please enter your departure time(EX: HH:MM): ");
        String departureTime = inputScanner.nextLine();
        System.out.println("Is your flight direct or a waypoint: ");
        String flightType = inputScanner.nextLine();
        System.out.println(
                startAirport + " " + endAirport + " " + departureDate + " " + departureTime + " " + flightType);
        inputScanner.close();
    }
}
