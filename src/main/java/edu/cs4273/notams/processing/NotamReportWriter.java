package edu.cs4273.notams.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.objects.Notam;
import edu.cs4273.notams.output.NotamOutput;

// Saves the NOTAM report to a timestamped .txt file.
public class NotamReportWriter
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			NotamReportWriter.class );

	private final NotamOutput fileOutput;

	public NotamReportWriter( final NotamOutput fileOutput )
	{
		this.fileOutput = fileOutput;
	}

	public Path save( final String departureCode,
					  final String destinationCode,
					  final List<Notam> notams ) throws IOException
	{
		final String timestamp = LocalDateTime.now().format(
				DateTimeFormatter.ofPattern( "yyyyMMdd-HHmmss-SSS" ) );
		final String fileName = departureCode.toLowerCase( Locale.ROOT )
				+ "-" + destinationCode.toLowerCase( Locale.ROOT )
				+ "-" + timestamp + ".txt";
		final Path file = Path.of( fileName ).toAbsolutePath();

		try {
			Files.writeString( file,
					fileOutput.renderToString( notams ),
					StandardOpenOption.CREATE_NEW,
					StandardOpenOption.WRITE );
			LOGGER.info( "Saved {} NOTAMs to {}", notams.size(), file );
		}
		catch( final IOException e ) {
			throw new IOException(
					"Could not save NOTAM results to " + file, e );
		}

		return file;
	}
}
