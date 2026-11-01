package safebank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Bank {

    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private int nextNumber = 1001;

    public Account openAccount(String holderName, BigDecimal openingBalance) throws InvalidAccountException {
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


    public void transfer(String fromId, String toId, BigDecimal amount)
            throws InvalidAccountException, InvalidAmountException, InsufficientFundsException {
        Account from = find(fromId);
        Account to = find(toId);
        if (from == to) {
            throw new InvalidAccountException("Cannot transfer to the same account.");
        }
        from.validateTransferOut(amount);
        to.validateDeposit(amount);
        from.applyTransferOut(amount);
        to.applyTransferIn(amount);
    }
}
