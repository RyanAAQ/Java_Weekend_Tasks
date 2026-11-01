package studyvault;

import java.time.Instant;

public record NoteInfo(String noteTitle, long sizeInBytes, int lineCount, Instant lastModified) {
}
