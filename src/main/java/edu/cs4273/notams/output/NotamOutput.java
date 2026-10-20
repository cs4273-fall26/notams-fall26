package edu.cs4273.notams.output;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.objects.Notam;

public class NotamOutput
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			NotamOutput.class );

	public void render( List<Notam> notams )
	{
		// Checks if the NOTAMs are empty
		if( notams == null || notams.isEmpty() ) {
			LOGGER.info( "There are no NOTAMs available." );
			return;
		}

		LOGGER.info( "Prioritized NOTAMs:\n" );

		int index = 1;
		for( Notam n : notams ) {

			// Print the NOTAM index and ID
			LOGGER.info( "{}. {}", index, n.getNotamId() );

			// Print the NOTAM Fields
			LOGGER.info( "Description: {}", n.getNotamText() );
			LOGGER.info( "Location: {}", n.getLocation() );
			LOGGER.info( "Start: {}", n.getStartTime() );
			LOGGER.info( "End: {}", n.getEndTime() );
			LOGGER.info( "" );

			index++;
		}
	}
}
