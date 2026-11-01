package studyvault;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudyVaultAppTest {

    @TempDir
    Path base;

    private String runApp(String script) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        java.io.InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            new StudyVaultApp(base).run();
            return bytes.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    @Test
    void createListOpenWorkThroughTheMenu() throws Exception {
        String output = runApp("1\nJava\nStreams\nCollections ₦\nEND\n2\n3\nJava\n0\n");
        assertTrue(output.contains("Note 'Java' saved."));
        assertTrue(output.contains("  - Java"));
        assertTrue(output.contains("Collections ₦"));
    }

    @Test
    void overwriteIsAskedAboutAndDecliningKeepsTheOriginal() throws Exception {
        runApp("1\nPlan\noriginal\nEND\n0\n");
        String output = runApp("1\nPlan\nreplacement\nEND\nn\n0\n");
        assertTrue(output.contains("Overwrite it?"));
        assertTrue(output.contains("Cancelled"));
        assertEquals(List.of("original"), Files.readAllLines(base.resolve("notes").resolve("Plan.txt")));
    }

    @Test
    void acceptingTheOverwriteReplacesTheNote() throws Exception {
        runApp("1\nPlan\noriginal\nEND\n0\n");
        runApp("1\nPlan\nreplacement\nEND\ny\n0\n");
        assertEquals(List.of("replacement"), Files.readAllLines(base.resolve("notes").resolve("Plan.txt")));
    }

    @Test
    void deleteNeedsConfirmation() throws Exception {
        runApp("1\nTemp\nx\nEND\n0\n");
        String declined = runApp("6\nTemp\nn\n0\n");
        assertTrue(declined.contains("Nothing was deleted"));
        assertTrue(Files.exists(base.resolve("notes").resolve("Temp.txt")));

        String confirmed = runApp("6\nTemp\ny\n0\n");
        assertTrue(confirmed.contains("Deleted 'Temp'"));
        assertFalse(Files.exists(base.resolve("notes").resolve("Temp.txt")));
    }

    @Test
    void invalidNamesAndMissingNotesShowFriendlyMessagesAndTheAppKeepsRunning() throws Exception {
        String output = runApp("1\n../evil\n3\nGhost\n99\n0\n");
        assertTrue(output.contains("Problem: A note name cannot contain '..'"));
        assertTrue(output.contains("Problem: No note called 'Ghost' was found."));
        assertTrue(output.contains("Invalid option '99'"));
        assertTrue(output.contains("Goodbye"));
        assertFalse(output.contains("Exception")); // no raw stack traces
    }

    @Test
    void exportThenRestoreAfterDeleteWorksThroughTheMenu() throws Exception {
        runApp("1\nKeep\nimportant ₦100\nEND\n7\nKeep\n6\nKeep\ny\n0\n");
        assertFalse(Files.exists(base.resolve("notes").resolve("Keep.txt")));

        String output = runApp("8\nKeep\n0\n");

        assertTrue(output.contains("Restored 'Keep'"));
        assertEquals(List.of("important ₦100"), Files.readAllLines(base.resolve("notes").resolve("Keep.txt")));
    }

    @Test
    void restoreAsksBeforeReplacingAnExistingNote() throws Exception {
        runApp("1\nDoc\nversion 1\nEND\n7\nDoc\n1\nDoc\nversion 2\nEND\ny\n0\n");
        String output = runApp("8\nDoc\nn\n0\n");
        assertTrue(output.contains("Replace it with the exported copy?"));
        assertEquals(List.of("version 2"), Files.readAllLines(base.resolve("notes").resolve("Doc.txt")));
    }

    @Test
    void searchPrintsFilenameAndLineNumber() throws Exception {
        String output = runApp("1\nAlpha\nfirst\nJava rocks\nEND\n5\njava\n0\n");
        assertTrue(output.contains("Alpha.txt, line 2: Java rocks"));
    }
}
