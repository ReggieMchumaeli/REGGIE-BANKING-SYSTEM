package bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract parent of all account types.
 * An account cannot exist without a customer, so the constructor requires one.
 */
public abstract class Account {
    private final String accountNumber;
    private final String branch;
    private final Customer customer;
    private double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, String branch, Customer customer, double openingDeposit) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BankException("Account number is required.");
        }
        if (branch == null || branch.isBlank()) {
            throw new BankException("Branch is required.");
        }
        if (openingDeposit < 0) {
            throw new BankException("Opening deposit cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.customer = Objects.requireNonNull(customer, "An account must be held by a customer.");
        if (openingDeposit > 0) {
            applyCredit(openingDeposit, TransactionType.DEPOSIT, "Opening deposit");
        }
    }

    // ---------- Overloaded deposit methods ----------

    public void deposit(double amount) {
        deposit(amount, "Deposit");
    }

    public void deposit(double amount, String description) {
        requirePositive(amount);
        applyCredit(amount, TransactionType.DEPOSIT, description);
    }

    // ---------- Overridden by each subclass ----------

    public abstract void withdraw(double amount);

    public abstract String getAccountType();

    // ---------- Persistence support ----------

    /** Used by the data access layer to put back the balance and history loaded from the database. */
    public void restoreState(double storedBalance, List<Transaction> history) {
        this.balance = round2(storedBalance);
        this.transactions.clear();
        this.transactions.addAll(history);
    }

    // ---------- Protected helpers for subclasses ----------

    protected void applyCredit(double amount, TransactionType type, String description) {
        balance = round2(balance + amount);
        transactions.add(new Transaction(type, amount, balance, description));
    }

    protected void applyDebit(double amount, String description) {
        requirePositive(amount);
        if (amount > balance) {
            throw new BankException(String.format(
                    "Insufficient funds: balance is BWP %.2f, requested BWP %.2f.", balance, amount));
        }
        balance = round2(balance - amount);
        transactions.add(new Transaction(TransactionType.WITHDRAWAL, amount, balance, description));
    }

    protected static void requirePositive(double amount) {
        if (amount <= 0) {
            throw new BankException("Amount must be greater than zero.");
        }
    }

    protected static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ---------- Getters ----------

    public String getAccountNumber() { return accountNumber; }
    public String getBranch() { return branch; }
    public Customer getCustomer() { return customer; }
    public double getBalance() { return balance; }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public String getAccountDetails() {
        return String.format("%s %s | branch: %s | balance: BWP %.2f",
                getAccountType(), accountNumber, branch, balance);
    }

    @Override
    public String toString() {
        return getAccountDetails();
    }
}
