package edu.cs4273.notams.prioritization;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.objects.Notam;

public class KeywordNotamPrioritizer implements NotamPrioritizer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			KeywordNotamPrioritizer.class );
	private static final int NO_KEYWORD = -1;
	private static final LocalTime NIGHT_START = LocalTime.of( 18, 0 );
	private static final LocalTime NIGHT_END = LocalTime.of( 6, 0 );

	private final LocalTime departureTime;

	public KeywordNotamPrioritizer()
	{
		this( (LocalTime) null );
	}

	public KeywordNotamPrioritizer( final LocalTime departureTime )
	{
		this.departureTime = departureTime;
	}

	public KeywordNotamPrioritizer( final String departureTime )
	{
		this.departureTime = parseDepartureTime( departureTime );
	}

	private static LocalTime parseDepartureTime( final String departureTime )
	{
		if( departureTime == null || departureTime.isBlank() ) {
			return null;
		}
		try {
			return LocalTime.parse( departureTime.trim() );
		}
		catch( final DateTimeParseException e ) {
			throw new IllegalArgumentException(
					"Departure time must use HH:mm format", e );
		}
	}


	// Based on keywords in the notam, a priority will be assigned
	@Override
	public List<Notam> prioritize( final List<Notam> notams )
	{
		if( notams == null || notams.isEmpty() ) {
			return notams;
		}
		return prioritize( notams, departureTime );
	}

	public List<Notam> prioritize( final List<Notam> notams,
			final LocalTime departureTime )
	{
		if( notams == null || notams.isEmpty() ) {
			return notams;
		}

		final Map<Notam, Integer> priorities = new IdentityHashMap<>();
		for( final Notam notam : notams ) {
			int priority = keywordPriority( notam );
			if( priority == NO_KEYWORD ) {
				LOGGER.debug( "No priority keyword in NOTAM {}: {}",
						notam.notamId(), notam.notamText() );
				priority = 2;
			}
			priority = timeAdjustedPriority(
					notam, priority, departureTime );
			priorities.put( notam, priority );
		}

		notams.sort( Comparator.comparingInt( priorities::get ) );

		return notams;
	}

	public List<Notam> prioritize( final List<Notam> notams,
			final String departureTime )
	{
		return prioritize( notams, parseDepartureTime( departureTime ) );
	}

	public int getPriority( final Notam notam )
	{
		return getPriority( notam, departureTime );
	}

	public int getPriority( final Notam notam,
			final LocalTime departureTime )
	{
		final int priority = keywordPriority( notam );
		return timeAdjustedPriority(
				notam,
				priority == NO_KEYWORD ? 2 : priority,
				departureTime );
	}

	public int getPriority( final Notam notam,
			final String departureTime )
	{
		return getPriority( notam, parseDepartureTime( departureTime ) );
	}

	// From 6pm to 6am, certain notams will be more important based on visibility
	public static boolean isNightTime( final LocalTime time )
	{
		if( time == null ) {
			return false;
		}
		return !time.isBefore( NIGHT_START ) || time.isBefore( NIGHT_END );
	}

	// Nighttime priorities depend on visibility and navigation needs.
	private int timeAdjustedPriority( final Notam notam,
			final int basePriority, final LocalTime departureTime )
	{
		if( !isNightTime( departureTime ) || notam == null
				|| notam.notamText() == null ) {
			return basePriority;
		}

		final String text =
				notam.notamText().toUpperCase( Locale.ROOT );

		// Prioritize nighttime lighting, navigation, and obstacle information.
		if( text.contains( "LIGHT" ) || text.contains( "LGT" )
				|| text.contains( "OBST" ) || text.contains( "NAV" )
				|| text.contains( "ILS" ) || text.contains( "VOR" )
				|| text.contains( "NDB" ) ) {
			return Math.min( basePriority, 1 );
		}

		// Move ground operations back to standard priority at night.
		if( text.contains( "TWY" ) || text.contains( "APRON" ) ) {
			return 2;
		}

		return basePriority;
	}

	private int keywordPriority( final Notam notam )
	{
		if( notam == null || notam.notamText() == null ) {
			return 2;
		}

		final String text =
				notam.notamText().toUpperCase( Locale.ROOT );

		final String[] words = text.split( "\\W+" );

		// Use the first subject keyword found in the text.
		for( final String word : words ) {
			if( word.equals( "AD" )           // Aerodrome / airport
					|| word.equals( "RWY" )      // Runway
					|| word.equals( "AIRSPACE" ) // Airspace activity or restrictions
					|| word.equals( "SECURITY" ) // Security-related notices
			) {
				return 1;
			}

			if( word.equals( "TWY" )       // Taxiway
					|| word.equals( "APRON" ) // Aircraft parking and loading area
					|| word.equals( "SVC" )   // Services
			) {
				return 3;
			}

			if( word.equals( "NAV" )       // Navigation aids
					|| word.equals( "COM" )   // Communications
					|| word.equals( "IAP" )   // Instrument approach procedure
					|| word.equals( "ODP" )   // Obstacle departure procedure
					|| word.equals( "SID" )   // Standard instrument departure
					|| word.equals( "STAR" )  // Standard terminal arrival
					|| word.equals( "OBST" )  // Obstruction
					|| word.equals( "CHART" ) // Aeronautical chart
					|| word.equals( "DATA" )  // Aeronautical data
					|| word.equals( "DVA" )   // Diverse vector area
					|| word.equals( "VFP" )   // Visual flight procedure
					|| word.equals( "ROUTE" ) // Published route
					|| word.equals( "SPECIAL" ) // Special instrument flight procedure
			) {
				return 2;
			}
		}

		// Unknown notices stay in the middle for review.
		return NO_KEYWORD;
	}
}