package bank;

/** Earns 0.05% monthly interest. Withdrawals are not allowed. */
public class SavingsAccount extends Account implements InterestCalculable {
    private static final double INTEREST_RATE = 0.0005; // 0.05% per month

    public SavingsAccount(String accountNumber, String branch, Customer customer, double openingDeposit) {
        super(accountNumber, branch, customer, openingDeposit);
    }

    @Override
    public void withdraw(double amount) {
        throw new BankException("Withdrawals are not allowed from a Savings account.");
    }

    @Override
    public double calculateInterest() {
        return round2(getBalance() * INTEREST_RATE);
    }

    @Override
    public void payInterest() {
        double interest = calculateInterest();
        if (interest > 0) {
            applyCredit(interest, TransactionType.INTEREST, "Monthly interest (0.05%)");
        }
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}
