
package edu.cs4273.notams.parsing;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.objects.Notam;

import static org.junit.jupiter.api.Assertions.*;

public class RawNotamResponseParserTest
{
	private final RawNotamResponseParser parser =
			new RawNotamResponseParser();

	@Test
	public void testParseOneNotam() throws Exception
	{
		final String json = """
                {
                  "data": {
                    "geojson": [
                      {
                        "properties": {
                          "coreNOTAMData": {
                            "notam": {
                              "id": "123",
                              "text": "RWY 18 CLSD",
                              "icaoLocation": "KOKC",
                              "effectiveStart": "2026-10-19T10:00:00Z",
                              "effectiveEnd": "2026-10-20T10:00:00Z"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
                """;

		final RawNotamResponse response =
				new RawNotamResponse(
						new LocationIdentifier( "KOKC" ), json );

		final List<Notam> result = parser.parse( response );

		assertEquals( 1, result.size() );
		assertEquals( "123", result.get( 0 ).getNotamId() );
		assertEquals( "RWY 18 CLSD", result.get( 0 ).getNotamText() );
		assertEquals( "KOKC", result.get( 0 ).getLocation() );
		assertEquals( "2026-10-19T10:00:00Z",
				result.get( 0 ).getStartTime() );
		assertEquals( "2026-10-20T10:00:00Z",
				result.get( 0 ).getEndTime() );
	}

	@Test
	public void testParseMultipleNotams() throws Exception
	{
		final String json = """
                {
                  "data": {
                    "geojson": [
                      {
                        "properties": {
                          "coreNOTAMData": {
                            "notam": {
                              "id": "1",
                              "text": "RWY 18 CLSD"
                            }
                          }
                        }
                      },
                      {
                        "properties": {
                          "coreNOTAMData": {
                            "notam": {
                              "id": "2",
                              "text": "TWY A CLSD"
                            }
                          }
                        }
                      }
                    ]
                  }
                }
                """;

		final RawNotamResponse response =
				new RawNotamResponse(
						new LocationIdentifier( "KOKC" ), json );

		final List<Notam> result = parser.parse( response );

		assertEquals( 2, result.size() );
		assertEquals( "1", result.get( 0 ).getNotamId() );
		assertEquals( "2", result.get( 1 ).getNotamId() );
		assertEquals( "TWY A CLSD",
				result.get( 1 ).getNotamText() );
		assertEquals( "KOKC", result.get( 1 ).getLocation() );
	}

	@Test
	public void testEmptyGeojson() throws Exception
	{
		final String json = """
                {
                  "data": {
                    "geojson": []
                  }
                }
                """;

		final RawNotamResponse response =
				new RawNotamResponse(
						new LocationIdentifier( "KOKC" ), json );

		final List<Notam> result = parser.parse( response );

		assertTrue( result.isEmpty() );
	}

	@Test
	public void testMissingGeojson()
	{
		final RawNotamResponse response =
				new RawNotamResponse(
						new LocationIdentifier( "KOKC" ), "{}" );

		assertThrows( IllegalArgumentException.class,
				() -> parser.parse( response ) );
	}

	@Test
	public void testNullResponse()
	{
		assertThrows( IllegalArgumentException.class,
				() -> parser.parse( null ) );
	}

	@Test
	public void testInvalidJson()
	{
		final RawNotamResponse response =
				new RawNotamResponse(
						new LocationIdentifier( "KOKC" ),
						"{ invalid json" );

		assertThrows( com.fasterxml.jackson.core.JsonProcessingException.class,
				() -> parser.parse( response ) );
	}
}
