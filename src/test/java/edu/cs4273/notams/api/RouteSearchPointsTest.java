package edu.cs4273.notams.api;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.cs4273.notams.api.AirportCoordinateLookup.Coordinates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteSearchPointsTest
{
	@Test
	void includesBothAirports()
	{
		final Coordinates start = new Coordinates( 37.650314, -97.428583 );
		final Coordinates end = new Coordinates( 35.393388, -97.598248 );

		final List<Coordinates> points =
				RouteSearchPoints.between( start, end, 10 );

		assertEquals( start, points.get( 0 ) );
		assertEquals( end, points.get( points.size() - 1 ) );
		assertTrue( points.size() > 2 );
	}

	@Test
	void dividesRouteIntoEvenlySpacedPoints()
	{
		// One degree along the equator is about 60 nautical miles.
		final Coordinates start = new Coordinates( 0, 0 );
		final Coordinates end = new Coordinates( 0, 1 );

		final List<Coordinates> points =
				RouteSearchPoints.between( start, end, 20 );

		// About 60 NM needs four sections to keep each below 20 NM.
		assertEquals( 5, points.size() );

		for( int i = 0; i < points.size(); i++ ) {
			assertEquals( 0, points.get( i ).latitude(), 0.000001 );
			assertEquals( i * 0.25,
					points.get( i ).longitude(), 0.000001 );
		}
	}

	@Test
	void sameAirportReturnsOnePoint()
	{
		final Coordinates airport = new Coordinates( 37.65, -97.43 );

		assertEquals( List.of( airport ),
				RouteSearchPoints.between( airport, airport, 10 ) );
	}

	@Test
	void crossesDateLineUsingShortRoute()
	{
		final Coordinates start = new Coordinates( 0, 179 );
		final Coordinates end = new Coordinates( 0, -179 );

		final List<Coordinates> points =
				RouteSearchPoints.between( start, end, 100 );

		assertEquals( 3, points.size() );
		assertEquals( 180,
				Math.abs( points.get( 1 ).longitude() ), 0.000001 );
		assertEquals( 0, points.get( 1 ).latitude(), 0.000001 );
	}

	@Test
	void rejectsInvalidSpacing()
	{
		final Coordinates start = new Coordinates( 0, 0 );
		final Coordinates end = new Coordinates( 0, 1 );

		for( final double spacing : new double[] {
				0, -10, Double.NaN, Double.POSITIVE_INFINITY } ) {
			assertThrows( IllegalArgumentException.class,
					() -> RouteSearchPoints.between(
							start, end, spacing ) );
		}
	}

	@Test
	void rejectsInvalidCoordinates()
	{
		final Coordinates end = new Coordinates( 0, 0 );

		assertThrows( IllegalArgumentException.class,
				() -> RouteSearchPoints.between(
						new Coordinates( 91, 0 ), end, 10 ) );

		assertThrows( IllegalArgumentException.class,
				() -> RouteSearchPoints.between(
						new Coordinates( 0, 181 ), end, 10 ) );

		assertThrows( IllegalArgumentException.class,
				() -> RouteSearchPoints.between(
						new Coordinates( Double.NaN, 0 ), end, 10 ) );
	}
}