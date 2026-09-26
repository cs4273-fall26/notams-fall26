/**
 * A transport or response-processing failure while contacting FAA NMS.
 */
public class NmsApiException extends Exception
{
	public NmsApiException( final String message )
	{
		super( message );
	}

	public NmsApiException( final String message, final Throwable cause )
	{
		super( message, cause );
	}
}
