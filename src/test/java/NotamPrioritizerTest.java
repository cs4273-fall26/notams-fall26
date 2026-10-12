import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.cs4273.notams.objects.Notam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NotamPrioritizerTest {

    private final NotamPrioritizer prioritizer = new NotamPrioritizer();

    // Test sorting NOTAMs from shortest to longest text
    @Test
    void testSortByTextLength() {
        Notam notam1 = new Notam("1", "Runway closed", "KOKC", "start", "end");
        Notam notam2 = new Notam("2", "Crane", "KTUL", "start", "end");
        Notam notam3 = new Notam("3", "Taxiway closed for construction", "KDFW", "start", "end");

        List<Notam> notams = new ArrayList<>(
            Arrays.asList(notam1, notam2, notam3)
        );

        List<Notam> result = prioritizer.prioritize(notams);

        assertEquals(Arrays.asList(notam2, notam1, notam3), result);
    }

    // Test that an already sorted list stays in order
    @Test
    void testAlreadySortedList() {
        Notam notam1 = new Notam("1", "Crane", "KOKC", "start", "end");
        Notam notam2 = new Notam("2", "Runway closed", "KTUL", "start", "end");

        List<Notam> notams = new ArrayList<>(
            Arrays.asList(notam1, notam2)
        );

        List<Notam> result = prioritizer.prioritize(notams);

        assertEquals(Arrays.asList(notam1, notam2), result);
    }

    // Test that equal-length NOTAMs maintain their original order
    @Test
    void testEqualLengthText() {
        Notam notam1 = new Notam("1", "Crane", "KOKC", "start", "end");
        Notam notam2 = new Notam("2", "Flood", "KTUL", "start", "end");

        List<Notam> notams = new ArrayList<>(
            Arrays.asList(notam1, notam2)
        );

        List<Notam> result = prioritizer.prioritize(notams);

        assertEquals(Arrays.asList(notam1, notam2), result);
    }

    // Test that an empty list stays empty
    @Test
    void testEmptyList() {
        List<Notam> notams = new ArrayList<>();

        List<Notam> result = prioritizer.prioritize(notams);

        assertTrue(result.isEmpty());
    }

    // Test that passing null returns null
    @Test
    void testNullList() {
        List<Notam> result = prioritizer.prioritize(null);

        assertNull(result);
    }
}

