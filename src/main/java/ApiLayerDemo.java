import java.util.List;

import edu.cs4273.notams.parsing.RawNotamResponseParser;
import edu.cs4273.notams.prioritization.KeywordNotamPrioritizer;
import edu.cs4273.notams.objects.Notam;
import com.fasterxml.jackson.core.JsonProcessingException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cs4273.notams.api.LocationIdentifier;
import edu.cs4273.notams.api.NmsApiAccess;
import edu.cs4273.notams.api.NmsApiException;
import edu.cs4273.notams.api.NmsConfiguration;
import edu.cs4273.notams.api.NmsHttpException;
import edu.cs4273.notams.api.RawNotamResponse;

/**
 * Temporary API demo using command-line arguments, separate from CAP-19's
 * interactive input. Requires FAA_CLIENT_ID and FAA_CLIENT_SECRET; see README
 * for environment selection.
 */
public class ApiLayerDemo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			ApiLayerDemo.class );

	public static void main( final String[] args )
	{
		try {
			final LocationIdentifier start = new LocationIdentifier(
					args.length >= 1 ? args[0] : "KOKC" );
			final LocationIdentifier end = new LocationIdentifier(
					args.length >= 2 ? args[1] : "KDFW" );
			final NmsApiAccess client = NmsConfiguration.fromEnvironment();
			LOGGER.info( "Requesting NOTAMs for {} to {}...", start.locationValue(),
					end.locationValue() );
			final List<RawNotamResponse> responses = client.fetchNotamsForRoute(
					start, end );
			for( final RawNotamResponse response : responses ) {
				final String label = start.equals( end ) ?
						"Start and End Location Response:" :
						response.location().equals( start ) ?
								"Start Location Response:" :
								"End Location Response:";
				System.out.println();
				System.out.println( "==================================================" );
				System.out.println( label + " " + response.location().locationValue() );
				System.out.println( "==================================================" );

				// printResponsePreview( response );
				try {
					final RawNotamResponseParser parser =
							new RawNotamResponseParser();

					final List<Notam> notams = parser.parse( response );
					final KeywordNotamPrioritizer prioritizer =
							new KeywordNotamPrioritizer();

					prioritizer.prioritize( notams );

					LOGGER.info( "Parsed NOTAMs: {}", notams.size() );
					final int limit = Math.min( 10, notams.size() );

					for( int i = 0; i < limit; i++ ) {
						final Notam notam = notams.get( i );

						System.out.println();
						System.out.println( "---------- NOTAM " + ( i + 1 ) + " ----------" );
						System.out.println( "ID: " + notam.getNotamId() );
						System.out.println( "Text: " + notam.getNotamText() );
						System.out.println( "Location: " + notam.getLocation() );
						System.out.println( "Start: " + notam.getStartTime() );
						System.out.println( "End: " + notam.getEndTime() );
					}

					System.out.println();
					System.out.println( "Showing " + limit + " of " + notams.size() + " NOTAMs." );
				}
				catch( final JsonProcessingException | IllegalArgumentException e ) {
					LOGGER.error( "Failed to parse NOTAMs: {}", e.getMessage() );
					return;
				}
			}
			LOGGER.info( "API Layer test completed successfully." );
		}
		catch( final NmsHttpException exception ) {
			LOGGER.error( "API Layer error: {}", exception.getMessage() );
			LOGGER.error( "HTTP status: {}", exception.getStatusCode() );
		}
		catch( final NmsApiException exception ) {
			LOGGER.error( "API Layer error: {}", exception.getMessage() );
		}
		catch( final IllegalArgumentException exception ) {
			LOGGER.error( "Input or configuration error: {}",
					exception.getMessage() );
		}
	}

	/**
	 * Prints at most 300 characters; the full JSON stays available to the
	 * parsing layer.
	 */
	private static void printResponsePreview( final RawNotamResponse response )
	{
		LOGGER.info( "Location: {}", response.location().locationValue() );
		LOGGER.info( "Raw response length: {} characters",
				response.rawJson().length() );
		final int previewLength = Math.min( 300, response.rawJson().length() );
		LOGGER.info( "Response preview:" );
		LOGGER.info( response.rawJson().substring( 0, previewLength ) );
		if( response.rawJson().length() > previewLength ) {
			LOGGER.info( "..." );
		}
	}
}
