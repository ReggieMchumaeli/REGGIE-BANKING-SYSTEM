package bank.dao;

import bank.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the customers table. */
public class CustomerDAO {
    private final Database database;

    public CustomerDAO(Database database) {
        this.database = database;
    }

    /** Create. */
    public void insert(Customer c) {
        String sql = "INSERT INTO customers "
                + "(customer_id, first_name, surname, address, phone_number, password_hash, employed) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerId());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getSurname());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getPhoneNumber());
            ps.setString(6, c.getPasswordHash());
            ps.setBoolean(7, c.isEmployed());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save customer: " + e.getMessage(), e);
        }
    }

    /** Read one customer (accounts are loaded separately by AccountDAO). Returns null if not found. */
    public Customer findById(String customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read customer: " + e.getMessage(), e);
        }
    }

    /** Read all customers. */
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers ORDER BY customer_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read customers: " + e.getMessage(), e);
        }
    }

    /** Update. */
    public void update(Customer c) {
        String sql = "UPDATE customers SET first_name = ?, surname = ?, address = ?, phone_number = ?, "
                + "password_hash = ?, employed = ? WHERE customer_id = ?";
        try (Connection conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getSurname());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getPhoneNumber());
            ps.setString(5, c.getPasswordHash());
            ps.setBoolean(6, c.isEmployed());
            ps.setString(7, c.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update customer: " + e.getMessage(), e);
        }
    }

    /** Delete a customer together with their accounts and transactions. */
    public void delete(String customerId) {
        try (Connection conn = database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                execute(conn, "DELETE FROM transactions WHERE account_number IN "
                        + "(SELECT account_number FROM accounts WHERE customer_id = ?)", customerId);
                execute(conn, "DELETE FROM accounts WHERE customer_id = ?", customerId);
                execute(conn, "DELETE FROM customers WHERE customer_id = ?", customerId);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete customer: " + e.getMessage(), e);
        }
    }

    public int count() {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM customers");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count customers: " + e.getMessage(), e);
        }
    }

    private static void execute(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ps.executeUpdate();
        }
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return Customer.restore(
                rs.getString("customer_id"),
                rs.getString("first_name"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("phone_number"),
                rs.getString("password_hash"),
                rs.getBoolean("employed"));
    }
}
