package edu.cs4273.notams.processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.objects.Notam;

// Removes the records we do not want to show to the user.
public final class NotamFilter
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			NotamFilter.class );

	private NotamFilter()
	{
	}

	// Different NOTAMs can share locations and effective times.
	// Keep them unless their NMS IDs match.
	public static List<Notam> removeRepeatedIds( final List<Notam> notams )
	{
		final Set<String> seenIds = new HashSet<>();
		final List<Notam> uniqueNotams = new ArrayList<>();

		for( final Notam notam : notams ) {
			final String id = notam.notamId();

			// Keep notices without IDs; remove repeated IDs from overlapping searches.
			if( id == null || id.isBlank() || seenIds.add( id ) ) {
				uniqueNotams.add( notam );
			}
		}

		LOGGER.info( "Fetched {} records; removed {} repeated IDs",
				notams.size(), notams.size() - uniqueNotams.size() );

		return uniqueNotams;
	}

	public static void removeCancellations( final List<Notam> notams )
	{
		notams.removeIf( notam -> notam.isCancellation() );
	}
}