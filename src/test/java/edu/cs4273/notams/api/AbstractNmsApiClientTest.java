package edu.cs4273.notams.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class AbstractNmsApiClientTest
{
	private HttpServer server;
	private URI base;
	private AbstractNmsApiClient client;
	private final AtomicInteger tokenRequests = new AtomicInteger();
	private final AtomicInteger notamRequests = new AtomicInteger();
	private final List<String> queries = Collections.synchronizedList(
			new ArrayList<>() );
	private final List<String> authorizations = Collections.synchronizedList(
			new ArrayList<>() );
	private final MutableClock clock = new MutableClock();
	private volatile String tokenBody = "{\"access_token\":\"test-token\",\"expires_in\":3600}";
	private volatile String tokenAuthorization;
	private volatile String tokenRequestBody;
	private volatile String tokenMethod;
	private volatile String responseFormat;
	private volatile String accept;
	private volatile int tokenStatus = 200;
	private volatile int notamStatus = 200;
	private volatile boolean rejectFirstNotam;
	private volatile long delayInMilliseconds;

	@BeforeEach
	void setUp() throws IOException
	{
		server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ),
				0 );
		server.createContext( "/token", exchange -> {
			tokenRequests.incrementAndGet();
			tokenAuthorization = exchange.getRequestHeaders()
					.getFirst( "Authorization" );
			tokenRequestBody = new String(
					exchange.getRequestBody().readAllBytes(),
					StandardCharsets.UTF_8 );
			tokenMethod = exchange.getRequestMethod();
			if( delayInMilliseconds > 0 ) {
				try {
					Thread.sleep( delayInMilliseconds );
				}
				catch( final InterruptedException exception ) {
					Thread.currentThread().interrupt();
				}
			}
			respond( exchange, tokenStatus, tokenBody );
		} );
		server.createContext( "/notams", exchange -> {
			final int count = notamRequests.incrementAndGet();
			queries.add( exchange.getRequestURI().getRawQuery() );
			authorizations.add(
					exchange.getRequestHeaders().getFirst( "Authorization" ) );
			responseFormat = exchange.getRequestHeaders()
					.getFirst( "nmsResponseFormat" );
			accept = exchange.getRequestHeaders().getFirst( "Accept" );
			respond( exchange,
					rejectFirstNotam && count == 1 ? 401 : notamStatus,
					" { \"location\": \"" + exchange.getRequestURI()
							.getRawQuery() + "\" }\n" );
		} );
		server.start();
		base = URI.create(
				"http://127.0.0.1:" + server.getAddress().getPort() );
		client = newClient( Duration.ofSeconds( 3 ) );
	}

	@AfterEach
	void tearDown()
	{
		server.stop( 0 );
	}

	private AbstractNmsApiClient newClient( final Duration timeout )
	{
		return new TestNmsApiClient( " id ", " secret ",
				base.resolve( "/token" ), base.resolve( "/notams" ),
				HttpClient.newHttpClient(), clock, timeout );
	}

	private static final class TestNmsApiClient extends AbstractNmsApiClient
	{
		TestNmsApiClient( final String clientId,
						  final String clientSecret,
						  final URI tokenUri,
						  final URI notamUri )
		{
			super( clientId, clientSecret, tokenUri, notamUri );
		}

		TestNmsApiClient( final String clientId,
						  final String clientSecret,
						  final URI tokenUri,
						  final URI notamUri,
						  final HttpClient httpClient,
						  final Clock clock,
						  final Duration requestTimeout )
		{
			super( clientId, clientSecret, tokenUri, notamUri, httpClient,
					clock, requestTimeout );
		}
	}

	private static void respond( final HttpExchange exchange,
								 final int status,
								 final String body ) throws IOException
	{
		try (exchange) {
			final byte[] bytes = body.getBytes( StandardCharsets.UTF_8 );
			exchange.getResponseHeaders()
					.set( "Content-Type", "application/json" );
			exchange.sendResponseHeaders( status, bytes.length );
			exchange.getResponseBody().write( bytes );
		}
	}

	private List<RawNotamResponse> fetch() throws NmsApiException
	{
		return client.fetchNotamsByLocation( new LocationIdentifier( "KOKC" ) );
	}

	@Test
	void preservesRawJsonAndUsesCorrectAuthenticationAndHeaders()
			throws Exception
	{
		final List<RawNotamResponse> responses = fetch();
		assertEquals( " { \"location\": \"location=KOKC\" }\n",
				responses.get( 0 ).rawJson() );
		assertEquals( new LocationIdentifier( "KOKC" ),
				responses.get( 0 ).location() );
		assertEquals( "Basic " + Base64.getEncoder().encodeToString(
						"id:secret".getBytes( StandardCharsets.UTF_8 ) ),
				tokenAuthorization );
		assertEquals( "POST", tokenMethod );
		assertEquals( "grant_type=client_credentials", tokenRequestBody );
		assertEquals( "Bearer test-token", authorizations.get( 0 ) );
		assertEquals( "GEOJSON", responseFormat );
		assertEquals( "application/json", accept );
		assertThrows( UnsupportedOperationException.class,
				() -> responses.clear() );
	}

	@Test
	void routeAndSingleLocationUseSameCollectionAndReuseToken() throws Exception
	{
		final List<RawNotamResponse> responses = client.fetchNotamsForRoute(
				new LocationIdentifier( "KOKC" ),
				new LocationIdentifier( "KDFW" ) );
		assertEquals( List.of( "KOKC", "KDFW" ), responses.stream()
				.map( response -> response.location().locationValue() )
				.toList() );
		assertEquals( List.of( "location=KOKC", "location=KDFW" ), queries );
		assertEquals( 1, tokenRequests.get() );
		assertEquals( 2, notamRequests.get() );
		assertEquals( 1, fetch().size() );
		assertEquals( 1, tokenRequests.get() );
	}

	@Test
	void equalNormalizedLocationsReturnJsonOnlyOnce() throws Exception
	{
		final List<RawNotamResponse> responses = client.fetchNotamsForRoute(
				new LocationIdentifier( " kokc " ),
				new LocationIdentifier( "KOKC" ) );
		assertEquals( 1, responses.size() );
		assertEquals( 1, notamRequests.get() );
	}

	@Test
	void rejectsInvalidEndBeforeAnyRequest()
	{
		assertThrows( IllegalArgumentException.class,
				() -> client.fetchNotamsForRoute(
						new LocationIdentifier( "KOKC" ),
						new LocationIdentifier( "bad!" ) ) );
		assertThrows( NullPointerException.class,
				() -> client.fetchNotamsForRoute(
						new LocationIdentifier( "KOKC" ), null ) );
		assertEquals( 0, tokenRequests.get() );
		assertEquals( 0, notamRequests.get() );
	}

	@Test
	void parsesEscapedTokenAndStringLifetimeAsJson() throws Exception
	{
		tokenBody = "{\"expires_in\":\"3600\",\"access_token\":\"abc\\u002d123\",\"extra\":true}";
		fetch();
		fetch();
		assertEquals( "Bearer abc-123", authorizations.get( 0 ) );
		assertEquals( 1, tokenRequests.get() );
	}

	@ParameterizedTest
	@ValueSource(strings = { "not json", "[]", "null", "{}",
			"{\"access_token\":4}", "{\"access_token\":\" \"}",
			"{\"access_token\":\"x\",\"expires_in\":-1}",
			"{\"access_token\":\"x\",\"expires_in\":0}",
			"{\"access_token\":\"x\",\"expires_in\":1.5}",
			"{\"access_token\":\"x\",\"expires_in\":null}",
			"{\"access_token\":\"x\",\"expires_in\":\"later\"}",
			"{\"access_token\":\"x\",\"expires_in\":9223372036854775808}",
			"{\"access_token\":\"x\",\"expires_in\":9223372036854775807}",
			"{\"access_token\":\"x\"} {}" })
	void rejectsMalformedAuthenticationWithoutSendingNotamRequest( final String body )
	{
		tokenBody = body;
		final NmsApiException exception = assertThrows( NmsApiException.class,
				this::fetch );
		assertFalse( exception instanceof NmsHttpException );
		assertEquals( 0, notamRequests.get() );
	}

	@Test
	void malformedTokenDoesNotPoisonCache() throws Exception
	{
		tokenBody = "{\"access_token\":\"bad\",\"expires_in\":\"oops\"}";
		assertThrows( NmsApiException.class, this::fetch );
		tokenBody = "{\"access_token\":\"good\",\"expires_in\":3600}";
		fetch();
		assertEquals( 2, tokenRequests.get() );
		assertEquals( List.of( "Bearer good" ), authorizations );
	}

	@Test
	void missingLifetimeUsesTokenOnceWithoutAssumingAnExpiry() throws Exception
	{
		tokenBody = "{\"access_token\":\"x\"}";
		fetch();
		fetch();
		assertEquals( 2, tokenRequests.get() );
	}

	@Test
	void refreshesAtExpirationMargin() throws Exception
	{
		tokenBody = "{\"access_token\":\"first\",\"expires_in\":120}";
		fetch();
		clock.advance( 59 );
		fetch();
		assertEquals( 1, tokenRequests.get() );
		tokenBody = "{\"access_token\":\"second\",\"expires_in\":120}";
		clock.advance( 1 );
		fetch();
		assertEquals( 2, tokenRequests.get() );
		assertEquals( "Bearer second", authorizations.get( 2 ) );
	}

	@Test
	void retriesUnauthorizedOnceWithFreshToken() throws Exception
	{
		rejectFirstNotam = true;
		fetch();
		assertEquals( 2, tokenRequests.get() );
		assertEquals( 2, notamRequests.get() );
	}

	@Test
	void repeatedUnauthorizedDoesNotRetryForever()
	{
		notamStatus = 401;
		final NmsHttpException exception = assertThrows( NmsHttpException.class,
				this::fetch );
		assertEquals( 401, exception.getStatusCode() );
		assertEquals( 2, tokenRequests.get() );
		assertEquals( 2, notamRequests.get() );
	}

	@ParameterizedTest
	@ValueSource(ints = { 400, 403, 404, 429, 500, 503 })
	void preservesNotamHttpStatusWithoutRetry( final int status )
	{
		notamStatus = status;
		final NmsHttpException exception = assertThrows( NmsHttpException.class,
				this::fetch );
		assertEquals( status, exception.getStatusCode() );
		assertEquals( 1, notamRequests.get() );
	}

	@Test
	void authenticationHttpFailureHasStatusAndStopsBeforeNotams()
	{
		tokenStatus = 403;
		assertEquals( 403, assertThrows( NmsHttpException.class,
				this::fetch ).getStatusCode() );
		assertEquals( 0, notamRequests.get() );
	}

	@Test
	void timeoutIsTransportFailureWithoutFakeHttpStatus()
	{
		delayInMilliseconds = 500;
		client = newClient( Duration.ofMillis( 100 ) );
		final NmsApiException exception = assertThrows( NmsApiException.class,
				this::fetch );
		assertFalse( exception instanceof NmsHttpException );
		assertInstanceOf( HttpTimeoutException.class, exception.getCause() );
	}

	@Test
	void connectionFailurePreservesCause() throws Exception
	{
		final int unusedPort;
		try (final ServerSocket socket = new ServerSocket( 0 )) {
			unusedPort = socket.getLocalPort();
		}
		client = new TestNmsApiClient( "id", "secret",
				URI.create( "http://127.0.0.1:" + unusedPort + "/token" ),
				base.resolve( "/notams" ) );
		final NmsApiException exception = assertThrows( NmsApiException.class,
				this::fetch );
		assertFalse( exception instanceof NmsHttpException );
		assertInstanceOf( IOException.class, exception.getCause() );
	}

	@Test
	void restoresInterruptedStatus()
	{
		try {
			Thread.currentThread().interrupt();
			final NmsApiException exception = assertThrows(
					NmsApiException.class, this::fetch );
			assertInstanceOf( InterruptedException.class,
					exception.getCause() );
			assertTrue( Thread.currentThread().isInterrupted() );
		}
		finally {
			Thread.interrupted();
		}
	}

	@Test
	void concurrentRequestsShareOneClientToken() throws Exception
	{
		final var executor = Executors.newFixedThreadPool( 4 );
		try {
			final var tasks = new ArrayList<java.util.concurrent.Future<List<RawNotamResponse>>>();
			for( int i = 0; i < 8; i++ ) {
				tasks.add( executor.submit( this::fetch ) );
			}
			for( final var task : tasks ) {
				assertEquals( 1, task.get( 5, TimeUnit.SECONDS ).size() );
			}
			assertEquals( 1, tokenRequests.get() );
			assertEquals( 8, notamRequests.get() );
		}
		finally {
			executor.shutdownNow();
		}
	}
	@Test
	void areaRequestUsesCoordinatesAndPreservesRawJson() throws Exception
	{
		final String json = client.fetchNotamsByArea(
				37.65, -97.43, 10 );

		assertEquals(
				List.of( "latitude=37.65&longitude=-97.43&radius=10.0" ),
				queries );
		assertEquals(
				" { \"location\": \"latitude=37.65&longitude=-97.43&radius=10.0\" }\n",
				json );
		assertEquals( "Bearer test-token", authorizations.get( 0 ) );
		assertEquals( "GEOJSON", responseFormat );
		assertEquals( "application/json", accept );
	}

	@Test
	void areaAndLocationRequestsReuseToken() throws Exception
	{
		fetch();
		client.fetchNotamsByArea( 37.65, -97.43, 10 );

		assertEquals( 1, tokenRequests.get() );
		assertEquals( 2, notamRequests.get() );
	}

	@Test
	void areaRequestRetriesUnauthorizedOnce() throws Exception
	{
		rejectFirstNotam = true;

		client.fetchNotamsByArea( 37.65, -97.43, 10 );

		assertEquals( 2, tokenRequests.get() );
		assertEquals( 2, notamRequests.get() );
		assertEquals( queries.get( 0 ), queries.get( 1 ) );
	}

	@Test
	void areaRequestStopsAfterRepeatedUnauthorized()
	{
		notamStatus = 401;

		final NmsHttpException exception = assertThrows(
				NmsHttpException.class,
				() -> client.fetchNotamsByArea( 37.65, -97.43, 10 ) );

		assertEquals( 401, exception.getStatusCode() );
		assertEquals( 2, notamRequests.get() );
	}

	@ParameterizedTest
	@ValueSource(ints = { 400, 403, 404, 429, 500, 503 })
	void areaRequestPreservesHttpErrors( final int status )
	{
		notamStatus = status;

		final NmsHttpException exception = assertThrows(
				NmsHttpException.class,
				() -> client.fetchNotamsByArea( 37.65, -97.43, 10 ) );

		assertEquals( status, exception.getStatusCode() );
		assertEquals( 1, notamRequests.get() );
	}

	@Test
	void areaRequestRejectsInvalidValuesBeforeSending()
	{
		for( final double latitude : new double[] {
				-91, 91, Double.NaN,
				Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY } ) {
			assertThrows( IllegalArgumentException.class,
					() -> client.fetchNotamsByArea( latitude, -97.43, 10 ) );
		}

		for( final double longitude : new double[] {
				-181, 181, Double.NaN,
				Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY } ) {
			assertThrows( IllegalArgumentException.class,
					() -> client.fetchNotamsByArea( 37.65, longitude, 10 ) );
		}

		for( final double radius : new double[] {
				-1, 101, Double.NaN,
				Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY } ) {
			assertThrows( IllegalArgumentException.class,
					() -> client.fetchNotamsByArea( 37.65, -97.43, radius ) );
		}

		assertEquals( 0, tokenRequests.get() );
		assertEquals( 0, notamRequests.get() );
	}

	@Test
	void areaRequestAcceptsBoundaryValues() throws Exception
	{
		client.fetchNotamsByArea( -90, -180, 0 );
		client.fetchNotamsByArea( 90, 180, 100 );

		assertEquals( List.of(
						"latitude=-90.0&longitude=-180.0&radius=0.0",
						"latitude=90.0&longitude=180.0&radius=100.0" ),
				queries );
	}

	private static final class MutableClock extends Clock
	{
		private Instant now = Instant.parse( "2026-01-01T00:00:00Z" );

		void advance( final long seconds )
		{
			now = now.plusSeconds( seconds );
		}

		@Override
		public ZoneId getZone()
		{
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone( final ZoneId zone )
		{
			return Clock.fixed( now, zone );
		}

		@Override
		public Instant instant()
		{
			return now;
		}
	}
}
