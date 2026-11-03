package safebank;

import safebank.exceptions.InsufficientFundsException;
import safebank.exceptions.InvalidAccountException;
import safebank.exceptions.InvalidAmountException;
import safebank.exceptions.WithdrawalLimitExceededException;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class SafeBankApp {

    private final Scanner scanner;
    private final Bank bank = new Bank();
    private Account current;

    public SafeBankApp() {
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new SafeBankApp().run();
    }

    public void run() {
        System.out.println("=== NIGGER SAFEBANK ===");
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
                    case "0" -> {
                        System.out.println("Thank you for using SafeBank. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option '" + choice + "'. Please enter a number from the menu.");
                }
            }
        } catch (NoSuchElementException endOfInput) {
            System.out.println("\nNo more input. Goodbye!");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Current account: " + (current == null ? "none selected" : current.getId() + " (" + current.getHolderName() + ")"));
        System.out.println("1. Create account");
        System.out.println("2. Select account");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Show balance");
        System.out.println("6. Show transaction history");
        System.out.println("0. Exit");
    }

    private void createAccount() {
        String name = prompt("Account holder's name: ");
        BigDecimal opening = readAmount("Opening balance (₦): ");
        if (opening == null) {
            return;
        }
        try {
            current = bank.openAccount(name, opening);
            System.out.println("Account created: " + current.getId() + " for " + current.getHolderName() + " with balance " + money(current.getBalance()));
        } catch (InvalidAccountException error) {
            System.out.println("Could not create account. " + error.getMessage());
        }
    }

    private void selectAccount() {
        if (bank.allAccounts().isEmpty()) {
            System.out.println("There are no accounts yet. Create one first.");
            return;
        }
        for (Account account : bank.allAccounts()) {
            System.out.println("  " + account.getId() + "  " + account.getHolderName() + "  " + money(account.getBalance()));
        }
        try {
            current = bank.find(prompt("Enter account ID: "));
            System.out.println("Now using " + current.getId() + ".");
        } catch (InvalidAccountException error) {
            System.out.println(error.getMessage());
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
            System.out.println("Deposit successful. New balance: " + money(current.getBalance()));
        } catch (InvalidAmountException error) {
            System.out.println("Deposit rejected. " + error.getMessage());
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
            System.out.println("Withdrawal successful. New balance: " + money(current.getBalance()));
        } catch (InvalidAmountException | InsufficientFundsException | WithdrawalLimitExceededException error) {
            System.out.println("Withdrawal rejected. " + error.getMessage());
        }
    }

    private void showBalance() {
        if (hasAccount()) {
            System.out.println("Balance for " + current.getId() + ": " + money(current.getBalance()));
        }
    }

    private void showHistory() {
        if (!hasAccount()) {
            return;
        }
        System.out.println("Successful transactions for " + current.getId() + ":");
        int number = 1;
        for (Transaction transaction : current.getHistory()) {
            System.out.println(String.format("  %2d. %s", number++, transaction));
        }
    }

    private boolean hasAccount() {
        if (current == null) {
            System.out.println("No account selected. Create or select an account first.");
            return false;
        }
        return true;
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private BigDecimal readAmount(String message) {
        String text = prompt(message).replace(",", "");
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException error) {
            System.out.println("'" + text + "' is not a valid number. Please enter digits only, for example 2500 or 2500.50.");
            return null;
        }
    }

    private static String money(BigDecimal value) {
        return String.format("₦%,.2f", value);
    }
}
