package edu.cs4273.notams.api;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class NmsConfigurationTest
{
	@Test
	void reportsAllMissingCredentialsTogether()
	{
		final String message = assertThrows(
				IllegalArgumentException.class,
				() -> NmsConfiguration.fromEnvironment(
						Map.of() ) ).getMessage();

		assertTrue( message.contains( "FAA_CLIENT_ID" ) );
		assertTrue( message.contains( "FAA_CLIENT_SECRET" ) );
	}

	@Test
	void reportsBlankCredentialsTogether()
	{
		final String message = assertThrows(
				IllegalArgumentException.class,
				() -> NmsConfiguration.fromEnvironment(
						Map.of(
								"FAA_CLIENT_ID", " ",
								"FAA_CLIENT_SECRET", "\t" ) ) )
				.getMessage();

		assertTrue( message.contains( "FAA_CLIENT_ID" ) );
		assertTrue( message.contains( "FAA_CLIENT_SECRET" ) );
	}

	@Test
	void defaultsToStagingWhenProductionFlagIsMissing()
	{
		final NmsApiAccess client =
				NmsConfiguration.fromEnvironment(
						Map.of(
								"FAA_CLIENT_ID", "test-id",
								"FAA_CLIENT_SECRET", "test-secret" ) );

		assertTrue( client instanceof StagingNmsApiClient );
	}

	@Test
	void usesStagingWhenProductionFlagIsFalse()
	{
		final NmsApiAccess client =
				NmsConfiguration.fromEnvironment(
						Map.of(
								"FAA_CLIENT_ID", "test-id",
								"FAA_CLIENT_SECRET", "test-secret",
								"USE_PRODUCTION_NMS_API", "false" ) );

		assertTrue( client instanceof StagingNmsApiClient );
	}

	@Test
	void usesProductionWhenProductionFlagIsTrue()
	{
		final NmsApiAccess client =
				NmsConfiguration.fromEnvironment(
						Map.of(
								"FAA_CLIENT_ID", "test-id",
								"FAA_CLIENT_SECRET", "test-secret",
								"USE_PRODUCTION_NMS_API", "true" ) );

		assertTrue( client instanceof ProductionNmsApiClient );
	}
}
