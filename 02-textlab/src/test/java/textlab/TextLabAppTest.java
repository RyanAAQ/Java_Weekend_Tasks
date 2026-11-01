package textlab;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class TextLabAppTest {

    private String runApp(String script) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new TextLabApp(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), out).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void statisticsOnAnEmptyPassageDoNotCrash() {
        String output = runApp("2\n7\n0\n");
        assertTrue(output.contains("Words:                           0"));
        assertTrue(output.contains("There are no words in the passage yet."));
    }

    @Test
    void originalIsPreservedWhileTheCurrentPassageIsEdited() {
        String script = "1\nI like Java   and JavaScript\nEND\n3\n5\njava\nKotlin\n8\n9\n0\n";
        String output = runApp(script);
        assertTrue(output.contains("Original: \"I like Java   and JavaScript\""));
        assertTrue(output.contains("Current:  \"I like Kotlin and JavaScript\""));
        assertTrue(output.contains("first differ at position"));
    }

    @Test
    void invalidMenuChoiceGivesAHelpfulMessage() {
        assertTrue(runApp("abc\n0\n").contains("Invalid option 'abc'"));
    }

    @Test
    void palindromeCheckWorksThroughTheMenu() {
        String output = runApp("6\nA man, a plan, a canal: Panama!\n0\n");
        assertTrue(output.contains("Yes, it is a palindrome"));
    }

    @Test
    void searchShowsWholeWordFrequency() {
        String output = runApp("1\nJava JAVA JavaScript\nEND\n4\njava\n0\n");
        assertTrue(output.contains("\"java\" appears 2 times"));
    }
}
