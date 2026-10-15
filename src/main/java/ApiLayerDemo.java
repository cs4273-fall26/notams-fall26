import java.util.List;

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
				LOGGER.info( label );
				printResponsePreview( response );
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
