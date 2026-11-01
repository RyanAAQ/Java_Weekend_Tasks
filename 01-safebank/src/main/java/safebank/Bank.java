package safebank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Holds the accounts and performs transfers between them. */
public class Bank {

    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private int nextNumber = 1001;

    public Account openAccount(String holderName, BigDecimal openingBalance) throws InvalidAccountException {
        // The Account constructor validates everything. The id counter only moves if it succeeds.
        Account account = new Account("ACC-" + nextNumber, holderName, openingBalance);
        accounts.put(account.getId(), account);
        nextNumber++;
        return account;
    }

    public Account find(String id) throws InvalidAccountException {
        Account account = id == null ? null : accounts.get(id.trim().toUpperCase());
        if (account == null) {
            throw new InvalidAccountException("No account found with ID '" + id + "'.");
        }
        return account;
    }

    public List<Account> allAccounts() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * Stretch goal: transfer between two accounts.
     * The ENTIRE transfer is validated before either balance changes, so a failed transfer can
     * never leave money taken from one account and not given to the other.
     */
    public void transfer(String fromId, String toId, BigDecimal amount)
            throws InvalidAccountException, InvalidAmountException, InsufficientFundsException {
        Account from = find(fromId);
        Account to = find(toId);
        if (from == to) {
            throw new InvalidAccountException("Cannot transfer to the same account.");
        }

        // 1. validate everything first (nothing changes if any of these throw)
        from.validateTransferOut(amount);
        to.validateDeposit(amount);

        // 2. only now change both balances
        from.applyTransferOut(amount);
        to.applyTransferIn(amount);
    }
}
