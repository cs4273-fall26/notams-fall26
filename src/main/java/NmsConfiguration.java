import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public final class NmsConfiguration
{
	private static final String STAGING_TOKEN_URL =
			"https://api-staging.cgifederal-aim.com/v1/auth/token";

	private static final String STAGING_NOTAM_URL =
			"https://api-staging.cgifederal-aim.com/nmsapi/v1/notams";

	private static final String PRODUCTION_TOKEN_URL =
			"https://api-nms.aim.faa.gov/v1/auth/token";

	private static final String PRODUCTION_NOTAM_URL =
			"https://api-nms.aim.faa.gov/nmsapi/v1/notams";

	private NmsConfiguration()
	{
	}

	public static NmsApiClient fromEnvironment()
	{
		return fromEnvironment( System.getenv() );
	}

	static NmsApiClient fromEnvironment(
			final Map<String, String> environment )
	{
		final String clientId = environment.get( "FAA_CLIENT_ID" );
		final String clientSecret = environment.get( "FAA_CLIENT_SECRET" );

		final List<String> missing = new ArrayList<>();

		if( StringUtils.isBlank( clientId ) ) {
			missing.add( "FAA_CLIENT_ID" );
		}

		if( StringUtils.isBlank( clientSecret ) ) {
			missing.add( "FAA_CLIENT_SECRET" );
		}

		if( !missing.isEmpty() ) {
			throw new IllegalArgumentException(
					"Missing API authentication environment variables: "
							+ String.join( ", ", missing ) );
		}

		final boolean useProduction = Boolean.parseBoolean(
				environment.get( "USE_PRODUCTION_NMS_API" ) );

		final String tokenUrl;
		final String notamUrl;

		if( useProduction ) {
			tokenUrl = PRODUCTION_TOKEN_URL;
			notamUrl = PRODUCTION_NOTAM_URL;
		}
		else {
			tokenUrl = STAGING_TOKEN_URL;
			notamUrl = STAGING_NOTAM_URL;
		}

		return new NmsApiClient(
				clientId.trim(),
				clientSecret.trim(),
				URI.create( tokenUrl ),
				URI.create( notamUrl ) );
	}
}