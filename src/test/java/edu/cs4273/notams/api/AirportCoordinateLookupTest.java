package edu.cs4273.notams.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirportCoordinateLookupTest
{
	@Test
	void resolvesDomesticAndIcaoCodes() throws Exception
	{
		final AirportCoordinateLookup lookup = new AirportCoordinateLookup();

		assertEquals(
				lookup.lookup( new LocationIdentifier( "KICT" ) ),
				lookup.lookup( new LocationIdentifier( "ICT" ) ) );

		assertEquals(
				lookup.lookup( new LocationIdentifier( "KOKC" ) ),
				lookup.lookup( new LocationIdentifier( "OKC" ) ) );
	}

	@Test
	void returnsExpectedCoordinates() throws Exception
	{
		final AirportCoordinateLookup lookup = new AirportCoordinateLookup();

		final AirportCoordinateLookup.Coordinates ict =
				lookup.lookup( new LocationIdentifier( "ICT" ) );
		final AirportCoordinateLookup.Coordinates okc =
				lookup.lookup( new LocationIdentifier( "OKC" ) );

		assertEquals( 37.650314, ict.latitude(), 0.000001 );
		assertEquals( -97.428583, ict.longitude(), 0.000001 );
		assertEquals( 35.393388, okc.latitude(), 0.000001 );
		assertEquals( -97.598248, okc.longitude(), 0.000001 );
	}

	@Test
	void rejectsUnknownAirport()
			throws Exception
	{
		final AirportCoordinateLookup lookup = new AirportCoordinateLookup();

		assertThrows( IllegalArgumentException.class,
				() -> lookup.lookup( new LocationIdentifier( "ZZZZZ" ) ) );
	}
}