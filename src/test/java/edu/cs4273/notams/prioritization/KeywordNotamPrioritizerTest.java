package edu.cs4273.notams.prioritization;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cs4273.notams.objects.Notam;

import static org.junit.jupiter.api.Assertions.*;

public class KeywordNotamPrioritizerTest
{
	private final KeywordNotamPrioritizer prioritizer =
			new KeywordNotamPrioritizer();

	@Test
	public void testSortByPriority()
	{
		final Notam taxiway =
				new Notam( "1", "TWY A CLSD", "KOKC", "", "" );
		final Notam navigation =
				new Notam( "2", "NAV ILS U/S", "KOKC", "", "" );
		final Notam runway =
				new Notam( "3", "RWY 18/36 CLSD", "KOKC", "", "" );

		final List<Notam> notams = new ArrayList<>();
		notams.add( taxiway );
		notams.add( navigation );
		notams.add( runway );

		final List<Notam> result = prioritizer.prioritize( notams );

		assertEquals( List.of( runway, navigation, taxiway ), result );
	}

	@Test
	public void testSamePriorityKeepsOriginalOrder()
	{
		final Notam first =
				new Notam( "1", "RWY 18 CLSD", "KOKC", "", "" );
		final Notam second =
				new Notam( "2", "AD CLSD", "KOKC", "", "" );
		final Notam taxiway =
				new Notam( "3", "TWY A CLSD", "KOKC", "", "" );

		final List<Notam> notams = new ArrayList<>();
		notams.add( taxiway );
		notams.add( first );
		notams.add( second );

		final List<Notam> result = prioritizer.prioritize( notams );

		assertEquals( List.of( first, second, taxiway ), result );
	}

	@Test
	public void testLowercaseKeywords()
	{
		final Notam taxiway =
				new Notam( "1", "twy a clsd", "KOKC", "", "" );
		final Notam runway =
				new Notam( "2", "rwy 18 clsd", "KOKC", "", "" );

		final List<Notam> notams = new ArrayList<>();
		notams.add( taxiway );
		notams.add( runway );

		final List<Notam> result = prioritizer.prioritize( notams );

		assertEquals( List.of( runway, taxiway ), result );
	}

	@Test
	public void testUnknownTextStaysInMiddle()
	{
		final Notam unknown =
				new Notam( "1", "UNRECOGNIZED NOTICE", "KOKC", "", "" );
		final Notam taxiway =
				new Notam( "2", "TWY A CLSD", "KOKC", "", "" );
		final Notam runway =
				new Notam( "3", "RWY 18 CLSD", "KOKC", "", "" );

		final List<Notam> notams = new ArrayList<>();
		notams.add( taxiway );
		notams.add( unknown );
		notams.add( runway );

		final List<Notam> result = prioritizer.prioritize( notams );

		assertEquals( List.of( runway, unknown, taxiway ), result );
	}

	@Test
	public void testFirstKeywordDeterminesPriority()
	{
		final Notam navigation =
				new Notam( "1", "NAV ILS RWY 18 U/S", "KOKC", "", "" );
		final Notam runway =
				new Notam( "2", "RWY 18 CLSD", "KOKC", "", "" );

		final List<Notam> notams = new ArrayList<>();
		notams.add( navigation );
		notams.add( runway );

		final List<Notam> result = prioritizer.prioritize( notams );

		// NAV should get priority 2 even though RWY appears later.
		assertEquals( List.of( runway, navigation ), result );
	}

	@Test
	public void testEmptyList()
	{
		final List<Notam> notams = new ArrayList<>();

		final List<Notam> result = prioritizer.prioritize( notams );

		assertTrue( result.isEmpty() );
	}

	@Test
	public void testNullList()
	{
		assertNull( prioritizer.prioritize( null ) );
	}

	@Test
	public void testMissingTextStaysInMiddle()
	{
		final Notam missingText =
				new Notam( "1", null, "KOKC", "", "" );
		final Notam taxiway =
				new Notam( "2", "TWY A CLSD", "KOKC", "", "" );
		final Notam runway =
				new Notam( "3", "RWY 18 CLSD", "KOKC", "", "" );

		final List<Notam> notams = new ArrayList<>();
		notams.add( taxiway );
		notams.add( missingText );
		notams.add( runway );

		final List<Notam> result = prioritizer.prioritize( notams );

		assertEquals( List.of( runway, missingText, taxiway ), result );
	}

	@Test
	public void testSortMixedNotams()
	{
		final List<Notam> notams = new ArrayList<>();

		notams.add( new Notam(
				"1", "TWY A CLSD", "KOKC", "", "" ) );
		notams.add( new Notam(
				"2", "NAV ILS RWY 18 U/S", "KOKC", "", "" ) );
		notams.add( new Notam(
				"3", "RWY 18/36 CLSD", "KOKC", "", "" ) );
		notams.add( new Notam(
				"4", "UNRECOGNIZED NOTICE", "KOKC", "", "" ) );
		notams.add( new Notam(
				"5", "APRON WIP", "KOKC", "", "" ) );

		final List<Notam> result = prioritizer.prioritize( notams );

		assertEquals( 5, result.size() );
		assertEquals( "3", result.get( 0 ).getNotamId() );
		assertEquals( "2", result.get( 1 ).getNotamId() );
		assertEquals( "4", result.get( 2 ).getNotamId() );
		assertEquals( "1", result.get( 3 ).getNotamId() );
		assertEquals( "5", result.get( 4 ).getNotamId() );
	}


}