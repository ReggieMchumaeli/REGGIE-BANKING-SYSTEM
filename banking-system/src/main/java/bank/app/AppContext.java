package bank.app;

import bank.Account;
import bank.Bank;
import bank.Customer;
import bank.controller.AccountController;
import bank.controller.CustomerController;
import bank.controller.LoginController;
import bank.controller.Session;
import bank.dao.AccountDAO;
import bank.dao.CustomerDAO;
import bank.dao.Database;
import bank.dao.TransactionDAO;

/**
 * Wires every layer together: Database -> DAOs -> Bank (model) -> Controllers.
 * The views ask this class for their controller; they never touch the model or the DAOs.
 */
public class AppContext {
    private static AppContext instance;

    public static void init(String jdbcUrl) {
        instance = new AppContext(jdbcUrl, true);
    }

    public static AppContext get() {
        if (instance == null) {
            throw new IllegalStateException("AppContext.init() has not been called.");
        }
        return instance;
    }

    private final Bank bank;
    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;
    private final LoginController loginController;
    private final CustomerController customerController;
    private final AccountController accountController;

    public AppContext(String jdbcUrl, boolean seedSampleData) {
        Database database = new Database(jdbcUrl);
        this.customerDAO = new CustomerDAO(database);
        this.accountDAO = new AccountDAO(database, new TransactionDAO(database));
        this.bank = new Bank("B001", "Demo Bank Botswana", "Gaborone Main");

        loadBankFromDatabase();
        if (seedSampleData && customerDAO.count() == 0) {
            SampleData.seed(bank, customerDAO, accountDAO);
        }

        Session session = new Session();
        this.loginController = new LoginController(bank, session);
        this.customerController = new CustomerController(bank, customerDAO);
        this.accountController = new AccountController(bank, accountDAO, session);
    }

    private void loadBankFromDatabase() {
        for (Customer customer : customerDAO.findAll()) {
            bank.restoreCustomer(customer);
            for (Account account : accountDAO.findByCustomer(customer)) {
                bank.restoreAccount(account);
            }
        }
    }

    public Bank getBank() { return bank; }
    public CustomerDAO getCustomerDAO() { return customerDAO; }
    public AccountDAO getAccountDAO() { return accountDAO; }
    public LoginController getLoginController() { return loginController; }
    public CustomerController getCustomerController() { return customerController; }
    public AccountController getAccountController() { return accountController; }
}
