package edu.cs4273.notams;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiClient;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.NmsConfiguration;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.input.CliArgParser;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.prioritizer.NotamPrioritizer;
import edu.cs4273.notams.prioritizer.TextLengthNotamPrioritizer;

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
		NmsApiClient apiClient = NmsConfiguration.fromEnvironment();

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
		// TODO: need to parse raw response into Notam objects

		// prioritize notams
		final NotamPrioritizer prioritizer = new TextLengthNotamPrioritizer();
		final List<Notam> prioritizedList = prioritizer.prioritize( null );

		// output prioritized notams
		final NotamOutput output = new NotamOutput();
		output.render();
	}
}
