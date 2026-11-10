package edu.cs4273.notams.api;

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
					"The location can't be null or blank." );
		}

		final String normalized = StringUtils.trimToEmpty( locationValue )
				.toUpperCase( Locale.US );

		if( !normalized.matches( "[A-Z0-9]{3,5}" ) ) {
			throw new IllegalArgumentException(
					"Location must be a 3-5 character FAA or ICAO identifier, such as KOKC." );
		}

		if( !ContinentalUsAirports.contains( normalized ) ) {
			throw new IllegalArgumentException( "Location " + normalized
					+ " is not a known airport in the continental US." );
		}

		this.locationValue = normalized;
	}
}