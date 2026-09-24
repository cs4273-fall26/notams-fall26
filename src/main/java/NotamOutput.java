import java.util.List;

public class NotamOutput {

    public void render(List<Notam> notams) {
        System.out.println("Prioritized NOTAMs:\n");

        int index = 1;
        for (Notam n : notams) {

            // Print the NOTAM index and ID
            System.out.println(index + ". " + n.getNotamId());

            // Print the NOTAM Fields
            System.out.println("Description: " + n.getNotamText());
            System.out.println("Location: " + n.getLocation());
            System.out.println("Start: " + n.getStartTime());
            System.out.println("End: " + n.getEndTime());
            System.out.println();

            index++;
        }
    }
}
