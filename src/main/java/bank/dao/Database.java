package bank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens JDBC connections and creates the schema (customers, accounts, transactions). */
public class Database {
    private final String url;

    public Database(String url) {
        this.url = url;
        createSchema();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    private void createSchema() {
        String customers = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id   VARCHAR(20)  PRIMARY KEY,
                    first_name    VARCHAR(50)  NOT NULL,
                    surname       VARCHAR(50)  NOT NULL,
                    address       VARCHAR(200),
                    phone_number  VARCHAR(20),
                    password_hash VARCHAR(64)  NOT NULL,
                    employed      BOOLEAN      NOT NULL
                )""";
        String accounts = """
                CREATE TABLE IF NOT EXISTS accounts (
                    account_number  VARCHAR(20)  PRIMARY KEY,
                    account_type    VARCHAR(12)  NOT NULL,
                    branch          VARCHAR(50)  NOT NULL,
                    customer_id     VARCHAR(20)  NOT NULL,
                    balance         DECIMAL(15,2) NOT NULL,
                    company_name    VARCHAR(100),
                    company_address VARCHAR(200),
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                )""";
        String transactions = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id VARCHAR(20)  PRIMARY KEY,
                    account_number VARCHAR(20)  NOT NULL,
                    tx_type        VARCHAR(12)  NOT NULL,
                    amount         DECIMAL(15,2) NOT NULL,
                    tx_date        TIMESTAMP    NOT NULL,
                    balance_after  DECIMAL(15,2) NOT NULL,
                    description    VARCHAR(200),
                    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
                )""";
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute(customers);
            st.execute(accounts);
            st.execute(transactions);
        } catch (SQLException e) {
            throw new DataAccessException("Could not create the database schema: " + e.getMessage(), e);
        }
    }
}
