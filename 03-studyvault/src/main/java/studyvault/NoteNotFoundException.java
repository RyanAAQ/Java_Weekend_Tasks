package studyvault;

/** Thrown when a note (or an exported backup) does not exist. */
public class NoteNotFoundException extends NoteException {
    public NoteNotFoundException(String message) {
        super(message);
    }
}
