package bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Bank {
    private final String bankId;
    private final String name;
    private final String branch;
    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private int savingsCount = 0;
    private int investmentCount = 0;
    private int chequeCount = 0;

    public Bank(String bankId, String name, String branch) {
        this.bankId = bankId;
        this.name = name;
        this.branch = branch;
    }

    public void addCustomer(Customer customer) {
        if (customers.containsKey(customer.getCustomerId())) {
            throw new BankException("A customer with ID " + customer.getCustomerId() + " already exists.");
        }
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer findCustomer(String customerId) {
        return customers.get(customerId);
    }

    public Account openSavingsAccount(Customer customer, double initialDeposit) {
        requireRegistered(customer);
        String number = String.format("SAV%04d", ++savingsCount);
        return register(new SavingsAccount(number, branch, customer, initialDeposit));
    }

    public Account openInvestmentAccount(Customer customer, double initialDeposit) {
        requireRegistered(customer);
        String number = String.format("INV%04d", ++investmentCount);
        return register(new InvestmentAccount(number, branch, customer, initialDeposit));
    }

    public Account openChequeAccount(Customer customer, double initialDeposit,
                                     String companyName, String companyAddress) {
        requireRegistered(customer);
        String number = String.format("CHQ%04d", ++chequeCount);
        return register(new ChequeAccount(number, branch, customer, initialDeposit, companyName, companyAddress));
    }

    public Account findAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    /** Polymorphism: every interest-bearing account pays interest, whatever its type. */
    public void payMonthlyInterest() {
        for (Account account : accounts.values()) {
            if (account instanceof InterestCalculable payer) {
                payer.payInterest();
            }
        }
    }

    // ---------- Used when loading the bank back from the database ----------

    public void restoreCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    public void restoreAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
        account.getCustomer().addAccount(account);
        syncCounter(account.getAccountNumber());
    }

    /** Keeps new account numbers unique after a restart (e.g. SAV0003 means the next one is SAV0004). */
    private void syncCounter(String accountNumber) {
        int n = Integer.parseInt(accountNumber.substring(3));
        switch (accountNumber.substring(0, 3)) {
            case "SAV" -> savingsCount = Math.max(savingsCount, n);
            case "INV" -> investmentCount = Math.max(investmentCount, n);
            case "CHQ" -> chequeCount = Math.max(chequeCount, n);
            default -> { }
        }
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers.values()));
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(new ArrayList<>(accounts.values()));
    }

    private void requireRegistered(Customer customer) {
        if (customer == null || !customers.containsKey(customer.getCustomerId())) {
            throw new BankException("Customer must be registered with the bank first.");
        }
    }

    private Account register(Account account) {
        accounts.put(account.getAccountNumber(), account);
        account.getCustomer().addAccount(account);
        return account;
    }

    public String getBankId() { return bankId; }
    public String getName() { return name; }
    public String getBranch() { return branch; }
}
