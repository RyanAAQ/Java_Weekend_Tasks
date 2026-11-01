package safebank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A bank account. All business rules live here, so no caller (menu, test, transfer) can bypass them.
 *
 * Money is stored as BigDecimal because double cannot represent amounts like 0.10 exactly.
 * Every method validates FIRST and changes state LAST, so a rejected transaction leaves the
 * balance and history exactly as they were.
 */
public class Account {

    public static final BigDecimal WITHDRAWAL_LIMIT = new BigDecimal("100000.00");

    private final String id;
    private final String holderName;
    private BigDecimal balance;
    private final List<Transaction> history = new ArrayList<>();

    public Account(String id, String holderName, BigDecimal openingBalance) throws InvalidAccountException {
        if (holderName == null || holderName.isBlank()) {
            throw new InvalidAccountException("Account holder's name cannot be blank.");
        }
        if (openingBalance == null) {
            throw new InvalidAccountException("An opening balance is required.");
        }
        if (openingBalance.signum() < 0) {
            throw new InvalidAccountException("Opening balance cannot be negative.");
        }
        this.id = id;
        this.holderName = holderName.trim();
        this.balance = scale(openingBalance);
        this.history.add(new Transaction(Transaction.Type.OPENING_BALANCE, this.balance, this.balance));
    }

    public String getId() {
        return id;
    }

    public String getHolderName() {
        return holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /** Read-only view: callers cannot add or remove history entries. */
    public List<Transaction> getHistory() {
        return Collections.unmodifiableList(history);
    }

    // ---------------------------------------------------------------- deposit

    public void deposit(BigDecimal amount) throws InvalidAmountException {
        validateDeposit(amount);
        applyCredit(amount, Transaction.Type.DEPOSIT);
    }

    /** Checks a deposit WITHOUT changing anything. Used by deposit() and by transfers. */
    void validateDeposit(BigDecimal amount) throws InvalidAmountException {
        requirePositive(amount);
    }

    // --------------------------------------------------------------- withdraw

    public void withdraw(BigDecimal amount)
            throws InvalidAmountException, InsufficientFundsException, WithdrawalLimitExceededException {
        validateWithdrawal(amount);
        applyDebit(amount, Transaction.Type.WITHDRAWAL);
    }

    /** Checks a withdrawal WITHOUT changing anything. */
    void validateWithdrawal(BigDecimal amount)
            throws InvalidAmountException, InsufficientFundsException, WithdrawalLimitExceededException {
        validateDebitable(amount);
        if (amount.compareTo(WITHDRAWAL_LIMIT) > 0) {
            throw new WithdrawalLimitExceededException(String.format(
                    "The single-withdrawal limit is ₦%,.2f.", WITHDRAWAL_LIMIT));
        }
    }

    // --------------------------------------------------------------- transfer

    /** Checks that this account could send {@code amount} (positive and covered by the balance). */
    void validateTransferOut(BigDecimal amount) throws InvalidAmountException, InsufficientFundsException {
        validateDebitable(amount);
    }

    void applyTransferOut(BigDecimal amount) {
        applyDebit(amount, Transaction.Type.TRANSFER_OUT);
    }

    void applyTransferIn(BigDecimal amount) {
        applyCredit(amount, Transaction.Type.TRANSFER_IN);
    }

    // ---------------------------------------------------------------- helpers

    private void validateDebitable(BigDecimal amount) throws InvalidAmountException, InsufficientFundsException {
        requirePositive(amount);
        if (amount.compareTo(balance) > 0) {
            BigDecimal shortfall = amount.subtract(balance);
            throw new InsufficientFundsException(String.format(
                    "Insufficient funds: balance is ₦%,.2f but ₦%,.2f was requested.", balance, amount), shortfall);
        }
    }

    private static void requirePositive(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }

    private void applyCredit(BigDecimal amount, Transaction.Type type) {
        BigDecimal value = scale(amount);
        balance = balance.add(value);
        history.add(new Transaction(type, value, balance));
    }

    private void applyDebit(BigDecimal amount, Transaction.Type type) {
        BigDecimal value = scale(amount);
        balance = balance.subtract(value);
        history.add(new Transaction(type, value, balance));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
