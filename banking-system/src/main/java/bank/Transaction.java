package bank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime date;
    private final double balanceAfter;
    private final String description;

    /** Creates a brand-new transaction (id and date are generated). */
    public Transaction(TransactionType type, double amount, double balanceAfter, String description) {
        this(newId(), type, amount, LocalDateTime.now(), balanceAfter, description);
    }

    /** Used by the data access layer to rebuild a stored transaction. */
    public Transaction(String transactionId, TransactionType type, double amount,
                       LocalDateTime date, double balanceAfter, String description) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    private static String newId() {
        return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }

    public String getTransactionDetails() {
        return String.format("%s | %s | %-10s | BWP %10.2f | balance BWP %10.2f | %s",
                transactionId,
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                type, amount, balanceAfter, description);
    }

    @Override
    public String toString() {
        return getTransactionDetails();
    }
}
