import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/** Loads credentials and environment settings before constructing an API client. */
public final class NmsConfiguration
{
	private NmsConfiguration() {}

	public static NmsApiClient fromEnvironment()
	{
		return fromEnvironment( System.getenv() );
	}

	static NmsApiClient fromEnvironment( final Map<String, String> environment )
	{
		final String mode = StringUtils.defaultIfBlank(
				environment.get( "FAA_ENVIRONMENT" ), "staging" ).trim().toLowerCase( Locale.ROOT );
		if( !mode.equals( "staging" ) && !mode.equals( "production" ) ) {
			throw new IllegalArgumentException( "FAA_ENVIRONMENT must be staging or production." );
		}

		final String prefix = "FAA_" + mode.toUpperCase( Locale.ROOT ) + "_";
		final List<String> required = new ArrayList<>( List.of( "FAA_CLIENT_ID", "FAA_CLIENT_SECRET" ) );
		if( mode.equals( "production" ) ) {
			required.add( prefix + "TOKEN_URL" );
			required.add( prefix + "NOTAM_URL" );
		}
		final List<String> missing = required.stream()
				.filter( name -> StringUtils.isBlank( environment.get( name ) ) ).toList();
		if( !missing.isEmpty() ) {
			throw new IllegalArgumentException( "Missing environment variables: " + String.join( ", ", missing ) );
		}

		final URI tokenUri = endpoint( environment, prefix + "TOKEN_URL",
				"https://api-staging.cgifederal-aim.com/v1/auth/token" );
		final URI notamUri = endpoint( environment, prefix + "NOTAM_URL",
				"https://api-staging.cgifederal-aim.com/nmsapi/v1/notams" );
		return new NmsApiClient( environment.get( "FAA_CLIENT_ID" ),
				environment.get( "FAA_CLIENT_SECRET" ), tokenUri, notamUri );
	}

	private static URI endpoint( final Map<String, String> environment,
			final String name, final String defaultUrl )
	{
		try {
			final URI uri = URI.create( StringUtils.defaultIfBlank( environment.get( name ), defaultUrl ).trim() );
			if( !"https".equalsIgnoreCase( uri.getScheme() ) || uri.getHost() == null
					|| uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null ) {
				throw new IllegalArgumentException();
			}
			return uri;
		}
		catch( final IllegalArgumentException exception ) {
			throw new IllegalArgumentException( name + " must be an absolute HTTPS URL without user info, query, or fragment." );
		}
	}
}
