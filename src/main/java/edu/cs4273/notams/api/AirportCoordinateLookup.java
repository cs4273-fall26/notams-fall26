package edu.cs4273.notams.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AirportCoordinateLookup
{
	public record Coordinates( double latitude, double longitude )
	{
	}

	private final Map<String, Coordinates> coordinates = new HashMap<>();
	private final Map<String, String> owners = new HashMap<>();
	private final Set<String> ambiguousCodes = new HashSet<>();

	public AirportCoordinateLookup() throws IOException
	{
		try( InputStream input = AirportCoordinateLookup.class
				.getResourceAsStream( "/airport-coordinates.json" ) ) {
			if( input == null ) {
				throw new IOException(
						"Missing airport-coordinates.json resource." );
			}

			final JsonNode airports = new ObjectMapper().readTree( input );
			if( airports == null || !airports.isArray() || airports.isEmpty() ) {
				throw new IOException(
						"Airport coordinate resource must contain an array of airports." );
			}

			for( final JsonNode airport : airports ) {
				final String ident = airport.path( "ident" ).asText( "" ).trim();
				final double latitude = airport.path( "latitude_deg" )
						.asDouble( Double.NaN );
				final double longitude = airport.path( "longitude_deg" )
						.asDouble( Double.NaN );

				if( ident.isEmpty() || !Double.isFinite( latitude )
						|| latitude < -90 || latitude > 90
						|| !Double.isFinite( longitude )
						|| longitude < -180 || longitude > 180 ) {
					throw new IOException(
							"Invalid airport coordinate record: " + ident );
				}

				final Coordinates point = new Coordinates(
						latitude, longitude );

				for( final String field : new String[] {
						"ident", "icao_code", "gps_code", "local_code" } ) {
					final String code = airport.path( field ).asText( "" )
							.trim().toUpperCase( Locale.ROOT );

					if( code.isEmpty() ) {
						continue;
					}

					final String previousOwner = owners.putIfAbsent(
							code, ident );
					if( previousOwner != null
							&& !previousOwner.equals( ident ) ) {
						ambiguousCodes.add( code );
					}
					else {
						coordinates.putIfAbsent( code, point );
					}
				}
			}
		}
	}

	public Coordinates lookup( final LocationIdentifier airport )
	{
		final String code = airport.locationValue();

		if( ambiguousCodes.contains( code ) ) {
			throw new IllegalArgumentException(
					"Airport code is ambiguous in the coordinate dataset: "
							+ code );
		}

		final Coordinates result = coordinates.get( code );
		if( result == null ) {
			throw new IllegalArgumentException(
					"No coordinates found in the US airport dataset for: "
							+ code );
		}

		return result;
	}
}