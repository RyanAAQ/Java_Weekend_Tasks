package studyvault;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class NoteStore {

    private static final String EXTENSION = ".txt";
    private static final int BUFFER_SIZE = 8192;
    private static final String FORBIDDEN_CHARACTERS = "/\\<>:\"|?*";
    private static final int MAX_NAME_LENGTH = 100;

    private final Path notesDir;
    private final Path backupsDir;

    public NoteStore(Path baseDir) throws NoteException {
        this.notesDir = baseDir.resolve("notes").toAbsolutePath().normalize();
        this.backupsDir = baseDir.resolve("backups").toAbsolutePath().normalize();
        try {
            Files.createDirectories(notesDir);
            Files.createDirectories(backupsDir);
        } catch (IOException e) {
            throw new NoteStorageException("Could not create the storage folders: " + e.getMessage(), e);
        }
    }

    public void create(String title, List<String> lines, boolean overwrite) throws NoteException {
        Path file = notePath(title);
        if (Files.exists(file) && !overwrite) {
            throw new NoteExistsException("A note called '" + cleanTitle(title) + "' already exists.");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writeLines(writer, lines);
        } catch (IOException e) {
            throw new NoteStorageException("Could not save the note: " + e.getMessage(), e);
        }
    }

    public void append(String title, List<String> lines) throws NoteException {
        Path file = existingNote(title);
        try {
            boolean needsNewline = Files.size(file) > 0 && !endsWithNewline(file);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND)) {
                if (needsNewline) {
                    writer.newLine();
                }
                writeLines(writer, lines);
            }
        } catch (IOException e) {
            throw new NoteStorageException("Could not append to the note: " + e.getMessage(), e);
        }
    }

    public List<String> read(String title) throws NoteException {
        Path file = existingNote(title);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return readAllLines(reader);
        } catch (IOException e) {
            throw new NoteStorageException("Could not read the note: " + e.getMessage(), e);
        }
    }

    public List<String> list() throws NoteException {
        return listTitlesIn(notesDir);
    }

    public List<String> listBackups() throws NoteException {
        return listTitlesIn(backupsDir);
    }

    public List<SearchHit> search(String keyword) throws NoteException {
        if (keyword == null || keyword.isBlank()) {
            throw new NoteException("Please enter a keyword to search for.");
        }
        String needle = keyword.trim().toLowerCase();
        List<SearchHit> hits = new ArrayList<>();
        for (String title : list()) {
            try (BufferedReader reader = Files.newBufferedReader(notePath(title), StandardCharsets.UTF_8)) {
                String line;
                int number = 0;
                while ((line = reader.readLine()) != null) {
                    number++;
                    if (line.toLowerCase().contains(needle)) {
                        hits.add(new SearchHit(title, number, line));
                    }
                }
            } catch (IOException e) {
                throw new NoteStorageException("Could not search note '" + title + "': " + e.getMessage(), e);
            }
        }
        return hits;
    }

    public void delete(String title) throws NoteException {
        Path file = existingNote(title);
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new NoteStorageException("Could not delete the note: " + e.getMessage(), e);
        }
    }

    public Path export(String title) throws NoteException {
        Path source = existingNote(title);
        Path target = backupPath(title);
        try {
            copyBytes(source, target);
        } catch (IOException e) {
            throw new NoteStorageException("Could not export the note: " + e.getMessage(), e);
        }
        return target;
    }

    public void restore(String title, boolean overwrite) throws NoteException {
        Path backup = backupPath(title);
        if (!Files.exists(backup)) {
            throw new NoteNotFoundException("There is no exported copy of '" + cleanTitle(title) + "' to restore.");
        }
        Path target = notePath(title);
        if (Files.exists(target) && !overwrite) {
            throw new NoteExistsException("A note called '" + cleanTitle(title) + "' already exists.");
        }
        try {
            copyBytes(backup, target);
        } catch (IOException e) {
            throw new NoteStorageException("Could not restore the note: " + e.getMessage(), e);
        }
    }

    public NoteInfo info(String title) throws NoteException {
        Path file = existingNote(title);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            return new NoteInfo(cleanTitle(title), Files.size(file), lines,
                    Files.getLastModifiedTime(file).toInstant());
        } catch (IOException e) {
            throw new NoteStorageException("Could not read note details: " + e.getMessage(), e);
        }
    }

    Path notePath(String title) throws InvalidNoteNameException {
        return safePath(notesDir, title);
    }

    private Path backupPath(String title) throws InvalidNoteNameException {
        return safePath(backupsDir, title);
    }

    private Path safePath(Path folder, String title) throws InvalidNoteNameException {
        String name = cleanTitle(title);
        if (name.isEmpty()) {
            throw new InvalidNoteNameException("A note name cannot be blank.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidNoteNameException("A note name cannot be longer than " + MAX_NAME_LENGTH + " characters.");
        }
        if (name.contains("..") || name.startsWith(".")) {
            throw new InvalidNoteNameException("A note name cannot contain '..' or start with a dot.");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (FORBIDDEN_CHARACTERS.indexOf(c) >= 0 || Character.isISOControl(c)) {
                throw new InvalidNoteNameException(
                        "A note name cannot contain slashes or any of these characters: < > : \" | ? *");
            }
        }
        Path resolved = folder.resolve(name + EXTENSION).normalize();
        if (!folder.equals(resolved.getParent())) {
            throw new InvalidNoteNameException("That note name is not allowed.");
        }
        return resolved;
    }

    private static String cleanTitle(String title) {
        String name = title == null ? "" : title.trim();
        if (name.toLowerCase().endsWith(EXTENSION)) {
            name = name.substring(0, name.length() - EXTENSION.length()).trim();
        }
        return name;
    }

    private Path existingNote(String title) throws NoteException {
        Path file = notePath(title);
        if (!Files.isRegularFile(file)) {
            throw new NoteNotFoundException("No note called '" + cleanTitle(title) + "' was found.");
        }
        return file;
    }

    private static List<String> listTitlesIn(Path folder) throws NoteException {
        try (Stream<Path> files = Files.list(folder)) {
            List<String> titles = new ArrayList<>();
            files.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.toLowerCase().endsWith(EXTENSION))
                    .forEach(n -> titles.add(n.substring(0, n.length() - EXTENSION.length())));
            Collections.sort(titles, String.CASE_INSENSITIVE_ORDER);
            return titles;
        } catch (NoSuchFileException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new NoteStorageException("Could not list notes: " + e.getMessage(), e);
        }
    }

    private static void writeLines(BufferedWriter writer, List<String> lines) throws IOException {
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
    }

    private static List<String> readAllLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private static boolean endsWithNewline(Path file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            raf.seek(raf.length() - 1);
            int last = raf.read();
            return last == '\n' || last == '\r';
        }
    }

    static void copyBytes(Path source, Path target) throws IOException {
        try (InputStream in = new FileInputStream(source.toFile());
             OutputStream out = new FileOutputStream(target.toFile())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
