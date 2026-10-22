package edu.cs4273.notams;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

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
	public static void main( String[] args )
	{
		// parse departure/destination airport code input
		final Pair<String, String> input = CliArgParser.parse( args );
		final String departureCode = input.getLeft();
		final String destinationCode = input.getRight();

		System.out.println( departureCode );
		System.out.println( destinationCode );

		// call the API
		final NmsApiAccess apiClient = NmsConfiguration.fromEnvironment();

		final LocationIdentifier departure = new LocationIdentifier(
				departureCode );
		final LocationIdentifier destination = new LocationIdentifier(
				destinationCode );

		List<RawNotamResponse> rawResponseList;
		try {
			rawResponseList = apiClient.fetchNotamsForRoute(
					departure, destination );
		}
		catch( final NmsApiException e ) {
			throw new RuntimeException( e );
		}
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

		// output prioritized notams
		final NotamOutput output = new NotamOutput();
		output.render( prioritizedList );
	}
}
