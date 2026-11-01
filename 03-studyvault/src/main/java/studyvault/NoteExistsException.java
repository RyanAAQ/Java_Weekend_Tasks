package studyvault;

/**
 * Thrown instead of silently overwriting. The menu catches this, asks the user,
 * and only then repeats the operation with overwrite allowed.
 */
public class NoteExistsException extends NoteException {
    public NoteExistsException(String message) {
        super(message);
    }
}
