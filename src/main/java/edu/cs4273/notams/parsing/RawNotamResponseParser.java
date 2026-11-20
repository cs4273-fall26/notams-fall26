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

		final String location = response.location().locationValue();
		return parseJson( response.rawJson(), location, location );
	}

	public List<Notam> parseArea( final String rawJson )
			throws JsonProcessingException
	{
		if( rawJson == null ) {
			throw new IllegalArgumentException(
					"Area response cannot be null" );
		}

		return parseJson( rawJson, "", "area search" );
	}

	private List<Notam> parseJson( final String rawJson,
								   final String fallbackLocation,
								   final String source ) throws JsonProcessingException
	{
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode root;
		try {
			root = mapper.readTree( rawJson );
		}
		catch( final JsonProcessingException e ) {
			LOGGER.error( "FAA response for {} is not valid JSON", source );
			throw e;
		}

		if( root == null ) {
			LOGGER.error( "FAA response for {} is empty", source );
			throw new IllegalArgumentException(
					"FAA response is empty" );
		}

		final JsonNode data = root.path( "data" );
		final JsonNode geojson = data.path( "geojson" );

		if( data.isObject() && data.isEmpty() ) {
			LOGGER.debug( "FAA response for {} has no NOTAMs", source );
			return new ArrayList<>();
		}

		if( !geojson.isArray() ) {
			LOGGER.error( "FAA response for {} is missing the geojson array",
					source );
			throw new IllegalArgumentException(
					"FAA response is missing the geojson array" );
		}

		LOGGER.debug( "FAA response for {} has {} NOTAMs", source,
				geojson.size() );
		final List<Notam> notams = new ArrayList<>();

		for( int i = 0; i < geojson.size(); i++ ) {
			final JsonNode feature = geojson.get( i );
			final JsonNode notamData = feature.path( "properties" )
					.path( "coreNOTAMData" ).path( "notam" );

			if( !notamData.isObject() ) {
				LOGGER.error( "Could not parse NOTAM {} of {} for {}: {}",
						i + 1, geojson.size(), source, feature );
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
				LOGGER.debug( "NOTAM {} has no location, using fallback '{}'",
						id, fallbackLocation );
			}

			final String startTime = notamData.path( "effectiveStart" ).asText( "" );
			final String endTime = notamData.path( "effectiveEnd" ).asText( "" );
			final String number = notamData.path( "number" ).asText( "" );
			final String classification = notamData.path( "classification" ).asText( "" );
			final String type = notamData.path( "type" ).asText( "" );

			final List<String> missingFields = new ArrayList<>();
			if( id.isBlank() ) {
				missingFields.add( "id" );
			}
			if( text.isBlank() ) {
				missingFields.add( "text" );
			}
			if( startTime.isBlank() ) {
				missingFields.add( "effectiveStart" );
			}
			if( !missingFields.isEmpty() ) {
				LOGGER.warn( "NOTAM {} of {} for {} is missing {}: {}",
						i + 1, geojson.size(), source,
						String.join( ", ", missingFields ), notamData );
			}

			notams.add( new Notam( id, text, location, startTime, endTime,
					number, classification, type ) );
		}

		LOGGER.debug( "Parsed {} NOTAMs for {}", notams.size(), source );
		return notams;
	}
}
