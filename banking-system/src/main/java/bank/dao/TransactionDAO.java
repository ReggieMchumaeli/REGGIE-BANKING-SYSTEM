package bank.dao;

import bank.Transaction;
import bank.TransactionType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Data access for the transactions table. */
public class TransactionDAO {
    private final Database database;

    public TransactionDAO(Database database) {
        this.database = database;
    }

    /** Create or update (MERGE) one transaction, using the caller's connection so it joins its SQL transaction. */
    void save(Connection c, String accountNumber, Transaction t) throws SQLException {
        String sql = "MERGE INTO transactions "
                + "(transaction_id, account_number, tx_type, amount, tx_date, balance_after, description) "
                + "KEY (transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getTransactionId());
            ps.setString(2, accountNumber);
            ps.setString(3, t.getType().name());
            ps.setBigDecimal(4, BigDecimal.valueOf(t.getAmount()));
            ps.setTimestamp(5, Timestamp.valueOf(t.getDate()));
            ps.setBigDecimal(6, BigDecimal.valueOf(t.getBalanceAfter()));
            ps.setString(7, t.getDescription());
            ps.executeUpdate();
        }
    }

    List<Transaction> findByAccount(Connection c, String accountNumber) throws SQLException {
        String sql = "SELECT transaction_id, tx_type, amount, tx_date, balance_after, description "
                + "FROM transactions WHERE account_number = ? ORDER BY tx_date, transaction_id";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getString("transaction_id"),
                            TransactionType.valueOf(rs.getString("tx_type")),
                            rs.getBigDecimal("amount").doubleValue(),
                            rs.getTimestamp("tx_date").toLocalDateTime(),
                            rs.getBigDecimal("balance_after").doubleValue(),
                            rs.getString("description")));
                }
            }
        }
        return list;
    }

    void deleteByAccount(Connection c, String accountNumber) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM transactions WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        }
    }

    /** Read: the history of one account, oldest first. */
    public List<Transaction> findByAccount(String accountNumber) {
        try (Connection c = database.getConnection()) {
            return findByAccount(c, accountNumber);
        } catch (SQLException e) {
            throw new DataAccessException("Could not read transactions: " + e.getMessage(), e);
        }
    }

    public int count() {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM transactions");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count transactions: " + e.getMessage(), e);
        }
    }
}
