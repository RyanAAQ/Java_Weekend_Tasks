package safebank;

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

    // ------------------------------------------------------------ transfers

    @Test
    void successfulTransferMovesMoneyAndRecordsBothSides() throws Exception {
        Bank bank = new Bank();
        Account a = bank.openAccount("Ada", bd("10000"));
        Account b = bank.openAccount("Bola", bd("500"));

        bank.transfer(a.getId(), b.getId(), bd("2500"));

        assertEquals(0, bd("7500").compareTo(a.getBalance()));
        assertEquals(0, bd("3000").compareTo(b.getBalance()));
        assertEquals(Transaction.Type.TRANSFER_OUT, a.getHistory().get(1).type());
        assertEquals(Transaction.Type.TRANSFER_IN, b.getHistory().get(1).type());
    }

    @Test
    void failedTransferChangesNeitherBalance() throws Exception {
        Bank bank = new Bank();
        Account a = bank.openAccount("Ada", bd("1000"));
        Account b = bank.openAccount("Bola", bd("500"));

        assertThrows(InsufficientFundsException.class, () -> bank.transfer(a.getId(), b.getId(), bd("5000")));
        assertThrows(InvalidAmountException.class, () -> bank.transfer(a.getId(), b.getId(), bd("-5")));
        assertThrows(InvalidAccountException.class, () -> bank.transfer(a.getId(), "ACC-9999", bd("10")));
        assertThrows(InvalidAccountException.class, () -> bank.transfer(a.getId(), a.getId(), bd("10")));

        assertEquals(0, bd("1000").compareTo(a.getBalance()));
        assertEquals(0, bd("500").compareTo(b.getBalance()));
        assertEquals(1, a.getHistory().size());
        assertEquals(1, b.getHistory().size());
    }

    @Test
    void failedAccountCreationDoesNotUseUpAnAccountNumber() throws Exception {
        Bank bank = new Bank();
        assertThrows(InvalidAccountException.class, () -> bank.openAccount("", bd("100")));
        assertEquals("ACC-1001", bank.openAccount("Ada", bd("100")).getId());
    }

    // ------------------------------------------------------ console behaviour

    private String runApp(String script) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new SafeBankApp(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), out).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void nonNumericInputGivesAHelpfulMessageAndTheAppKeepsRunning() {
        String output = runApp("1\nAda\nabc\n0\n");
        assertTrue(output.contains("is not a valid number"));
        assertTrue(output.contains("Goodbye"));
    }

    @Test
    void invalidMenuSelectionGivesAHelpfulMessage() {
        String output = runApp("99\nhello\n0\n");
        assertTrue(output.contains("Invalid option '99'"));
        assertTrue(output.contains("Invalid option 'hello'"));
    }

    @Test
    void successAfterFailureWorksThroughTheMenu() {
        // create account, deposit -500 (rejected), deposit 1000 (works), exit
        String output = runApp("1\nAda\n200000\n3\n-500\n3\n1000\n5\n0\n");
        assertTrue(output.contains("Deposit rejected"));
        assertTrue(output.contains("Deposit successful"));
        assertTrue(output.contains("₦201,000.00"));
    }

    @Test
    void withdrawalLimitMessageIsShownThroughTheMenu() {
        String output = runApp("1\nAda\n200000\n4\n120000\n0\n");
        assertTrue(output.contains("single-withdrawal limit"));
    }

    @Test
    void actionsWithoutAnAccountAreHandled() {
        String output = runApp("3\n4\n5\n6\n0\n");
        assertTrue(output.contains("No account selected"));
    }

    @Test
    void appEndsGracefullyWhenInputRunsOut() {
        String output = runApp("1\n");
        assertTrue(output.contains("No more input"));
    }
}
