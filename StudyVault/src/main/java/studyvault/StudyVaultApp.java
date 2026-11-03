package studyvault;

import studyvault.exceptions.NoteException;
import studyvault.exceptions.NoteExistsException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StudyVaultApp {

    private final Scanner scanner;
    private final NoteStore store;

    public StudyVaultApp(Path baseDir) throws NoteException {
        scanner = new Scanner(System.in);
        store = new NoteStore(baseDir);
    }

    public static void main(String[] args) {
        try {
            new StudyVaultApp(Paths.get("data")).run();
        } catch (NoteException error) {
            System.out.println("StudyVault could not start: " + error.getMessage());
        }
    }

    public void run() {
        System.out.println("=== StudyVault: File-Based Notes ===");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = prompt("Choose an option: ");
                try {
                    switch (choice) {
                        case "1" -> createNote();
                        case "2" -> listNotes();
                        case "3" -> openNote();
                        case "4" -> appendToNote();
                        case "5" -> searchNotes();
                        case "6" -> deleteNote();
                        case "7" -> exportNote();
                        case "8" -> restoreNote();
                        case "0" -> {
                            System.out.println("Goodbye! Your notes are saved.");
                            running = false;
                        }
                        default -> System.out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                    }
                } catch (NoteException error) {
                    System.out.println("Problem: " + error.getMessage());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            System.out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Create a note");
        System.out.println("2. List notes");
        System.out.println("3. Open a note");
        System.out.println("4. Append to a note");
        System.out.println("5. Search notes for a keyword");
        System.out.println("6. Delete a note");
        System.out.println("7. Export a note (backup)");
        System.out.println("8. Restore an exported note");
        System.out.println("0. Exit");
    }

    private void createNote() throws NoteException {
        String title = prompt("Note title: ");
        store.notePath(title);
        List<String> lines = readContent();
        try {
            store.create(title, lines, false);
        } catch (NoteExistsException exists) {
            if (!confirm(exists.getMessage() + " Overwrite it? (y/n): ")) {
                System.out.println("Cancelled. The existing note was not changed.");
                return;
            }
            store.create(title, lines, true);
        }
        System.out.println("Note '" + title + "' saved.");
    }

    private void listNotes() throws NoteException {
        List<String> titles = store.list();
        if (titles.isEmpty()) {
            System.out.println("You have no notes yet.");
            return;
        }
        System.out.println("Your notes:");
        for (String title : titles) {
            System.out.println("  - " + title);
        }
    }

    private void openNote() throws NoteException {
        String title = prompt("Note title to open: ");
        List<String> lines = store.read(title);
        System.out.println("--- " + title + " ---");
        for (String line : lines) {
            System.out.println(line);
        }
        System.out.println("--- end ---");
    }

    private void appendToNote() throws NoteException {
        String title = prompt("Note title to append to: ");
        store.read(title);
        store.append(title, readContent());
        System.out.println("Added to '" + title + "'.");
    }

    private void searchNotes() throws NoteException {
        String keyword = prompt("Keyword: ");
        List<SearchHit> hits = store.search(keyword);
        if (hits.isEmpty()) {
            System.out.println("No matches for '" + keyword + "'.");
            return;
        }
        for (SearchHit hit : hits) {
            System.out.println(hit.noteTitle() + ".txt, line " + hit.lineNumber() + ": " + hit.line());
        }
    }

    private void deleteNote() throws NoteException {
        String title = prompt("Note title to delete: ");
        store.read(title);
        if (confirm("Really delete '" + title + "'? This cannot be undone. (y/n): ")) {
            store.delete(title);
            System.out.println("Deleted '" + title + "'.");
        } else {
            System.out.println("Cancelled. Nothing was deleted.");
        }
    }

    private void exportNote() throws NoteException {
        String title = prompt("Note title to export: ");
        Path target = store.export(title);
        System.out.println("Exported to backups/" + target.getFileName());
    }

    private void restoreNote() throws NoteException {
        List<String> backups = store.listBackups();
        if (backups.isEmpty()) {
            System.out.println("There are no exported notes to restore.");
            return;
        }
        System.out.println("Exported notes: " + String.join(", ", backups));
        String title = prompt("Note title to restore: ");
        try {
            store.restore(title, false);
        } catch (NoteExistsException exists) {
            if (!confirm(exists.getMessage() + " Replace it with the exported copy? (y/n): ")) {
                System.out.println("Cancelled. The existing note was not changed.");
                return;
            }
            store.restore(title, true);
        }
        System.out.println("Restored '" + title + "'.");
    }

    private List<String> readContent() {
        System.out.println("Type the content. Finish with a line containing only END.");
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = scanner.nextLine();
            if (line.strip().equals("END")) {
                return lines;
            }
            lines.add(line);
        }
    }

    private boolean confirm(String question) {
        return prompt(question).equalsIgnoreCase("y");
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
