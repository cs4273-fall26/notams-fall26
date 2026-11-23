package edu.cs4273.notams.api;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * Validated input shared by the input and API layers. Only airports in the 48
 * contiguous states and DC are accepted.
 */
public record LocationIdentifier(String locationValue)
{
	public LocationIdentifier( final String locationValue )
	{
		if( StringUtils.isBlank( locationValue ) ) {
			throw new IllegalArgumentException(
					"The airport code can't be null or blank." );
		}

		final String normalized = StringUtils.trimToEmpty( locationValue )
				.toUpperCase( Locale.US );

		if( !normalized.matches( "[A-Z0-9]{3,4}" ) ) {
			throw invalidCodeMessage(
					"The airport code is invalid. Enter a 3-4 character airport "
							+ "identifier.",
					normalized );
		}

		if( !ContinentalUsAirports.contains( normalized ) ) {
			throw invalidCodeMessage( "Airport code " + normalized
					+ " was not found in the supported airport list for the "
					+ "contiguous US.", normalized );
		}

		this.locationValue = normalized;
	}

	public String returnLocationValue()
	{
		return locationValue;
	}

	private static IllegalArgumentException invalidCodeMessage(
			final String message, final String normalized )
	{
		final List<String> suggestions =
				ContinentalUsAirports.suggestions( normalized );
		if( suggestions.isEmpty() ) {
			return new IllegalArgumentException( message );
		}
		return new IllegalArgumentException( message + " Possible matches: "
				+ String.join( ", ", suggestions ) + "." );
	}

	public static List<String> suggestions( final String airportCode )
	{
		final String normalized = airportCode == null
				? ""
				: airportCode.trim().toUpperCase( Locale.US );

		return ContinentalUsAirports.suggestions( normalized );
	}


}
