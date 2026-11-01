package studyvault;

/** Thrown for blank names, path separators, ".." and anything that could escape the storage folder. */
public class InvalidNoteNameException extends NoteException {
    public InvalidNoteNameException(String message) {
        super(message);
    }
}
