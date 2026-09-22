import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connects the application to the FAA NMS API.
 *
 * Responsibilities:
 * 1. Read and use the FAA credentials.
 * 2. Request an access token.
 * 3. Request NOTAM data using airport location codes.
 * 4. Return the original JSON without parsing it.
 * 5. Handle timeouts, connection failures, and HTTP errors.
 */
public class NmsApiClient {

    // FAA pre-production authentication endpoint.
    private static final String TOKEN_URL =
            "https://api-staging.cgifederal-aim.com/v1/auth/token";

    // FAA pre-production NOTAM endpoint.
    private static final String NOTAM_URL =
            "https://api-staging.cgifederal-aim.com/nmsapi/v1/notams";

    // Maximum time allowed to establish a connection.
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);

    // Maximum time allowed for one complete API request.
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    // Finds the access_token value inside the authentication JSON.
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile(
            "\"access_token\"\\s*:\\s*\"([^\"]+)\""
    );

    // Finds the expires_in value inside the authentication JSON.
    private static final Pattern EXPIRES_IN_PATTERN = Pattern.compile(
            "\"expires_in\"\\s*:\\s*\"?(\\d+)\"?"
    );

    private final String clientId;
    private final String clientSecret;
    private final HttpClient httpClient;

    // The access token is temporarily saved so we do not request a new
    // token for every airport.
    private String cachedAccessToken;
    private Instant tokenExpirationTime = Instant.EPOCH;

    /**
     * Creates the API client using the supplied FAA credentials.
     */
    public NmsApiClient(String clientId, String clientSecret) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("FAA Client ID is required.");
        }

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("FAA Client Secret is required.");
        }

        this.clientId = clientId.trim();
        this.clientSecret = clientSecret;

        // Java's built-in HTTP client means no external library is required.
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT)
                .build();
    }

    /**
     * Creates an API client using credentials stored in environment variables.
     *
     * Expected variables:
     * FAA_CLIENT_ID
     * FAA_CLIENT_SECRET
     */
    public static NmsApiClient fromEnvironment() throws NmsApiException {
        String clientId = System.getenv("FAA_CLIENT_ID");
        String clientSecret = System.getenv("FAA_CLIENT_SECRET");

        if (clientId == null || clientId.isBlank()) {
            throw new NmsApiException(
                    "The FAA_CLIENT_ID environment variable is missing."
            );
        }

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new NmsApiException(
                    "The FAA_CLIENT_SECRET environment variable is missing."
            );
        }

        return new NmsApiClient(clientId, clientSecret);
    }

    /**
     * Receives the start and end locations from the User Input Layer.
     *
     * It requests NOTAMs for both locations and returns the two raw JSON
     * responses for the Parsing Layer.
     */
    public RawRouteResponses fetchNotamsForRoute(
            String startLocation,
            String endLocation) throws NmsApiException {

        String cleanStart = validateLocation(startLocation, "start");
        String cleanEnd = validateLocation(endLocation, "end");

        String startJson = fetchNotamsByLocation(cleanStart);

        // Avoid sending the same request twice if both locations are equal.
        String endJson;
        if (cleanStart.equals(cleanEnd)) {
            endJson = startJson;
        } else {
            endJson = fetchNotamsByLocation(cleanEnd);
        }

        return new RawRouteResponses(
                cleanStart,
                startJson,
                cleanEnd,
                endJson
        );
    }

    /**
     * Requests raw NOTAM JSON for one airport or location.
     */
    public String fetchNotamsByLocation(String location)
            throws NmsApiException {

        String cleanLocation = validateLocation(location, "requested");

        HttpResponse<String> response =
                sendNotamRequest(cleanLocation);

        /*
         * A 401 response can mean that the cached token expired.
         * Clear it, request a new token, and retry one time.
         */
        if (response.statusCode() == 401) {
            clearCachedToken();
            response = sendNotamRequest(cleanLocation);
        }

        if (isSuccessful(response.statusCode())) {
            // Return the exact JSON received from the FAA.
            return response.body();
        }

        throw createHttpException(
                "NOTAM request for " + cleanLocation,
                response
        );
    }

    /**
     * Constructs and sends the FAA request for one location.
     */
    private HttpResponse<String> sendNotamRequest(String location)
            throws NmsApiException {

        String encodedLocation = URLEncoder.encode(
                location,
                StandardCharsets.UTF_8
        );

        URI requestUri = URI.create(
                NOTAM_URL + "?location=" + encodedLocation
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + getAccessToken())
                .header("Accept", "application/json")
                .header("nmsResponseFormat", "GEOJSON")
                .GET()
                .build();

        return sendRequest(request, "FAA NOTAM request");
    }

    /**
     * Returns a valid access token.
     *
     * A cached token is reused until it is close to expiring.
     */
    private synchronized String getAccessToken()
            throws NmsApiException {

        if (cachedAccessToken != null
                && Instant.now().isBefore(tokenExpirationTime)) {
            return cachedAccessToken;
        }

        // The token endpoint uses HTTP Basic authentication.
        String credentials = clientId + ":" + clientSecret;
        String basicAuthorization = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Basic " + basicAuthorization)
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                )
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=client_credentials"
                ))
                .build();

        HttpResponse<String> response =
                sendRequest(request, "FAA authentication request");

        if (!isSuccessful(response.statusCode())) {
            throw createHttpException(
                    "FAA authentication request",
                    response
            );
        }

        Matcher tokenMatcher =
                ACCESS_TOKEN_PATTERN.matcher(response.body());

        if (!tokenMatcher.find()) {
            throw new NmsApiException(
                    "FAA authentication succeeded, but no access token "
                            + "was found in the response."
            );
        }

        cachedAccessToken = tokenMatcher.group(1);

        // FAA tokens normally last about 30 minutes.
        long expiresInSeconds = 1799;
        Matcher expirationMatcher =
                EXPIRES_IN_PATTERN.matcher(response.body());

        if (expirationMatcher.find()) {
            expiresInSeconds =
                    Long.parseLong(expirationMatcher.group(1));
        }

        /*
         * Treat the token as expired 60 seconds early so it does not expire
         * while an API request is being sent.
         */
        long safeLifetime = Math.max(1, expiresInSeconds - 60);
        tokenExpirationTime =
                Instant.now().plusSeconds(safeLifetime);

        return cachedAccessToken;
    }

    /**
     * Sends an HTTP request and converts network problems into clear,
     * API-specific exceptions.
     */
    private HttpResponse<String> sendRequest(
            HttpRequest request,
            String action) throws NmsApiException {

        try {
            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (HttpTimeoutException exception) {
            throw new NmsApiException(
                    action + " timed out after "
                            + REQUEST_TIMEOUT.toSeconds() + " seconds.",
                    exception
            );
        } catch (IOException exception) {
            throw new NmsApiException(
                    action + " failed because of a connection problem.",
                    exception
            );
        } catch (InterruptedException exception) {
            // Restore Java's interrupted status before reporting the error.
            Thread.currentThread().interrupt();

            throw new NmsApiException(
                    action + " was interrupted.",
                    exception
            );
        }
    }

    /**
     * Validates and standardizes a location entered by the user.
     *
     * Example:
     * " kokc " becomes "KOKC".
     */
    private String validateLocation(String location, String label)
            throws NmsApiException {

        if (location == null || location.isBlank()) {
            throw new NmsApiException(
                    "The " + label + " location cannot be empty."
            );
        }

        String cleanLocation =
                location.trim().toUpperCase(Locale.US);

        // Accept common FAA and ICAO location identifiers.
        if (!cleanLocation.matches("[A-Z0-9]{3,5}")) {
            throw new NmsApiException(
                    "Invalid " + label + " location: "
                            + cleanLocation
                            + ". Enter a code such as KOKC or KDFW."
            );
        }

        return cleanLocation;
    }

    /**
     * Creates an understandable error for common HTTP status codes.
     */
    private NmsApiException createHttpException(
            String action,
            HttpResponse<String> response) {

        int statusCode = response.statusCode();
        String explanation;

        if (statusCode == 400) {
            explanation = "The FAA rejected the request format.";
        } else if (statusCode == 401 || statusCode == 403) {
            explanation = "The FAA credentials or access token were rejected.";
        } else if (statusCode == 404) {
            explanation = "The requested FAA resource was not found.";
        } else if (statusCode == 429) {
            explanation = "Too many requests were sent to the FAA API.";
        } else if (statusCode >= 500) {
            explanation = "The FAA server is currently experiencing an error.";
        } else {
            explanation = "The FAA API returned an unsuccessful response.";
        }

        return new NmsApiException(
                statusCode,
                action + " failed with HTTP "
                        + statusCode + ". " + explanation
        );
    }

    // HTTP status codes from 200 through 299 represent success.
    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    // Removes an expired or rejected access token.
    private synchronized void clearCachedToken() {
        cachedAccessToken = null;
        tokenExpirationTime = Instant.EPOCH;
    }
}