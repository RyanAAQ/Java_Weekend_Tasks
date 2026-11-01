package textlab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class TextProcessor {

    private TextProcessor() {
    }

    public static int charCount(String text) {
        return text == null ? 0 : text.length();
    }


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
            } else if (Character.isLetterOrDigit(c)) {
                fragmentHasContent = true;
            }
        }
        if (fragmentHasContent) {
            sentences++;
        }
        return sentences;
    }


    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(text.length());
        boolean pendingSpace = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                pendingSpace = result.length() > 0;
            } else {
                if (pendingSpace) {
                    result.append(' ');
                    pendingSpace = false;
                }
                result.append(c);
            }
        }
        return result.toString();
    }


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


    public static List<String> longestWords(String text) {
        int longest = 0;
        Map<String, String> unique = new LinkedHashMap<>();
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
