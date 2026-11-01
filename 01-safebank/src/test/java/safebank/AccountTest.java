package safebank;

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
        account = new Account("ACC-1", "Ada Okafor", bd("200000"));
    }

    // ------------------------------------------------------------ creation

    @Test
    void blankNameIsRejected() {
        assertThrows(InvalidAccountException.class, () -> new Account("A", "   ", bd("100")));
    }

    @Test
    void negativeOpeningBalanceIsRejected() {
        assertThrows(InvalidAccountException.class, () -> new Account("A", "Ada", bd("-1")));
    }

    @Test
    void zeroOpeningBalanceIsAllowed() throws Exception {
        assertEquals(0, new Account("A", "Ada", BigDecimal.ZERO).getBalance().compareTo(BigDecimal.ZERO));
    }

    // ------------------------------------------------------------- deposit

    @Test
    void validDepositIncreasesBalanceAndIsRecorded() throws Exception {
        account.deposit(bd("500"));
        assertEquals(0, bd("200500").compareTo(account.getBalance()));
        assertEquals(2, account.getHistory().size()); // opening balance + deposit
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

    // ------------------------------------------------------------ withdraw

    @Test
    void validWithdrawalReducesBalance() throws Exception {
        account.withdraw(bd("40000"));
        assertEquals(0, bd("160000").compareTo(account.getBalance()));
    }

    @Test
    void withdrawalExceedingBalanceIsRejectedAndNothingChanges() throws Exception {
        Account small = new Account("S", "Small", bd("5000"));
        InsufficientFundsException e = assertThrows(InsufficientFundsException.class,
                () -> small.withdraw(bd("8000")));
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

    // ------------------------------------------- success right after failure

    @Test
    void successWorksImmediatelyAfterAFailure() throws Exception {
        assertThrows(InvalidAmountException.class, () -> account.deposit(bd("-500")));
        assertThrows(WithdrawalLimitExceededException.class, () -> account.withdraw(bd("120000")));

        account.deposit(bd("1000"));
        account.withdraw(bd("500"));

        assertEquals(0, bd("200500").compareTo(account.getBalance()));
        assertEquals(3, account.getHistory().size()); // opening + the two successes only
    }

    @Test
    void historyCannotBeModifiedFromOutside() {
        assertThrows(UnsupportedOperationException.class, () -> account.getHistory().clear());
    }

    @Test
    void decimalAmountsAreExact() throws Exception {
        Account a = new Account("D", "Dee", bd("0.10"));
        a.deposit(bd("0.20"));
        assertEquals(0, bd("0.30").compareTo(a.getBalance())); // would be 0.30000000000000004 with double
    }
}
