package bank.dao;

import bank.Account;
import bank.ChequeAccount;
import bank.Customer;
import bank.InvestmentAccount;
import bank.SavingsAccount;
import bank.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the accounts table (an account's transactions are saved with it). */
public class AccountDAO {
    private final Database database;
    private final TransactionDAO transactionDAO;

    public AccountDAO(Database database, TransactionDAO transactionDAO) {
        this.database = database;
        this.transactionDAO = transactionDAO;
    }

    /** Create or update an account row and all of its transactions in one SQL transaction. */
    public void save(Account a) {
        String sql = "MERGE INTO accounts (account_number, account_type, branch, customer_id, balance, "
                + "company_name, company_address) KEY (account_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, a.getAccountNumber());
                    ps.setString(2, a.getAccountType());
                    ps.setString(3, a.getBranch());
                    ps.setString(4, a.getCustomer().getCustomerId());
                    ps.setBigDecimal(5, BigDecimal.valueOf(a.getBalance()));
                    if (a instanceof ChequeAccount cheque) {
                        ps.setString(6, cheque.getCompanyName());
                        ps.setString(7, cheque.getCompanyAddress());
                    } else {
                        ps.setNull(6, Types.VARCHAR);
                        ps.setNull(7, Types.VARCHAR);
                    }
                    ps.executeUpdate();
                }
                for (Transaction t : a.getTransactions()) {
                    transactionDAO.save(c, a.getAccountNumber(), t);
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save account: " + e.getMessage(), e);
        }
    }

    /** Read all accounts of one customer, with balances and transaction history restored. */
    public List<Account> findByCustomer(Customer owner) {
        String sql = "SELECT account_number, account_type, branch, balance, company_name, company_address "
                + "FROM accounts WHERE customer_id = ? ORDER BY account_number";
        List<Account> list = new ArrayList<>();
        try (Connection c = database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, owner.getCustomerId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account account = build(rs, owner);
                    account.restoreState(rs.getBigDecimal("balance").doubleValue(),
                            transactionDAO.findByAccount(c, account.getAccountNumber()));
                    list.add(account);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read accounts: " + e.getMessage(), e);
        }
    }

    public boolean exists(String accountNumber) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check account: " + e.getMessage(), e);
        }
    }

    /** Delete an account and its transactions. */
    public void delete(String accountNumber) {
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try {
                transactionDAO.deleteByAccount(c, accountNumber);
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE account_number = ?")) {
                    ps.setString(1, accountNumber);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete account: " + e.getMessage(), e);
        }
    }

    public int count() {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM accounts");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count accounts: " + e.getMessage(), e);
        }
    }

    private static Account build(ResultSet rs, Customer owner) throws SQLException {
        String type = rs.getString("account_type");
        String number = rs.getString("account_number");
        String branch = rs.getString("branch");
        return switch (type) {
            case "Savings" -> new SavingsAccount(number, branch, owner, 0);
            case "Investment" -> new InvestmentAccount(number, branch, owner);
            case "Cheque" -> new ChequeAccount(number, branch, owner, 0,
                    rs.getString("company_name"), rs.getString("company_address"));
            default -> throw new DataAccessException("Unknown account type in database: " + type);
        };
    }
}
