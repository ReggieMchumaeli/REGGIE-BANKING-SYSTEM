package bank.controller;

import bank.Account;
import bank.Bank;
import bank.BankException;
import bank.Customer;
import bank.Transaction;
import bank.dao.AccountDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mediates between the account screens and the core model.
 * It validates input, calls the domain classes for the business rules, then saves through the DAO.
 */
public class AccountController {
    private final Bank bank;
    private final AccountDAO accountDAO;
    private final Session session;

    public AccountController(Bank bank, AccountDAO accountDAO, Session session) {
        this.bank = bank;
        this.accountDAO = accountDAO;
        this.session = session;
    }

    public List<Account> getAccounts() {
        return requireLogin().getAccounts();
    }

    /** Newest transaction first. */
    public List<Transaction> getTransactions(String accountNumber) {
        List<Transaction> history = new ArrayList<>(ownedAccount(accountNumber).getTransactions());
        Collections.reverse(history);
        return history;
    }

    public Account openAccount(String type, String depositText, String companyName, String companyAddress) {
        Customer customer = requireLogin();
        if (type == null || type.isBlank()) {
            throw new BankException("Choose an account type.");
        }
        double deposit = parseAmount(depositText, true);
        Account account = switch (type) {
            case "Savings" -> bank.openSavingsAccount(customer, deposit);
            case "Investment" -> bank.openInvestmentAccount(customer, deposit);
            case "Cheque" -> bank.openChequeAccount(customer, deposit, companyName, companyAddress);
            default -> throw new BankException("Unknown account type: " + type);
        };
        accountDAO.save(account);
        return account;
    }

    public void deposit(String accountNumber, String amountText) {
        Account account = ownedAccount(accountNumber);
        account.deposit(parseAmount(amountText, false));
        accountDAO.save(account);
    }

    public void withdraw(String accountNumber, String amountText) {
        Account account = ownedAccount(accountNumber);
        account.withdraw(parseAmount(amountText, false));
        accountDAO.save(account);
    }

    /** Staff function: pays this month's interest into every interest-bearing account. */
    public void payMonthlyInterest() {
        requireLogin();
        bank.payMonthlyInterest();
        for (Account account : bank.getAccounts()) {
            accountDAO.save(account);
        }
    }

    // ---------- helpers ----------

    private Customer requireLogin() {
        if (!session.isLoggedIn()) {
            throw new BankException("Please log in first.");
        }
        return session.getCurrentCustomer();
    }

    /** A customer may only touch their own accounts. */
    private Account ownedAccount(String accountNumber) {
        Account account = requireLogin().getAccount(accountNumber);
        if (account == null) {
            throw new BankException("Account not found.");
        }
        return account;
    }

    private static double parseAmount(String text, boolean blankMeansZero) {
        if (text == null || text.isBlank()) {
            if (blankMeansZero) {
                return 0;
            }
            throw new BankException("Enter an amount.");
        }
        try {
            double value = Double.parseDouble(text.trim().replace(",", ""));
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw new BankException("Enter a valid amount, for example 250.00.");
        }
    }
}
