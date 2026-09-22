/**
 * An unsuccessful HTTP response received from FAA NMS.
 */
public final class NmsHttpException extends NmsApiException
{
	private final int httpStatusCode;

	public NmsHttpException( final int httpStatusCode, final String message )
	{
		super( message );
		this.httpStatusCode = httpStatusCode;
	}

	public int getStatusCode()
	{
		return httpStatusCode;
	}
}