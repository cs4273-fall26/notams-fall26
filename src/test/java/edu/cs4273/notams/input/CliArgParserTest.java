package edu.cs4273.notams.input;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

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
			final String[] params = { "-e", "okc" };
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
}
