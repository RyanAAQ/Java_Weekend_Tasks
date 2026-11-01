package textlab;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextProcessorTest {

    @Test
    void cleanCollapsesSpacesTabsAndLineBreaksAndTrims() {
        String messy = "  Hello \t\t world \n\n  from   Java \r\n ";
        assertEquals("Hello world from Java", TextProcessor.clean(messy));
    }

    @Test
    void cleanDoesNotChangeTheOriginalString() {
        String original = "  a   b  ";
        TextProcessor.clean(original);
        assertEquals("  a   b  ", original);
    }

    @Test
    void characterCountsWithAndWithoutWhitespace() {
        String text = "a b\tc\nd";
        assertEquals(7, TextProcessor.charCount(text));
        assertEquals(4, TextProcessor.charCountWithoutWhitespace(text));
    }

    @Test
    void wordCountIgnoresExtraWhitespaceAndPunctuation() {
        assertEquals(4, TextProcessor.wordCount("  Hello,   world!\n\tThis is  "));
    }

    @Test
    void sentencesAreSplitAtRunsOfTerminators() {
        assertEquals(3, TextProcessor.sentenceCount("Hello world. How are you?! Fine..."));
    }

    @Test
    void finalFragmentWithoutPunctuationCountsAsASentence() {
        assertEquals(2, TextProcessor.sentenceCount("First one. And the last one"));
    }

    @Test
    void consecutiveTerminatorsAreOneBoundary() {
        assertEquals(1, TextProcessor.sentenceCount("Really?!?!"));
        assertEquals(1, TextProcessor.sentenceCount("Wait..."));
    }

    @Test
    void searchIgnoresCase() {
        assertEquals(3, TextProcessor.wordFrequency("Java JAVA java", "java"));
    }

    @Test
    void searchForJavaDoesNotMatchJavaScript() {
        String passage = "I like Java and JavaScript, but not JAVASCRIPTS. Java rocks.";
        assertEquals(2, TextProcessor.wordFrequency(passage, "Java"));
        assertEquals(1, TextProcessor.wordFrequency(passage, "JavaScript"));
    }

    @Test
    void aHyphenSeparatesWordsSoJavaInsideJavaDashScriptIsAWholeWord() {
        assertEquals(1, TextProcessor.wordFrequency("java-script", "java"));
    }

    @Test
    void replaceChangesOnlyWholeWordsAndIgnoresCase() {
        String passage = "Java, JAVA and JavaScript.";
        assertEquals("Kotlin, Kotlin and JavaScript.", TextProcessor.replaceWord(passage, "java", "Kotlin"));
    }

    @Test
    void replaceKeepsSpacingAndPunctuation() {
        assertEquals("cat,  dog\tcat!", TextProcessor.replaceWord("cat,  dog\tcat!", "bird", "x"));
        assertEquals("a-X-c", TextProcessor.replaceWord("a-b-c", "b", "X"));
    }

    @Test
    void classicPalindromeWithPunctuationIsRecognised() {
        assertTrue(TextProcessor.isPalindrome("A man, a plan, a canal: Panama!"));
    }

    @Test
    void nonPalindromeIsRejected() {
        assertFalse(TextProcessor.isPalindrome("Hello world"));
    }

    @Test
    void textWithPunctuationButNoWordsIsNotAPalindromeAndDoesNotCrash() {
        assertFalse(TextProcessor.isPalindrome("?!... ,,, ---"));
    }

    @Test
    void longestWordsReturnsAllTiesWithoutDuplicates() {
        List<String> result = TextProcessor.longestWords("The quick brown fox jumps over the lazy QUICK dogs");
        assertEquals(List.of("quick", "brown", "jumps"), result.stream().map(String::toLowerCase).toList());
    }

    @Test
    void longestWordsDeduplicatesIgnoringCase() {
        assertEquals(List.of("Planet"), TextProcessor.longestWords("Planet PLANET planet sun"));
    }

    @Test
    void emptyAndWhitespaceOnlyInputNeverCrash() {
        for (String text : new String[] {"", "   ", "\t\n  ", null}) {
            assertEquals(0, TextProcessor.wordCount(text));
            assertEquals(0, TextProcessor.sentenceCount(text));
            assertEquals(0, TextProcessor.charCountWithoutWhitespace(text));
            assertEquals("", TextProcessor.clean(text));
            assertEquals(0, TextProcessor.wordFrequency(text, "a"));
            assertTrue(TextProcessor.longestWords(text).isEmpty());
            assertFalse(TextProcessor.isPalindrome(text));
        }
    }

    @Test
    void passageWithPunctuationButNoWordsHasZeroWordsAndSentences() {
        String text = "!!! ... ??? --- ,,,";
        assertEquals(0, TextProcessor.wordCount(text));
        assertEquals(0, TextProcessor.sentenceCount(text));
        assertTrue(TextProcessor.longestWords(text).isEmpty());
    }

    @Test
    void blankSearchWordIsHandled() {
        assertEquals(0, TextProcessor.wordFrequency("some text", "  "));
        assertEquals("some text", TextProcessor.replaceWord("some text", "", "x"));
    }

    @Test
    void firstDifferenceFindsTheFirstDifferingPosition() {
        assertEquals(4, TextProcessor.firstDifference("The cat sat", "The dog sat"));
        assertEquals(-1, TextProcessor.firstDifference("same", "same"));
        assertEquals(3, TextProcessor.firstDifference("abc", "abcd"));
    }
}
