package textlab;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TextLabApp {

    private final Scanner in;

    private String original = "";
    private String current = "";

    public TextLabApp() {
        in = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new TextLabApp().run();
    }

    public void run() {
        System.out.println("=== TextLab: Text Analysis & Cleanup ===");
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
                    case "0" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                }
            }
        } catch (NoSuchElementException endOfInput) {
            System.out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Enter a new passage");
        System.out.println("2. Show statistics");
        System.out.println("3. Clean whitespace");
        System.out.println("4. Search for a word");
        System.out.println("5. Replace a word");
        System.out.println("6. Check if text is a palindrome");
        System.out.println("7. Show longest words");
        System.out.println("8. Show original and current passage");
        System.out.println("0. Exit");
    }

    private void enterPassage() {
        System.out.println("Type or paste your passage. Finish with a line containing only END.");
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
        System.out.println("Passage saved (" + TextProcessor.charCount(original) + " characters).");
    }

    private void showStatistics() {
        System.out.println("Characters (with whitespace):    " + TextProcessor.charCount(current));
        System.out.println("Characters (without whitespace): " + TextProcessor.charCountWithoutWhitespace(current));
        System.out.println("Words:                           " + TextProcessor.wordCount(current));
        System.out.println("Sentences:                       " + TextProcessor.sentenceCount(current));
    }

    private void cleanPassage() {
        current = TextProcessor.clean(current);
        System.out.println("Whitespace cleaned. Current passage: \"" + current + "\"");
    }

    private void searchWord() {
        String word = prompt("Word to search for: ");
        if (word.isEmpty()) {
            System.out.println("Please type a word to search for.");
            return;
        }
        int count = TextProcessor.wordFrequency(current, word);
        System.out.println("\"" + word + "\" appears " + count + (count == 1 ? " time" : " times") + " (whole word, ignoring case).");
    }

    private void replaceWord() {
        String target = prompt("Word to replace: ");
        if (target.isEmpty()) {
            System.out.println("Please type the word to replace.");
            return;
        }
        String replacement = prompt("Replace with: ");
        int found = TextProcessor.wordFrequency(current, target);
        current = TextProcessor.replaceWord(current, target, replacement);
        System.out.println("Replaced " + found + (found == 1 ? " occurrence." : " occurrences."));
    }

    private void checkPalindrome() {
        String text = prompt("Text to check: ");
        if (TextProcessor.isPalindrome(text)) {
            System.out.println("Yes, it is a palindrome (ignoring case, spaces and punctuation).");
        } else {
            System.out.println("No, it is not a palindrome (or it contains no letters or digits).");
        }
    }

    private void showLongestWords() {
        List<String> longest = TextProcessor.longestWords(current);
        if (longest.isEmpty()) {
            System.out.println("There are no words in the passage yet.");
        } else {
            System.out.println("Longest word(s) (" + longest.get(0).length() + " characters): " + String.join(", ", longest));
        }
    }

    private void showPassages() {
        System.out.println("Original: \"" + original.stripTrailing() + "\"");
        System.out.println("Current:  \"" + current.stripTrailing() + "\"");
    }

    private String prompt(String message) {
        System.out.print(message);
        return in.nextLine().trim();
    }
}
