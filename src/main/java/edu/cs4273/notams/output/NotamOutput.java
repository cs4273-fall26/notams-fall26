package edu.cs4273.notams.output;

import java.util.List;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.objects.Notam;

public class NotamOutput
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			NotamOutput.class );

	private final ToIntFunction<Notam> priorityLookup;
	private final boolean useColor;

	public NotamOutput()
	{
		this( notam -> 0, false );
	}

	public NotamOutput( final ToIntFunction<Notam> priorityLookup )
	{
		this( priorityLookup, NotamColor.isEnabled(
				System.getenv( "NO_COLOR" ), System.console() != null ) );
	}

	NotamOutput( final ToIntFunction<Notam> priorityLookup,
			final boolean useColor )
	{
		this.priorityLookup = priorityLookup;
		this.useColor = useColor;
	}

	public void render( List<Notam> notams )
	{
		// Checks if the NOTAMs are empty
		if( notams == null || notams.isEmpty() ) {
			LOGGER.info( "There are no NOTAMs available." );
			return;
		}

		for( Notam n : notams ) {
			LOGGER.info( "{}", formatLine( n ) );
		}
	}

	// Formats a single notam as a one line string
	// Also, it optionally applies color based on priority
	String formatLine( final Notam n )
	{
		final String location = n.getLocation().trim();
		final String text = n.getNotamText().trim().replaceAll( "\\s+", " " );
		final String startTime = n.getStartTime().trim();
		final String endTime = n.getEndTime().trim();
		final String validity = startTime.isEmpty() && endTime.isEmpty()
				? ""
				: startTime + "-" + endTime;
		final String line = String.join( " ",
				"!" + location, n.getNotamId().trim(), location, text,
				validity ).trim();

		if( !useColor ) {
			return line;
		}

		// Priority determines the color applied to the line
		final int priority = priorityLookup.applyAsInt( n );
		return NotamColor.apply( line, priority );
	}
}
