package cohorthub;

/** Base type for every rule violation in CohortHub (duplicate learner, unknown group, and so on). */
public class CohortException extends Exception {
    public CohortException(String message) {
        super(message);
    }
}
