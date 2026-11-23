package edu.cs4273.notams.prioritization;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.objects.Notam;

public class KeywordNotamPrioritizer implements NotamPrioritizer
{
	public enum FlightType
	{
		IFR,
		VFR;

		public static FlightType fromArgument( final String value )
		{
			if( value == null || value.isBlank() ) {
				return IFR;
			}
			try {
				return valueOf( value.trim().toUpperCase( Locale.ROOT ) );
			}
			catch( final IllegalArgumentException e ) {
				throw new IllegalArgumentException(
						"Flight type must be IFR or VFR", e );
			}
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(
			KeywordNotamPrioritizer.class );
	private static final int NO_KEYWORD = -1;
	private static final LocalTime NIGHT_START = LocalTime.of( 18, 0 );
	private static final LocalTime NIGHT_END = LocalTime.of( 6, 0 );

	private final FlightType flightType;
	private final LocalTime departureTime;

	public KeywordNotamPrioritizer()
	{
		this( FlightType.IFR, (LocalTime) null );
	}

	public KeywordNotamPrioritizer( final FlightType flightType )
	{
		this( flightType, (LocalTime) null );
	}

	public KeywordNotamPrioritizer( final LocalTime departureTime )
	{
		this( FlightType.IFR, departureTime );
	}

	public KeywordNotamPrioritizer( final String departureTime )
	{
		this( FlightType.IFR, parseDepartureTime( departureTime ) );
	}

	public KeywordNotamPrioritizer( final FlightType flightType,
									final String departureTime )
	{
		this( flightType, parseDepartureTime( departureTime ) );
	}

	public KeywordNotamPrioritizer( final FlightType flightType,
									final LocalTime departureTime )
	{
		this.flightType = Objects.requireNonNull( flightType,
				"flightType must not be null" );
		this.departureTime = departureTime;
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
	public int getPriority( final Notam notam )
	{
		return getPriority( notam, departureTime );
	}

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
			int priority = keywordPriority( notam, flightType );
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

	public int getPriority( final Notam notam,
			final LocalTime departureTime )
	{
		final int priority = keywordPriority( notam, flightType );
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

	private int keywordPriority( final Notam notam, final FlightType type )
	{
		if( notam == null || notam.notamText() == null ) {
			return 2;
		}

		final String text =
				notam.notamText().toUpperCase( Locale.ROOT );

		final String[] words = text.split( "\\W+" );

		// Use the first subject keyword found in the text.
		for( final String word : words ) {
			final int priority = priorityForKeyword( word, type );
			if( priority != 0 ) {
				return priority;
			}
		}

		// Unknown notices stay in the middle for review.
		return NO_KEYWORD;
	}

	private static int priorityForKeyword( final String word,
										   final FlightType type )
	{
		if( word.equals( "AD" ) || word.equals( "RWY" )
				|| word.equals( "AIRSPACE" ) || word.equals( "SECURITY" ) ) {
			return 1;
		}

		if( type == FlightType.VFR
				&& ( word.equals( "VFP" ) || word.equals( "OBST" ) ) ) {
			return 1;
		}

		if( type == FlightType.IFR
				&& ( word.equals( "SPECIAL" ) || word.equals( "SID" )
						|| word.equals( "IAP" ) ) ) {
			return 1;
		}

		if( word.equals( "TWY" ) || word.equals( "APRON" )
				|| word.equals( "SVC" ) ) {
			return 3;
		}

		if( type == FlightType.IFR ) {
			if( word.equals( "NAV" ) || word.equals( "COM" )
					|| word.equals( "ODP" ) || word.equals( "STAR" )
					|| word.equals( "OBST" ) || word.equals( "CHART" )
					|| word.equals( "DATA" ) || word.equals( "DVA" )
					|| word.equals( "VFP" ) || word.equals( "ROUTE" ) ) {
				return 2;
			}
		}
		else if( word.equals( "NAV" ) || word.equals( "COM" )
				|| word.equals( "CHART" ) || word.equals( "DATA" )
				|| word.equals( "DVA" ) || word.equals( "ROUTE" ) ) {
			return 2;
		}
		else if( word.equals( "IAP" ) || word.equals( "ODP" )
				|| word.equals( "SID" ) || word.equals( "STAR" )
				|| word.equals( "SPECIAL" ) ) {
			return 3;
		}

		return 0;
	}

}