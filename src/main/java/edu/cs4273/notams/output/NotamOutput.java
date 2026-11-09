package edu.cs4273.notams.output;

import java.util.ArrayList;
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
		for( String line : formatLines( notams ) ) {
			LOGGER.info( "{}", line );
		}
	}

	public List<String> formatLines( List<Notam> notams )
	{
		if( notams == null || notams.isEmpty() ) {
			return List.of( "There are no NOTAMs available." );
		}

		final List<String> lines = new ArrayList<>();
		lines.add( "Prioritized NOTAMs:" );
		lines.add( "" );

		int index = 1;
		for( Notam n : notams ) {
			lines.add( index + ". " + n.getNotamId() );
			lines.add( "Number: " + n.getNumber() );
			lines.add( "Classification: " + n.getClassification() );
			lines.add( "Description: " + n.getNotamText() );
			lines.add( "Location: " + n.getLocation() );
			lines.add( "Start: " + n.getStartTime() );
			lines.add( "End: " + n.getEndTime() );
			lines.add( "" );
			index++;
		}

		return lines;
	}
}