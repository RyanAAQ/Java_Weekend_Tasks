package textlab;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console menu for TextLab. It only reads input and prints results;
 * every calculation is delegated to {@link TextProcessor}.
 *
 * The original passage is kept in a separate field and never changed, so it can always be compared
 * with the edited version.
 */
public class TextLabApp {

    private final Scanner in;
    private final PrintStream out;

    private String original = "";
    private String current = "";

    public TextLabApp(InputStream input, PrintStream output) {
        this.in = new Scanner(input, StandardCharsets.UTF_8);
        this.out = output;
    }

    public static void main(String[] args) {
        new TextLabApp(System.in, new PrintStream(System.out, true, StandardCharsets.UTF_8)).run();
    }

    public void run() {
        out.println("=== TextLab: Text Analysis & Cleanup ===");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = prompt("Choose an option: ");
                switch (choice) {
                    case "1" -> enterPassage();
                    case "2" -> showStatistics();
                    case "3" -> cleanPassage();
                    case "4" -> searchWord();
                    case "5" -> replaceWord();
                    case "6" -> checkPalindrome();
                    case "7" -> showLongestWords();
                    case "8" -> showPassages();
                    case "9" -> comparePassages();
                    case "0" -> {
                        out.println("Goodbye!");
                        running = false;
                    }
                    default -> out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        out.println();
        out.println("1. Enter a new passage");
        out.println("2. Show statistics");
        out.println("3. Clean whitespace");
        out.println("4. Search for a word");
        out.println("5. Replace a word");
        out.println("6. Check if text is a palindrome");
        out.println("7. Show longest words");
        out.println("8. Show original and current passage");
        out.println("9. Find first difference (original vs current)");
        out.println("0. Exit");
    }

    // ---------------------------------------------------------------- actions

    private void enterPassage() {
        out.println("Type or paste your passage. Finish with a line containing only END.");
        StringBuilder text = new StringBuilder();
        while (true) {
            String line = in.nextLine();
            if (line.strip().equals("END")) {
                break;
            }
            text.append(line).append('\n');
        }
        original = text.toString();
        current = original;
        out.println("Passage saved (" + TextProcessor.charCount(original) + " characters).");
    }

    private void showStatistics() {
        out.println("Characters (with whitespace):    " + TextProcessor.charCount(current));
        out.println("Characters (without whitespace): " + TextProcessor.charCountWithoutWhitespace(current));
        out.println("Words:                           " + TextProcessor.wordCount(current));
        out.println("Sentences:                       " + TextProcessor.sentenceCount(current));
    }

    private void cleanPassage() {
        current = TextProcessor.clean(current);
        out.println("Whitespace cleaned. Current passage: \"" + current + "\"");
    }

    private void searchWord() {
        String word = prompt("Word to search for: ");
        if (word.isEmpty()) {
            out.println("Please type a word to search for.");
            return;
        }
        int count = TextProcessor.wordFrequency(current, word);
        out.println("\"" + word + "\" appears " + count + (count == 1 ? " time" : " times") + " (whole word, ignoring case).");
    }

    private void replaceWord() {
        String target = prompt("Word to replace: ");
        if (target.isEmpty()) {
            out.println("Please type the word to replace.");
            return;
        }
        String replacement = prompt("Replace with: ");
        int found = TextProcessor.wordFrequency(current, target);
        current = TextProcessor.replaceWord(current, target, replacement);
        out.println("Replaced " + found + (found == 1 ? " occurrence." : " occurrences."));
    }

    private void checkPalindrome() {
        String text = prompt("Text to check: ");
        if (TextProcessor.isPalindrome(text)) {
            out.println("Yes, it is a palindrome (ignoring case, spaces and punctuation).");
        } else {
            out.println("No, it is not a palindrome (or it contains no letters or digits).");
        }
    }

    private void showLongestWords() {
        List<String> longest = TextProcessor.longestWords(current);
        if (longest.isEmpty()) {
            out.println("There are no words in the passage yet.");
        } else {
            out.println("Longest word(s) (" + longest.get(0).length() + " characters): " + String.join(", ", longest));
        }
    }

    private void showPassages() {
        out.println("Original: \"" + original.stripTrailing() + "\"");
        out.println("Current:  \"" + current.stripTrailing() + "\"");
    }

    private void comparePassages() {
        int position = TextProcessor.firstDifference(original, current);
        if (position == -1) {
            out.println("The original and current passages are identical.");
        } else {
            out.println("They first differ at position " + position + " (counting from 0).");
        }
    }

    private String prompt(String message) {
        out.print(message);
        return in.nextLine().trim();
    }
}
