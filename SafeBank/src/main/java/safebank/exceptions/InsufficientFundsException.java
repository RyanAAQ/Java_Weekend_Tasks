package safebank.exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends BankException {
    private final BigDecimal shortfall;

    public InsufficientFundsException(String message, BigDecimal shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public BigDecimal getShortfall() {
        return shortfall;
    }
}
