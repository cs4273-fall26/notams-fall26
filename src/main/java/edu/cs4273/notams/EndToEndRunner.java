package edu.cs4273.notams;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsConfiguration;
import edu.cs4273.notams.input.CliArgParser;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.output.NotamOutput;
import edu.cs4273.notams.processing.NotamReportWriter;
import edu.cs4273.notams.prioritization.KeywordNotamPrioritizer;
import edu.cs4273.notams.prioritization.NotamPrioritizer;
import edu.cs4273.notams.processing.NotamFilter;
import edu.cs4273.notams.processing.RouteNotamFetcher;
import edu.cs4273.notams.input.AirportCodeResolver;

public class EndToEndRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			EndToEndRunner.class );

	public static void main( String[] args ) throws IOException
	{
		final Pair<String, String> input = CliArgParser.parse( args );
		LOGGER.info( "Departure code: {}", input.getLeft() );
		LOGGER.info( "Destination code: {}", input.getRight() );

		final Scanner scanner = new Scanner( System.in );

		final LocationIdentifier departure =
				AirportCodeResolver.resolve( input.getLeft(), scanner );

		final LocationIdentifier destination =
				AirportCodeResolver.resolve( input.getRight(), scanner );

		final NmsApiAccess apiClient = NmsConfiguration.fromEnvironment();

		final List<Notam> prioritizedList = process(
				departure, destination, apiClient );

		final NotamOutput output = new NotamOutput(
				new KeywordNotamPrioritizer()::getPriority );
		output.render( prioritizedList );

		// Save the report without console colors.
		final NotamOutput fileOutput = new NotamOutput(
				new KeywordNotamPrioritizer()::getPriority, false );
				new NotamReportWriter( fileOutput ).save(
					departure.locationValue(),
					destination.locationValue(),
					prioritizedList );
	}

	static List<Notam> process(
			final LocationIdentifier departure,
			final LocationIdentifier destination,
			final NmsApiAccess apiClient )
	{
		final List<Notam> notams = new RouteNotamFetcher( apiClient )
				.fetch( departure, destination );

		final List<Notam> uniqueNotams = NotamFilter.removeRepeatedIds( notams );
		final int beforeCancellations = uniqueNotams.size();
		NotamFilter.removeCancellations( uniqueNotams );
		LOGGER.debug( "Removed {} cancellation NOTAMs",
				beforeCancellations - uniqueNotams.size() );

		final NotamPrioritizer prioritizer = new KeywordNotamPrioritizer();
		final List<Notam> prioritizedList = prioritizer.prioritize( uniqueNotams );
		LOGGER.debug( "Prioritized {} NOTAMs", prioritizedList.size() );

		return prioritizedList;
	}
}