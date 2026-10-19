import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs4273.notams.objects.Notam;

class NotamTest
{
	private Notam notam;

	@BeforeEach
	void setUp()
	{
		notam = new Notam( "A1234/26", "RWY 17L/35R CLSD", "KOKC",
				"2026-10-03T12:00Z", "2026-10-04T12:00Z" );
	}

	@Test
	void gettersReturnConstructorValues()
	{
		assertEquals( "A1234/26", notam.getNotamId() );
		assertEquals( "RWY 17L/35R CLSD", notam.getNotamText() );
		assertEquals( "KOKC", notam.getLocation() );
		assertEquals( "2026-10-03T12:00Z", notam.getStartTime() );
		assertEquals( "2026-10-04T12:00Z", notam.getEndTime() );
	}

}