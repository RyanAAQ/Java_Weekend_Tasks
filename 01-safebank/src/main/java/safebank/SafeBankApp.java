package safebank;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console menu for SafeBank. It only handles typing and printing.
 * All rules are enforced by Account and Bank; this class just catches the exceptions they throw
 * and turns them into friendly messages, so a bad entry never ends the program.
 *
 * The input and output streams are passed in so the same class can run from the keyboard or from a script.
 */
public class SafeBankApp {

    private final Scanner in;
    private final PrintStream out;
    private final Bank bank = new Bank();
    private Account current; // the account being worked on

    public SafeBankApp(InputStream input, PrintStream output) {
        this.in = new Scanner(input, StandardCharsets.UTF_8);
        this.out = output;
    }

    public static void main(String[] args) {
        PrintStream utf8 = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        new SafeBankApp(System.in, utf8).run();
    }

    public void run() {
        out.println("=== SafeBank Transaction Simulator ===");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = prompt("Choose an option: ");
                switch (choice) {
                    case "1" -> createAccount();
                    case "2" -> selectAccount();
                    case "3" -> deposit();
                    case "4" -> withdraw();
                    case "5" -> showBalance();
                    case "6" -> showHistory();
                    case "7" -> transfer();
                    case "0" -> {
                        out.println("Thank you for using SafeBank. Goodbye!");
                        running = false;
                    }
                    default -> out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        out.println();
        out.println("Current account: " + (current == null ? "none selected"
                : current.getId() + " (" + current.getHolderName() + ")"));
        out.println("1. Create account");
        out.println("2. Select account");
        out.println("3. Deposit");
        out.println("4. Withdraw");
        out.println("5. Show balance");
        out.println("6. Show transaction history");
        out.println("7. Transfer (stretch)");
        out.println("0. Exit");
    }

    // ----------------------------------------------------------------- actions

    private void createAccount() {
        String name = prompt("Account holder's name: ");
        BigDecimal opening = readAmount("Opening balance (₦): ");
        if (opening == null) {
            return;
        }
        try {
            current = bank.openAccount(name, opening);
            out.println("Account created: " + current.getId() + " for " + current.getHolderName()
                    + " with balance " + money(current.getBalance()));
        } catch (InvalidAccountException e) {
            out.println("Could not create account. " + e.getMessage());
        }
    }

    private void selectAccount() {
        if (bank.allAccounts().isEmpty()) {
            out.println("There are no accounts yet. Create one first.");
            return;
        }
        for (Account a : bank.allAccounts()) {
            out.println("  " + a.getId() + "  " + a.getHolderName() + "  " + money(a.getBalance()));
        }
        try {
            current = bank.find(prompt("Enter account ID: "));
            out.println("Now using " + current.getId() + ".");
        } catch (InvalidAccountException e) {
            out.println(e.getMessage());
        }
    }

    private void deposit() {
        if (!hasAccount()) {
            return;
        }
        BigDecimal amount = readAmount("Deposit amount (₦): ");
        if (amount == null) {
            return;
        }
        try {
            current.deposit(amount);
            out.println("Deposit successful. New balance: " + money(current.getBalance()));
        } catch (InvalidAmountException e) {
            out.println("Deposit rejected. " + e.getMessage());
        }
    }

    private void withdraw() {
        if (!hasAccount()) {
            return;
        }
        BigDecimal amount = readAmount("Withdrawal amount (₦): ");
        if (amount == null) {
            return;
        }
        try {
            current.withdraw(amount);
            out.println("Withdrawal successful. New balance: " + money(current.getBalance()));
        } catch (InvalidAmountException | InsufficientFundsException | WithdrawalLimitExceededException e) {
            // Specific exception types are caught together because the reaction is the same: show the message.
            out.println("Withdrawal rejected. " + e.getMessage());
        }
    }

    private void showBalance() {
        if (hasAccount()) {
            out.println("Balance for " + current.getId() + ": " + money(current.getBalance()));
        }
    }

    private void showHistory() {
        if (!hasAccount()) {
            return;
        }
        out.println("Successful transactions for " + current.getId() + ":");
        int n = 1;
        for (Transaction t : current.getHistory()) {
            out.println(String.format("  %2d. %s", n++, t));
        }
    }

    private void transfer() {
        if (!hasAccount()) {
            return;
        }
        String toId = prompt("Send to account ID: ");
        BigDecimal amount = readAmount("Transfer amount (₦): ");
        if (amount == null) {
            return;
        }
        try {
            bank.transfer(current.getId(), toId, amount);
            out.println("Transfer successful. Your new balance: " + money(current.getBalance()));
        } catch (InvalidAccountException | InvalidAmountException | InsufficientFundsException e) {
            out.println("Transfer rejected. " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------- helpers

    private boolean hasAccount() {
        if (current == null) {
            out.println("No account selected. Create or select an account first.");
            return false;
        }
        return true;
    }

    private String prompt(String message) {
        out.print(message);
        return in.nextLine().trim();
    }

    /** Reads a number. Returns null (after printing a message) if the text is not numeric. */
    private BigDecimal readAmount(String message) {
        String text = prompt(message).replace(",", "");
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            out.println("'" + text + "' is not a valid number. Please enter digits only, for example 2500 or 2500.50.");
            return null;
        }
    }

    private static String money(BigDecimal value) {
        return String.format("₦%,.2f", value);
    }
}
