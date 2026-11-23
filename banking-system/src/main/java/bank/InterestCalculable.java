package bank;

/** Implemented only by accounts that earn interest (Savings and Investment). */
public interface InterestCalculable {

    /** Interest for one month, based on the current balance. */
    double calculateInterest();

    /** Calculates this month's interest and credits it to the account. */
    void payInterest();
}
