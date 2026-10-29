package edu.cs4273.notams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.NmsConfiguration;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.input.CliArgParser;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.output.NotamOutput;
import edu.cs4273.notams.parsing.RawNotamResponseParser;
import edu.cs4273.notams.prioritization.NotamPrioritizer;
import edu.cs4273.notams.prioritization.TextLengthNotamPrioritizer;

public class EndToEndRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			EndToEndRunner.class );

	public static void main( String[] args )
	{
		// parse departure/destination airport code input
		final Pair<String, String> input = CliArgParser.parse( args );
		LOGGER.info( "Departure code: {}", input.getLeft() );
		LOGGER.info( "Destination code: {}", input.getRight() );

		// call the API
		final NmsApiAccess apiClient = NmsConfiguration.fromEnvironment();
		final List<Notam> prioritizedList = process( input, apiClient );

		// output prioritized notams
		final NotamOutput output = new NotamOutput();
		output.render( prioritizedList );
	}

	static List<Notam> process( final Pair<String, String> input,
			final NmsApiAccess apiClient )
	{
		final LocationIdentifier departure = new LocationIdentifier(
				input.getLeft() );
		final LocationIdentifier destination = new LocationIdentifier(
				input.getRight() );

		List<RawNotamResponse> rawResponseList;
		try {
			rawResponseList = apiClient.fetchNotamsForRoute(
					departure, destination );
		}
		catch( final NmsApiException e ) {
			throw new RuntimeException( e );
		}
		// parse raw API responses into NOTAM objects
		final RawNotamResponseParser parser = new RawNotamResponseParser();
		final List<Notam> notams = new ArrayList<>();
		for( final RawNotamResponse rawResponse : rawResponseList ) {
			try {
				notams.addAll( parser.parse( rawResponse ) );
			}
			catch( final JsonProcessingException e ) {
				throw new RuntimeException(
						"Unable to parse FAA response", e );
			}
		}

		// prioritize notams
		final NotamPrioritizer prioritizer = new TextLengthNotamPrioritizer();
		final List<Notam> prioritizedList = prioritizer.prioritize( notams );

		return prioritizedList;
	}
}
