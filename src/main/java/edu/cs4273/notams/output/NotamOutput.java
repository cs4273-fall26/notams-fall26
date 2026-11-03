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

		LOGGER.info( "Prioritized NOTAMs:\n" );

		int index = 1;
		for( Notam n : notams ) {
			for( String line : formatLines( index, n ) ) {
				LOGGER.info( "{}", line );
			}
			LOGGER.info( "" );

			index++;
		}
	}

	List<String> formatLines( final int index, final Notam n )
	{
		final List<String> lines = List.of(
				index + ". " + n.getNotamId(),
				"Description: " + n.getNotamText(),
				"Location: " + n.getLocation(),
				"Start: " + n.getStartTime(),
				"End: " + n.getEndTime() );

		if( !useColor ) {
			return lines;
		}

		final int priority = priorityLookup.applyAsInt( n );
		return lines.stream()
				.map( line -> NotamColor.apply( line, priority ) )
				.toList();
	}
}
