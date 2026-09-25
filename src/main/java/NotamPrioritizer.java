import java.util.List;

public class NotamPrioritizer {

    /**
     * Takes a list of NOTAMs and sorts them by the length of their text
     * 
     *  This is a dummy prioritization algorithm intended as a
     * placeholder until the final prioritization logic is implemented.
     * 
     * @param notams the list of NOTAMs to be prioritized
     * @return the list of NOTAMs sorted by text length
     */ 
    public List<Notam> prioritize(List<Notam> notams) {

        if (notams == null || notams.isEmpty()) {
            return notams;
        }

        notams.sort((notam1, notam2) ->
            Integer.compare(
                notam1.getNotamText().length(),
                notam2.getNotamText().length()
            )
        );

      
        return notams;
    }
}