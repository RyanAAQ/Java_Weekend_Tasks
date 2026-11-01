# 01. SafeBank Transaction Simulator

**Focus: exception handling.** A banking simulator that protects balances from invalid transactions.

## What it does

- Create accounts with a holder's name and an opening balance (several accounts can exist).
- Deposit and withdraw money.
- Show the current balance and the history of **successful** transactions.
- Transfer money between two accounts (stretch goal).
- Keep running until the user chooses to exit.

## Business rules

| Rule | Where it is enforced |
|------|----------------------|
| Name must not be blank, opening balance must not be negative | `Account` constructor |
| Deposits and withdrawals must be greater than zero | `Account.deposit` / `withdraw` |
| A withdrawal cannot exceed the balance or the ₦100,000 single-withdrawal limit | `Account.withdraw` |
| A rejected transaction leaves balance and history unchanged | every method validates first and changes state last |
| Bad menu choices and non-numeric input give helpful messages | `SafeBankApp` |

## Design decisions

- **Four custom checked exceptions** (all extend `BankException`): `InvalidAmountException`, `InsufficientFundsException`, `WithdrawalLimitExceededException`, `InvalidAccountException`. They are *checked* so the compiler forces every caller to decide what to do with a rejected transaction.
- **Rules live in `Account`, not in the menu.** The menu only catches the exceptions and prints their messages, so no other code can bypass a rule.
- **Validate first, change last.** Each operation checks everything before touching the balance, which is why a failed transaction can never leave half-done changes behind.
- **`BigDecimal` for money**, because `double` cannot store values like 0.10 exactly. There is a test for this.
- **History is a read-only view** (`Collections.unmodifiableList`) of immutable `Transaction` records, so outside code cannot edit it.
- **No empty catch blocks and no stack traces** shown to the user. Related exceptions are caught together with a multi-catch only where the reaction is the same.
- **Input and output streams are passed into `SafeBankApp`**, so the same class runs from the keyboard or from a test script.

## Acceptance checklist

| Check | Input | Expected | Actual |
|-------|-------|----------|--------|
| Deposit of -500 | `3`, `-500` | Rejected, balance unchanged | `Deposit rejected. Amount must be greater than zero.` Passed |
| Withdrawal exceeding the balance | `4`, `300000` on ₦201,000 | Rejected, balance unchanged | `Insufficient funds: balance is ₦201,000.00 but ₦300,000.00 was requested.` Passed |
| ₦120,000 withdrawal from ₦200,000 | `4`, `120000` | Rejected by the limit | `The single-withdrawal limit is ₦100,000.00.` Passed |
| Non-numeric input | `4`, `abc` | Helpful message, app keeps running | `'abc' is not a valid number...` Passed |
| Success right after a failure | `3`, `1000` after the failures above | Works normally | `Deposit successful. New balance: ₦201,000.00` Passed |
| History only shows successes | `6` | Opening balance, the deposit and the transfer only | 3 entries (`OPENING_BALANCE`, `DEPOSIT`, `TRANSFER_OUT`), none of the rejected attempts. Passed |
| Invalid menu choice | `42` | Helpful message | `Invalid option '42'...` Passed |
| Transfer succeeds | `7`, `ACC-1002`, `25000` | Both balances change | `Your new balance: ₦176,000.00` Passed |
| Transfer to an unknown account | `7`, `ACC-9999`, `100` | Rejected, nothing changes | `No account found with ID 'ACC-9999'.` Passed |
| Transfer larger than the balance | `7`, `ACC-1002`, `900000` | Rejected, **neither** balance changes | `Insufficient funds: balance is ₦176,000.00 but ₦900,000.00 was requested.` Balance afterwards is still ₦176,000.00. Passed |

The full console session is in [`sample/output.txt`](sample/output.txt) and the typed input in [`sample/input.txt`](sample/input.txt).

## Tests

23 JUnit 5 tests in `src/test/java/safebank`: `AccountTest` (every rule, including that a failed transaction changes nothing) and `BankAndAppTest` (transfers and the console menu).

## Stretch goal: completed

Transfers between two accounts (menu option 7). `Bank.transfer` validates the **entire** transfer (both accounts exist, they are different, the amount is positive, the sender has enough money) before either balance is changed.

## Assumptions and known limitations

- Account IDs are generated (`ACC-1001`, `ACC-1002`, ...). The ID is only used to pick an account; there is no login or PIN.
- The ₦100,000 limit applies to **withdrawals only**. Transfers are only limited by the sender's balance.
- Amounts are rounded to 2 decimal places.
- Data is kept in memory and is lost when the program exits.
