package bank;

/** For working customers. Any deposit or withdrawal allowed (within the balance). No interest. */
public class ChequeAccount extends Account {
    private final String companyName;
    private final String companyAddress;

    public ChequeAccount(String accountNumber, String branch, Customer customer, double openingDeposit,
                         String companyName, String companyAddress) {
        super(accountNumber, branch, requireEmployed(customer), openingDeposit);
        if (companyName == null || companyName.isBlank()
                || companyAddress == null || companyAddress.isBlank()) {
            throw new BankException("Employer name and address are required for a Cheque account.");
        }
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    private static Customer requireEmployed(Customer customer) {
        if (customer == null || !customer.isEmployed()) {
            throw new BankException("A Cheque account can only be opened for a customer who is working.");
        }
        return customer;
    }

    /** Convenience method: a salary is a deposit with a description. */
    public void receiveSalary(double amount) {
        deposit(amount, "Salary from " + companyName);
    }

    @Override
    public void withdraw(double amount) {
        applyDebit(amount, "Withdrawal");
    }

    @Override
    public String getAccountType() {
        return "Cheque";
    }

    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
}
