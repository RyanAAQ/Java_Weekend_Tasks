# StudyVault

StudyVault is a console notes app that stores notes as text files, so they’re still there after the app closes. You can create, list, open, append to, search, delete, export, and restore notes. The details option shows a note’s size, line count, and last-modified time.

## Where files go

Notes are stored in `data/notes` and exported copies in `data/backups`, relative to the directory where you start the app. The folders are created when needed. Text is read and written as UTF-8, and file streams are closed automatically even if an operation fails.

Note names can’t be blank, longer than 100 characters, start with a dot, contain `..`, path separators, control characters, or characters that could cause problems in a filename. The app checks the resolved path before touching a file to make sure it stays inside the notes or backups folder. Typing `.txt` at the end is optional; `Maths` and `Maths.txt` refer to the same note.

Restoring a note won’t replace an existing one without asking. Appending to a file that doesn’t end in a newline adds one first, so the existing last line doesn’t run into the new text. Backups are copied with an 8 KB buffer.

## Sample and tests

The sample input and output are in [`sample/input.txt`](sample/input.txt) and [`sample/output.txt`](sample/output.txt). There are 29 JUnit 5 tests under `src/test/java/studyvault`. The storage tests use temporary folders; the menu tests cover the prompts and confirmations.

## Limits

Notes are plain `.txt` files, so titles must be unique. Only one backup is kept for each note; exporting again replaces the previous backup. Note content is entered in the console and ends with a line containing only `END`.
