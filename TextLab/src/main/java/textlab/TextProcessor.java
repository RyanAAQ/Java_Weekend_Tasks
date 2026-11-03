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
        for (int index = 0; index < text.length(); index++) {
            if (!Character.isWhitespace(text.charAt(index))) {
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
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (isSentenceEnd(character)) {
                if (fragmentHasContent) {
                    sentences++;
                    fragmentHasContent = false;
                }
            } else if (Character.isLetterOrDigit(character)) {
                fragmentHasContent = true;
            }
        }
        if (fragmentHasContent) {
            sentences++;
        }
        return sentences;
    }

    public static String clean(String text) {
        return text == null ? "" : text.strip().replaceAll("\\p{javaWhitespace}+", " ");
    }

    public static int wordFrequency(String text, String word) {
        if (word == null || word.isBlank()) {
            return 0;
        }
        String target = word.trim();
        int count = 0;
        for (String currentWord : words(text)) {
            if (currentWord.equalsIgnoreCase(target)) {
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
        int index = 0;
        while (index < text.length()) {
            if (Character.isLetterOrDigit(text.charAt(index))) {
                int start = index;
                while (index < text.length() && Character.isLetterOrDigit(text.charAt(index))) {
                    index++;
                }
                String word = text.substring(start, index);
                result.append(word.equalsIgnoreCase(wanted) ? with : word);
            } else {
                result.append(text.charAt(index));
                index++;
            }
        }
        return result.toString();
    }

    public static boolean isPalindrome(String text) {
        if (text == null) {
            return false;
        }
        StringBuilder letters = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (Character.isLetterOrDigit(character)) {
                letters.append(Character.toLowerCase(character));
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
        for (String word : words(text)) {
            if (word.length() > longest) {
                longest = word.length();
                unique.clear();
            }
            if (word.length() == longest) {
                unique.putIfAbsent(word.toLowerCase(), word);
            }
        }
        return new ArrayList<>(unique.values());
    }

    public static int firstDifference(String firstText, String secondText) {
        String first = firstText == null ? "" : firstText;
        String second = secondText == null ? "" : secondText;
        int length = Math.min(first.length(), second.length());
        for (int index = 0; index < length; index++) {
            if (first.charAt(index) != second.charAt(index)) {
                return index;
            }
        }
        return first.length() == second.length() ? -1 : length;
    }

    static List<String> words(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        for (String word : text.split("[^\\p{L}\\p{Nd}]+")) {
            if (!word.isEmpty()) {
                result.add(word);
            }
        }
        return result;
    }

    private static boolean isSentenceEnd(char character) {
        return character == '.' || character == '!' || character == '?';
    }
}
