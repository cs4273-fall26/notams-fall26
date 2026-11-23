package edu.cs4273.notams.input;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import edu.cs4273.notams.api.LocationIdentifier;

public class AirportCodeResolver
{
    public static LocationIdentifier resolve(
            String airportCode, Scanner scanner )
    {
        try {
            LocationIdentifier location =
                    new LocationIdentifier( airportCode );

            if( !airportCode.equals( location.locationValue() ) ) {
                System.out.println(
                        "Airport code normalized: " + airportCode
                        + " -> " + location.locationValue() );
            }

            return location;
        }
        catch( IllegalArgumentException e ) {
            System.out.println( e.getMessage() );

            String normalized = airportCode == null
                    ? ""
                    : airportCode.trim().toUpperCase( Locale.US );

            List<String> suggestions =
                    LocationIdentifier.suggestions( normalized );

            if( suggestions.isEmpty() ) {
                throw e;
            }

            System.out.println( "Please select an airport code:" );

for( int i = 0; i < suggestions.size(); i++ ) {
    System.out.println(
            ( i + 1 ) + ". " + suggestions.get( i ) );
}

System.out.println( "0. Cancel" );

while( true ) {
    System.out.print( "Enter your choice: " );

    if( !scanner.hasNextLine() ) {
        throw e;
    }

    String answer = scanner.nextLine().trim();

    if( answer.equals( "0" ) ) {
        throw e;
    }

    try {
        int choice = Integer.parseInt( answer );

        if( choice >= 1 && choice <= suggestions.size() ) {
            String replacement = suggestions.get( choice - 1 );

            LocationIdentifier location =
                    new LocationIdentifier( replacement );

            System.out.println(
                    "Airport code modified: "
                    + airportCode + " -> "
                    + location.locationValue() );

            return location;
        }
    }
    catch( NumberFormatException ex ) {
        // Invalid input; ask the user again.
    }

    System.out.println(
            "Invalid selection. Please enter a number between 0 and "
                    + suggestions.size() + "." );
        }
        }
    }
}