package edu.cs4273.notams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cs4273.notams.api.AirportCoordinateLookup;
import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.objects.Notam;

class EndToEndRunnerTest
{
	@Test
	void processFetchesParsesAndPrioritizesRouteNotams()
			throws NmsApiException
	{
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
                              "text": "RWY 18 CLOSED FOR MAINTENANCE",
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
                              "text": "TWY A CLOSED"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
                """ );

		// Return the taxiway first so the test checks that sorting moves the runway up.
		final RecordingApiClient apiClient = new RecordingApiClient(
				List.of( destinationResponse, departureResponse ) );

		final List<Notam> result = EndToEndRunner.process(
				departure, destination, apiClient );

		assertEquals( "KOKC", apiClient.startLocation.locationValue() );
		assertEquals( "KDEN", apiClient.endLocation.locationValue() );
		assertEquals( 2, result.size() );

		final Notam first = result.get( 0 );
		assertEquals( "LONG", first.getNotamId() );
		assertEquals( "KOKC", first.getLocation() );
		assertEquals( "2026-10-19T10:00:00Z", first.getStartTime() );

		final Notam second = result.get( 1 );
		assertEquals( "SHORT", second.getNotamId() );
		assertEquals( "KDEN", second.getLocation() );
	}

	@Test
	void processIncludesAreaNotamsAndRemovesRepeatedIds()
	{
		final LocationIdentifier departure = new LocationIdentifier( "ICT" );
		final LocationIdentifier destination = new LocationIdentifier( "OKC" );

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
				new ArrayList<>();

		final NmsApiAccess apiClient = new NmsApiAccess()
		{
			@Override
			public List<RawNotamResponse> fetchNotamsForRoute(
					final LocationIdentifier start,
					final LocationIdentifier end )
			{
				return List.of( new RawNotamResponse( start, airportJson ) );
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

		assertTrue( requestedPoints.size() > 2 );

		final AirportCoordinateLookup.Coordinates first = requestedPoints.get( 0 );
		assertEquals( 37.650314, first.latitude(), 0.000001 );
		assertEquals( -97.428583, first.longitude(), 0.000001 );

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