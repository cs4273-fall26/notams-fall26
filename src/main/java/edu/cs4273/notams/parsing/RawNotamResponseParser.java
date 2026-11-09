package edu.cs4273.notams.parsing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cs4273.notams.api.RawNotamResponse;
import edu.cs4273.notams.objects.Notam;

public class RawNotamResponseParser
{
	private static final Logger LOGGER = LoggerFactory.getLogger(
			RawNotamResponseParser.class );

	public List<Notam> parse( final RawNotamResponse response )
			throws JsonProcessingException
	{
		if( response == null ) {
			throw new IllegalArgumentException(
					"RawNotamResponse cannot be null" );
		}

		return parseJson( response.rawJson(),
				response.location().locationValue() );
	}

	public List<Notam> parseArea( final String rawJson )
			throws JsonProcessingException
	{
		if( rawJson == null ) {
			throw new IllegalArgumentException(
					"Area response cannot be null" );
		}

		return parseJson( rawJson, "" );
	}

	private List<Notam> parseJson( final String rawJson,
								   final String fallbackLocation ) throws JsonProcessingException
	{
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode root = mapper.readTree( rawJson );

		if( root == null ) {
			throw new IllegalArgumentException(
					"FAA response is empty" );
		}

		final JsonNode data = root.path( "data" );
		final JsonNode geojson = data.path( "geojson" );

		if( data.isObject() && data.isEmpty() ) {
			return new ArrayList<>();
		}

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

			String location = notamData.path( "icaoLocation" ).asText( "" );

			if( location.isEmpty() ) {
				location = notamData.path( "location" ).asText( "" );
			}

			if( location.isEmpty() ) {
				location = fallbackLocation;
			}

			final String startTime = notamData.path( "effectiveStart" ).asText( "" );
			final String endTime = notamData.path( "effectiveEnd" ).asText( "" );
			final String number = notamData.path( "number" ).asText( "" );
			final String classification = notamData.path( "classification" ).asText( "" );

			final Notam notam = new Notam(
					id, text, location, startTime, endTime, number, classification );
			notam.setType( notamData.path( "type" ).asText( "" ) );
			notams.add( notam );
		}

		return notams;
	}
}