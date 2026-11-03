package safebank;

import safebank.exceptions.InsufficientFundsException;
import safebank.exceptions.InvalidAccountException;
import safebank.exceptions.InvalidAmountException;
import safebank.exceptions.WithdrawalLimitExceededException;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTest {

    private Account account;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setUp() throws Exception {
        account = new Account("ACC-1", "Ryan", bd("200000"));
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(InvalidAccountException.class, () -> new Account("A", "   ", bd("100")));
    }

    @Test
    void negativeOpeningBalanceIsRejected() {
        assertThrows(InvalidAccountException.class, () -> new Account("A", "Ryan", bd("-1")));
    }

    @Test
    void zeroOpeningBalanceIsAllowed() throws Exception {
        assertEquals(0, new Account("A", "Ryan", BigDecimal.ZERO).getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void validDepositIncreasesBalanceAndIsRecorded() throws Exception {
        account.deposit(bd("500"));
        assertEquals(0, bd("200500").compareTo(account.getBalance()));
        assertEquals(2, account.getHistory().size());
    }

    @Test
    void negativeDepositOfMinus500IsRejectedAndNothingChanges() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(bd("-500")));
        assertEquals(0, bd("200000").compareTo(account.getBalance()));
        assertEquals(1, account.getHistory().size());
    }

    @Test
    void zeroDepositIsRejected() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(BigDecimal.ZERO));
    }

    @Test
    void validWithdrawalReducesBalance() throws Exception {
        account.withdraw(bd("40000"));
        assertEquals(0, bd("160000").compareTo(account.getBalance()));
    }

    @Test
    void withdrawalExceedingBalanceIsRejectedAndNothingChanges() throws Exception {
        Account small = new Account("S", "Ryan", bd("5000"));
        InsufficientFundsException e = assertThrows(InsufficientFundsException.class, () -> small.withdraw(bd("8000")));
        assertEquals(0, bd("3000").compareTo(e.getShortfall()));
        assertEquals(0, bd("5000").compareTo(small.getBalance()));
        assertEquals(1, small.getHistory().size());
    }

    @Test
    void withdrawing120000From200000HitsTheLimitAndNothingChanges() {
        assertThrows(WithdrawalLimitExceededException.class, () -> account.withdraw(bd("120000")));
        assertEquals(0, bd("200000").compareTo(account.getBalance()));
        assertEquals(1, account.getHistory().size());
    }

    @Test
    void withdrawingExactlyTheLimitIsAllowed() throws Exception {
        account.withdraw(bd("100000"));
        assertEquals(0, bd("100000").compareTo(account.getBalance()));
    }

    @Test
    void zeroAndNegativeWithdrawalsAreRejected() {
        assertThrows(InvalidAmountException.class, () -> account.withdraw(BigDecimal.ZERO));
        assertThrows(InvalidAmountException.class, () -> account.withdraw(bd("-10")));
    }

    @Test
    void successWorksImmediatelyAfterAFailure() throws Exception {
        assertThrows(InvalidAmountException.class, () -> account.deposit(bd("-500")));
        assertThrows(WithdrawalLimitExceededException.class, () -> account.withdraw(bd("120000")));

        account.deposit(bd("1000"));
        account.withdraw(bd("500"));

        assertEquals(0, bd("200500").compareTo(account.getBalance()));
        assertEquals(3, account.getHistory().size());
    }

    @Test
    void historyCannotBeModifiedFromOutside() {
        assertThrows(UnsupportedOperationException.class, () -> account.getHistory().clear());
    }

    @Test
    void decimalAmountsAreExact() throws Exception {
        Account account = new Account("D", "Quadri Ariyo", bd("0.10"));
        account.deposit(bd("0.20"));
        assertEquals(0, bd("0.30").compareTo(account.getBalance()));
    }
}
