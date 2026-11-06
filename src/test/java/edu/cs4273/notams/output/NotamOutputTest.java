package edu.cs4273.notams.output;

import java.util.List;

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
		return new Notam( "1", text, "KOKC", "start", "end" );
	}

	private void assertAllLinesColored( final String text, final String color )
	{
		final NotamOutput output =
				new NotamOutput( prioritizer::getPriority, true );

		final List<String> lines = output.formatLines( 1, notam( text ) );

		assertEquals( 5, lines.size() );
		for( final String line : lines ) {
			assertTrue( line.startsWith( color ), line );
			assertTrue( line.endsWith( NotamColor.RESET ), line );
		}
	}

	@Test
	public void testRunwayNotamIsRed()
	{
		assertAllLinesColored( "RWY 18/36 CLSD", NotamColor.RED );
	}

	@Test
	public void testNavigationNotamIsOrange()
	{
		assertAllLinesColored( "NAV ILS U/S", NotamColor.ORANGE );
	}

	@Test
	public void testUnknownNotamIsOrange()
	{
		assertAllLinesColored( "NOTHING KNOWN HERE", NotamColor.ORANGE );
	}

	@Test
	public void testTaxiwayNotamIsYellow()
	{
		assertAllLinesColored( "TWY A CLSD", NotamColor.YELLOW );
	}

	@Test
	public void testColorOffPrintsPlainLines()
	{
		final NotamOutput output =
				new NotamOutput( prioritizer::getPriority, false );

		assertEquals(
				List.of( "2. 1", "Description: RWY 18 CLSD", "Location: KOKC",
						"Start: start", "End: end" ),
				output.formatLines( 2, notam( "RWY 18 CLSD" ) ) );
	}

	@Test
	public void testDefaultOutputPrintsPlainLines()
	{
		final List<String> lines =
				new NotamOutput().formatLines( 1, notam( "RWY 18 CLSD" ) );

		assertEquals( "Description: RWY 18 CLSD", lines.get( 1 ) );
	}
}
