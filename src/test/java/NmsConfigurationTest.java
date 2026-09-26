import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NmsConfigurationTest
{
	@Test
	void reportsAllMissingCredentialsTogether()
	{
		final String message = assertThrows( IllegalArgumentException.class,
				() -> NmsConfiguration.fromEnvironment(
						Map.of() ) ).getMessage();
		assertTrue( message.contains( "FAA_CLIENT_ID" ) );
		assertTrue( message.contains( "FAA_CLIENT_SECRET" ) );
	}

	@Test
	void reportsBlankCredentialsAndBothMissingProductionEndpointsTogether()
	{
		final String message = assertThrows( IllegalArgumentException.class,
				() -> NmsConfiguration.fromEnvironment(
						Map.of( "FAA_ENVIRONMENT", "production",
								"FAA_CLIENT_ID", " ", "FAA_CLIENT_SECRET",
								"\t" ) ) ).getMessage();
		for( final String name : new String[] { "FAA_CLIENT_ID",
				"FAA_CLIENT_SECRET", "FAA_PRODUCTION_TOKEN_URL",
				"FAA_PRODUCTION_NOTAM_URL" } ) {
			assertTrue( message.contains( name ), name );
		}
	}

	@Test
	void stagingAndProductionCanBeConfiguredWithoutContactingFaa()
	{
		final Map<String, String> settings = new HashMap<>(
				Map.of( "FAA_CLIENT_ID", "test-id", "FAA_CLIENT_SECRET",
						"test-secret" ) );
		assertNotNull( NmsConfiguration.fromEnvironment( settings ) );
		settings.put( "FAA_STAGING_TOKEN_URL",
				"https://staging.example/token" );
		settings.put( "FAA_STAGING_NOTAM_URL",
				"https://staging.example/notams" );
		assertNotNull( NmsConfiguration.fromEnvironment( settings ) );
		settings.put( "FAA_ENVIRONMENT", " PRODUCTION " );
		settings.put( "FAA_PRODUCTION_TOKEN_URL",
				"https://production.example/token" );
		settings.put( "FAA_PRODUCTION_NOTAM_URL",
				"https://production.example/notams" );
		settings.put( "FAA_STAGING_TOKEN_URL", "invalid-unused-staging-url" );
		assertNotNull( NmsConfiguration.fromEnvironment( settings ) );
	}

	@Test
	void rejectsUnknownEnvironment()
	{
		assertThrows( IllegalArgumentException.class,
				() -> NmsConfiguration.fromEnvironment(
						Map.of( "FAA_ENVIRONMENT", "prodution" ) ) );
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://example.com/token", "/token",
			"https://user@example.com/token",
			"https://example.com/token?query=1",
			"https://example.com/token#fragment", "not a url" })
	void rejectsInvalidConfiguredEndpoints( final String url )
	{
		assertThrows( IllegalArgumentException.class,
				() -> NmsConfiguration.fromEnvironment(
						Map.of( "FAA_CLIENT_ID", "id", "FAA_CLIENT_SECRET",
								"secret", "FAA_STAGING_TOKEN_URL", url ) ) );
	}
}
