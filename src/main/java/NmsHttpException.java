/**
 * An unsuccessful HTTP response received from FAA NMS.
 */
public final class NmsHttpException extends NmsApiException
{
	private final int statusCode;

	public NmsHttpException( final int statusCode, final String message )
	{
		super( message );
		this.statusCode = statusCode;
	}

	public int getStatusCode()
	{
		return statusCode;
	}
}
