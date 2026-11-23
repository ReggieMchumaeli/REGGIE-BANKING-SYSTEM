package bank;

import bank.app.AppContext;
import bank.controller.AccountController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests: controller -> core model -> DAO -> database, then a second
 * AppContext reloads everything from the database to prove it was really saved.
 * (The JavaFX views only forward input to these controllers, so this is the full flow below the screen.)
 */
class BankIntegrationTest {
    private String url;
    private AppContext ctx;

    @BeforeEach
    void setUp() {
        url = "jdbc:h2:mem:test_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        ctx = new AppContext(url, false);
    }

    private AccountController registerAndLogin(AppContext context, boolean employed) {
        context.getCustomerController().register(
                "C100", "Neo", "Kgosi", "Plot 1, Gaborone", "71111111", "secret", "secret", employed);
        context.getLoginController().login("C100", "secret");
        return context.getAccountController();
    }

    @Test
    void fullFlow_registerLoginOpenDepositWithdraw_isSavedInDatabase() {
        AccountController accounts = registerAndLogin(ctx, true);

        Account savings = accounts.openAccount("Savings", "1000", "", "");
        Account investment = accounts.openAccount("Investment", "500", "", "");
        accounts.deposit(savings.getAccountNumber(), "250");
        accounts.withdraw(investment.getAccountNumber(), "100");

        assertEquals(1250.00, savings.getBalance(), 0.001);
        assertEquals(400.00, investment.getBalance(), 0.001);

        // A brand-new application instance reads everything back from the database.
        AppContext reloaded = new AppContext(url, false);
        reloaded.getLoginController().login("C100", "secret");
        Account savings2 = reloaded.getBank().findAccount(savings.getAccountNumber());
        Account investment2 = reloaded.getBank().findAccount(investment.getAccountNumber());

        assertNotNull(savings2);
        assertEquals(1250.00, savings2.getBalance(), 0.001);
        assertEquals(2, savings2.getTransactions().size());     // opening deposit + deposit
        assertEquals(400.00, investment2.getBalance(), 0.001);
        assertEquals(2, investment2.getTransactions().size());  // opening deposit + withdrawal
    }

    @Test
    void businessRules_areEnforced_andNothingBadIsSaved() {
        AccountController accounts = registerAndLogin(ctx, false);
        Account savings = accounts.openAccount("Savings", "1000", "", "");

        assertThrows(BankException.class, () -> accounts.withdraw(savings.getAccountNumber(), "50"));
        assertThrows(BankException.class, () -> accounts.deposit(savings.getAccountNumber(), "-5"));
        assertThrows(BankException.class, () -> accounts.openAccount("Investment", "100", "", ""));
        assertThrows(BankException.class, () -> accounts.openAccount("Cheque", "0", "Acme", "Plot 9"));
        assertThrows(BankException.class, () -> ctx.getLoginController().login("C100", "wrong"));

        AppContext reloaded = new AppContext(url, false);
        assertEquals(1, reloaded.getBank().getAccounts().size());
        assertEquals(1000.00, reloaded.getBank().findAccount(savings.getAccountNumber()).getBalance(), 0.001);
    }

    @Test
    void monthlyInterest_isPaidAndPersisted() {
        AccountController accounts = registerAndLogin(ctx, true);
        Account savings = accounts.openAccount("Savings", "1000", "", "");
        Account investment = accounts.openAccount("Investment", "500", "", "");
        Account cheque = accounts.openAccount("Cheque", "2000", "Acme Ltd", "Plot 9, Francistown");

        accounts.payMonthlyInterest();

        AppContext reloaded = new AppContext(url, false);
        assertEquals(1000.50, reloaded.getBank().findAccount(savings.getAccountNumber()).getBalance(), 0.001);
        assertEquals(525.00, reloaded.getBank().findAccount(investment.getAccountNumber()).getBalance(), 0.001);
        assertEquals(2000.00, reloaded.getBank().findAccount(cheque.getAccountNumber()).getBalance(), 0.001);
    }

    @Test
    void customer_cannotTouchAnotherCustomersAccount() {
        AccountController accounts = registerAndLogin(ctx, false);
        Account mine = accounts.openAccount("Savings", "100", "", "");
        ctx.getLoginController().logout();

        ctx.getCustomerController().register("C101", "Sam", "Tau", "Maun", "72222222", "pass1", "pass1", false);
        ctx.getLoginController().login("C101", "pass1");
        assertThrows(BankException.class, () -> accounts.deposit(mine.getAccountNumber(), "10"));
    }

    @Test
    void sampleData_inserts_atLeastTenCustomers() {
        AppContext seeded = new AppContext("jdbc:h2:mem:seed_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", true);
        assertTrue(seeded.getCustomerDAO().count() >= 10);
        assertTrue(seeded.getAccountDAO().count() >= 10);
    }
}
