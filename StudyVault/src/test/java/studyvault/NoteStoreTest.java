package studyvault;

import studyvault.exceptions.InvalidNoteNameException;
import studyvault.exceptions.NoteException;
import studyvault.exceptions.NoteExistsException;
import studyvault.exceptions.NoteNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NoteStoreTest {

    @TempDir
    Path base;

    private NoteStore store;

    @BeforeEach
    void setUp() throws Exception {
        store = new NoteStore(base);
    }

    @Test
    void foldersAreCreatedIfAbsent() throws Exception {
        Path fresh = base.resolve("does").resolve("not").resolve("exist");
        new NoteStore(fresh);
        assertTrue(Files.isDirectory(fresh.resolve("notes")));
        assertTrue(Files.isDirectory(fresh.resolve("backups")));
    }

    @Test
    void notesSurviveARestart() throws Exception {
        store.create("Java", List.of("Streams are fun", "Collections too"), false);

        NoteStore afterRestart = new NoteStore(base);
        assertEquals(List.of("Java"), afterRestart.list());
        assertEquals(List.of("Streams are fun", "Collections too"), afterRestart.read("Java"));
    }

    @Test
    void appendingKeepsExistingContent() throws Exception {
        store.create("Todo", List.of("line one", "line two"), false);
        store.append("Todo", List.of("line three"));
        assertEquals(List.of("line one", "line two", "line three"), store.read("Todo"));
    }

    @Test
    void appendingToAFileWithoutATrailingNewlineDoesNotGlueLinesTogether() throws Exception {
        Files.writeString(base.resolve("notes").resolve("Raw.txt"), "no newline at end", StandardCharsets.UTF_8);
        store.append("Raw", List.of("added"));
        assertEquals(List.of("no newline at end", "added"), store.read("Raw"));
    }

    @Test
    void appendingToAMissingNoteFailsClearlyAndCreatesNothing() throws Exception {
        assertThrows(NoteNotFoundException.class, () -> store.append("Ghost", List.of("x")));
        assertTrue(store.list().isEmpty());
    }

    @Test
    void utf8TextSuchAsTheNairaSignSurvivesSavingAndReading() throws Exception {
        List<String> lines = List.of("Budget: ₦5,000", "Café – résumé – 日本語");
        store.create("Budget", lines, false);
        assertEquals(lines, store.read("Budget"));
        String raw = Files.readString(base.resolve("notes").resolve("Budget.txt"), StandardCharsets.UTF_8);
        assertTrue(raw.contains("₦5,000"));
    }

    @Test
    void creatingOverAnExistingNoteIsRefusedAndTheOriginalIsKept() throws Exception {
        store.create("Plan", List.of("original"), false);
        assertThrows(NoteExistsException.class, () -> store.create("Plan", List.of("replacement"), false));
        assertEquals(List.of("original"), store.read("Plan"));
    }

    @Test
    void overwriteOnlyHappensWhenExplicitlyAllowed() throws Exception {
        store.create("Plan", List.of("original"), false);
        store.create("Plan", List.of("replacement"), true);
        assertEquals(List.of("replacement"), store.read("Plan"));
    }

    @Test
    void invalidNamesAreRejectedBeforeAnyFileIsTouched() throws Exception {
        String[] bad = {"", "   ", null, "../secret", "..", "a/b", "a\\b", "C:evil", "name?", ".hidden", "x..y"};
        for (String name : bad) {
            assertThrows(InvalidNoteNameException.class, () -> store.create(name, List.of("x"), true),
                    "should reject: " + name);
        }
        assertTrue(store.list().isEmpty());
        try (var files = Files.list(base)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void aNameCannotEscapeTheStorageFolder() throws Exception {
        assertThrows(InvalidNoteNameException.class, () -> store.read("../../etc/passwd"));
        assertThrows(InvalidNoteNameException.class, () -> store.delete("../backups/x"));
        assertThrows(InvalidNoteNameException.class, () -> store.export("..\\notes\\x"));
    }

    @Test
    void typingTheTxtExtensionRefersToTheSameNote() throws Exception {
        store.create("Maths.txt", List.of("algebra"), false);
        assertEquals(List.of("algebra"), store.read("Maths"));
        assertEquals(List.of("Maths"), store.list());
    }

    @Test
    void missingNotesGiveClearExceptionsNotCrashes() {
        assertThrows(NoteNotFoundException.class, () -> store.read("Nope"));
        assertThrows(NoteNotFoundException.class, () -> store.delete("Nope"));
        assertThrows(NoteNotFoundException.class, () -> store.export("Nope"));
        assertThrows(NoteNotFoundException.class, () -> store.restore("Nope", false));
        assertThrows(NoteNotFoundException.class, () -> store.info("Nope"));
    }

    @Test
    void searchReportsFilenameAndLineNumberIgnoringCase() throws Exception {
        store.create("Alpha", List.of("first", "Java is great", "third"), false);
        store.create("Beta", List.of("nothing here", "more JAVA"), false);

        List<SearchHit> hits = store.search("java");

        assertEquals(2, hits.size());
        assertEquals(new SearchHit("Alpha", 2, "Java is great"), hits.get(0));
        assertEquals(new SearchHit("Beta", 2, "more JAVA"), hits.get(1));
    }

    @Test
    void searchWithNoMatchesOrBlankKeywordIsHandled() throws Exception {
        store.create("Alpha", List.of("hello"), false);
        assertTrue(store.search("zzz").isEmpty());
        assertThrows(NoteException.class, () -> store.search("  "));
    }

    @Test
    void deleteRemovesTheNote() throws Exception {
        store.create("Temp", List.of("x"), false);
        store.delete("Temp");
        assertTrue(store.list().isEmpty());
    }

    @Test
    void exportedCopyHasIdenticalBytes() throws Exception {
        store.create("Budget", List.of("Budget: ₦5,000", "second line"), false);
        Path backup = store.export("Budget");
        assertArrayEquals(Files.readAllBytes(base.resolve("notes").resolve("Budget.txt")),
                Files.readAllBytes(backup));
    }

    @Test
    void largeFilesBiggerThanTheBufferAreCopiedExactly() throws Exception {
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < 5000; index++) {
            lines.add("Line " + index + " with naira ₦" + index);
        }
        store.create("Big", lines, false);
        Path backup = store.export("Big");
        assertTrue(Files.size(backup) > 8192);
        assertArrayEquals(Files.readAllBytes(base.resolve("notes").resolve("Big.txt")), Files.readAllBytes(backup));
    }

    @Test
    void restoreBringsBackADeletedNote() throws Exception {
        store.create("Keep", List.of("important"), false);
        store.export("Keep");
        store.delete("Keep");

        store.restore("Keep", false);

        assertEquals(List.of("important"), store.read("Keep"));
    }

    @Test
    void restoreDoesNotSilentlyOverwriteAnExistingNote() throws Exception {
        store.create("Doc", List.of("version 1"), false);
        store.export("Doc");
        store.create("Doc", List.of("version 2"), true);

        assertThrows(NoteExistsException.class, () -> store.restore("Doc", false));
        assertEquals(List.of("version 2"), store.read("Doc"));

        store.restore("Doc", true);
        assertEquals(List.of("version 1"), store.read("Doc"));
    }

    @Test
    void listBackupsShowsExportedNotes() throws Exception {
        store.create("B", List.of("b"), false);
        store.create("A", List.of("a"), false);
        store.export("B");
        store.export("A");
        assertEquals(List.of("A", "B"), store.listBackups());
    }

    @Test
    void infoReportsSizeLinesAndLastModified() throws Exception {
        store.create("Stats", List.of("abc", "₦"), false);
        NoteInfo info = store.info("Stats");
        assertEquals(8, info.sizeInBytes());
        assertEquals(2, info.lineCount());
        assertNotNull(info.lastModified());
    }
}
