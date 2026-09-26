import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LocationIdentifierTest
{
	@Test
	void normalizesBeforeCallingApi()
	{
		assertEquals( new LocationIdentifier( "KOKC" ), new LocationIdentifier( " kokc " ) );
		assertEquals( "1A2", new LocationIdentifier( "1a2" ).value() );
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "AB", "ABCDEF", "K!KC", "K OKC", "OKC?x=1" })
	void invalidInputIsNotAnApiFailure( final String input )
	{
		assertThrows( IllegalArgumentException.class, () -> new LocationIdentifier( input ) );
	}
}
