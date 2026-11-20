package edu.cs4273.notams.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.api.AirportCoordinateLookup;
import edu.cs4273.notams.api.AirportCoordinateLookup.Coordinates;
import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.api.RouteSearchPoints;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.parsing.RawNotamResponseParser;

// Fetches the NOTAMs for both airports and for the points along the route.
public class RouteNotamFetcher
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			RouteNotamFetcher.class );

	private static final double SEARCH_SPACING_NM = 10;
	private static final double SEARCH_RADIUS_NM = Double.parseDouble(
			System.getProperty( "notam.radiusNm", "50" ) );

	private final NmsApiAccess apiClient;

	public RouteNotamFetcher( final NmsApiAccess apiClient )
	{
		this.apiClient = apiClient;
	}

	public List<Notam> fetch(
			final LocationIdentifier departure,
			final LocationIdentifier destination )
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
			LOGGER.debug( "Fetched {} airport API responses", responses.size() );

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
			LOGGER.debug( "Parsed {} NOTAMs in total", notams.size() );
		}
		catch( final NmsApiException e ) {
			throw new RuntimeException(
					"Unable to fetch FAA NOTAMs for the route", e );
		}
		catch( final IOException e ) {
			throw new RuntimeException(
					"Unable to load airport coordinates or parse FAA response", e );
		}

		return notams;
	}
}
