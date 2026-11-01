package studyvault;

/**
 * Base type for everything that can go wrong with a note.
 * Each subclass lets the menu show a precise, friendly message instead of a raw stack trace.
 */
public class NoteException extends Exception {
    public NoteException(String message) {
        super(message);
    }

    public NoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
