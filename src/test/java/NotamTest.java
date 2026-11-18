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
		assertEquals( "A1234/26", notam.notamId() );
		assertEquals( "RWY 17L/35R CLSD", notam.notamText() );
		assertEquals( "KOKC", notam.location() );
		assertEquals( "2026-10-03T12:00Z", notam.startTime() );
		assertEquals( "2026-10-04T12:00Z", notam.endTime() );
	}

}