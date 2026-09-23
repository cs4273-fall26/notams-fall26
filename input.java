import java.util.Scanner;

// This program collects flight information from the user and prints it out.
public class input {
    public static void main(String[] var0){
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Please enter your departure airport: ");
        String startAirport = inputScanner.nextLine();
        System.out.println("Please enter your destination airport: ");
        String endAirport = inputScanner.nextLine();
        System.out.println("Please enter your departure date: ");
        String departureDate = inputScanner.nextLine(); 
        System.out.println("Please enter your departure time: ");
        String departureTime = inputScanner.nextLine();
        System.out.println("Is your fight direct or a waypoint: ");
        String flightType = inputScanner.nextLine();
        System.out.println(startAirport + " " + endAirport + " " +  departureDate + " " + departureTime + " " + flightType);  
        inputScanner.close();
    }
}
