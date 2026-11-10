package edu.cs4273.notams.output;

import org.junit.jupiter.api.Test;

import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.prioritization.KeywordNotamPrioritizer;

import static org.junit.jupiter.api.Assertions.*;

public class NotamOutputTest
{
	private final KeywordNotamPrioritizer prioritizer =
			new KeywordNotamPrioritizer();

	private static Notam notam( final String text )
	{
		return new Notam( "1", text, "KOKC", "2026-11-10T12:00:00Z",
				"2026-11-10T18:00:00Z" );
	}


	// Tests formatted line will use expected colors
	private void assertAllLinesColored( final String text, final String color )
	{
		final NotamOutput output =
				new NotamOutput( prioritizer::getPriority, true );

		final String line = output.formatLine( notam( text ) );

		assertTrue( line.startsWith( color ), line );
		assertTrue( line.endsWith( NotamColor.RESET ), line );
	}


	// Tests runway notams are red
	@Test
	public void testRunwayNotamIsRed()
	{
		assertAllLinesColored( "RWY 18/36 CLSD", NotamColor.RED );
	}

	// Tests navigation notams are orange
	@Test
	public void testNavigationNotamIsOrange()
	{
		assertAllLinesColored( "NAV ILS U/S", NotamColor.ORANGE );
	}

	// Tests unknown notams are orange
	@Test
	public void testUnknownNotamIsOrange()
	{
		assertAllLinesColored( "NOTHING KNOWN HERE", NotamColor.ORANGE );
	}

	// Tests taxiway notams are yellow
	@Test
	public void testTaxiwayNotamIsYellow()
	{
		assertAllLinesColored( "TWY A CLSD", NotamColor.YELLOW );
	}

	// Tests color off prints standard single line
	@Test
	public void testColorOffPrintsStandardSingleLine()
	{
		final NotamOutput output =
				new NotamOutput( prioritizer::getPriority, false );

		assertEquals(
				"!KOKC 1 KOKC RWY 18 CLSD 2026-11-10T12:00:00Z-2026-11-10T18:00:00Z",
				output.formatLine( notam( "RWY 18 CLSD" ) ) );
	}

	@Test
	public void testDefaultOutputPrintsPlainLines()
	{
		final String line =
				new NotamOutput().formatLine( notam( "RWY 18 CLSD" ) );

		assertEquals(
				"!KOKC 1 KOKC RWY 18 CLSD 2026-11-10T12:00:00Z-2026-11-10T18:00:00Z",
				line );
	}

	@Test
	public void testPreservesValidityTimes()
	{
		final Notam timedNotam = new Notam( "11/001", "RWY 18/36 CLSD",
				"OUN", "2611101200", "2611101800" );

		assertEquals(
				"!OUN 11/001 OUN RWY 18/36 CLSD 2611101200-2611101800",
				new NotamOutput( prioritizer::getPriority, false )
						.formatLine( timedNotam ) );
	}

	@Test
	public void testFlattensMultilineTextAndOmitsMissingValidity()
	{
		final Notam noTimeNotam = new Notam( "11/001",
				"RWY 18 CLSD\nDUE TO WORK", "OUN", "", "" );

		assertEquals( "!OUN 11/001 OUN RWY 18 CLSD DUE TO WORK",
				new NotamOutput( prioritizer::getPriority, false )
						.formatLine( noTimeNotam ) );
	}
}
