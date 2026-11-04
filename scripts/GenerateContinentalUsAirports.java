import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds src/main/resources/continental-us-airports.txt from FAA airport data.
 * <p>
 * Download a cycle's APT CSV zip from the page below and unzip it:
 * https://www.faa.gov/air_traffic/flight_info/aeronav/aero_data/NASR_Subscription/
 * The current list came from the 2026-10-29 cycle:
 * https://nfdc.faa.gov/webContent/28DaySub/extra/29_Oct_2026_APT_CSV.zip
 * <p>
 * Usage: java scripts/GenerateContinentalUsAirports.java <APT_BASE.csv>
 * <output.txt>
 */
public class GenerateContinentalUsAirports
{
	private static final Set<String> STATES = Set.of( "AL", "AR", "AZ", "CA",
			"CO", "CT", "DC", "DE", "FL", "GA", "IA", "ID", "IL", "IN", "KS",
			"KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC",
			"ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA",
			"RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV",
			"WY" );
	private static final String SOURCE_PAGE = "https://www.faa.gov/air_traffic/flight_info/aeronav/aero_data/NASR_Subscription/";

	public static void main( final String[] args ) throws IOException
	{
		if( args.length != 2 ) {
			System.err.println(
					"Usage: java scripts/GenerateContinentalUsAirports.java <APT_BASE.csv> <output.txt>" );
			System.exit( 1 );
		}

		final List<String> lines = new String(
				Files.readAllBytes( Path.of( args[0] ) ),
				StandardCharsets.UTF_8 ).replace( "\uFEFF", "" ).lines()
				.toList();
		final List<String> header = parseLine( lines.get( 0 ) );
		final int effectiveDate = header.indexOf( "EFF_DATE" );
		final int country = header.indexOf( "COUNTRY_CODE" );
		final int state = header.indexOf( "STATE_CODE" );
		final int faaCode = header.indexOf( "ARPT_ID" );
		final int icaoCode = header.indexOf( "ICAO_ID" );

		final Set<String> identifiers = new TreeSet<>();
		int sites = 0;
		for( final String line : lines.subList( 1, lines.size() ) ) {
			final List<String> row = parseLine( line );
			if( row.get( country ).equals( "US" ) && STATES.contains(
					row.get( state ) ) ) {
				sites++;
				identifiers.add( row.get( faaCode ) );
				identifiers.add( row.get( icaoCode ) );
			}
		}
		identifiers.remove( "" );

		final String cycle = parseLine( lines.get( 1 ) ).get( effectiveDate )
				.replace( '/', '-' );
		final StringBuilder output = new StringBuilder();
		output.append(
				"# FAA identifiers and ICAO codes for the 48 contiguous states and DC.\n" );
		output.append( "# Source: FAA NASR APT_BASE.csv, " ).append( cycle )
				.append( " cycle.\n" );
		output.append( "# " ).append( SOURCE_PAGE ).append( "\n" );
		for( final String identifier : identifiers ) {
			output.append( identifier ).append( "\n" );
		}
		Files.writeString( Path.of( args[1] ), output.toString() );

		System.out.println(
				"Wrote " + identifiers.size() + " codes from " + sites
						+ " sites to " + args[1] );
	}

	private static List<String> parseLine( final String line )
	{
		final List<String> fields = new ArrayList<>();
		final StringBuilder field = new StringBuilder();
		boolean quoted = false;
		for( int i = 0; i < line.length(); i++ ) {
			final char c = line.charAt( i );
			if( c == '"' && quoted && i + 1 < line.length()
					&& line.charAt( i + 1 ) == '"' ) {
				field.append( '"' );
				i++;
			}
			else if( c == '"' ) {
				quoted = !quoted;
			}
			else if( c == ',' && !quoted ) {
				fields.add( field.toString() );
				field.setLength( 0 );
			}
			else {
				field.append( c );
			}
		}
		fields.add( field.toString() );
		return fields;
	}
}
