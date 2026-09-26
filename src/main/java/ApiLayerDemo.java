import java.util.List;

/**
 * Temporary API demo using command-line arguments, separate from CAP-19's
 * interactive input. Requires FAA_CLIENT_ID and FAA_CLIENT_SECRET; see README
 * for environment selection.
 */
public class ApiLayerDemo
{
	public static void main( final String[] args )
	{
		try {
			final LocationIdentifier start = new LocationIdentifier(
					args.length >= 1 ? args[0] : "KOKC" );
			final LocationIdentifier end = new LocationIdentifier(
					args.length >= 2 ? args[1] : "KDFW" );
			final NmsApiClient client = NmsConfiguration.fromEnvironment();
			System.out.println(
					"Requesting NOTAMs for " + start.value() + " to "
							+ end.value() + "..." );
			final List<RawNotamResponse> responses = client.fetchNotamsForRoute(
					start, end );
			for( final RawNotamResponse response : responses ) {
				final String label = start.equals( end ) ?
						"Start and End Location Response:" :
						response.location().equals( start ) ?
								"Start Location Response:" :
								"End Location Response:";
				System.out.println( label );
				printResponsePreview( response );
			}
			System.out.println( "API Layer test completed successfully." );
		}
		catch( final NmsHttpException exception ) {
			System.err.println( "API Layer error: " + exception.getMessage() );
			System.err.println( "HTTP status: " + exception.getStatusCode() );
		}
		catch( final NmsApiException exception ) {
			System.err.println( "API Layer error: " + exception.getMessage() );
		}
		catch( final IllegalArgumentException exception ) {
			System.err.println(
					"Input or configuration error: " + exception.getMessage() );
		}
	}

	/**
	 * Prints at most 300 characters; the full JSON stays available to the
	 * parsing layer.
	 */
	private static void printResponsePreview( final RawNotamResponse response )
	{
		System.out.println( "Location: " + response.location().value() );
		System.out.println(
				"Raw response length: " + response.rawJson().length()
						+ " characters" );
		final int previewLength = Math.min( 300, response.rawJson().length() );
		System.out.println( "Response preview:" );
		System.out.println( response.rawJson().substring( 0, previewLength ) );
		if( response.rawJson().length() > previewLength ) {
			System.out.println( "..." );
		}
	}
}
