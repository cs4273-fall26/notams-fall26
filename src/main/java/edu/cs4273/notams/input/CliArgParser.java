package edu.cs4273.notams.input;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

public class CliArgParser
{
	// ExitHandler solely exists for unit testing
	private static ExitHandler exitHandler = new ExitHandler();

	public static void setExitHandler( ExitHandler exitHandler )
	{
		CliArgParser.exitHandler = exitHandler;
	}

	public static class ExitHandler
	{
		public void exit( final int exitCode )
		{
			System.exit( exitCode );
		}
	}

	public static void main( String[] args )
	{
		// Options could be marked as "required" but this interferes with the
		// ability to provide the help option (providing only `--help` means
		// that it will complain that --departure and --destination weren't
		// provided). Required args are manually checked below after handling
		// the help case.
		final Options options = new Options();

		// @formatter:off
		final Option departureOption = Option.builder( "d" )
				.longOpt( "departure" )
				.hasArg()
				.desc( "Departure airport code" )
				.get();
		options.addOption( departureOption );

		// Short opt 'a' for 'arrival', it's not ideal but sidesteps clumsy
		// letter repetition ike -d and -D since 'departure' and 'destination'
		// both start with D
		final Option destinationOption = Option.builder( "a" )
				.longOpt( "destination" )
				.hasArg()
				.desc( "Destination airport code" )
				.get();
		options.addOption( destinationOption );

		final Option helpOption = Option.builder( "h" )
				.longOpt( "help" )
				.desc( "Show help" )
				.get();
		options.addOption( helpOption );
		// @formatter:on

		final CommandLineParser commandLineParser = new DefaultParser();
		final CommandLine cmdLine;

		try {
			cmdLine = commandLineParser.parse( options, args );
		}
		catch( final ParseException e ) {
			System.err.println( "Unable to parse command line arguments" );
			throw new RuntimeException( e );
		}

		if( cmdLine.hasOption( helpOption )
				|| cmdLine.getOptions().length == 0 ) {
			final HelpFormatter helpFormatter = HelpFormatter.builder().get();
			try {
				helpFormatter.printHelp( "CliArgParser",
						"CS4273 Group O Notam Prioritization System Options",
						options,
						"Please report any bugs to https://capstone-fall26.atlassian.net/jira/",
						true );
			}
			catch( final IOException e ) {
				// log and swallow exception, we're exiting anyway
				System.err.println( "Unable to print help" );
				exitHandler.exit( 1 );
			}
			exitHandler.exit( 0 );
			return;
		}

		final boolean deptProvided = cmdLine.hasOption( departureOption );
		final boolean destProvided = cmdLine.hasOption( destinationOption );

		if( !deptProvided || !destProvided ) {
			// @formatter:off
			throw new RuntimeException(new ParseException(
					"Missing required args: " +
							( !deptProvided ? "--departure <airport-code> " : "" )
							+ ( !destProvided ? "--destination <airport-code>" : "" ) ) );
			// @formatter:on
		}

		final String departureCode = cmdLine.getOptionValue( departureOption );
		final String destinationCode = cmdLine.getOptionValue(
				destinationOption );

		System.out.println( "Departure code: " + departureCode );
		System.out.println( "Destination code: " + destinationCode );
	}
}
