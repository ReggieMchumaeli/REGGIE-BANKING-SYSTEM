package bank.controller;

import bank.Bank;
import bank.BankException;
import bank.Customer;
import bank.dao.CustomerDAO;

/** Handles customer registration: validates the input, then calls the model and the DAO. */
public class CustomerController {
    private final Bank bank;
    private final CustomerDAO customerDAO;

    public CustomerController(Bank bank, CustomerDAO customerDAO) {
        this.bank = bank;
        this.customerDAO = customerDAO;
    }

    public Customer register(String customerId, String firstName, String surname, String address,
                             String phone, String password, String confirmPassword, boolean employed) {
        if (customerId == null || customerId.isBlank()) {
            throw new BankException("Customer ID is required.");
        }
        if (firstName == null || firstName.isBlank() || surname == null || surname.isBlank()) {
            throw new BankException("First name and surname are required.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            throw new BankException("Passwords do not match.");
        }
        if (phone != null && !phone.isBlank() && !phone.trim().matches("[0-9+ ]{7,15}")) {
            throw new BankException("Enter a valid phone number (digits only, 7 to 15 characters).");
        }
        if (bank.findCustomer(customerId.trim()) != null) {
            throw new BankException("A customer with ID " + customerId.trim() + " already exists.");
        }

        Customer customer = new Customer(customerId.trim(), firstName.trim(), surname.trim(),
                address == null ? "" : address.trim(), phone == null ? "" : phone.trim(), password);
        customer.setEmployed(employed);

        customerDAO.insert(customer);   // save first: if this fails, the bank is unchanged
        bank.addCustomer(customer);
        return customer;
    }
}
