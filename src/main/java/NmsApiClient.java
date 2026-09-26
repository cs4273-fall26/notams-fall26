import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

/**
 * Connects to FAA's NOTAM Management Service (NMS) and preserves raw NOTAM
 * JSON.
 */
public class NmsApiClient
{
	private static final int CONNECTION_TIMEOUT_IN_SECONDS = 10;
	private static final int REQUEST_TIMEOUT_IN_SECONDS = 30;
	private static final int TOKEN_EXPIRATION_MARGIN_IN_SECONDS = 60;
	private static final ObjectMapper JSON = new ObjectMapper().enable(
			DeserializationFeature.FAIL_ON_TRAILING_TOKENS );

	private final String clientId;
	private final String clientSecret;
	private final URI tokenUri;
	private final URI notamUri;
	private final HttpClient httpClient;
	private final Clock clock;
	private final Duration requestTimeout;

	// Cache per client to avoid authenticating for every request; guarded by this client.
	private String cachedAccessToken;
	private Instant tokenExpirationTime = Instant.EPOCH;

	public NmsApiClient( final String clientId,
						 final String clientSecret,
						 final URI tokenUri,
						 final URI notamUri )
	{
		this( clientId, clientSecret, tokenUri, notamUri,
				HttpClient.newBuilder().connectTimeout(
								Duration.ofSeconds( CONNECTION_TIMEOUT_IN_SECONDS ) )
						.build(), Clock.systemUTC(),
				Duration.ofSeconds( REQUEST_TIMEOUT_IN_SECONDS ) );
	}

	NmsApiClient( final String clientId,
				  final String clientSecret,
				  final URI tokenUri,
				  final URI notamUri,
				  final HttpClient httpClient,
				  final Clock clock,
				  final Duration requestTimeout )
	{
		final List<String> missing = new ArrayList<>();
		if( StringUtils.isBlank( clientId ) ) {
			missing.add( "clientId" );
		}
		if( StringUtils.isBlank( clientSecret ) ) {
			missing.add( "clientSecret" );
		}
		if( !missing.isEmpty() ) {
			throw new IllegalArgumentException(
					"Missing credentials: " + String.join( ", ", missing ) );
		}
		this.clientId = clientId.trim();
		this.clientSecret = clientSecret.trim();
		this.tokenUri = Objects.requireNonNull( tokenUri, "tokenUri" );
		this.notamUri = Objects.requireNonNull( notamUri, "notamUri" );
		this.httpClient = Objects.requireNonNull( httpClient, "httpClient" );
		this.clock = Objects.requireNonNull( clock, "clock" );
		this.requestTimeout = Objects.requireNonNull( requestTimeout,
				"requestTimeout" );
	}

	/**
	 * Returns one response per distinct validated location, in route order.
	 */
	public List<RawNotamResponse> fetchNotamsForRoute( final LocationIdentifier startLocation,
													   final LocationIdentifier endLocation )
			throws NmsApiException
	{
		Objects.requireNonNull( startLocation, "startLocation" );
		Objects.requireNonNull( endLocation, "endLocation" );
		final RawNotamResponse start = fetchResponse( startLocation );
		if( startLocation.equals( endLocation ) ) {
			return List.of( start );
		}
		final RawNotamResponse end = fetchResponse( endLocation );
		return List.of( start, end );
	}

	/**
	 * Uses the same response collection as route requests.
	 */
	public List<RawNotamResponse> fetchNotamsByLocation( final LocationIdentifier location )
			throws NmsApiException
	{
		return List.of( fetchResponse(
				Objects.requireNonNull( location, "location" ) ) );
	}

	private RawNotamResponse fetchResponse( final LocationIdentifier location )
			throws NmsApiException
	{
		HttpResponse<String> response = sendNotamRequest( location );
		// A 401 can indicate that the cached token is no longer valid.
		if( response.statusCode() == 401 ) {
			clearCachedToken();
			response = sendNotamRequest( location );
		}
		if( !isSuccessful( response.statusCode() ) ) {
			throw createHttpException( "NOTAM request for " + location.value(),
					response );
		}
		return new RawNotamResponse( location, response.body() );
	}

	private HttpResponse<String> sendNotamRequest( final LocationIdentifier location )
			throws NmsApiException
	{
		final String encodedLocation = URLEncoder.encode( location.value(),
				StandardCharsets.UTF_8 );
		final URI requestUri = URI.create(
				notamUri + "?location=" + encodedLocation );
		final HttpRequest request = HttpRequest.newBuilder( requestUri )
				.timeout( requestTimeout )
				.header( "Authorization", "Bearer " + getAccessToken() )
				.header( "Accept", "application/json" )
				.header( "nmsResponseFormat", "GEOJSON" ).GET().build();
		return sendRequest( request, "FAA NOTAM request" );
	}

	private synchronized String getAccessToken() throws NmsApiException
	{
		if( cachedAccessToken != null && clock.instant()
				.isBefore( tokenExpirationTime ) ) {
			return cachedAccessToken;
		}
		final String credentials = clientId + ":" + clientSecret;
		final String basicAuthorization = Base64.getEncoder().encodeToString(
				credentials.getBytes( StandardCharsets.UTF_8 ) );
		final HttpRequest request = HttpRequest.newBuilder( tokenUri )
				.timeout( requestTimeout )
				.header( "Authorization", "Basic " + basicAuthorization )
				.header( "Content-Type", "application/x-www-form-urlencoded" )
				.header( "Accept", "application/json" )
				.POST( HttpRequest.BodyPublishers.ofString(
						"grant_type=client_credentials" ) ).build();
		final Instant requestedAt = clock.instant();
		final HttpResponse<String> response = sendRequest( request,
				"FAA authentication request" );
		if( !isSuccessful( response.statusCode() ) ) {
			throw createHttpException( "FAA authentication request", response );
		}

		final JsonNode tokenResponse;
		try {
			tokenResponse = JSON.readTree( response.body() );
		}
		catch( final JsonProcessingException exception ) {
			// Parser messages can contain the authentication response, so do not expose them.
			throw new NmsApiException(
					"FAA authentication returned invalid JSON." );
		}
		if( tokenResponse == null || !tokenResponse.isObject() ) {
			throw new NmsApiException(
					"FAA authentication must return a JSON object." );
		}
		final JsonNode token = tokenResponse.get( "access_token" );
		if( token == null || !token.isTextual() || StringUtils.isBlank(
				token.textValue() ) ) {
			throw new NmsApiException(
					"FAA authentication response is missing a nonblank access_token." );
		}
		final Instant expiration = tokenExpiration(
				tokenResponse.get( "expires_in" ), requestedAt );
		cachedAccessToken = token.textValue();
		tokenExpirationTime = expiration;
		return cachedAccessToken;
	}

	private Instant tokenExpiration( final JsonNode expiresIn,
									 final Instant requestedAt )
			throws NmsApiException
	{
		// Without a lifetime, use the token once but do not assume it remains valid.
		if( expiresIn == null ) {
			return requestedAt;
		}
		try {
			final long lifetimeInSeconds;
			if( expiresIn.isIntegralNumber() && expiresIn.canConvertToLong() ) {
				lifetimeInSeconds = expiresIn.longValue();
			}
			else if( expiresIn.isTextual() ) {
				lifetimeInSeconds = Long.parseLong( expiresIn.textValue() );
			}
			else {
				throw new IllegalArgumentException();
			}
			if( lifetimeInSeconds <= 0 ) {
				throw new IllegalArgumentException();
			}
			final long safeLifetimeInSeconds = Math.max( 0,
					lifetimeInSeconds - TOKEN_EXPIRATION_MARGIN_IN_SECONDS );
			return requestedAt.plusSeconds( safeLifetimeInSeconds );
		}
		catch( final IllegalArgumentException | ArithmeticException |
					 DateTimeException exception ) {
			throw new NmsApiException(
					"FAA authentication returned an invalid expires_in value." );
		}
	}

	private HttpResponse<String> sendRequest( final HttpRequest request,
											  final String action )
			throws NmsApiException
	{
		try {
			return httpClient.send( request,
					HttpResponse.BodyHandlers.ofString() );
		}
		catch( final HttpTimeoutException exception ) {
			throw new NmsApiException( action + " timed out (request limit "
					+ requestTimeout.toMillis() + " milliseconds).",
					exception );
		}
		catch( final IOException exception ) {
			throw new NmsApiException(
					action + " failed because of a connection problem.",
					exception );
		}
		catch( final InterruptedException exception ) {
			Thread.currentThread().interrupt();
			throw new NmsApiException( action + " was interrupted.",
					exception );
		}
	}

	private NmsHttpException createHttpException( final String action,
												  final HttpResponse<String> response )
	{
		final int statusCode = response.statusCode();
		final String explanation;
		if( statusCode == 400 ) {
			explanation = "The FAA rejected the request format.";
		}
		else if( statusCode == 401 || statusCode == 403 ) {
			explanation = "The FAA credentials or access token were rejected.";
		}
		else if( statusCode == 404 ) {
			explanation = "The requested FAA resource was not found.";
		}
		else if( statusCode == 429 ) {
			explanation = "Too many requests were sent to the FAA API.";
		}
		else if( statusCode >= 500 ) {
			explanation = "The FAA server is currently experiencing an error.";
		}
		else {
			explanation = "The FAA API returned an unsuccessful response.";
		}
		return new NmsHttpException( statusCode,
				action + " failed with HTTP " + statusCode + ". "
						+ explanation );
	}

	private boolean isSuccessful( final int statusCode )
	{
		return statusCode >= 200 && statusCode < 300;
	}

	private synchronized void clearCachedToken()
	{
		cachedAccessToken = null;
		tokenExpirationTime = Instant.EPOCH;
	}
}
