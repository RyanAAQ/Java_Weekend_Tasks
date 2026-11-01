package studyvault;

import java.time.Instant;

/** Stretch goal: size in bytes, number of lines and last-modified time for a note. */
public record NoteInfo(String noteTitle, long sizeInBytes, int lineCount, Instant lastModified) {
}
