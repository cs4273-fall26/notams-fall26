import java.util.List;

public class NotamPrioritizer {

    // Takes a list of NOTAMs and sorts them by the length of their text
    public List<Notam> prioritize(List<Notam> notams) {

        // Compare the text length of two NOTAMs and sort shortest to longest
        notams.sort((notam1, notam2) ->
            Integer.compare(
                notam1.getNotamText().length(),
                notam2.getNotamText().length()
            )
        );

        // Return the sorted list of NOTAMs
        return notams;
    }
}