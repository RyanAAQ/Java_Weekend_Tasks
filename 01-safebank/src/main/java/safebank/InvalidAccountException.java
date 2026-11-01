package safebank;

public class InvalidAccountException extends BankException {
    public InvalidAccountException(String message) {
        super(message);
    }
}
