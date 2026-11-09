package edu.cs4273.notams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.api.AirportCoordinateLookup;
class EndToEndRunnerTest
{
	@Test
	void processFetchesParsesAndPrioritizesRouteNotams()
			throws NmsApiException
	{
		// responsible for creating locations of routes and sample raw API responses
		final LocationIdentifier departure = new LocationIdentifier( "KOKC" );
		final LocationIdentifier destination = new LocationIdentifier( "KDEN" );
		final RawNotamResponse departureResponse = new RawNotamResponse(
				departure, """
                   {
                     "data": {
                       "geojson": [
                         {
                           "properties": {
                             "coreNOTAMData": {
                               "notam": {
                                 "id": "LONG",
                                 "text": "RUNWAY 18 CLOSED FOR MAINTENANCE",
                                 "effectiveStart": "2026-10-19T10:00:00Z",
                                 "effectiveEnd": "2026-10-20T10:00:00Z"
                               }
                             }
                           }
                         }
                       ]
                     }
                   }
                   """ );
		final RawNotamResponse destinationResponse = new RawNotamResponse(
				destination, """
                   {
                     "data": {
                       "geojson": [
                         {
                           "properties": {
                             "coreNOTAMData": {
                               "notam": {
                                 "id": "SHORT",
                                 "text": "TAXIWAY A CLOSED"
                               }
                             }
                           }
                         }
                       ]
                     }
                   }
                   """ );
		final RecordingApiClient apiClient = new RecordingApiClient(
				List.of( departureResponse, destinationResponse ) );

		// goes through the entire end-to-end process
		final List<Notam> result = EndToEndRunner.process(
				departure, destination, apiClient );

		// checks the route, parsed notams, location, and priority order for accuracy
		assertEquals( "KOKC", apiClient.startLocation.locationValue() );
		assertEquals( "KDEN", apiClient.endLocation.locationValue() );
		assertEquals( 2, result.size() );
		assertEquals( "SHORT", result.get( 0 ).getNotamId() );
		assertEquals( "KDEN", result.get( 0 ).getLocation() );
		assertEquals( "LONG", result.get( 1 ).getNotamId() );
		assertEquals( "2026-10-19T10:00:00Z",
				result.get( 1 ).getStartTime() );
	}

	@Test
	void processIncludesAreaNotamsAndRemovesRepeatedIds()
	{
		final LocationIdentifier departure =
				new LocationIdentifier( "ICT" );
		final LocationIdentifier destination =
				new LocationIdentifier( "OKC" );

		final String airportJson = """
                {
                  "data": {
                    "geojson": [
                      {
                        "properties": {
                          "coreNOTAMData": {
                            "notam": {
                              "id": "AIRPORT",
                              "text": "RWY CLSD",
                              "icaoLocation": "KICT"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
                """;

		final String areaJson = """
                {
                  "data": {
                    "geojson": [
                      {
                        "properties": {
                          "coreNOTAMData": {
                            "notam": {
                              "id": "AIRPORT",
                              "text": "RWY CLSD",
                              "icaoLocation": "KICT"
                            }
                          }
                        }
                      },
                      {
                        "properties": {
                          "coreNOTAMData": {
                            "notam": {
                              "id": "NEARBY",
                              "text": "OBST TOWER LGT U/S",
                              "icaoLocation": "KAAO"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
                """;

		final List<AirportCoordinateLookup.Coordinates> requestedPoints =
				new java.util.ArrayList<>();

		final NmsApiAccess apiClient = new NmsApiAccess()
		{
			@Override
			public List<RawNotamResponse> fetchNotamsForRoute(
					final LocationIdentifier start,
					final LocationIdentifier end )
			{
				return List.of(
						new RawNotamResponse( start, airportJson ) );
			}

			@Override
			public List<RawNotamResponse> fetchNotamsByLocation(
					final LocationIdentifier location )
			{
				return List.of();
			}

			@Override
			public String fetchNotamsByArea(
					final double latitude,
					final double longitude,
					final double radiusNauticalMiles )
			{
				assertEquals( 50.0, radiusNauticalMiles, 0.000005 );
				requestedPoints.add(
						new AirportCoordinateLookup.Coordinates(
								latitude, longitude ) );
				return areaJson;
			}
		};

		final List<Notam> result = EndToEndRunner.process(
				departure, destination, apiClient );

		assertEquals( 2, result.size() );
		assertEquals( "AIRPORT", result.get( 0 ).getNotamId() );
		assertEquals( "NEARBY", result.get( 1 ).getNotamId() );
		assertEquals( "KAAO", result.get( 1 ).getLocation() );

		org.junit.jupiter.api.Assertions.assertTrue(
				requestedPoints.size() > 2 );

		assertEquals( 37.650314,
				requestedPoints.get( 0 ).latitude(), 0.000001 );
		assertEquals( -97.428583,
				requestedPoints.get( 0 ).longitude(), 0.000001 );

		final AirportCoordinateLookup.Coordinates last =
				requestedPoints.get( requestedPoints.size() - 1 );

		assertEquals( 35.393388, last.latitude(), 0.000001 );
		assertEquals( -97.598248, last.longitude(), 0.000001 );
	}

	private static final class RecordingApiClient implements NmsApiAccess
	{
		private final List<RawNotamResponse> responses;
		private LocationIdentifier startLocation;
		private LocationIdentifier endLocation;

		private RecordingApiClient( final List<RawNotamResponse> responses )
		{
			this.responses = responses;
		}

		// while this method is called, it takes the start and end locations for checking later
		@Override
		public List<RawNotamResponse> fetchNotamsForRoute(
				final LocationIdentifier startLocation,
				final LocationIdentifier endLocation )
				throws NmsApiException
		{
			this.startLocation = startLocation;
			this.endLocation = endLocation;
			return responses;
		}

		@Override
		public List<RawNotamResponse> fetchNotamsByLocation(
				final LocationIdentifier location )
				throws NmsApiException
		{
			return List.of();
		}

		@Override
		public String fetchNotamsByArea(
				final double latitude,
				final double longitude,
				final double radiusNauticalMiles )
				throws NmsApiException
		{
			return """
                 {
                   "data": {
                     "geojson": []
                   }
                 }
                 """;
		}
	}
}