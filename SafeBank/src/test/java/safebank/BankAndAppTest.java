package safebank;

import safebank.exceptions.InsufficientFundsException;
import safebank.exceptions.InvalidAccountException;
import safebank.exceptions.InvalidAmountException;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class BankAndAppTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    void successfulTransferMovesMoneyAndRecordsBothSides() throws Exception {
        Bank bank = new Bank();
        Account sourceAccount = bank.openAccount("Ryan", bd("10000"));
        Account destinationAccount = bank.openAccount("Quadri Ariyo", bd("500"));

        bank.transfer(sourceAccount.getId(), destinationAccount.getId(), bd("2500"));

        assertEquals(0, bd("7500").compareTo(sourceAccount.getBalance()));
        assertEquals(0, bd("3000").compareTo(destinationAccount.getBalance()));
        assertEquals(Transaction.Type.TRANSFER_OUT, sourceAccount.getHistory().get(1).type());
        assertEquals(Transaction.Type.TRANSFER_IN, destinationAccount.getHistory().get(1).type());
    }

    @Test
    void failedTransferChangesNeitherBalance() throws Exception {
        Bank bank = new Bank();
        Account sourceAccount = bank.openAccount("Ryan", bd("1000"));
        Account destinationAccount = bank.openAccount("Quadri Ariyo", bd("500"));

        assertThrows(InsufficientFundsException.class, () -> bank.transfer(sourceAccount.getId(), destinationAccount.getId(), bd("5000")));
        assertThrows(InvalidAmountException.class, () -> bank.transfer(sourceAccount.getId(), destinationAccount.getId(), bd("-5")));
        assertThrows(InvalidAccountException.class, () -> bank.transfer(sourceAccount.getId(), "ACC-9999", bd("10")));
        assertThrows(InvalidAccountException.class, () -> bank.transfer(sourceAccount.getId(), sourceAccount.getId(), bd("10")));

        assertEquals(0, bd("1000").compareTo(sourceAccount.getBalance()));
        assertEquals(0, bd("500").compareTo(destinationAccount.getBalance()));
        assertEquals(1, sourceAccount.getHistory().size());
        assertEquals(1, destinationAccount.getHistory().size());
    }

    @Test
    void failedAccountCreationDoesNotUseUpAnAccountNumber() throws Exception {
        Bank bank = new Bank();
        assertThrows(InvalidAccountException.class, () -> bank.openAccount("", bd("100")));
        assertEquals("ACC-1001", bank.openAccount("Ryan", bd("100")).getId());
    }
}
