package edu.cs4273.notams.api;

import java.util.List;

public interface NmsApiAccess
{

	List<RawNotamResponse> fetchNotamsForRoute(
			// Fetch NOTAMs for a specific route
			LocationIdentifier startLocation, LocationIdentifier endLocation )
			throws NmsApiException;

	List<RawNotamResponse> fetchNotamsByLocation(
			// Fetch NOTAMs by a specific location
			LocationIdentifier location ) throws NmsApiException;
}
