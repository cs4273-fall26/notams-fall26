import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/** Validated input shared by the input and API layers; does not prove an airport exists. */
public record LocationIdentifier( String value )
{
	public LocationIdentifier( final String value )
	{
		final String normalized = StringUtils.trimToEmpty( value ).toUpperCase( Locale.ROOT );
		if( !normalized.matches( "[A-Z0-9]{3,5}" ) ) {
			throw new IllegalArgumentException(
					"Location must be a 3-5 character FAA or ICAO identifier, such as KOKC." );
		}
		this.value = normalized;
	}
}
