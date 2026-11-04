package edu.cs4273.notams.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

final class ContinentalUsAirports
{
	private static final String RESOURCE = "/continental-us-airports.txt";
	private static final Set<String> IDENTIFIERS = load();

	private ContinentalUsAirports()
	{
	}

	static boolean contains( final String identifier )
	{
		return IDENTIFIERS.contains( identifier );
	}

	private static Set<String> load()
	{
		try (InputStream stream = ContinentalUsAirports.class.getResourceAsStream(
				RESOURCE )) {
			if( stream == null ) {
				throw new IllegalStateException(
						"Missing resource " + RESOURCE );
			}
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader( stream, StandardCharsets.UTF_8 ) );
			return reader.lines().filter( line -> !line.startsWith( "#" ) )
					.collect( Collectors.toUnmodifiableSet() );
		}
		catch( final IOException e ) {
			throw new UncheckedIOException( e );
		}
	}
}
