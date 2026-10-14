import java.util.List;

import edu.cs4273.notams.objects.Notam;

public interface NotamPrioritizer
{
	List<Notam> prioritize( List<Notam> notams );
}
