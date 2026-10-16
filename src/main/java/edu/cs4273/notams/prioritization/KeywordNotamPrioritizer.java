package edu.cs4273.notams.prioritization;

import java.util.List;
import java.util.Locale;

import edu.cs4273.notams.objects.Notam;

public class KeywordNotamPrioritizer implements NotamPrioritizer
{
	@Override
	public List<Notam> prioritize( final List<Notam> notams )
	{
		if( notams == null || notams.isEmpty() ) {
			return notams;
		}

		notams.sort( ( first, second ) -> {
			final int firstPriority = getPriority( first );
			final int secondPriority = getPriority( second );

			if( firstPriority < secondPriority ) {
				return -1;
			}

			if( firstPriority > secondPriority ) {
				return 1;
			}

			return 0;
		} );

		return notams;
	}

	private int getPriority( final Notam notam )
	{
		if( notam == null || notam.getNotamText() == null ) {
			return 2;
		}

		final String text =
				notam.getNotamText().toUpperCase( Locale.ROOT );

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

		// Keep unknown notices in the middle for review.
		return 2;
	}
}