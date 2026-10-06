import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import edu.cs4273.notams.objects.Notam;

class NotamTest
{
	private static Notam sample()
	{
		return new Notam( "A1234/26", "RWY 17L/35R CLSD", "KOKC",
				"2026-10-03T12:00Z", "2026-10-04T12:00Z" );
	}

	@Test
	void gettersReturnConstructorValues()
	{
		final Notam n = sample();
		assertEquals( "A1234/26", n.getNotamId() );
		assertEquals( "RWY 17L/35R CLSD", n.getNotamText() );
		assertEquals( "KOKC", n.getLocation() );
		assertEquals( "2026-10-03T12:00Z", n.getStartTime() );
		assertEquals( "2026-10-04T12:00Z", n.getEndTime() );
	}

}