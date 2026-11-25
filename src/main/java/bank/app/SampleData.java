package bank.app;

import bank.Bank;
import bank.ChequeAccount;
import bank.Customer;
import bank.Account;
import bank.dao.AccountDAO;
import bank.dao.CustomerDAO;

/** Inserts 10 sample customers (with accounts and transactions) when the database is empty. */
public final class SampleData {
    private SampleData() { }

    private static final String[][] PEOPLE = {
            // id, first name, surname, address, phone, employed, employer
            {"C001", "Thato", "Molefe", "Plot 123, Gaborone", "71234567", "yes", "Choppies"},
            {"C002", "Neo", "Kgosi", "Plot 45, Francistown", "72345678", "no", ""},
            {"C003", "Lesego", "Mothibi", "Plot 8, Maun", "73456789", "yes", "Debswana"},
            {"C004", "Kabo", "Seretse", "Plot 77, Kasane", "74567890", "no", ""},
            {"C005", "Boitumelo", "Dube", "Plot 12, Palapye", "75678901", "yes", "Botswana Power Corporation"},
            {"C006", "Tshepo", "Ramotswa", "Plot 3, Serowe", "76789012", "no", ""},
            {"C007", "Onalenna", "Phiri", "Plot 90, Lobatse", "77890123", "yes", "First National Bank"},
            {"C008", "Mpho", "Tau", "Plot 21, Molepolole", "71112223", "no", ""},
            {"C009", "Gorata", "Moeng", "Plot 56, Selebi-Phikwe", "72223334", "yes", "Orange Botswana"},
            {"C010", "Keabetswe", "Modise", "Plot 14, Mahalapye", "73334445", "no", ""}
    };

    public static void seed(Bank bank, CustomerDAO customerDAO, AccountDAO accountDAO) {
        for (int i = 0; i < PEOPLE.length; i++) {
            String[] p = PEOPLE[i];
            boolean employed = p[5].equals("yes");

            Customer customer = new Customer(p[0], p[1], p[2], p[3], p[4], "pass1234");
            customer.setEmployed(employed);
            customerDAO.insert(customer);
            bank.addCustomer(customer);

            Account savings = bank.openSavingsAccount(customer, 1000 + 250 * i);
            savings.deposit(100 + 50 * i);
            accountDAO.save(savings);

            if (i % 2 == 0) {
                Account investment = bank.openInvestmentAccount(customer, 500 + 100 * i);
                investment.deposit(200);
                accountDAO.save(investment);
            }

            if (employed) {
                ChequeAccount cheque = (ChequeAccount) bank.openChequeAccount(customer, 0, p[6], "Gaborone");
                cheque.receiveSalary(6000 + 500 * i);
                cheque.withdraw(750);
                accountDAO.save(cheque);
            }
        }
    }
}
