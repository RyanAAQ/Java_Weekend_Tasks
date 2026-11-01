package cohorthub;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CohortHubAppTest {

    private String runApp(String script) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        java.io.InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            new CohortHubApp().run();
            return bytes.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    @Test
    void duplicateRegistrationShowsAFriendlyMessageAndTheAppKeepsRunning() {
        String output = runApp("1\nL1\nAda\nada@mail.com\n1\nL1\nBola\nbola@mail.com\n1\nL2\nBola\nADA@mail.com\n5\n0\n");
        assertTrue(output.contains("Problem: A learner with ID 'L1' is already registered."));
        assertTrue(output.contains("Problem: The email 'ADA@mail.com' is already registered."));
        assertTrue(output.contains("Goodbye"));
    }

    @Test
    void attendingAnEmptyQueueGivesAHelpfulMessage() {
        assertTrue(runApp("10\n0\n").contains("No learners are waiting for support right now."));
    }

    @Test
    void groupsQueueAndReportWorkTogetherThroughTheMenu() {
        String script = "1\nL1\nAda\nada@mail.com\n"
                + "1\nL2\nBola\nbola@mail.com\n"
                + "6\nJava\n"
                + "7\nJava\nL1\n"
                + "7\nJava\nL1\n"
                + "9\nL2\n9\nL1\n10\n"
                + "11\n0\n";
        String output = runApp(script);
        assertTrue(output.contains("already in 'Java'"));
        assertTrue(output.contains("Now attending to: Bola (L2)"));
        assertTrue(output.contains("Total learners:         2"));
        assertTrue(output.contains("Java: 1 member(s)"));
        assertTrue(output.contains("Waiting for support:    1"));
    }

    @Test
    void invalidMenuChoiceAndMissingLearnersAreHandled() {
        String output = runApp("99\n2\nGHOST\n0\n");
        assertTrue(output.contains("Invalid option '99'"));
        assertTrue(output.contains("Problem: No learner is registered with ID 'GHOST'."));
    }
}
