package bank.controller;

import bank.Bank;
import bank.BankException;
import bank.Customer;

/** Handles login and logout requests coming from the views. */
public class LoginController {
    private final Bank bank;
    private final Session session;

    public LoginController(Bank bank, Session session) {
        this.bank = bank;
        this.session = session;
    }

    public Customer login(String customerId, String password) {
        if (customerId == null || customerId.isBlank() || password == null || password.isEmpty()) {
            throw new BankException("Enter your customer ID and password.");
        }
        Customer customer = bank.findCustomer(customerId.trim());
        if (customer == null || !customer.authenticate(password)) {
            throw new BankException("Invalid customer ID or password.");
        }
        session.setCurrentCustomer(customer);
        return customer;
    }

    public void logout() {
        session.clear();
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    public String getCurrentCustomerName() {
        Customer c = session.getCurrentCustomer();
        return c == null ? "" : c.getFirstName() + " " + c.getSurname();
    }
}
