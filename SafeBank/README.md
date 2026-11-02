# SafeBank

SafeBank is a small console banking simulator focused on exception handling. You can create accounts, deposit and withdraw money, check balances and transaction history, and transfer money between accounts.

## Rules and implementation

Account names can’t be blank, and an account can’t start with a negative balance. Deposits and withdrawals must be positive. A withdrawal can’t exceed the available balance or the ₦100,000 per transaction limit. Transfers check both accounts and the amount before changing either balance, so a rejected transfer leaves both accounts untouched.

These checks live in the account and bank classes rather than in the menu. They report errors with custom checked exceptions, which the menu catches and displays. Money is stored as `BigDecimal` to avoid the rounding problems that come with `double`. The transaction history contains successful transactions only and is exposed as a read-only list.

The app accepts input and output streams in its constructor, which makes the same menu usable from the console and from tests.

## Sample and tests

A sample session is in [`sample/output.txt`](sample/output.txt), with the corresponding input in [`sample/input.txt`](sample/input.txt).

There are 23 JUnit 5 tests under `src/test/java/safebank`. They cover account rules, failed transactions, transfers, and the menu.

## Limits

Account IDs are generated (for example, `ACC-1001`); there is no login or PIN. The withdrawal limit applies to cash withdrawals, not transfers. Amounts are rounded to two decimal places. Accounts exist only while the app is running, so they are lost when it exits.
