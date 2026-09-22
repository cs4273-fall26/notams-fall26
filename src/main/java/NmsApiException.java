/**
 * Custom exception used by the API layer.
 *
 * It represents problems such as connection failures, request timeouts,
 * and HTTP errors returned by the FAA API.
 */
public class NmsApiException extends Exception {

    // Stores the HTTP status code, such as 400, 401, 404, or 500.
    // A value of -1 means the FAA server did not return an HTTP response.
    private final int statusCode;

    /**
     * Used for an error that does not have an HTTP status code.
     *
     * Example: invalid airport input.
     */
    public NmsApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    /**
     * Used when another exception caused the API request to fail.
     *
     * Example: a connection failure or interrupted request.
     */
    public NmsApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    /**
     * Used when the FAA API returns an unsuccessful HTTP response.
     *
     * Example: status code 401 means unauthorized access.
     */
    public NmsApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code connected to this error.
     *
     * @return the HTTP status code, or -1 if no response was received
     */
    public int getStatusCode() {
        return statusCode;
    }
}