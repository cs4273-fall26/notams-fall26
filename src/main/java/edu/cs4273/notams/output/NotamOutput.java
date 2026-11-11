package edu.cs4273.notams.output;

import java.util.ArrayList;
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

	// Format one NOTAM for console output.
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

	// Format the full list for the plain-text report.
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