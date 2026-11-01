package studyvault;

/** Wraps an underlying IOException (disk full, permission denied...) in a message the user can read. */
public class NoteStorageException extends NoteException {
    public NoteStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
