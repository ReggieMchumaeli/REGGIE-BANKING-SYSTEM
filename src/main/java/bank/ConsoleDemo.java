package bank;

/** Console demo of the core model. Run this to check everything works. */
public class ConsoleDemo {
    public static void main(String[] args) {
        Bank bank = new Bank("B001", "Demo Bank", "Gaborone Main");

        Customer thato = new Customer("C001", "Thato", "Molefe", "Plot 123, Gaborone", "71234567", "secret1");
        thato.setEmployed(true);
        bank.addCustomer(thato);
        System.out.println(thato.showCustomerDetails());
        System.out.println("Login with right password: " + thato.authenticate("secret1"));
        System.out.println("Login with wrong password: " + thato.authenticate("nope"));

        // One customer, three account types
        Account savings = bank.openSavingsAccount(thato, 1000);
        Account investment = bank.openInvestmentAccount(thato, 500);
        ChequeAccount cheque = (ChequeAccount) bank.openChequeAccount(
                thato, 0, "Acme Ltd", "Plot 9, Francistown");

        // Deposits (overloaded methods) and salary
        savings.deposit(500);
        investment.deposit(1000, "Top-up");
        cheque.receiveSalary(8000);

        // Withdrawals
        investment.withdraw(200);
        cheque.withdraw(1500);

        // Rule checks
        attempt("Withdraw from Savings", () -> savings.withdraw(100));
        attempt("Overdraw Cheque", () -> cheque.withdraw(1_000_000));
        attempt("Negative deposit", () -> cheque.deposit(-5));
        attempt("Investment below BWP 500", () -> bank.openInvestmentAccount(thato, 100));

        Customer sam = new Customer("C002", "Sam", "Kgosi", "Plot 5, Maun", "72345678", "pass1234");
        bank.addCustomer(sam); // not employed
        attempt("Cheque account for unemployed customer",
                () -> bank.openChequeAccount(sam, 0, "Acme Ltd", "Plot 9"));

        // Monthly interest
        System.out.println("\n--- Paying monthly interest ---");
        bank.payMonthlyInterest();

        System.out.println("\n--- Accounts ---");
        for (Account a : thato.getAccounts()) {
            System.out.println(a.getAccountDetails());
            for (Transaction t : a.getTransactions()) {
                System.out.println("    " + t.getTransactionDetails());
            }
        }
    }

    private static void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println("[UNEXPECTED SUCCESS] " + label);
        } catch (BankException e) {
            System.out.println("[Rejected] " + label + " -> " + e.getMessage());
        }
    }
}
