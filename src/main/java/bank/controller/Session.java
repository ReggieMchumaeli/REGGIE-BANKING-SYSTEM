package bank.controller;

import bank.Customer;

/** Remembers who is logged in. */
public class Session {
    private Customer currentCustomer;

    public Customer getCurrentCustomer() { return currentCustomer; }
    public void setCurrentCustomer(Customer customer) { this.currentCustomer = customer; }
    public void clear() { this.currentCustomer = null; }
    public boolean isLoggedIn() { return currentCustomer != null; }
}
