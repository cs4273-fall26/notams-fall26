package edu.cs4273.notams.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public final class NmsConfiguration // Utility class for creating NmsApiAccess instances from environment variables
{
	private NmsConfiguration()
	{
	}

	public static NmsApiAccess fromEnvironment() // Create an NmsApiAccess instance from environment variables
	{
		return fromEnvironment( System.getenv() );
	}

	static NmsApiAccess fromEnvironment(
			// Create an NmsApiAccess instance from the provided environment map
			final Map<String, String> environment )
	{
		final String clientId = environment.get( "FAA_CLIENT_ID" );
		final String clientSecret = environment.get( "FAA_CLIENT_SECRET" );

		final List<String> missing = new ArrayList<>();

		if( StringUtils.isBlank(
				clientId ) ) { // Check if the client ID is missing
			missing.add( "FAA_CLIENT_ID" );
		}

		if( StringUtils.isBlank(
				clientSecret ) ) { // Check if the client secret is missing
			missing.add( "FAA_CLIENT_SECRET" );
		}

		if( !missing.isEmpty() ) { // Throw an exception if any required environment variables are missing
			throw new IllegalArgumentException(
					"Missing API authentication environment variables: "
							+ String.join( ", ", missing ) );
		}

		final boolean useProduction = Boolean.parseBoolean( // Determine if the production NMS API should be used
				environment.get( "USE_PRODUCTION_NMS_API" ) );

		if( useProduction ) { // Use the production NMS API if the flag is set
			return new ProductionNmsApiClient( clientId.trim(),
					clientSecret.trim() );
		}
		return new StagingNmsApiClient( clientId.trim(),
				clientSecret.trim() ); // Use the staging NMS API by default
	}
}
