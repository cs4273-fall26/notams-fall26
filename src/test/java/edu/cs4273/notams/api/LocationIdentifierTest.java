package edu.cs4273.notams.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LocationIdentifierTest
{
	@Test
	void normalizesBeforeCallingApi()
	{
		assertEquals( new LocationIdentifier( "KOKC" ),
				new LocationIdentifier( " kokc " ) );
		assertEquals( "1A2", new LocationIdentifier( "1a2" ).locationValue() );
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "AB", "ABCDEF", "K!KC", "K OKC", "OKC?x=1" })
	void invalidInputIsNotAnApiFailure( final String input )
	{
		assertThrows( IllegalArgumentException.class,
				() -> new LocationIdentifier( input ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "OKC", "KOKC", "DFW", "KDFW", "DCA", "KDCA",
			"1A2" })
	void continentalUsAirportsAreAccepted( final String input )
	{
		assertEquals( input, new LocationIdentifier( input ).locationValue() );
	}

	@ParameterizedTest
	@ValueSource(strings = { "PANC", "ANC", "PHNL", "HNL", "TJSJ", "SJU",
			"PWAK", "MMMY", "CYYZ", "ZZZZ" })
	void airportsOutsideContinentalUsAreRejected( final String input )
	{
		final IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> new LocationIdentifier( input ) );
		assertTrue( thrown.getMessage().contains( "continental US" ) );
	}
}
