# 03. StudyVault: File-Based Notes

**Focus: file handling and I/O streams.** A notes manager that keeps study notes after the application closes.

## What it does

- Create a note with a title and multi-line content.
- List saved notes and open a selected note.
- Append content to an existing note.
- Search all notes for a keyword and show the matching **file names and line numbers**.
- Delete a note after confirmation.
- Export a note by copying it to a backups folder.
- Restore an exported note **without silently overwriting** an existing note.
- Stretch: show each note's size in bytes, number of lines and last-modified time.

## File rules

| Rule | How it is done |
|------|----------------|
| Notes live in `data/notes`, exported copies in `data/backups`, created if missing | `NoteStore` constructor uses `Files.createDirectories` |
| UTF-8 when reading and writing text | every reader and writer is created with `StandardCharsets.UTF_8` |
| Reject blank names, path separators and names that could escape the folder | `NoteStore.safePath` (see below) |
| Ask before overwriting | `NoteExistsException` is thrown; the menu asks, and only then retries with overwrite allowed |
| Appending preserves existing content | the file is opened with `StandardOpenOption.APPEND` |
| Useful message for missing files or failures | `NoteNotFoundException` and `NoteStorageException`, caught in one place in the menu |

**How names are checked (`safePath`):** the name must not be blank or over 100 characters, must not contain `..` or start with a dot, and must not contain `/ \ < > : " | ? *` or control characters. As a final safety net, the resolved path is normalized and must sit directly inside the notes (or backups) folder. All of this happens **before** any file is touched. A trailing `.txt` typed by the user is ignored, so `Maths` and `Maths.txt` are the same note.

## Implementation notes

- **Text** uses `BufferedReader` and `BufferedWriter` (created through `Files.newBufferedReader/Writer` with UTF-8).
- **Backup copying** uses `FileInputStream` and `FileOutputStream` with an **8 KB byte buffer**. It does not copy one byte at a time and does **not** use `Files.copy()`.
- **Everything uses try-with-resources**, so files are closed even when something fails halfway.
- **File operations are separate from menu logic.** `NoteStore` has no `Scanner` and no printing. `StudyVaultApp` only asks questions and prints.
- Appending to a file that does not end with a newline first adds a line break, so the old last line and the new first line never join together.

## Acceptance checklist

| Check | Input | Expected | Actual |
|-------|-------|----------|--------|
| Create a multi-line note | option `1`, title `Budget`, 2 lines | Saved | `Note 'Budget' saved.` Passed |
| Never silently overwrite | create `Budget` again, answer `n` | Asks first, original kept | `Overwrite it? (y/n): n` then `Cancelled. The existing note was not changed.` Passed |
| Safe appending | option `4` | Old lines kept, new line added at the end | note shows all 3 lines in order. Passed |
| UTF-8 text such as ₦ | lines containing `₦150,000` | Saved and shown correctly | `Rent: ₦150,000` after reading back. Passed |
| Search with line numbers | option `5`, `data` | File name and line number | `Budget.txt, line 2: Data: ₦5,000` Passed |
| Export | option `7` | Copy in the backups folder | `Exported to backups/Budget.txt` Passed |
| Exported contents are identical | (unit test) | Same bytes as the original, including files larger than the buffer | Passed |
| Delete needs confirmation | option `6`, answer `y` | Deleted only after `y` | `Deleted 'Budget'.` Passed |
| Missing file | open `Budget` after deleting | Helpful message, no crash | `Problem: No note called 'Budget' was found.` Passed |
| Restore | option `8` | Note comes back with the same content | all 3 lines restored. Passed |
| Restore does not silently overwrite | (unit and menu tests) | Asks before replacing a note that exists | Passed |
| Invalid filename | title `../secret` | Rejected before touching any file | `Problem: A note name cannot contain '..'...` Passed |
| Persistence after restart | (unit test, new `NoteStore` on the same folder) | Notes are still there | Passed |
| Details (stretch) | option `9` | Size, lines, last modified | `53 bytes`, `3` lines, timestamp. Passed |

The full console session is in [`sample/output.txt`](sample/output.txt) and the typed input in [`sample/input.txt`](sample/input.txt).

## Tests

29 JUnit 5 tests in `src/test/java/studyvault`: `NoteStoreTest` (uses a temporary folder for every test, so no real files are touched) and `StudyVaultAppTest` (the menu, including the confirmation prompts).

## Stretch goal: completed

Menu option `9` reports each note's size in bytes, line count and last-modified time (`NoteStore.info`).

## Known limitations

- Notes are plain `.txt` files named after their title, so two notes cannot share a title.
- Exporting a note again replaces its previous backup (only one backup per note is kept).
- Content is typed in the console and ends with a line containing only `END`.
