package bank;

/** Earns 5% monthly interest. Needs BWP 500.00 to open. Deposits and withdrawals allowed. */
public class InvestmentAccount extends Account implements InterestCalculable {
    private static final double INTEREST_RATE = 0.05; // 5% per month
    private static final double MINIMUM_OPENING_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, String branch, Customer customer, double openingDeposit) {
        super(accountNumber, branch, customer, checkOpeningDeposit(openingDeposit));
    }

    /**
     * Used only by the data access layer to rebuild a stored account.
     * The BWP 500 rule was already checked when the account was first opened.
     */
    public InvestmentAccount(String accountNumber, String branch, Customer customer) {
        super(accountNumber, branch, customer, 0);
    }

    /** The BWP 500 rule applies only when the account is opened, not to later deposits. */
    private static double checkOpeningDeposit(double openingDeposit) {
        if (openingDeposit < MINIMUM_OPENING_DEPOSIT) {
            throw new BankException(String.format(
                    "An Investment account needs an opening deposit of at least BWP %.2f.",
                    MINIMUM_OPENING_DEPOSIT));
        }
        return openingDeposit;
    }

    @Override
    public void withdraw(double amount) {
        applyDebit(amount, "Withdrawal");
    }

    @Override
    public double calculateInterest() {
        return round2(getBalance() * INTEREST_RATE);
    }

    @Override
    public void payInterest() {
        double interest = calculateInterest();
        if (interest > 0) {
            applyCredit(interest, TransactionType.INTEREST, "Monthly interest (5%)");
        }
    }

    @Override
    public String getAccountType() {
        return "Investment";
    }
}
