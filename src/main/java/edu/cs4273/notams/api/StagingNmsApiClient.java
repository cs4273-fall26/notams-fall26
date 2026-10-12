package edu.cs4273.notams.api;

import java.net.URI;

/**
 * Client configured for the FAA NMS staging environment.
 */
public class StagingNmsApiClient extends
		AbstractNmsApiClient // Client configured for the FAA NMS staging environment
		implements NmsApiAccess
{
	private static final URI TOKEN_URI = URI.create( // Staging NMS API token URI
			"https://api-staging.cgifederal-aim.com/v1/auth/token" );
	private static final URI NOTAM_URI = URI.create(
			"https://api-staging.cgifederal-aim.com/nmsapi/v1/notams" );

	public StagingNmsApiClient( final String clientId,
								// Constructor for the staging NMS API client
								final String clientSecret )
	{
		super( clientId, clientSecret, TOKEN_URI, NOTAM_URI );
	}
}
