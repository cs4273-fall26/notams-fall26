package edu.cs4273.notams.api;

import java.net.URI;

/**
 * Client configured for the FAA NMS production environment.
 */
public class ProductionNmsApiClient extends AbstractNmsApiClient
		implements NmsApiAccess
{
	private static final URI TOKEN_URI = URI.create( // Production NMS API token URI
			"https://api-nms.aim.faa.gov/v1/auth/token" );
	private static final URI NOTAM_URI = URI.create( // Production NMS API NOTAM URI
			"https://api-nms.aim.faa.gov/nmsapi/v1/notams" );

	public ProductionNmsApiClient( final String clientId,
								   // Constructor for the production NMS API client
								   final String clientSecret )
	{
		super( clientId, clientSecret, TOKEN_URI, NOTAM_URI );
	}
}
