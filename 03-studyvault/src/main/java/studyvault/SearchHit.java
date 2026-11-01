package studyvault;

/** One search match: which note, which line number (starting at 1) and the text of that line. */
public record SearchHit(String noteTitle, int lineNumber, String line) {
}
