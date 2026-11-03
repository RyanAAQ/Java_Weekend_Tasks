package safebank.exceptions;

public class InvalidAmountException extends BankException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
