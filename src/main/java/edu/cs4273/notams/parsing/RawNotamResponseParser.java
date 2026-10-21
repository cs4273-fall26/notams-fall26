
package edu.cs4273.notams.parsing;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.objects.Notam;

public class RawNotamResponseParser
{
	public List<Notam> parse( final RawNotamResponse response )
			throws JsonProcessingException
	{
		if( response == null ) {
			throw new IllegalArgumentException(
					"RawNotamResponse cannot be null" );
		}

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode root = mapper.readTree( response.rawJson() );

		if( root == null ) {
			throw new IllegalArgumentException(
					"FAA response is empty" );
		}

		final JsonNode geojson = root.path( "data" ).path( "geojson" );

		if( !geojson.isArray() ) {
			throw new IllegalArgumentException(
					"FAA response is missing the geojson array" );
		}

		final List<Notam> notams = new ArrayList<>();

		for( final JsonNode feature : geojson ) {

			final JsonNode notamData = feature.path( "properties" )
					.path( "coreNOTAMData" ).path( "notam" );

			if( !notamData.isObject() ) {
				throw new IllegalArgumentException(
						"FAA response contains invalid NOTAM data" );
			}

			final String id = notamData.path( "id" ).asText( "" );

			final String text = notamData.path( "text" ).asText( "" );

			String location = notamData.path( "icaoLocation" )
					.asText( "" );

			if( location.isEmpty() ) {
				location = notamData.path( "location" ).asText( "" );
			}

			if( location.isEmpty() ) {
				location = response.location().locationValue();
			}

			final String startTime = notamData
					.path( "effectiveStart" ).asText( "" );

			final String endTime = notamData
					.path( "effectiveEnd" ).asText( "" );

			final Notam notam = new Notam(
					id, text, location, startTime, endTime );

			notams.add( notam );
		}

		return notams;
	}
}
