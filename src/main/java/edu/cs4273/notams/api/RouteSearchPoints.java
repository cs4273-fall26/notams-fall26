package edu.cs4273.notams.api;

import java.util.ArrayList;
import java.util.List;

import edu.cs4273.notams.api.AirportCoordinateLookup.Coordinates;

public final class RouteSearchPoints
{
	private static final double EARTH_RADIUS_NM = 3440.065;

	private RouteSearchPoints()
	{
	}

	// Uses the haversine and great-circle intermediate point formulas.
	// Reference: https://www.movable-type.co.uk/scripts/latlong.html
	public static List<Coordinates> between(
			final Coordinates start,
			final Coordinates end,
			final double maximumSpacingNm )
	{
		checkCoordinates( start );
		checkCoordinates( end );

		if( !Double.isFinite( maximumSpacingNm )
				|| maximumSpacingNm <= 0 ) {
			throw new IllegalArgumentException(
					"Spacing must be a positive number." );
		}

		final double lat1 = Math.toRadians( start.latitude() );
		final double lon1 = Math.toRadians( start.longitude() );
		final double lat2 = Math.toRadians( end.latitude() );
		final double lon2 = Math.toRadians( end.longitude() );

		// Find the distance between the airports.
		final double latDifference = lat2 - lat1;
		final double lonDifference = lon2 - lon1;

		double a = Math.pow( Math.sin( latDifference / 2 ), 2 )
				+ Math.cos( lat1 ) * Math.cos( lat2 )
				* Math.pow( Math.sin( lonDifference / 2 ), 2 );

		// Keep rounding errors from putting a outside the valid range.
		a = Math.max( 0, Math.min( 1, a ) );

		final double angle = 2 * Math.atan2(
				Math.sqrt( a ), Math.sqrt( 1 - a ) );
		final double distanceNm = angle * EARTH_RADIUS_NM;

		if( angle < 1e-12 ) {
			return List.of( start );
		}

		if( Math.PI - angle < 1e-8 ) {
			throw new IllegalArgumentException(
					"Opposite points on Earth need an intermediate waypoint." );
		}

		final double segmentCount = Math.ceil(
				distanceNm / maximumSpacingNm );

		if( segmentCount > 10000 ) {
			throw new IllegalArgumentException(
					"Too many search points. Use a larger spacing." );
		}

		final int segments = Math.max( 1, (int) segmentCount );
		final List<Coordinates> points = new ArrayList<>();
		points.add( start );

		// Add evenly spaced points between the airports.
		for( int i = 1; i < segments; i++ ) {
			final double fraction = (double) i / segments;

			final double startWeight =
					Math.sin( ( 1 - fraction ) * angle ) / Math.sin( angle );
			final double endWeight =
					Math.sin( fraction * angle ) / Math.sin( angle );

			final double x =
					startWeight * Math.cos( lat1 ) * Math.cos( lon1 )
							+ endWeight * Math.cos( lat2 ) * Math.cos( lon2 );

			final double y =
					startWeight * Math.cos( lat1 ) * Math.sin( lon1 )
							+ endWeight * Math.cos( lat2 ) * Math.sin( lon2 );

			final double z =
					startWeight * Math.sin( lat1 )
							+ endWeight * Math.sin( lat2 );

			final double latitude = Math.toDegrees(
					Math.atan2( z, Math.sqrt( x * x + y * y ) ) );
			final double longitude = Math.toDegrees( Math.atan2( y, x ) );

			points.add( new Coordinates( latitude, longitude ) );
		}

		points.add( end );
		return List.copyOf( points );
	}

	private static void checkCoordinates( final Coordinates point )
	{
		if( point == null ) {
			throw new IllegalArgumentException(
					"Coordinates cannot be null." );
		}

		if( !Double.isFinite( point.latitude() )
				|| point.latitude() < -90 || point.latitude() > 90
				|| !Double.isFinite( point.longitude() )
				|| point.longitude() < -180 || point.longitude() > 180 ) {
			throw new IllegalArgumentException(
					"Invalid latitude or longitude." );
		}
	}
}