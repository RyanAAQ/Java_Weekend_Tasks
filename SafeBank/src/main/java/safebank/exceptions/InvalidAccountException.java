package safebank.exceptions;

public class InvalidAccountException extends BankException {
    public InvalidAccountException(String message) {
        super(message);
    }
}
