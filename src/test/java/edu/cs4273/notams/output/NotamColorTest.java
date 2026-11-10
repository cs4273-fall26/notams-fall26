package edu.cs4273.notams.output;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotamColorTest
{
	@Test
	public void testPriorityOneIsRed()
	{
		assertEquals( NotamColor.RED + "text" + NotamColor.RESET,
				NotamColor.apply( "text", 1 ) );
	}

	@Test
	public void testPriorityTwoIsOrange()
	{
		assertEquals( NotamColor.ORANGE + "text" + NotamColor.RESET,
				NotamColor.apply( "text", 2 ) );
	}

	@Test
	public void testPriorityThreeIsYellow()
	{
		assertEquals( NotamColor.YELLOW + "text" + NotamColor.RESET,
				NotamColor.apply( "text", 3 ) );
	}

	@Test
	public void testUnknownPriorityIsPlain()
	{
		assertEquals( "text", NotamColor.apply( "text", 0 ) );
	}

	@Test
	public void testEnabledWithConsoleAndNoColorUnset()
	{
		assertTrue( NotamColor.isEnabled( null, true ) );
	}

	@Test
	public void testEnabledWhenNoColorIsEmpty()
	{
		assertTrue( NotamColor.isEnabled( "", true ) );
	}

	@Test
	public void testDisabledWhenNoColorIsSet()
	{
		assertFalse( NotamColor.isEnabled( "1", true ) );
	}

	@Test
	public void testDisabledWithoutConsole()
	{
		assertFalse( NotamColor.isEnabled( null, false ) );
	}
}
