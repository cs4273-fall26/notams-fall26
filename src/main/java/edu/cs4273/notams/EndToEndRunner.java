package edu.cs4273.notams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.api.AirportCoordinateLookup;
import edu.cs4273.notams.api.AirportCoordinateLookup.Coordinates;
import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.NmsConfiguration;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.api.RouteSearchPoints;
import edu.cs4273.notams.input.CliArgParser;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.output.NotamOutput;
import edu.cs4273.notams.parsing.RawNotamResponseParser;
import edu.cs4273.notams.prioritization.KeywordNotamPrioritizer;
import edu.cs4273.notams.prioritization.NotamPrioritizer;

public class EndToEndRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			EndToEndRunner.class );

	private static final double SEARCH_SPACING_NM = 10;
	private static final double SEARCH_RADIUS_NM = Double.parseDouble(
			System.getProperty( "notam.radiusNm", "50" ) );

	public static void main( String[] args ) throws IOException
	{
		final Pair<String, String> input = CliArgParser.parse( args );
		LOGGER.info( "Departure code: {}", input.getLeft() );
		LOGGER.info( "Destination code: {}", input.getRight() );

		final NmsApiAccess apiClient = NmsConfiguration.fromEnvironment();
		final LocationIdentifier departure = new LocationIdentifier(
				input.getLeft() );
		final LocationIdentifier destination = new LocationIdentifier(
				input.getRight() );

		final List<Notam> prioritizedList = process(
				departure, destination, apiClient );

		final NotamOutput output = new NotamOutput(
				new KeywordNotamPrioritizer()::getPriority );
		output.render( prioritizedList );

		final String timestamp = LocalDateTime.now().format(
				DateTimeFormatter.ofPattern( "yyyyMMdd-HHmmss-SSS" ) );
		final String fileName = input.getLeft().toLowerCase( Locale.ROOT ) + "-"
				+ input.getRight().toLowerCase( Locale.ROOT ) + "-"
				+ timestamp + ".txt";
		final Path file = Path.of( fileName ).toAbsolutePath();

		try {
			Files.write( file, output.formatLines( prioritizedList ),
					StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE );
			LOGGER.info( "Saved {} NOTAMs to {}", prioritizedList.size(), file );
		}
		catch( IOException e ) {
			throw new IOException( "Could not save NOTAM results to " + file, e );
		}
	}

	static List<Notam> process(
			final LocationIdentifier departure,
			final LocationIdentifier destination,
			final NmsApiAccess apiClient )
	{
		final RawNotamResponseParser parser = new RawNotamResponseParser();
		final List<Notam> notams = new ArrayList<>();

		try {
			final AirportCoordinateLookup lookup = new AirportCoordinateLookup();
			final Coordinates start = lookup.lookup( departure );
			final Coordinates end = lookup.lookup( destination );

			final List<Coordinates> searchPoints = RouteSearchPoints.between(
					start, end, SEARCH_SPACING_NM );

			// Fetch notices for the departure and destination airports.
			final List<RawNotamResponse> responses =
					apiClient.fetchNotamsForRoute( departure, destination );

			for( final RawNotamResponse response : responses ) {
				notams.addAll( parser.parse( response ) );
			}

			LOGGER.info( "Airport NOTAM records: {}", notams.size() );
			LOGGER.info( "Searching {} route points with radius {} NM",
					searchPoints.size(), SEARCH_RADIUS_NM );

			// Fetch nearby notices along the direct route.
			for( final Coordinates point : searchPoints ) {
				final String areaJson = apiClient.fetchNotamsByArea(
						point.latitude(), point.longitude(), SEARCH_RADIUS_NM );
				notams.addAll( parser.parseArea( areaJson ) );
			}
		}
		catch( final NmsApiException e ) {
			throw new RuntimeException(
					"Unable to fetch FAA NOTAMs for the route", e );
		}
		catch( final IOException e ) {
			throw new RuntimeException(
					"Unable to load airport coordinates or parse FAA response", e );
		}

		final Set<String> seenIds = new HashSet<>();
		final List<Notam> uniqueNotams = new ArrayList<>();

		for( final Notam notam : notams ) {
			final String id = notam.getNotamId();

			// Keep notices that have no ID. Overlapping searches can repeat an ID.
			if( id == null || id.isBlank() || seenIds.add( id ) ) {
				uniqueNotams.add( notam );
			}
		}

		LOGGER.info( "Fetched {} records; removed {} repeated IDs",
				notams.size(), notams.size() - uniqueNotams.size() );

		uniqueNotams.removeIf( notam -> notam.isCancellation() );

		final NotamPrioritizer prioritizer = new KeywordNotamPrioritizer();
		return prioritizer.prioritize( removeIcaoTwins( uniqueNotams ) );
	}

	static List<Notam> removeIcaoTwins( final List<Notam> notams )
	{
		final Set<String> originals = new HashSet<>();

		for( final Notam n : notams ) {
			if( !"INTL".equals( n.getClassification() ) ) {
				originals.add( twinKey( n ) );
			}
		}

		final List<Notam> result = new ArrayList<>();

		for( final Notam n : notams ) {
			if( "INTL".equals( n.getClassification() )
					&& originals.contains( twinKey( n ) ) ) {
				continue;
			}
			result.add( n );
		}

		return result;
	}

	private static String twinKey( final Notam n )
	{
		return n.getLocation() + "|" + n.getStartTime() + "|" + n.getEndTime();
	}
}