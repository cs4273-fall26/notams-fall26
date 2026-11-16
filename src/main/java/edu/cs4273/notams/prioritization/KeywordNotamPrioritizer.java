package edu.cs4273.notams.prioritization;

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

	@Override
	public List<Notam> prioritize( final List<Notam> notams )
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
			priorities.put( notam, priority );
		}

		notams.sort( Comparator.comparingInt( priorities::get ) );

		return notams;
	}

	public int getPriority( final Notam notam )
	{
		final int priority = keywordPriority( notam );
		return priority == NO_KEYWORD ? 2 : priority;
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