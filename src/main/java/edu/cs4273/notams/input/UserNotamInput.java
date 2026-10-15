package edu.cs4273.notams.input;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This program collects flight information from the user and prints it out.
public class UserNotamInput {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            UserNotamInput.class);

    public static void main(String[] var0) {
        Scanner inputScanner = new Scanner(System.in);
        LOGGER.info("Please enter your departure airport: ");
        String startAirport = inputScanner.nextLine();
        LOGGER.info("Please enter your destination airport: ");
        String endAirport = inputScanner.nextLine();
        LOGGER.info("Please enter your departure date(EX: YYYY-MM-DD): ");
        String departureDate = inputScanner.nextLine();
        LOGGER.info("Please enter your departure time(EX: HH:MM): ");
        String departureTime = inputScanner.nextLine();
        LOGGER.info("Is your flight direct or a waypoint: ");
        String flightType = inputScanner.nextLine();
        LOGGER.info(
                startAirport + " " + endAirport + " " + departureDate + " " + departureTime + " " + flightType);
        inputScanner.close();
    }
}
