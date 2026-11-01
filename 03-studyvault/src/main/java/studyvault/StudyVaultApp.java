package studyvault;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console menu for StudyVault. It only handles typing and printing and asks the questions
 * (overwrite? delete?). All file work is done by {@link NoteStore}.
 */
public class StudyVaultApp {

    private final Scanner in;
    private final PrintStream out;
    private final NoteStore store;

    public StudyVaultApp(Path baseDir, InputStream input, PrintStream output) throws NoteException {
        this.in = new Scanner(input, StandardCharsets.UTF_8);
        this.out = output;
        this.store = new NoteStore(baseDir);
    }

    public static void main(String[] args) {
        PrintStream utf8 = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        try {
            new StudyVaultApp(Paths.get("data"), System.in, utf8).run();
        } catch (NoteException e) {
            utf8.println("StudyVault could not start: " + e.getMessage());
        }
    }

    public void run() {
        out.println("=== StudyVault: File-Based Notes ===");
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
                        case "9" -> showInfo();
                        case "0" -> {
                            out.println("Goodbye! Your notes are saved.");
                            running = false;
                        }
                        default -> out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                    }
                } catch (NoteException e) {
                    out.println("Problem: " + e.getMessage()); // one friendly message, never a stack trace
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        out.println();
        out.println("1. Create a note");
        out.println("2. List notes");
        out.println("3. Open a note");
        out.println("4. Append to a note");
        out.println("5. Search notes for a keyword");
        out.println("6. Delete a note");
        out.println("7. Export a note (backup)");
        out.println("8. Restore an exported note");
        out.println("9. Note details (size, lines, last modified)");
        out.println("0. Exit");
    }

    // ---------------------------------------------------------------- actions

    private void createNote() throws NoteException {
        String title = prompt("Note title: ");
        store.notePath(title); // fail fast on a bad name before asking the user to type everything
        List<String> lines = readContent();
        try {
            store.create(title, lines, false);
        } catch (NoteExistsException exists) {
            if (!confirm(exists.getMessage() + " Overwrite it? (y/n): ")) {
                out.println("Cancelled. The existing note was not changed.");
                return;
            }
            store.create(title, lines, true);
        }
        out.println("Note '" + title + "' saved.");
    }

    private void listNotes() throws NoteException {
        List<String> titles = store.list();
        if (titles.isEmpty()) {
            out.println("You have no notes yet.");
            return;
        }
        out.println("Your notes:");
        for (String title : titles) {
            out.println("  - " + title);
        }
    }

    private void openNote() throws NoteException {
        String title = prompt("Note title to open: ");
        List<String> lines = store.read(title);
        out.println("--- " + title + " ---");
        for (String line : lines) {
            out.println(line);
        }
        out.println("--- end ---");
    }

    private void appendToNote() throws NoteException {
        String title = prompt("Note title to append to: ");
        store.read(title); // confirms the note exists before asking for content
        store.append(title, readContent());
        out.println("Added to '" + title + "'.");
    }

    private void searchNotes() throws NoteException {
        String keyword = prompt("Keyword: ");
        List<SearchHit> hits = store.search(keyword);
        if (hits.isEmpty()) {
            out.println("No matches for '" + keyword + "'.");
            return;
        }
        for (SearchHit hit : hits) {
            out.println(hit.noteTitle() + ".txt, line " + hit.lineNumber() + ": " + hit.line());
        }
    }

    private void deleteNote() throws NoteException {
        String title = prompt("Note title to delete: ");
        store.read(title); // confirms the note exists before asking
        if (confirm("Really delete '" + title + "'? This cannot be undone. (y/n): ")) {
            store.delete(title);
            out.println("Deleted '" + title + "'.");
        } else {
            out.println("Cancelled. Nothing was deleted.");
        }
    }

    private void exportNote() throws NoteException {
        String title = prompt("Note title to export: ");
        Path target = store.export(title);
        out.println("Exported to backups/" + target.getFileName());
    }

    private void restoreNote() throws NoteException {
        List<String> backups = store.listBackups();
        if (backups.isEmpty()) {
            out.println("There are no exported notes to restore.");
            return;
        }
        out.println("Exported notes: " + String.join(", ", backups));
        String title = prompt("Note title to restore: ");
        try {
            store.restore(title, false);
        } catch (NoteExistsException exists) {
            if (!confirm(exists.getMessage() + " Replace it with the exported copy? (y/n): ")) {
                out.println("Cancelled. The existing note was not changed.");
                return;
            }
            store.restore(title, true);
        }
        out.println("Restored '" + title + "'.");
    }

    private void showInfo() throws NoteException {
        NoteInfo info = store.info(prompt("Note title: "));
        out.println("Title:         " + info.noteTitle());
        out.println("Size:          " + info.sizeInBytes() + " bytes");
        out.println("Lines:         " + info.lineCount());
        out.println("Last modified: " + info.lastModified());
    }

    // ---------------------------------------------------------------- helpers

    private List<String> readContent() {
        out.println("Type the content. Finish with a line containing only END.");
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = in.nextLine();
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
        out.print(message);
        return in.nextLine().trim();
    }
}
