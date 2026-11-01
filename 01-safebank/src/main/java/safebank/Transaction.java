package safebank;

import java.math.BigDecimal;

public record Transaction(Type type, BigDecimal amount, BigDecimal balanceAfter) {

    public enum Type {
        OPENING_BALANCE, DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }

    @Override
    public String toString() {
        return String.format("%-15s %,15.2f   balance: %,.2f", type, amount, balanceAfter);
    }
}
