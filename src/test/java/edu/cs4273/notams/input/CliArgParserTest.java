package edu.cs4273.notams.input;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class CliArgParserTest
{
	@Test
	public void testMissingDestinationThrowsException()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			// only departure airport is provided, destination is missing
			final String[] params = { "--departure", "okc" };
			CliArgParser.main( params );
		} );

		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testMissingDestinationShortThrowsException()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			final String[] params = { "-d", "okc" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testMissingDepartureThrowsException()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			// only destination airport is provided, departure is missing
			final String[] params = { "--destination", "okc" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testMissingDepartureShortThrowsException()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			final String[] params = { "-a", "okc" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testMissingDepartureArgThrowsException()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			final String[] params = { "--departure" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testMissingDestinationArgThrowsException()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			final String[] params = { "--destination" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testNoArgs() throws Exception
	{
		CliArgParser.ExitHandler exitHandler = mock(
				CliArgParser.ExitHandler.class );
		CliArgParser.setExitHandler( exitHandler );
		final String[] params = {};
		CliArgParser.main( params );
		// Should just print help and exit with return code of zero
		verify( exitHandler ).exit( 0 );
	}

	@Test
	public void testHelp() throws Exception
	{
		CliArgParser.ExitHandler exitHandler = mock(
				CliArgParser.ExitHandler.class );
		CliArgParser.setExitHandler( exitHandler );
		final String[] params = { "--help" };
		CliArgParser.main( params );
		verify( exitHandler ).exit( 0 );
	}

	@Test
	public void testUnsupportedOptions()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			final String[] params = { "--clowns", "all-of-them" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testValidOptionsWithUnsupportedOption()
	{
		RuntimeException thrown = assertThrows( RuntimeException.class, () -> {
			final String[] params = { "--departure", "okc", "--destination",
					"dfw", "--clowns", "all-of-them" };
			CliArgParser.main( params );
		} );
		Throwable cause = thrown.getCause();
		assertNotNull( cause, "Expected a cause inside RuntimeException" );
		assertInstanceOf( ParseException.class, cause,
				"Cause should be a ParseException" );
	}

	@Test
	public void testValidShortOptions()
	{
		final String[] params = { "-d", "okc", "-a", "dfw" };

		assertDoesNotThrow( () -> CliArgParser.main( params ) );
	}

	@Test
	public void testMixedShortAndLongOptions()
	{
		final String[] params = { "-d", "okc", "--destination", "dfw" };

		assertDoesNotThrow( () -> CliArgParser.main( params ) );
	}

	@Test
	public void testShortOptionsReversedOrder()
	{
		final String[] params = { "-a", "dfw", "-d", "okc" };

		assertDoesNotThrow( () -> CliArgParser.main( params ) );
	}

	@Test
	public void testShortHelp()
	{
		CliArgParser.ExitHandler exitHandler =
				mock( CliArgParser.ExitHandler.class );

		CliArgParser.setExitHandler( exitHandler );

		final String[] params = { "-h" };

		CliArgParser.main( params );

		verify( exitHandler ).exit( 0 );
	}

	@Test
	@ResourceLock("SYSTEM_OUT")
	public void testDepartureAndDestinationNotSwapped()
	{
		final String[] params = {
			"--departure", "okc",
			"--destination", "dfw"
		};

		// Capture the console output
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

			CliArgParser.main(params);

			String consoleOutput = output.toString(StandardCharsets.UTF_8);

			// Check that the correct airport codes were recorded
			assertTrue(consoleOutput.contains("Departure code: okc"));
			assertTrue(consoleOutput.contains("Destination code: dfw"));
		}
		finally {
			// Restore the original console output
			System.setOut(originalOut);
		}
	}
}
