import java.util.Objects;

/**
 * One location's unmodified FAA JSON for the parsing layer.
 */
public record RawNotamResponse(
		LocationIdentifier location,
		String rawJson)
{
	public RawNotamResponse( final LocationIdentifier location,
							 final String rawJson )
	{
		this.location = Objects.requireNonNull(
				location, "The location cannot be null" );
		this.rawJson = Objects.requireNonNull(
				rawJson, "The rawJson cannot be null" );
	}
}