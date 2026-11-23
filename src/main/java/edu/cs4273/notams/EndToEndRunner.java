package edu.cs4273.notams;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsConfiguration;
import edu.cs4273.notams.input.CliArgParser;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.output.NotamOutput;
import edu.cs4273.notams.prioritization.KeywordNotamPrioritizer;
import edu.cs4273.notams.processing.NotamFilter;
import edu.cs4273.notams.processing.NotamReportWriter;
import edu.cs4273.notams.processing.RouteNotamFetcher;

public class EndToEndRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			EndToEndRunner.class );

	public static void main( String[] args ) throws IOException
	{
		final Triple<String, String, String> input = CliArgParser.parse( args );
		LOGGER.info( "Departure code: {}", input.getLeft() );
		LOGGER.info( "Destination code: {}", input.getMiddle() );
		LOGGER.info( "Flight type: {}", input.getRight() );
		final String departureTime = CliArgParser.parseDepartureTime( args );
		LOGGER.info( "Departure time: {}", departureTime );

		final NmsApiAccess apiClient = NmsConfiguration.fromEnvironment();
		final LocationIdentifier departure = new LocationIdentifier(
				input.getLeft() );
		final LocationIdentifier destination = new LocationIdentifier(
				input.getMiddle() );

		final KeywordNotamPrioritizer prioritizer =
				new KeywordNotamPrioritizer(
						KeywordNotamPrioritizer.FlightType.fromArgument(
								input.getRight() ),
						departureTime );
		final List<Notam> prioritizedList = process(
				departure, destination, apiClient, prioritizer );

		final NotamOutput output = new NotamOutput(
				prioritizer::getPriority );
		output.render( prioritizedList );

		// Save the report without console colors.
		final NotamOutput fileOutput = new NotamOutput(
				prioritizer::getPriority, false );
		new NotamReportWriter( fileOutput ).save(
				input.getLeft(), input.getMiddle(), prioritizedList );
	}

	
	static List<Notam> process(
			final LocationIdentifier departure,
			final LocationIdentifier destination,
			final NmsApiAccess apiClient )
	{
		return process( departure, destination, apiClient,
				new KeywordNotamPrioritizer() );
	}

	static List<Notam> process(
			final LocationIdentifier departure,
			final LocationIdentifier destination,
			final NmsApiAccess apiClient,
			final KeywordNotamPrioritizer prioritizer )
	{
		final List<Notam> notams = new RouteNotamFetcher( apiClient )
				.fetch( departure, destination );

		final List<Notam> uniqueNotams = NotamFilter.removeRepeatedIds( notams );
		final int beforeCancellations = uniqueNotams.size();
		NotamFilter.removeCancellations( uniqueNotams );
		LOGGER.debug( "Removed {} cancellation NOTAMs",
				beforeCancellations - uniqueNotams.size() );

		final List<Notam> prioritizedList = prioritizer.prioritize( uniqueNotams );
		LOGGER.debug( "Prioritized {} NOTAMs", prioritizedList.size() );

		return prioritizedList;
	}
}