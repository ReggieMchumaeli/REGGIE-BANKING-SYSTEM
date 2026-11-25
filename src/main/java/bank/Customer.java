package bank;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {
    private final String customerId;
    private final String firstName;
    private final String surname;
    private final String address;
    private final String phoneNumber;
    private final String passwordHash;
    private boolean employed;
    private final List<Account> accounts = new ArrayList<>();

    /** Registers a new customer. The password is stored only as a SHA-256 hash. */
    public Customer(String customerId, String firstName, String surname,
                    String address, String phoneNumber, String password) {
        this(customerId, firstName, surname, address, phoneNumber, hashChecked(password), false);
    }

    private Customer(String customerId, String firstName, String surname,
                     String address, String phoneNumber, String passwordHash, boolean employed) {
        if (customerId == null || customerId.isBlank() || firstName == null || firstName.isBlank()
                || surname == null || surname.isBlank()) {
            throw new BankException("Customer id, first name and surname are required.");
        }
        this.customerId = customerId;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.employed = employed;
    }

    /** Used by the data access layer to rebuild a stored customer (the hash is already computed). */
    public static Customer restore(String customerId, String firstName, String surname,
                                   String address, String phoneNumber, String passwordHash, boolean employed) {
        return new Customer(customerId, firstName, surname, address, phoneNumber, passwordHash, employed);
    }

    /** Called by the Bank when it opens an account for this customer. */
    public void addAccount(Account account) {
        if (account.getCustomer() != this) {
            throw new BankException("This account belongs to a different customer.");
        }
        accounts.add(account);
    }

    public Account getAccount(String accountNumber) {
        for (Account a : accounts) {
            if (a.getAccountNumber().equals(accountNumber)) {
                return a;
            }
        }
        return null;
    }

    public boolean authenticate(String password) {
        return password != null && passwordHash.equals(hash(password));
    }

    public String showCustomerDetails() {
        return String.format("%s %s (ID: %s) | %s | %s | employed: %s | accounts: %d",
                firstName, surname, customerId, address, phoneNumber, employed ? "yes" : "no", accounts.size());
    }

    private static String hashChecked(String password) {
        if (password == null || password.length() < 4) {
            throw new BankException("Password must be at least 4 characters.");
        }
        return hash(password);
    }

    private static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEmployed() { return employed; }
    public void setEmployed(boolean employed) { this.employed = employed; }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }
}
