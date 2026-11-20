package edu.cs4273.notams.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
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

	// Shows the closest matches to the input identifier
	static List<String> suggestions( final String identifier )
	{

		// An identifier that is empty or longer than 5 characters is not a valid code
		if( identifier.isEmpty() || identifier.length() > 5 ) {
			return List.of();
		}

		// Based on the edit distance, this finds the closest matches to the input identifier
		return IDENTIFIERS.stream()
				.map( candidate -> new Suggestion( candidate,
						editDistance( identifier, candidate ),
						commonPrefixLength( identifier, candidate ) ) )
				.filter( suggestion -> suggestion.distance() <= 1
						&& suggestion.identifier().length() >= 3
						&& suggestion.identifier().length() <= 4 )
				.sorted( Comparator.comparingInt( Suggestion::distance )
						.thenComparing( Comparator.comparingInt(
								Suggestion::commonPrefixLength ).reversed() )
						.thenComparing( Suggestion::identifier ) )
				.limit( 3 )
				.map( Suggestion::identifier )
				.toList();
	}


	// Determines the edit distance between two strings
	private static int editDistance( final String first, final String second )
	{
		final int[] previous = new int[second.length() + 1];
		final int[] current = new int[second.length() + 1];

		// Completes the edit distance matrix to find the minimum number of edits required 
		// to convert one string into another
		for( int column = 0; column <= second.length(); column++ ) {
			previous[column] = column;
		}
		for( int row = 1; row <= first.length(); row++ ) {
			current[0] = row;
			for( int column = 1; column <= second.length(); column++ ) {
				final int substitutionCost =
						first.charAt( row - 1 ) == second.charAt( column - 1 )
								? 0 : 1;
				current[column] = Math.min(
						Math.min( current[column - 1 ] + 1,
								previous[column] + 1 ),
						previous[column - 1] + substitutionCost );
			}
			System.arraycopy( current, 0, previous, 0, current.length );
		}

		// The final value from the edit distance matrix is returned
		return previous[second.length()];
	}


	// Finds the length of the common prefix between two strings
	private static int commonPrefixLength(
			final String first, final String second )
	{
		int length = 0;
		while( length < first.length() && length < second.length()
				&& first.charAt( length ) == second.charAt( length ) ) {
			length++;
		}
		return length;
	}

	// Nearby airport suggestion for an invalid airport code
	private record Suggestion(
			String identifier, int distance, int commonPrefixLength )
	{
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
