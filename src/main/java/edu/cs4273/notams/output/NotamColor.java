package edu.cs4273.notams.output;

final class NotamColor
{
	static final String RED = "\u001B[38;5;196m";
	static final String ORANGE = "\u001B[38;5;208m";
	static final String YELLOW = "\u001B[38;5;226m";
	static final String RESET = "\u001B[0m";

	private NotamColor()
	{
	}

	static String apply( final String text, final int priority )
	{
		return switch( priority ) {
			case 1 -> RED + text + RESET;
			case 2 -> ORANGE + text + RESET;
			case 3 -> YELLOW + text + RESET;
			default -> text;
		};
	}

	static boolean isEnabled( final String noColor, final boolean hasConsole )
	{
		return hasConsole && ( noColor == null || noColor.isEmpty() );
	}
}
