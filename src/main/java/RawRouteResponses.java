/**
 * Stores the raw NOTAM responses for the start and end locations.
 * <p>
 * This class does not parse the JSON. It passes the original FAA responses to
 * the Parsing Layer, which will process them later.
 */
public class RawRouteResponses
{

	// Airport or location code entered as the starting point.
	private final String startLocation;

	// Raw JSON returned by the FAA API for the starting point.
	private final String startResponse;

	// Airport or location code entered as the ending point.
	private final String endLocation;

	// Raw JSON returned by the FAA API for the ending point.
	private final String endResponse;

	/**
	 * Creates an object containing both raw FAA API responses.
	 */
	public RawRouteResponses( String startLocation,
							  String startResponse,
							  String endLocation,
							  String endResponse )
	{

		this.startLocation = startLocation;
		this.startResponse = startResponse;
		this.endLocation = endLocation;
		this.endResponse = endResponse;
	}

	// Returns the starting location code.
	public String getStartLocation()
	{
		return startLocation;
	}

	// Returns the raw JSON for the starting location.
	public String getStartResponse()
	{
		return startResponse;
	}

	// Returns the ending location code.
	public String getEndLocation()
	{
		return endLocation;
	}

	// Returns the raw JSON for the ending location.
	public String getEndResponse()
	{
		return endResponse;
	}
}