package textlab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All text-processing logic lives here, with no Scanner and no System.out,
 * so every rule can be tested without touching the console.
 *
 * Definitions (also listed in the README):
 *  - A WORD is a run of letters or digits. Anything else (space, punctuation) separates words.
 *  - A SENTENCE BOUNDARY is one or more consecutive '.', '!' or '?'.
 */
public final class TextProcessor {

    private TextProcessor() {
    }

    // ------------------------------------------------------------- counting

    public static int charCount(String text) {
        return text == null ? 0 : text.length();
    }

    /** Characters that are not whitespace (spaces, tabs and line breaks are all excluded). */
    public static int charCountWithoutWhitespace(String text) {
        if (text == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    public static int wordCount(String text) {
        return words(text).size();
    }

    /**
     * Counts sentences. Walks the text once, splitting at runs of . ! ?
     * A fragment counts as a sentence if it contains at least one letter or digit,
     * so a final fragment with no closing punctuation still counts, but stray punctuation does not.
     */
    public static int sentenceCount(String text) {
        if (text == null) {
            return 0;
        }
        int sentences = 0;
        boolean fragmentHasContent = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isSentenceEnd(c)) {
                if (fragmentHasContent) {
                    sentences++;
                    fragmentHasContent = false;
                }
                // consecutive terminators ("?!", "...") fall through here and change nothing
            } else if (Character.isLetterOrDigit(c)) {
                fragmentHasContent = true;
            }
        }
        if (fragmentHasContent) {
            sentences++; // final fragment with no closing punctuation
        }
        return sentences;
    }

    // ------------------------------------------------------------- cleaning

    /**
     * Removes leading/trailing whitespace and collapses every run of whitespace
     * (spaces, tabs, line breaks) into a single space.
     * Uses a StringBuilder because the result is built up one piece at a time.
     */
    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(text.length());
        boolean pendingSpace = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                pendingSpace = result.length() > 0; // ignore leading whitespace
            } else {
                if (pendingSpace) {
                    result.append(' ');
                    pendingSpace = false;
                }
                result.append(c);
            }
        }
        return result.toString(); // a trailing space is never added, so trailing whitespace is gone
    }

    // ---------------------------------------------------------- word search

    /** How many times the whole word appears, ignoring case. "Java" does not match "JavaScript". */
    public static int wordFrequency(String text, String word) {
        if (word == null || word.isBlank()) {
            return 0;
        }
        String target = word.trim();
        int count = 0;
        for (String w : words(text)) {
            if (w.equalsIgnoreCase(target)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Replaces every whole-word occurrence of {@code target} (ignoring case) with {@code replacement}.
     * Spacing and punctuation between words are kept exactly as they were.
     */
    public static String replaceWord(String text, String target, String replacement) {
        if (text == null) {
            return "";
        }
        if (target == null || target.isBlank()) {
            return text;
        }
        String wanted = target.trim();
        String with = replacement == null ? "" : replacement;

        StringBuilder result = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                int start = i;
                while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) {
                    i++;
                }
                String word = text.substring(start, i);
                result.append(word.equalsIgnoreCase(wanted) ? with : word);
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    // ----------------------------------------------------------- palindromes

    /** Palindrome check that ignores case, spaces and punctuation. Text with no letters/digits is not a palindrome. */
    public static boolean isPalindrome(String text) {
        if (text == null) {
            return false;
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                letters.append(Character.toLowerCase(c));
            }
        }
        if (letters.length() == 0) {
            return false;
        }
        for (int left = 0, right = letters.length() - 1; left < right; left++, right--) {
            if (letters.charAt(left) != letters.charAt(right)) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------- longest words

    /**
     * Every word tied for the greatest length, without duplicates (ignoring case),
     * in the order they first appear. The first spelling seen is the one reported.
     */
    public static List<String> longestWords(String text) {
        int longest = 0;
        Map<String, String> unique = new LinkedHashMap<>(); // lower-case key -> first spelling seen
        for (String w : words(text)) {
            if (w.length() > longest) {
                longest = w.length();
                unique.clear();
            }
            if (w.length() == longest) {
                unique.putIfAbsent(w.toLowerCase(), w);
            }
        }
        return new ArrayList<>(unique.values());
    }

    // ------------------------------------------------------------ stretch goal

    /**
     * Stretch goal: the first position (0-based) where two passages differ,
     * or -1 if they are identical. If one is a prefix of the other, the position is the shorter length.
     */
    public static int firstDifference(String a, String b) {
        String x = a == null ? "" : a;
        String y = b == null ? "" : b;
        int shared = Math.min(x.length(), y.length());
        for (int i = 0; i < shared; i++) {
            if (x.charAt(i) != y.charAt(i)) {
                return i;
            }
        }
        return x.length() == y.length() ? -1 : shared;
    }

    // ---------------------------------------------------------------- helpers

    /** Splits text into words: runs of letters or digits. */
    static List<String> words(String text) {
        List<String> found = new ArrayList<>();
        if (text == null) {
            return found;
        }
        int i = 0;
        while (i < text.length()) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                int start = i;
                while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) {
                    i++;
                }
                found.add(text.substring(start, i));
            } else {
                i++;
            }
        }
        return found;
    }

    private static boolean isSentenceEnd(char c) {
        return c == '.' || c == '!' || c == '?';
    }
}
